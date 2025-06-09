# Android 通知拦截插件使用说明

## 功能概述

这个插件可以拦截Android系统的所有通知，并将通知内容发送到Tauri前端。

## 主要功能

1. **通知拦截**: 监听系统所有应用的通知
2. **权限管理**: 自动请求通知访问权限
3. **事件发送**: 将通知数据以事件形式发送到前端
4. **通知解析**: 提取通知的应用名、类别、标题、内容等信息

## 安装和配置

### 1. 依赖项已配置

插件已经集成到项目中，包含以下组件：

- **Rust插件**: `src-tauri/src/plugins/notification_interceptor/`
- **Android服务**: `src-tauri/android/src/main/java/com/tauri/notification/`
- **权限配置**: Android Manifest 和 Tauri 配置文件

### 2. Android权限

应用需要以下权限：
- `android.permission.BIND_NOTIFICATION_LISTENER_SERVICE`

### 3. 系统设置

用户需要在Android设置中手动开启通知访问权限：
设置 > 通知 > 通知使用权限 > 找到你的应用并开启

## 使用方法

### 前端监听通知事件

```typescript
import { listen } from "@tauri-apps/api/event";

interface NotificationData {
  app_name: string;
  category: string;
  title: string;
  content: string;
  timestamp: number;
}

// 监听通知事件
const unlisten = await listen<NotificationData>("notification_received_original", (event) => {
  console.log("收到通知:", event.payload);
  // 处理通知数据...
});
```

### 权限管理

```typescript
import { invoke } from "@tauri-apps/api/core";

// 请求通知权限
async function requestPermission() {
  try {
    const result = await invoke("plugin:notification_interceptor|request_notification_permission");
    console.log("权限请求结果:", result);
  } catch (error) {
    console.error("权限请求失败:", error);
  }
}

// 检查权限状态
async function checkPermission() {
  try {
    const result = await invoke("plugin:notification_interceptor|check_notification_permission");
    console.log("权限状态:", result);
  } catch (error) {
    console.error("检查权限失败:", error);
  }
}
```

## 通知数据结构

```json
{
  "app_name": "微信",
  "category": "msg",
  "title": "张三",
  "content": "你好，今天有空吗？",
  "timestamp": 1640995200000
}
```

## 构建和运行

### 开发模式
```bash
pnpm tauri android dev
```

### 构建APK
```bash
pnpm tauri android build
```

## 注意事项

1. **权限限制**: 通知访问权限是敏感权限，需要用户手动在系统设置中开启
2. **Android版本**: 支持Android 4.3+（API Level 18+）
3. **测试**: 在真机上测试，模拟器可能无法正常获取通知
4. **隐私**: 请遵守隐私法规，合理使用获取的通知数据

## 故障排除

### 1. 权限问题
- 确认在Android设置中开启了通知访问权限
- 检查应用是否已安装并运行过

### 2. 通知获取不到
- 确认NotificationListenerService是否正在运行
- 检查Android日志中的相关错误信息

### 3. 编译问题
- 确认Java 17已正确安装
- 检查Android SDK配置是否正确

## 扩展功能

你可以基于这个插件进一步开发：

1. **通知过滤**: 根据应用或内容过滤通知
2. **通知聚合**: 将相似通知合并处理
3. **智能回复**: 基于AI生成回复内容
4. **统计分析**: 分析通知模式和频率 