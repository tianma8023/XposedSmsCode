# XposedSmsCode
A Xposed Module which can recognize ,parse SMS code and copy it to clipboard when a new message arrives.It can also auto-input SMS code.

[中文版说明](/README-CN.md)

<img src="ss/ss_01.png" width="180"/><img src="ss/ss_02.jpg" width="180"/><img src="ss/ss_03.jpg" width="180"/><img src="ss/ss_04.jpg" width="180"/><img src="ss/ss_05.jpg" width="180"/>

You can download this module on [Xposed Repository](http://repo.xposed.info/module/com.github.tianma8023.xposed.smscode) or [Coolapk](https://www.coolapk.com/apk/com.github.tianma8023.xposed.smscode). 

# Usage
1. Root your device and install Xposed Framework.
2. Install and activite this xposed module and then reboot.
3. Enjoy it!

Welcome any feedbacks.

# Attention
- **This module is suitable for AOSP ROM, it may not work well on other 3rd-party Rom.**
- **Compatibility: Requires Android 4.4+ (api level ≥ 19).**
- **Read the FAQ in app first if you encountered any problems.**

# Features
- Copy verification code to clipboard when a new message arrives.
- Show toast when a SMS verification code is copied.
- Mark verification code message as read (experimental).
- Delete verification SMS when it's extracted successfully.
- Custom keywords about verification code message (regular expressions allowed).
- Support the SMS code match rules customization, importation and exportation.
- Auto-input SMS code.
- Various theme color to choose.

# Update Logs
[Update Logs](/LOG-EN.md)

# Thanks
- [NekoSMS](https://github.com/apsun/NekoSMS)
- [SmsCodeHelper](https://github.com/drakeet/SmsCodeHelper)
- [ButterKnife](https://github.com/JakeWharton/butterknife)
- [Remote Preferences](https://github.com/apsun/RemotePreferences)
- [Material Dialogs](https://github.com/afollestad/material-dialogs)
- [Android Shell](https://github.com/jaredrummler/AndroidShell)

# License
All code is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 