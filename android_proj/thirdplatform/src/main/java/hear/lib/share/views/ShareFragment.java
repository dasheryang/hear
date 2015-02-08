package hear.lib.share.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.media.QQShareContent;
import com.umeng.socialize.media.QZoneShareContent;
import com.umeng.socialize.media.SinaShareContent;
import com.umeng.socialize.media.TencentWbShareContent;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.sso.QZoneSsoHandler;
import com.umeng.socialize.sso.SinaSsoHandler;
import com.umeng.socialize.sso.TencentWBSsoHandler;
import com.umeng.socialize.sso.UMQQSsoHandler;
import com.umeng.socialize.sso.UMSsoHandler;
import com.umeng.socialize.weixin.controller.UMWXHandler;
import com.umeng.socialize.weixin.media.WeiXinShareContent;

import hear.lib.share.R;
import hear.lib.umeng.SharePlatformInfo;

/**
 * Created by ZhengYi on 15/2/8.
 */
public class ShareFragment extends Fragment {

    private UMSocialService mSocialService = UMServiceFactory
            .getUMSocialService("com.umeng.share");

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.share__frag_share, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initContentView(view);
        configPlatforms();
        setShareContent("标题", "内容", "http://www.baidu.com", "http://www.baidu.com/img/bd_logo1.png");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMSsoHandler handler = mSocialService.getConfig().getSsoHandler(requestCode);
        if (handler != null)
            handler.authorizeCallBack(requestCode, resultCode, data);
    }

    /**
     * 打开分享面板
     */
    public void openShareBroad() {
        mSocialService.openShare(getActivity(), false);
    }

    /**
     * 设置分享内容
     *
     * @param title     标题
     * @param content   内容
     * @param targetURL 目标URL
     * @param imageURL  图标地址
     */
    public void setShareContent(String title, String content, String targetURL, String imageURL) {
        UMImage shareImage = new UMImage(getActivity(), imageURL);

        // 分享到QQ好友
        QQShareContent qqShareContent = new QQShareContent(content);
        qqShareContent.setTitle(title + " - QQ好友");
        qqShareContent.setTargetUrl(targetURL);
        qqShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(qqShareContent);

        // 分享到QQ空间
        QZoneShareContent qZoneShareContent = new QZoneShareContent(content);
        qZoneShareContent.setTitle(title + " - QQ空间");
        qZoneShareContent.setTargetUrl(targetURL);
        qZoneShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(qZoneShareContent);

        // 分享到腾讯微博
        TencentWbShareContent tencentWbShareContent = new TencentWbShareContent(content);
        tencentWbShareContent.setTitle(title + " - 腾讯微博");
        tencentWbShareContent.setTargetUrl(targetURL);
        tencentWbShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(tencentWbShareContent);

        // 分享到新浪微博
        SinaShareContent sinaShareContent = new SinaShareContent(content);
        sinaShareContent.setTitle(title + " - 新浪微博");
        sinaShareContent.setTargetUrl(targetURL);
        sinaShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(sinaShareContent);

        // 分享到微信好友、微信朋友圈
        WeiXinShareContent weiXinShareContent = new WeiXinShareContent(content);
        weiXinShareContent.setTitle(title + " - 微信");
        weiXinShareContent.setTargetUrl(targetURL);
        weiXinShareContent.setShareImage(shareImage);
        mSocialService.setShareMedia(weiXinShareContent);
    }

    /**
     * 获取点击后的跳转地址
     */
    public String getTargetURL() {
        return "http://www.umeng.com/social";
    }

    private void configPlatforms() {
        // 添加新浪SSO授权
        mSocialService.getConfig().setSsoHandler(new SinaSsoHandler());

        // 添加腾讯微博授权
        mSocialService.getConfig().setSsoHandler(new TencentWBSsoHandler());

        // 添加QQ、QZone平台
        addQQQZonePlatform();

        // 添加微信、微信朋友圈平台
        addWXPlatform();

        /**
         * 设置支持分享的平台
         */
        mSocialService.getConfig().setPlatforms(SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE,
                SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.SINA);
    }

    /**
     * 添加QQ平台支持 QQ分享的内容， 包含四种类型， 即单纯的文字、图片、音乐、视频. 参数说明 : title, summary,
     * image url中必须至少设置一个, targetUrl必须设置,网页地址必须以"http://"开头 . title :
     * 要分享标题 summary : 要分享的文字概述 image url : 图片地址 [以上三个参数至少填写一个] targetUrl
     * : 用户点击该分享时跳转到的目标地址 [必填] ( 若不填写则默认设置为友盟主页 )
     */
    private void addQQQZonePlatform() {
        String appId = SharePlatformInfo.QQ_APP_ID;
        String appKey = SharePlatformInfo.QQ_APP_KEY;
        // 添加QQ支持, 并且设置QQ分享内容的target url
        UMQQSsoHandler qqSsoHandler = new UMQQSsoHandler(getActivity(),
                appId, appKey);
        qqSsoHandler.setTargetUrl(getTargetURL());
        qqSsoHandler.addToSocialSDK();

        // 添加QZone平台
        QZoneSsoHandler qZoneSsoHandler = new QZoneSsoHandler(getActivity(), appId, appKey);
        qZoneSsoHandler.addToSocialSDK();
    }

    /**
     * 添加微信平台分享
     */
    private void addWXPlatform() {
        // 注意：在微信授权的时候，必须传递appSecret
        // wx967daebe835fbeac是你在微信开发平台注册应用的AppID, 这里需要替换成你注册的AppID
        String appId = SharePlatformInfo.WECHAT_APP_ID;
        String appSecret = SharePlatformInfo.WECHAT_APP_KEY;
        // 添加微信平台
        UMWXHandler wxHandler = new UMWXHandler(getActivity(), appId, appSecret);
        wxHandler.addToSocialSDK();

        // 支持微信朋友圈
        UMWXHandler wxCircleHandler = new UMWXHandler(getActivity(), appId, appSecret);
        wxCircleHandler.setToCircle(true);
        wxCircleHandler.addToSocialSDK();
    }

    private void initContentView(View contentView) {
        contentView.findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openShareBroad();
            }
        });
    }
}
