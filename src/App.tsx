import { useState, useEffect } from "react";
import reactLogo from "./assets/react.svg";
import { invoke } from "@tauri-apps/api/core";
import { listen } from "@tauri-apps/api/event";
import "./App.css";

interface NotificationData {
  app_name: string;
  category: string;
  title: string;
  content: string;
  timestamp: number;
}

function App() {
  const [greetMsg, setGreetMsg] = useState("");
  const [name, setName] = useState("");
  const [permissionGranted, setPermissionGranted] = useState(false);
  const [notifications, setNotifications] = useState<NotificationData[]>([]);

  async function greet() {
    // Learn more about Tauri commands at https://tauri.app/develop/calling-rust/
    setGreetMsg(await invoke("greet", { name }));
  }

  async function requestNotificationPermission() {
    try {
      // 直接调用Java插件方法
      const result = await invoke("requestNotificationPermission");
      console.log("权限请求结果:", result);
      setTimeout(() => {
        checkPermissionStatus();
      }, 1000); // 延迟检查权限状态
    } catch (error) {
      console.error("请求通知权限失败:", error);
    }
  }

  async function checkPermissionStatus() {
    try {
      // 直接调用Java插件方法
      const result = await invoke("checkNotificationPermission") as { granted: boolean };
      setPermissionGranted(result.granted);
    } catch (error) {
      console.error("检查权限状态失败:", error);
      setPermissionGranted(false);
    }
  }

  useEffect(() => {
    // 检查权限状态
    checkPermissionStatus();

    // 监听通知事件
    const unlisten = listen<NotificationData>("notification_received_original", (event) => {
      console.log("收到通知:", event.payload);
      setNotifications(prev => [event.payload, ...prev.slice(0, 9)]); // 只保留最新的10条
    });

    return () => {
      unlisten.then(fn => fn());
    };
  }, []);

  return (
    <main className="container">
      <h1>Android 通知拦截器</h1>

      <div className="notification-section">
        <h2>通知权限状态</h2>
        <p>权限状态: {permissionGranted ? "✅ 已授权" : "❌ 未授权"}</p>
        {!permissionGranted && (
          <button onClick={requestNotificationPermission}>
            请求通知访问权限
          </button>
        )}
      </div>

      <div className="notifications-list">
        <h2>接收到的通知 ({notifications.length})</h2>
        {notifications.length === 0 ? (
          <p>暂无通知</p>
        ) : (
          <div className="notifications-container">
            {notifications.map((notification, index) => (
              <div key={index} className="notification-item">
                <div className="notification-header">
                  <span className="app-name">{notification.app_name}</span>
                  <span className="category">{notification.category}</span>
                  <span className="timestamp">
                    {new Date(notification.timestamp).toLocaleTimeString()}
                  </span>
                </div>
                <div className="notification-title">{notification.title}</div>
                <div className="notification-content">{notification.content}</div>
              </div>
            ))}
          </div>
        )}
      </div>

      <div className="row">
        <a href="https://vitejs.dev" target="_blank">
          <img src="/vite.svg" className="logo vite" alt="Vite logo" />
        </a>
        <a href="https://tauri.app" target="_blank">
          <img src="/tauri.svg" className="logo tauri" alt="Tauri logo" />
        </a>
        <a href="https://reactjs.org" target="_blank">
          <img src={reactLogo} className="logo react" alt="React logo" />
        </a>
      </div>

      <form
        className="row"
        onSubmit={(e) => {
          e.preventDefault();
          greet();
        }}
      >
        <input
          id="greet-input"
          onChange={(e) => setName(e.currentTarget.value)}
          placeholder="输入名字..."
        />
        <button type="submit">问候</button>
      </form>
      <p>{greetMsg}</p>
    </main>
  );
}

export default App;
