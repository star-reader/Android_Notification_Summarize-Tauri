use tauri::{
    plugin::{Builder, TauriPlugin},
    Runtime, Manager, AppHandle,
};
use serde::{Deserialize, Serialize};

#[derive(Debug, Serialize, Deserialize)]
pub struct NotificationData {
    pub app_name: String,
    pub category: String,
    pub title: String,
    pub content: String,
    pub timestamp: i64,
}

#[tauri::command]
async fn request_notification_permission<R: Runtime>(
    app: AppHandle<R>,
) -> Result<serde_json::Value, String> {
    #[cfg(target_os = "android")]
    {
        // 直接返回成功，实际权限请求在Java端处理
        Ok(serde_json::json!({"success": true, "message": "Permission request initiated"}))
    }
    #[cfg(not(target_os = "android"))]
    {
        Err("Not running on Android".to_string())
    }
}

#[tauri::command]
async fn check_notification_permission<R: Runtime>(
    app: AppHandle<R>,
) -> Result<serde_json::Value, String> {
    #[cfg(target_os = "android")]
    {
        // 直接返回，实际检查在Java端处理
        Ok(serde_json::json!({"granted": false, "message": "Check in Java layer"}))
    }
    #[cfg(not(target_os = "android"))]
    {
        Err("Not running on Android".to_string())
    }
}

pub fn init<R: Runtime>() -> TauriPlugin<R> {
    Builder::new("notification_interceptor")
        .invoke_handler(tauri::generate_handler![
            request_notification_permission,
            check_notification_permission
        ])
        .setup(|app, api| {
            #[cfg(target_os = "android")]
            {
                // 在Android平台上设置通知监听
                let _ = setup_android_notification_listener(app.clone());
            }
            Ok(())
        })
        .build()
}

#[cfg(target_os = "android")]
fn setup_android_notification_listener<R: Runtime>(app: AppHandle<R>) -> Result<(), Box<dyn std::error::Error>> {
    // 这个函数会在Android初始化时被调用
    // 具体的通知监听逻辑会通过JNI与Java代码交互
    Ok(())
}

// 这个函数会被Java代码调用，用于发送通知事件到前端
#[cfg(target_os = "android")]
pub fn emit_notification_received(app: &AppHandle<impl Runtime>, notification: NotificationData) {
    let _ = app.emit("notification_received_original", notification);
} 