package com.tauri.notification;

import android.app.Notification;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import androidx.annotation.NonNull;
import org.json.JSONObject;
import org.json.JSONException;

public class NotificationInterceptorService extends NotificationListenerService {
    
    private static final String TAG = "NotificationInterceptor";
    private static NotificationInterceptorService instance;
    private static NotificationCallback callback;
    
    public interface NotificationCallback {
        void onNotificationReceived(String notificationJson);
    }
    
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.d(TAG, "NotificationInterceptorService created");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        instance = null;
        Log.d(TAG, "NotificationInterceptorService destroyed");
    }
    
    public static void setCallback(NotificationCallback cb) {
        callback = cb;
    }
    
    @Override
    public void onNotificationPosted(@NonNull StatusBarNotification sbn) {
        try {
            JSONObject notificationData = extractNotificationData(sbn);
            if (notificationData != null && callback != null) {
                callback.onNotificationReceived(notificationData.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing notification", e);
        }
    }
    
    @Override
    public void onNotificationRemoved(@NonNull StatusBarNotification sbn) {
        Log.d(TAG, "Notification removed: " + sbn.getPackageName());
    }
    
    private JSONObject extractNotificationData(StatusBarNotification sbn) {
        try {
            Notification notification = sbn.getNotification();
            Bundle extras = notification.extras;
            
            JSONObject json = new JSONObject();
            
            // 获取应用名称
            String appName = getAppName(sbn.getPackageName());
            json.put("app_name", appName != null ? appName : sbn.getPackageName());
            
            // 获取通知类别
            String category = notification.category != null ? notification.category : "unknown";
            json.put("category", category);
            
            // 获取标题
            CharSequence title = extras.getCharSequence(Notification.EXTRA_TITLE);
            json.put("title", title != null ? title.toString() : "");
            
            // 获取内容
            CharSequence text = extras.getCharSequence(Notification.EXTRA_TEXT);
            CharSequence bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT);
            String content = "";
            if (bigText != null) {
                content = bigText.toString();
            } else if (text != null) {
                content = text.toString();
            }
            json.put("content", content);
            
            // 时间戳
            json.put("timestamp", sbn.getPostTime());
            
            return json;
            
        } catch (JSONException e) {
            Log.e(TAG, "Error creating JSON", e);
            return null;
        }
    }
    
    private String getAppName(String packageName) {
        try {
            PackageManager pm = getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            return (String) pm.getApplicationLabel(info);
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }
    
    public static boolean isServiceEnabled(Context context) {
        ComponentName cn = new ComponentName(context, NotificationInterceptorService.class);
        String flat = android.provider.Settings.Secure.getString(
            context.getContentResolver(),
            "enabled_notification_listeners"
        );
        return flat != null && flat.contains(cn.flattenToString());
    }
} 