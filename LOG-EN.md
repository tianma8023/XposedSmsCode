# Update Logs
- 22.09.15 v2.4.0 Reboot required
  1. Change: Adapt to Android 13, fix the issue of cannot auto input SMS code
- 22.05.25 v2.3.1 Reboot required
  1. Change: Restore feature of "mark SMS as read" and "delete SMS code message"
  2. Change: SMS Code record support copy SMS message content to clipboard @fxdqe
  3. Others: fix some little bugs
- 22.05.23 v2.3.0 Reboot required
  1. New: custom auto input SMS code delay
  2. Fix: fix the issue of module configuration invalidation
  3. Remove: remove "mark SMS as read" and "delete SMS code message"
- 22.05.19 v2.2.7
  1. New: Add privacy policy statement
  2. Others: fix some little bug
- 22.02.23 v2.2.6
  1. Change：Adapt to Android 12
- 21.02.04 v2.2.5
  1. Fix: fix the crash issue of PhoneService on OnePlus Android 11.
  2. Change: Add the notice for EdXposed users.
  3. Change: Update FAQ.
- 20.11.09 v2.2.4
  1. Change: Adapt to Android 11
- 20.05.13 v2.2.3
  1. Fix: fix the issue of ContentResolver cannot acquire on some Roms.
- 20.01.01 v2.2.2
  1. Change: Adapt to Android Q
- 19.08.28 v2.2.1
  1. New option: filter out duplicate code SMS within a short time.
  2. Change: rearrange the preference items.
  3. Refactor: refactor the core logic. 
- 19.08.16 v2.2.0
  1. New: custom your own theme.
  2. Change: Adapt to Android Q (especially for MIUI Android Q).
  3. Fix: fix the snackbar appearance.
  4. Change: user notice.
- 19.08.02 v2.1.3
  1. New: check for updates when "version info" clicked.
  2. Change: unify the SnackBar background.
  3. Optimization: optimize the procedure of blocking auto input.
- 19.07.24 v2.1.2
  1. New: copy code to clipboard when notification clicked
  2. Optimization: optimize the algorithm of parsing SMS code.
  3. Change: Don't copy code to clipboard as default. Show code notification as default.
- 19.07.20 v2.1.1
  1. New: Add blacklist to block auto-input in certain Apps.
  2. Optimization: optimize some features.
- 19.07.18 v2.1.0
  1. Change: refactor the whole project.
  2. Change: migrate to AndroidX.
  3. Change: beautify some UI.
- 19.04.26 v2.0.4
  1. Change: remove Wechat donation entry
  2. Change: remove Bugly crash analyze tools
- 19.04.17 v2.0.3
  1. Change: notice for TaiChi users.
  2. Fix: fix the issue of log level.
- 19.04.11 v2.0.2
  1. New option: show SMS code notification
  2. Improvement: notice for TaiChi users.
  3. Optimization: optimize some features.
  4. Change: no longer support Android 5.x.
- 19.03.29 v2.0.1
  1. Fix: Module invalid issue before Android P.
  2. Change: change the default value for some options.
- 19.03.28 v2.0.0
  1. Important change: no service anymore.
  2. Optimization: optimize the algorithm of parsing SMS code.
  3. New option: kill me if extract succeed.
  4. Change: more prompt for TaiChi users.
  5. Fix: crash when open WeChat wallet for EdXposed users.
- 19.03.01 v1.7.0
  1. New option: block Code SMS.
  2. Optimize: improve the experiences for TaiChi-Magisk users.
- 19.02.16 v1.6.0
  1. New: adapt TaiChi·Magisk
  2. New: new entry for ignoring battery optimzation and notice for TaiChi users
  3. Optimization: optimize the algorithm of parsing SMS Code, adapt the navigation bar color
  4. Bug fixes: WeChat wallet crash for EdXposed users on Android P
  5. Other optimizations.
- 19.01.05 v1.5.6
  1. Beautify parts of UI
  2. Add entry: get alipay red packet
  3. Optimize the Auto-input mode selection and donation process.
  4. Backup code rule files under /sdcard/Documents/
- 18.12.29 v1.5.5
  1. Optimization: SMS message details can be shown in code records.
  2. Others: copywriting fix.
- 18.11.29 v1.5.4
  1. Bug fix：code record company parse issue.
- 18.11.17 v1.5.3
  1. Add configurable options: retain SMS code records.
  2. Add App shortcuts.
  3. Bug fix: crash when share backup files.
  4. No longer support 4.4.x
- 18.10.28 v1.5.2
  1. Add icon for every preference.
  2. Minify APK size.
  3. Bug fix: crash when auto input.
- 18.10.24 v1.5.1
  1. Add configurable options: mark verification code sms as read.
  2. Add configurable options: delete verification sms if it's extracted succeed.
  3. Add configurable options: copy sms code to clipboard.
  4. Add configurable options: start manual focus mode if auto focus failed.
  5. Add more user-friendly tips for settings.
- 18.10.02 v1.5.0
  1. New feature: add the support of SMS code rules customization, importation and exportation.
- 18.09.14 v1.4.6
  1. New feature: clear clipboard if auto input succeed (optional).
  2. Merge "auto-input root mode" and "auto-input accessibility mode" into "auto-input mode".
  3. Enhance the ability of auto input in webview.
  4. Fix some bugs and possible problems.
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
- 18.05.27 v0.0.1 
  1. Basic functions added.