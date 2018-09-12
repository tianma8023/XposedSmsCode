# XposedSmsCode
A Xposed Module which can recognize ,parse SMS code and copy it to clipboard when a new message arrives.It can also auto-input SMS code.

<img src="ss/ss_01.png" width="180"/><img src="ss/ss_02.jpg" width="180"/><img src="ss/ss_03.jpg" width="180"/><img src="ss/ss_04.jpg" width="180"/><img src="ss/ss_05.jpg" width="180"/>

You can download this module on [Xposed Repository](http://repo.xposed.info/module/com.github.tianma8023.xposed.smscode) or [Coolapk](https://www.coolapk.com/apk/com.github.tianma8023.xposed.smscode). 

# Usage
1. Root your device and install Xposed Framework.
2. Install and activite this xposed module and then reboot.
3. Have fun!

Welcome any feedbacks.

# Attention
**This module is suitable for AOSP ROM, it may not work well on other 3rd-party Rom.**

**Compatibility: Requires Android 4.4+ (api level ≥ 19).**

**Read the FAQ in app first if you encountered any problems.**

# Features
- Copy verification code to clipboard when a new message arrives.
- Show toast when a SMS verification code is copied.
- <s>Mark verification code message as read (experimental).</s>
- Custom keywords about verification code message (regular expressions allowed).
- Auto-input SMS code.

# Update Logs
- 18.09.02 v1.4.5
  1. Change app's name.
  2. Fix the issue of parsing incorrect SMS code if there is a decimal number in SMS message.
- 18.08.31 v1.4.4
  1. Optimize the algorithm of parsing SMS code for English Message.
  2. Bug fix: Accessibility input mode within Manual focus mode cannot auto-input SMS code.
  3. Update more FAQs.
- 18.08.20 v1.4.3
  1. Add feature: two Focus Modes(Auto mode, Manual mode).
  2. Add more FAQs.
- 18.08.16 v1.4.2
  1. Add feature: join QQ group for communication and bug report.
  2. Add more FAQs.
- 18.08.15 v1.4.1
  1. Bug fix: NullPointerException while starting 3rd party app if it isn't installed or enabled.
- 18.08.14 v1.4.0
  1. Optimize the algorithm of parsing SMS code.
  2. New feature: theme.
- 18.08.06 v1.3.0
  1. Bug fix: Auto-input bug (below Android 6.0).
  2. Optimize the strategy of auto-input.
  3. Add new language: Traditional Chinese.
  4. Add FAQs(Sorry for my poor English, there is no english version now, u can contact me on Github if u can help me translate it into English).
  5. Add a switch for verbose logging(Only for debug).
- 18.07.26 v1.2.1
  1. Optimize the strategy of Auto-input.
- 18.07.25 v1.2.0
  1. Remove the feature of marking SMS as read.
  2. Add the feature of Auto-Input SMS code.
  3. Integrate UMeng analyze tools.
- 18.07.11 v1.1.1
  1. Bug fix: Can't create handler inside thread that has not called Looper.prepare().
  2. Bug fix: test-only package cannot be installed
- 18.06.26 v1.0.1 
  1. Custom SMS code keywords rules of regular expressions.
  2. SMS code test feature added.
- 18.05.27 v0.0.1 Basic funtions added.

# Thanks
- [NekoSMS](https://github.com/apsun/NekoSMS)
- [SmsCodeHelper](https://github.com/drakeet/SmsCodeHelper)
- [ButterKnife](https://github.com/JakeWharton/butterknife)
- [Remote Preferences](https://github.com/apsun/RemotePreferences)
- [Material Dialogs](https://github.com/afollestad/material-dialogs)
- [Android Shell](https://github.com/jaredrummler/AndroidShell)

# License
All code is licensed under [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 