use std::process::Command;

pub fn list_devices() -> anyhow::Result<Vec<String>> {
    let output = Command::new("adb")
        .arg("devices")
        .arg("-l")
        .output()
        .map_err(|e| anyhow::anyhow!("adb not found: {e}"))?;

    let stdout = String::from_utf8_lossy(&output.stdout);
    let mut devices = Vec::new();

    for line in stdout.lines().skip(1) {
        let line = line.trim();
        if line.is_empty() || line.starts_with("*") {
            continue;
        }
        if let Some(serial) = line.split_whitespace().next() {
            if serial != "List" && serial != "of" {
                devices.push(serial.to_string());
            }
        }
    }

    Ok(devices)
}
