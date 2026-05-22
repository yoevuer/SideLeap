use std::collections::BTreeMap;
use std::path::PathBuf;

use anyhow::Context;
use serde::Serialize;

const CRASH_FILE: &str = "cache/crash/crashlog";
const SEPARATOR: &str = "gulugulu_CRASH_REPORT";

#[derive(Debug, Serialize)]
pub struct CrashEntry {
    pub timestamp: String,
    pub exception_type: String,
    pub message: String,
    pub stack_trace: Vec<String>,
}

#[derive(Debug, Serialize)]
pub struct CrashStatistics {
    pub total_crashes: usize,
    pub by_exception_type: BTreeMap<String, usize>,
    pub entries: Vec<CrashEntry>,
}

pub fn read_crash_log(data_dir: &str) -> anyhow::Result<String> {
    let path = PathBuf::from(data_dir).join(CRASH_FILE);
    if !path.exists() {
        return Ok(String::new());
    }
    std::fs::read_to_string(&path).with_context(|| format!("Failed to read {}", path.display()))
}

fn parse_crash_entry(text: &str) -> Option<CrashEntry> {
    let mut lines = text.lines().peekable();
    let timestamp = lines.next()?.strip_prefix("Time: ")?.to_string();
    let exception_line = lines.next()?;
    let (exception_type, message) = if let Some(idx) = exception_line.find(':') {
        (
            exception_line[..idx].trim().to_string(),
            exception_line[idx + 1..].trim().to_string(),
        )
    } else {
        (exception_line.to_string(), String::new())
    };
    let stack_trace: Vec<String> = lines.map(|l| l.to_string()).collect();
    Some(CrashEntry {
        timestamp,
        exception_type,
        message,
        stack_trace,
    })
}

pub fn analyze_crashes(data_dir: &str) -> anyhow::Result<CrashStatistics> {
    let raw = read_crash_log(data_dir)?;
    if raw.is_empty() {
        return Ok(CrashStatistics {
            total_crashes: 0,
            by_exception_type: BTreeMap::new(),
            entries: Vec::new(),
        });
    }

    let sections: Vec<&str> = raw.split(SEPARATOR).collect();
    let mut entries = Vec::new();
    let mut by_exception_type: BTreeMap<String, usize> = BTreeMap::new();

    for section in &sections {
        let trimmed = section.trim();
        if trimmed.is_empty() {
            continue;
        }
        if let Some(entry) = parse_crash_entry(trimmed) {
            *by_exception_type.entry(entry.exception_type.clone()).or_insert(0) += 1;
            entries.push(entry);
        }
    }

    Ok(CrashStatistics {
        total_crashes: entries.len(),
        by_exception_type,
        entries,
    })
}
