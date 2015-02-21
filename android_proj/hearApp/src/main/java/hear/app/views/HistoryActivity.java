package hear.app.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
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
import com.umeng.update.UmengUpdateAgent;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.List;

import hear.app.R;
import hear.app.engine.BaseHttpAsyncTask;
import hear.app.helper.AppContext;
import hear.app.helper.ArrayUtils;
import hear.app.helper.ToastUtil;
import hear.app.models.Article;
import hear.app.models.JsonRespWrapper;
import hear.app.models.SNSAccountStore;
import hear.lib.share.SocialServiceWrapper;

/**
 * Created by power on 14-8-11.
 */
public class HistoryActivity extends BaseFragmentActivity implements OnClickListener, ShareFragmentDelegate,ArticleFragmentDelegate {
    private ViewPager mViewPager;
    private TextView mEmptyButton;
    private ResideMenu mResideMenu;
    private View mLoginButton;
    private SocialServiceWrapper mLoginService;
    private UILogic mUILogic = new UILogic();
    private PlaybarControl mPlaybarControl;
    private WeakReference<Fragment> mSharingFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history);
        initContentView();
        updateAccountView();
        mPlaybarControl = new PlaybarControl(this);
        mPlaybarControl.prepare(findViewById(R.id.playbar));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mUILogic.refreshRemoteArticleIfNeeded();
        mPlaybarControl.update();
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
            if (SNSAccountStore.getInstance().isLogin()) {
                SNSAccountStore.getInstance().logout();
                updateAccountView();
            } else {
                showLoginBoardIfNeeded();
            }
        } else if (id == R.id.btn_collect)
            mUILogic.goToCollectListActivity();
        else if (id == R.id.btn_score)
            mUILogic.doScore();
        else if (id == R.id.btn_update)
            mUILogic.doCheckUpdate();
        else if (id == R.id.btn_aboutUS)
            mUILogic.goToAboutUsActivity();
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

        if (mSharingFragment.get() != null) {
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
        Toast.makeText(HistoryActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
        mLoginService = null;
        updateAccountView();
    }

    protected void onLoginFail() {
        Toast.makeText(HistoryActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
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
        } else {
            loginImage.setImageResource(R.drawable.like_item);
            loginLabel.setText("请登陆");
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
        List<Article> mArticles = mUILogic.getCacheArticles();
        if (ArrayUtils.isEmpty(mArticles)) {
            onNoArticles();
        } else {
            ArticlePageAdapter firstCategoryAdapter = new ArticlePageAdapter(
                    getResources(), this.getSupportFragmentManager(),
                    mArticles, this);
            mViewPager.setAdapter(firstCategoryAdapter);
            mViewPager.setCurrentItem(0);
            mViewPager.setVisibility(View.VISIBLE);
            mEmptyButton.setVisibility(View.GONE);
        }

        /** setup menu **/
        mResideMenu = new ResideMenu(this);
        mResideMenu.setBackground(R.drawable.play_bg);
        mResideMenu.attachToActivity(this);
//        mResideMenu.setMenuListener(menuListener);
        mResideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
//        mResideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_LEFT);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
        mResideMenu.setScaleValue(0.9f);

        View menuView = View.inflate(this, R.layout.slide_menu, null);

        mLoginButton = menuView.findViewById(R.id.btn_login);
        mLoginButton.setOnClickListener(this);
        menuView.findViewById(R.id.btn_collect).setOnClickListener(this);
        menuView.findViewById(R.id.btn_score).setOnClickListener(this);
        menuView.findViewById(R.id.btn_update).setOnClickListener(this);
        menuView.findViewById(R.id.btn_aboutUS).setOnClickListener(this);
        mResideMenu.setMenuLayout(menuView, ResideMenu.DIRECTION_LEFT);
    }

    private class UILogic {
        private boolean after22Refreshed = false;
        private InActivityHelper helper = new InActivityHelper(HistoryActivity.this);
        private List<Article> mArticle;

        public List<Article> getCacheArticles() {
            if (mArticle == null) {
                Article[] articles = Article.getAllArticles();
                if (ArrayUtils.isEmpty(articles)) {
                    return null;
                } else {
                    mArticle = ArrayUtils.from(articles);
                }
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
                        Article.saveArtilcleList(AppContext.getGSON().toJson(
                                wrapper.data));
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
            Intent intent = new Intent(HistoryActivity.this, AboutUsActivity.class);
            startActivity(intent);
        }

        public void goToCollectListActivity() {
            if (isLogin())
                CollectionActivity.show(HistoryActivity.this);
            else
                showLoginBoardIfNeeded();
        }

        public void doCheckUpdate() {
            UmengUpdateAgent.forceUpdate(HistoryActivity.this);
        }

        public void doScore() {
        }

        public boolean isLogin() {
            return SNSAccountStore.getInstance().isLogin();
        }
    }
}
