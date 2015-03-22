package hear.app.views;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.special.ResideMenu.ResideMenu;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.bean.SnsAccount;
import com.umeng.socialize.bean.SocializeUser;
import com.umeng.socialize.controller.listener.SocializeListeners;
import com.umeng.socialize.exception.SocializeException;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import hear.app.R;
import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.ArrayUtils;
import hear.app.helper.ToastHelper;
import hear.app.helper.ToastUtil;
import hear.app.models.Article;
import hear.app.models.JsonRespWrapper;
import hear.app.store.ArticleStore;
import hear.app.store.SNSAccountStore;
import hear.lib.share.SocialServiceWrapper;
import hear.lib.share.UpdateUrgent;

/**
 * Created by power on 14-8-11.
 */
public class MainActivity extends BaseFragmentActivity implements OnClickListener, ShareFragmentDelegate, ArticleFragmentDelegate {
    private ViewPager mViewPager;
    private TextView mEmptyButton;
    private ResideMenu mResideMenu;
    private View mLoginButton;
    private View mLogoutButton;
    private SocialServiceWrapper mLoginService;
    private UILogic mUILogic = new UILogic();
    private PlaybarControl mPlaybarControl;
    private WeakReference<Fragment> mSharingFragment;

    public static void show(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        super.onCreate(savedInstanceState);
        mPlaybarControl = new PlaybarControl(this);
        setContentView(R.layout.act_main);
        initContentView();
        updateAccountView();
        mPlaybarControl.prepare(findViewById(R.id.playbar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUILogic.refreshRemoteArticleIfNeeded();
        mPlaybarControl.onActivityResume();
        mPlaybarControl.update();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPlaybarControl.onActivityPause();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();
        if (itemID == android.R.id.home) {
            mResideMenu.openMenu(ResideMenu.DIRECTION_LEFT);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.btn_login) {
            if (!SNSAccountStore.getInstance().isLogin()) {
                showLoginBoardIfNeeded();
            }
        } else if (id == R.id.btn_collect)
            mUILogic.goToCollectListActivity();
        else if (id == R.id.btn_score)
            mUILogic.doScore();
        else if (id == R.id.btn_update)
            mUILogic.doCheckUpdate(new UpdateUrgent.Callback() {
                @Override
                public void onFinishCheckUpdate(boolean hasUpdate) {
                    if (!hasUpdate)
                        ToastHelper.showNoUpdate(MainActivity.this);
                }
            });
        else if (id == R.id.btn_aboutUS)
            mUILogic.goToAboutUsActivity();
        else if (id == R.id.btn_logout) {
            if (SNSAccountStore.getInstance().isLogin()) {
                SNSAccountStore.getInstance().logout();
                updateAccountView();
            }
        }
    }

    @Override
    public boolean dispatchTouchEvent(@NonNull MotionEvent ev) {
        return mResideMenu.dispatchTouchEvent(ev);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mLoginService != null)
            mLoginService.handleOnActivityResult(requestCode, resultCode, data);

        if (mSharingFragment != null && mSharingFragment.get() != null) {
            mSharingFragment.get().onActivityResult(requestCode, resultCode, data);
            mSharingFragment = null;
        }
    }

    @Override
    public void onFragmentPerformShare(Fragment fragment) {
        mSharingFragment = new WeakReference<>(fragment);
    }

    @Override
    public void onRequestPlayArticle(Article article) {
        mPlaybarControl.playArticle(article);
    }

    protected void onLoginSuccess() {
        Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
        mLoginService = null;
        updateAccountView();
    }

    protected void onLoginFail() {
        Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
        mLoginService = null;
    }

    protected void onNoArticles() {
        mViewPager.setVisibility(View.GONE);
        mEmptyButton.setVisibility(View.VISIBLE);
        mEmptyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUILogic.refreshRemoteArticle();
            }
        });
    }

    protected void updateAccountView() {
        View loginButton = mLoginButton;
        ImageView loginImage = (ImageView) loginButton.findViewById(R.id.img_login);
        TextView loginLabel = (TextView) loginButton.findViewById(R.id.label_login);

        if (SNSAccountStore.getInstance().isLogin()) {
            SnsAccount account = SNSAccountStore.getInstance().getLoginAccount();
            ImageLoader.getInstance().displayImage(account.getAccountIconUrl(), loginImage);
            loginLabel.setText(account.getUserName());
            mLogoutButton.setVisibility(View.VISIBLE);
        } else {
            loginImage.setImageResource(R.drawable.ic_center_avatar);
            loginLabel.setText(getString(R.string.label_login));
            mLogoutButton.setVisibility(View.GONE);
        }
    }

    protected void showLoginBoardIfNeeded() {
        if (!SNSAccountStore.getInstance().isLogin()) {
            mLoginService = new SocialServiceWrapper(this);
            mLoginService.showLoginBoard(new SocializeListeners.UMAuthListener() {
                @Override
                public void onStart(SHARE_MEDIA media) {
                }

                @Override
                public void onComplete(Bundle bundle, final SHARE_MEDIA media) {
                    mLoginService.getUserInfo(new SocializeListeners.FetchUserListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onComplete(int i, SocializeUser socializeUser) {
                            if (socializeUser.mAccounts.isEmpty()) {
                                onLoginFail();
                            } else {
                                mUILogic.updateAccount(socializeUser.mAccounts.get(0), media);
                                onLoginSuccess();
                            }
                        }
                    });
                }

                @Override
                public void onError(SocializeException e, SHARE_MEDIA media) {
                    onLoginFail();
                }

                @Override
                public void onCancel(SHARE_MEDIA media) {
                    onLoginFail();
                }
            });
        }
    }

    private void initContentView() {
        /** bind views **/
        mViewPager = (ViewPager) findViewById(R.id.vp_pages);
        mEmptyButton = (TextView) findViewById(R.id.btn_empty);

        /** setup ViewPager **/
        mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == 0)
                    mResideMenu.removeIgnoredView(mViewPager);
                else
                    mResideMenu.addIgnoredView(mViewPager);
                List<Article> articles = mUILogic.getCacheArticles();
                if (articles != null) {
                    mPlaybarControl.setDefaultArticle(articles.get(position));
                }
            }
        });
        mViewPager.setCurrentItem(0);
        List<Article> mArticles = mUILogic.getCacheArticles();
        if (ArrayUtils.isEmpty(mArticles)) {
            onNoArticles();
        } else {
            ArticlePageAdapter firstCategoryAdapter = new ArticlePageAdapter(
                    this.getSupportFragmentManager(),
                    mArticles);
            mViewPager.setAdapter(firstCategoryAdapter);
            mViewPager.setCurrentItem(0);
            mPlaybarControl.setDefaultArticle(mUILogic.getCacheArticles().get(0));
            mViewPager.setVisibility(View.VISIBLE);
            mEmptyButton.setVisibility(View.GONE);
        }

        /** setup menu **/
        mResideMenu = new ResideMenu(this);
        mResideMenu.setBackground(R.drawable.bg_menu);
        mResideMenu.attachToActivity(this);
//        mResideMenu.setMenuListener(menuListener);
        mResideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
//        mResideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
        mResideMenu.setScaleValue(0.9f);

        View menuView = View.inflate(this, R.layout.layer_user_center, null);

        mLoginButton = menuView.findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(this);
        mLogoutButton = menuView.findViewById(R.id.btn_logout);
        mLogoutButton.setOnClickListener(this);
        menuView.findViewById(R.id.btn_logout).setOnClickListener(this);
        menuView.findViewById(R.id.btn_collect).setOnClickListener(this);
        menuView.findViewById(R.id.btn_score).setOnClickListener(this);
        menuView.findViewById(R.id.btn_update).setOnClickListener(this);
        menuView.findViewById(R.id.btn_aboutUS).setOnClickListener(this);
        mResideMenu.setMenuLayout(menuView, ResideMenu.DIRECTION_LEFT);
    }

    private class UILogic {
        private boolean after22Refreshed = false;
        private InActivityHelper helper = new InActivityHelper(MainActivity.this);
        private List<Article> mArticle;

        public List<Article> getCacheArticles() {
            if (mArticle == null) {
                mArticle = ArticleStore.getInstance().getArticleSet();
            }

            return mArticle;
        }

        /**
         * 获取所有的文章
         */
        public void refreshRemoteArticle() {

            String url = "http://www.hearheart.com/getlist";

            BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

                @Override
                public Class getRespClass() {
                    return InActivityHelper.ArticleListWrapper.class;
                }

                @Override
                protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
                    if (jsonRespWrapper.ret == 0) {
                        InActivityHelper.ArticleListWrapper wrapper = (InActivityHelper.ArticleListWrapper) jsonRespWrapper;
                        ArticleStore.getInstance().setArticleSet(wrapper.data);
                        initContentView();
                    } else {
                        ToastUtil.Short(jsonRespWrapper.reason);
                    }
                }
            };

            asyncTask.get(null).execute();
        }

        public void refreshRemoteArticleIfNeeded() {
            Date d = new Date();
            if (d.getHours() >= 22 && !after22Refreshed) {
                helper.getRemoteActicles(1,
                        new InActivityHelper.OnFinishListener() {
                            @Override
                            public void onFinish() {
                                after22Refreshed = true;
                            }
                        });
            }
        }

        public void updateAccount(SnsAccount account, SHARE_MEDIA platform) {
            SNSAccountStore.getInstance().setLoginAccountAndType(account, platform).synchronize();
        }

        public void goToAboutUsActivity() {
            Intent intent = new Intent(MainActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }

        public void goToCollectListActivity() {
            if (isLogin())
                CollectionActivity.show(MainActivity.this);
            else
                showLoginBoardIfNeeded();
        }

        public void doCheckUpdate(UpdateUrgent.Callback callback) {
            UpdateUrgent.checkUpdate(MainActivity.this, true, false, callback);
        }

        public void doScore() {

            Uri uri = Uri.parse("market://details?id=" + getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(uri);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Intent chooser = Intent.createChooser(intent, getString(R.string.title_choose_market));
            int matchCount = getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size();
            if (matchCount == 0) {
                ToastHelper.showNoMarketFound(MainActivity.this);
            } else if (matchCount == 1) {
                startActivity(intent);
            } else {
                startActivity(chooser);
            }
        }

        public boolean isLogin() {
            return SNSAccountStore.getInstance().isLogin();
        }
    }
}
