#Hear 工程说明

###Activity & Fragment & Service

名称 | 简介
------------ | ------------- 
SplashActivity | 欢迎页面
GuideActivity | 引导页面
ArticleFragment | 主页面的文章
GuideFragment | 引导页的图片


###网络模块

```BaseHttpAsyncTask```，职责是对服务器发起请求并解析相应的数据，支持GET方式和POST方式。通过重载```getRespClass```来实现解析不同的模型。

###统计模块

依赖于百度统计SDK，开发者文档地址: [click me](http://mtj.baidu.com/web/welcome/sdk)

###分享模块

支持分享到QQ、新浪微博、腾讯微博、微信，依赖于友盟的社会化分享SDK。

###API接口文档地址

待补充...

###依赖的第三方库

名称 | 简介 | 主页
------------ | ------------- | ------------
ProgressWheel | 自定义的ProgressBar  | [链接](https://github.com/Todd-Davies/ProgressWheel)
UniversalImageLoader | 图片加载库 | [链接](https://github.com/nostra13/Android-Universal-Image-Loader)
Gson | JSON转Object库 | [链接](https://code.google.com/p/google-gson/)
AndroidResideMenu | 侧边栏UI库 | [链接](https://github.com/SpecialCyCi/AndroidResideMenu)
百度统计SDK | 数据统计分析 | [链接](http://mtj.baidu.com/web/welcome/sdk)
友盟社会化分享 | 分享工具库 | [链接](http://dev.umeng.com)
