# XposedSmsCode
![Total Downloads](https://img.shields.io/github/downloads/tianma8023/XposedSmsCode/total) ![Total Stars](https://img.shields.io/github/stars/tianma8023/XposedSmsCode?style=social) [![Latest Release](https://img.shields.io/github/v/release/tianma8023/XposedSmsCode?label=Latest%20Release)](https://github.com/tianma8023/XposedSmsCode/releases)

An Xposed module which can recognize, parse SMS code and copy it to clipboard when a new message arrives. It can also input SMS code automatically.

[中文版说明](/README-CN.md)

# Screenshots
<img src="/art/en/01.png" width="180"/><img src="/art/en/02.png" width="180"/><img src="/art/en/03.png" width="180"/>

You can download this module on [Xposed Repository](http://repo.xposed.info/module/com.github.tianma8023.xposed.smscode) or [Coolapk](https://www.coolapk.com/apk/com.github.tianma8023.xposed.smscode). 

# Usage
1. Root your device and install Xposed Framework.
2. Install and activite this xposed module and then reboot.
3. Enjoy it!

Welcome any feedbacks.

# Attention
- **This module is suitable for AOSP ROM, it may not work well on other 3rd-party Rom.**
- **Compatibility: Requires Android 6.0+ (api level ≥ 23).**
- **Support Xposed, EdXposed, LSPosed and TaiChi·Magisk**
- **Read the FAQ in app first if you encounter any problems.**

# Features
- Copy verification code to clipboard when a new message arrives.
- Show toast when a SMS verification code is copied.
- Show notification when code SMS parsed.
- Mark verification code message as read(experimental).
- Delete verification SMS when it's extracted successfully(experimental).
- Block verification SMS if it's extracted successfully(experimental).
- Custom keywords about verification code message (regular expressions allowed).
- Support the SMS code match rules customization, importation and exportation.
- Auto-input SMS code.
- Various theme color to choose.

# Release Log
[Release Logs](/LOG-EN.md)

# Thanks To
- [Xposed](https://github.com/rovo89/Xposed)
- [NekoSMS](https://github.com/apsun/NekoSMS)
- [ButterKnife](https://github.com/JakeWharton/butterknife)
- [Material Dialogs](https://github.com/afollestad/material-dialogs)
- [Android Shell](https://github.com/jaredrummler/AndroidShell)
- [EventBus](https://github.com/greenrobot/EventBus)
- [GreenDao](https://github.com/greenrobot/greenDAO)
- [GreenDaoUpgradeHelper](https://github.com/yuweiguocn/GreenDaoUpgradeHelper)
- [Gson](https://github.com/google/gson)
- [dagger](https://github.com/google/dagger)
- [rxjava](https://github.com/ReactiveX/RxJava)
- [rxandroid](https://github.com/ReactiveX/RxAndroid)
- [Cyanea](https://github.com/jaredrummler/Cyanea)

# License
All code is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 