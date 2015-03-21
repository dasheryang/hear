package hear.lib.share;

import android.app.Activity;
import android.content.Intent;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.utils.OauthHelper;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.CircleShareContent;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import hear.lib.share.controllers.CustomLoginBoard;
import hear.lib.share.controllers.CustomShareBoard;
import hear.lib.share.models.ShareContent;

/**
 * Created by ZhengYi on 15/2/10.
 */
public class SocialServiceWrapper {
    private Activity mContext;
    private UMSocialService mSocialService;
    private ShareContent mShareContent;

    public SocialServiceWrapper(Activity context) {
        mContext = context;
        mSocialService = UMServiceFactory
                .getUMSocialService("com.umeng.share");
    }

    public void setShareContent(ShareContent content) {
        mShareContent = content;
        configSharePlatforms(mContext, content);
        setShareContentCore(content);
    }

    public void showShareBoard(SocializeListeners.SnsPostListener listener) {
        CustomShareBoard shareBoard = new CustomShareBoard(mContext, mSocialService, mShareContent, listener);
        shareBoard.show();
    }

    public void showLoginBoard(SocializeListeners.UMAuthListener authListener) {
        configLoginPlatform(mContext);
        CustomLoginBoard loginBoard = new CustomLoginBoard(mContext, mSocialService, authListener);
        loginBoard.show();
    }

    public boolean isLogin(SHARE_MEDIA media) {
        return OauthHelper.isAuthenticated(mContext, media);
    }

    public void getUserInfo(SocializeListeners.FetchUserListener fetchUserListener) {
        mSocialService.getUserInfo(mContext, fetchUserListener);
    }

    public void handleOnActivityResult(int requestCode, int resultCode, Intent data) {
        UMSsoHandler handler = mSocialService.getConfig().getSsoHandler(requestCode);
        if (handler != null)
            handler.authorizeCallBack(requestCode, resultCode, data);
    }

    private void configLoginPlatform(Activity context) {
        // 添加新浪SSO授权
        mSocialService.getConfig().setSsoHandler(new SinaSsoHandler());

        // 添加QQ、QZone平台
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(context,
                SharePlatformInfo.QQ_APP_ID, SharePlatformInfo.QQ_APP_KEY);
        qqSsoHandler.addToSocialSDK();

        // 添加微信、微信朋友圈平台
        addWXPlatform(context);

        // 设置支持分享的平台
        mSocialService.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN,
                SHARE_MEDIA.QQ, SHARE_MEDIA.SINA);
    }

    private void configSharePlatforms(Activity context, ShareContent shareContent) {
        // 添加新浪SSO授权
        mSocialService.getConfig().setSsoHandler(new SinaSsoHandler());

        // 添加腾讯微博授权
        mSocialService.getConfig().setSsoHandler(new TencentWBSsoHandler());

        // 添加QQ、QZone平台
        addQQQZonePlatform(context, shareContent);

        // 添加微信、微信朋友圈平台
        addWXPlatform(context);

        // 设置支持分享的平台
        mSocialService.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA);
    }

    /**
     * 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
     * image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
     * 要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
     * : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
     */
    private void addQQQZonePlatform(Activity context, ShareContent shareContent) {
        String appId = SharePlatformInfo.QQ_APP_ID;
        String appKey = SharePlatformInfo.QQ_APP_KEY;
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(context,
                appId, appKey);
        if (shareContent != null) {
            qqSsoHandler.setTargetUrl(shareContent.getTargetURL(SHARE_MEDIA.QQ));
        }
        qqSsoHandler.addToSocialSDK();

        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(context, appId, appKey);
        qZoneSsoHandler.addToSocialSDK();
    }

    /**
     * 添加微信平台分享
     */
    private void addWXPlatform(Activity context) {
        // 注意：在微信授权的时候，必须传递appSecret
        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = SharePlatformInfo.WECHAT_APP_ID;
        String appSecret = SharePlatformInfo.WECHAT_APP_KEY;
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(context, appId, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(context, appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    /**
     * 设置分享内容
     *
     * @param shareContent 内容
     */
    protected void setShareContentCore(ShareContent shareContent) {
        UMImage shareImage = new UMImage(mContext, shareContent.imageURL);

        // 分享到QQ好友
        QQShareContent qqShareContent = new QQShareContent(shareContent.text);
        qqShareContent.setTitle(shareContent.title);
        qqShareContent.setTargetUrl(shareContent.getTargetURL(SHARE_MEDIA.QQ));
        qqShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(qqShareContent);

        // 分享到QQ空间
        QZoneShareContent qZoneShareContent = new QZoneShareContent(shareContent.text);
        qZoneShareContent.setTitle(shareContent.title);
        qZoneShareContent.setTargetUrl(shareContent.getTargetURL(SHARE_MEDIA.QZONE));
        qZoneShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(qZoneShareContent);

        // 分享到腾讯微博
//        TencentWbShareContent tencentWbShareContent = new TencentWbShareContent(shareContent.text);
//        tencentWbShareContent.setTitle(shareContent.title);
//        tencentWbShareContent.setTargetUrl(shareContent.getTargetURL());
//        tencentWbShareContent.setShareImage(shareImage);
//        mSocialService.setShareMedia(tencentWbShareContent);

        // 分享到新浪微博
        SinaShareContent sinaShareContent = new SinaShareContent(shareContent.text + "\n" + shareContent.getTargetURL(SHARE_MEDIA.SINA));
        sinaShareContent.setTitle(shareContent.title);
        sinaShareContent.setTargetUrl(shareContent.getTargetURL(SHARE_MEDIA.SINA));
        sinaShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(sinaShareContent);

        // 分享到微信好友、微信朋友圈
        WeiXinShareContent weiXinShareContent = new WeiXinShareContent(shareContent.text + shareContent.getTargetURL(SHARE_MEDIA.WEIXIN));
        weiXinShareContent.setTitle(shareContent.title);
        weiXinShareContent.setTargetUrl(shareContent.getTargetURL(SHARE_MEDIA.SINA));
        weiXinShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(weiXinShareContent);

        CircleShareContent circleShareContent = new CircleShareContent(shareContent.text);
        circleShareContent.setTitle(shareContent.text);
        circleShareContent.setTargetUrl(shareContent.getTargetURL(SHARE_MEDIA.WEIXIN_CIRCLE));
        circleShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(circleShareContent);
    }
}
