mod adb;
mod crash;
mod settings;

use clap::{Parser, Subcommand};

#[derive(Parser)]
#[command(name = "sideleap", about = "SideLeap CLI toolkit", version)]
struct Cli {
    #[command(subcommand)]
    command: Commands,
}

#[derive(Subcommand)]
enum Commands {
    /// Read and display app settings from DataStore files
    DumpSettings {
        /// Path to app data directory (e.g., /data/data/hunoia.sideleap)
        data_dir: String,

        /// Output format (json or yaml)
        #[arg(long, default_value = "yaml")]
        format: String,
    },

    /// Export crash log to a file or stdout
    ExportCrash {
        /// Path to app data directory
        data_dir: String,

        /// Output file (stdout if omitted)
        #[arg(short, long)]
        output: Option<String>,
    },

    /// Analyze crash log and print aggregate statistics
    AnalyzeCrash {
        /// Path to app data directory
        data_dir: String,
    },

    /// Discover connected Android devices via adb
    Devices,
}

fn main() -> anyhow::Result<()> {
    let cli = Cli::parse();

    match cli.command {
        Commands::DumpSettings { data_dir, format } => {
            let configs = settings::read_all_settings(&data_dir)?;
            match format.as_str() {
                "json" => println!("{}", serde_json::to_string_pretty(&configs)?),
                "yaml" => println!("{}", serde_yaml::to_string(&configs)?),
                _ => eprintln!("Unsupported format: {format}. Use json or yaml."),
            }
        }
        Commands::ExportCrash { data_dir, output } => {
            let report = crash::read_crash_log(&data_dir)?;
            match output {
                Some(path) => std::fs::write(&path, &report)?,
                None => print!("{report}"),
            }
        }
        Commands::AnalyzeCrash { data_dir } => {
            let stats = crash::analyze_crashes(&data_dir)?;
            println!("{}", serde_yaml::to_string(&stats)?);
        }
        Commands::Devices => {
            let devices = adb::list_devices()?;
            if devices.is_empty() {
                println!("No devices found.");
            } else {
                for d in &devices {
                    println!("{d}");
                }
            }
        }
    }

    Ok(())
}
