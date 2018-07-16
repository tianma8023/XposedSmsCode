# XposedSmsCode
A Xposed Module which can recognize ,parse verification code and copy it to clipboard when a new message arrives. / 识别短信验证码的Xposed模块，并将验证码拷贝到剪切板。

<img src="ss/ss_01.png" width="270" /> <img src="ss/ss_02.jpg" width="270" /> <img src="ss/ss_03.jpg" width="270" />

You can download this module on [Xposed Repository](http://repo.xposed.info/module/com.github.tianma8023.xposed.smscode) or [酷安](https://www.coolapk.com/apk/com.github.tianma8023.xposed.smscode). 

可以在 [Xposed仓库](http://repo.xposed.info/module/com.github.tianma8023.xposed.smscode) 或者 [酷安](https://www.coolapk.com/apk/com.github.tianma8023.xposed.smscode) 下载此模块。

# Usage / 使用
1. Root your device and install Xposed Framework. / Root你的设备，安装Xposed框架
2. Install and activite this xposed module and then reboot. / 安装本模块，激活并重启
3. Have fun!

Welcome any feedbacks. / 欢迎反馈，欢迎提出意见或建议。

# Features / 功能
- Copy verification code to clipboard when a new message arrives. / 收到验证码短信后将验证码复制到系统剪贴板
- Show toast when a SMS verification code is copied. / 当验证码被复制后显示Toast
- Mark verification code message as read (experimental). / 将验证码短信标记为已读（实验性）
- Custom keywords about verification code message (regular expressions allowed). / 自定义验证码短信关键字（正则表达式）

# Attention / 注意
**This module is suitable for AOSP ROM, it may not work well on other 3rd-party Rom. / 此模块适用于偏原生的系统，其他第三方定制Rom可能不适用。**

**Compatibility: Requires Android 4.4+ (api level ≥ 19). / 兼容性：兼容 Android 4.4及以上（api等级≥19）设备。**

# Thanks / 感谢
- [NekoSMS](https://github.com/apsun/NekoSMS)
- [SmsCodeHelper](https://github.com/drakeet/SmsCodeHelper)
- [ButterKnife](https://github.com/JakeWharton/butterknife)
- [Remote Preferences](https://github.com/apsun/RemotePreferences)
- [Material Dialogs](https://github.com/afollestad/material-dialogs)

# License / 协议
All code is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 

所有的源码均遵循 [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 协议