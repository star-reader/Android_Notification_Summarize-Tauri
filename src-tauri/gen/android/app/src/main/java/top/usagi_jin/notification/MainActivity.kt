package top.usagi_jin.notification

import android.os.Bundle
import app.tauri.TauriActivity
import com.tauri.notification.NotificationPlugin

class MainActivity : TauriActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 注册通知插件
        registerPlugin(NotificationPlugin::class.java)
    }
}