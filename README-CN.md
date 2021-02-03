# XposedSmsCode
识别短信验证码的Xposed模块，并将验证码拷贝到剪切板，亦可以自动输入验证码。

[English README](/README-EN.md)

# 应用截图
<img src="art/cn/01.png" width="180"/><img src="art/cn/02.png" width="180"/><img src="art/cn/03.png" width="180"/>

可以在 [Xposed仓库](http://repo.xposed.info/module/com.github.tianma8023.xposed.smscode) 或者 [酷安](https://www.coolapk.com/apk/com.github.tianma8023.xposed.smscode) 下载此模块。

# 使用
1. Root你的设备，安装Xposed框架；
2. 安装本模块，激活并重启；
3. Enjoy it！

欢迎反馈，欢迎提出意见或建议。

# 注意
- **此模块适用于偏原生的系统，其他第三方定制Rom可能不适用。**
- **兼容性：兼容 Android 6.0 及以上（api等级≥23）设备。**
- **支持 Xposed, EdXposed 和 太极·magisk**
- **遇到问题请先阅读模块中的"常见问题"**

# 功能
- 收到验证码短信后将验证码复制到系统剪贴板
- 收到验证码时显示Toast
- 收到验证码时显示通知
- 将验证码短信标记为已读（实验性）
- 删除验证码短信（实验性）
- 拦截验证码短信（实验性）
- 自定义验证码短信关键字（正则表达式）
- 自定义验证码匹配规则，并支持规则导入导出
- 自动输入验证码
- 主题换肤

# 更新日志
[更新日志](/LOG-CN.md)

# 感谢
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


# 协议
所有的源码均遵循 [GPLv3](https://www.gnu.org/licenses/gpl-3.0.txt) 协议