package com.tauri.notification;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;
import app.tauri.annotation.Command;
import app.tauri.annotation.InvokeArg;
import app.tauri.annotation.TauriPlugin;
import app.tauri.plugin.Invoke;
import app.tauri.plugin.JSObject;
import app.tauri.plugin.Plugin;
import app.tauri.plugin.PluginCall;

@TauriPlugin
public class NotificationPlugin extends Plugin {
    
    private static final String TAG = "NotificationPlugin";
    private static final int REQUEST_NOTIFICATION_ACCESS = 1001;
    
    @Override
    public void load(Context context) {
        super.load(context);
        Log.d(TAG, "NotificationPlugin loaded");
        
        // 设置通知回调
        NotificationInterceptorService.setCallback(new NotificationInterceptorService.NotificationCallback() {
            @Override
            public void onNotificationReceived(String notificationJson) {
                // 将通知数据发送到前端
                JSObject data = new JSObject();
                try {
                    org.json.JSONObject json = new org.json.JSONObject(notificationJson);
                    data.put("app_name", json.getString("app_name"));
                    data.put("category", json.getString("category"));
                    data.put("title", json.getString("title"));
                    data.put("content", json.getString("content"));
                    data.put("timestamp", json.getLong("timestamp"));
                    
                    trigger("notification_received_original", data);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing notification JSON", e);
                }
            }
        });
    }
    
    @Command
    public void requestNotificationPermission(PluginCall call) {
        Context context = getContext();
        
        if (NotificationInterceptorService.isServiceEnabled(context)) {
            JSObject result = new JSObject();
            result.put("granted", true);
            call.resolve(result);
            return;
        }
        
        // 引导用户去设置页面开启通知访问权限
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            
            JSObject result = new JSObject();
            result.put("granted", false);
            result.put("message", "请在设置中开启通知访问权限");
            call.resolve(result);
        } catch (Exception e) {
            Log.e(TAG, "Error opening notification settings", e);
            call.reject("Failed to open notification settings", e);
        }
    }
    
    @Command
    public void checkNotificationPermission(PluginCall call) {
        Context context = getContext();
        boolean isEnabled = NotificationInterceptorService.isServiceEnabled(context);
        
        JSObject result = new JSObject();
        result.put("granted", isEnabled);
        call.resolve(result);
    }
    
    @Command
    public void initializeNotificationListener(PluginCall call) {
        Log.d(TAG, "Initializing notification listener");
        
        // 通知监听器初始化逻辑已经在load方法中完成
        JSObject result = new JSObject();
        result.put("initialized", true);
        call.resolve(result);
    }
} 