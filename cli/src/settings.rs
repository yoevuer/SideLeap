use std::collections::BTreeMap;
use std::path::PathBuf;

use anyhow::Context;
use serde_json::Value;

const DATASTORE_FILES: &[(&str, &str)] = &[
    ("aa", "initial_settings"),
    ("bb", "advanced_settings"),
    ("cc", "gesture_settings"),
    ("dd", "side_gesture_buttons"),
    ("ee", "bottom_gesture_buttons"),
    ("ff", "action_settings"),
    ("gg", "quick_app_launcher"),
    ("hh", "frozen_app_settings"),
    ("ii", "sub_gesture_settings"),
];

fn read_datastore_file(dir: &str, file_name: &str) -> anyhow::Result<Option<Value>> {
    let path = PathBuf::from(dir).join("files").join("ds").join(file_name);
    if !path.exists() {
        return Ok(None);
    }
    let content = std::fs::read_to_string(&path)
        .with_context(|| format!("Failed to read {}", path.display()))?;
    if content.trim().is_empty() {
        return Ok(None);
    }
    let value: Value =
        serde_json::from_str(&content).with_context(|| format!("Invalid JSON in {}", path.display()))?;
    Ok(Some(value))
}

pub fn read_all_settings(data_dir: &str) -> anyhow::Result<BTreeMap<String, Value>> {
    let mut result = BTreeMap::new();
    for (file_name, key) in DATASTORE_FILES {
        match read_datastore_file(data_dir, file_name)? {
            Some(value) => {
                result.insert(key.to_string(), value);
            }
            None => {
                result.insert(key.to_string(), Value::Null);
            }
        }
    }
    Ok(result)
}
