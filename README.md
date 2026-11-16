# 计算器

Android 原生计算器，参考用户手机上荣耀计算器的黑底、灰色圆键与橙色等号。独立包名 `local.roundcalc`。

- 基础计算支持运算优先级、小数、百分数、正负、删除和即时预览。
- 进制模式同步显示 HEX / DEC / OCT / BIN，点击对应行切换输入进制，长按数值复制。
- 主键盘在两个页面保持同一位置；A–F 使用预留行，非当前进制数字禁用，不调用系统输入法。
- 进制模式采用任意精度有符号整数，负数显示负号，整数除法向零取整；不是 64 位补码模拟器。
- 两种模式分别保存输入，支持数字粘贴、复制、最近历史和重启恢复。
- 无网络权限，不替换或卸载系统计算器。

## 构建

需要 JDK 17+、Gradle 8.13、Android SDK 35。

```sh
./gradlew assembleDebug lintDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`。

## 已验证

2026-10-19，在 MBH-N49、Android 16、1060×2376 真机查看系统计算器和智算，以 ARTEMIS 截图/层级读取配合 ADB 交互确认界面。ARTEMIS 自动代理因未配置 LLM API Key 未启动。设备交互验证记录见 VALIDATION.md。
