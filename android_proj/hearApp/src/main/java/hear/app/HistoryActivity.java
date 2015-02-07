package hear.app;

import hear.app.engine.BaseHttpAsyncTask;
import hear.app.engine.JsonRespWrapper;
import hear.app.util.ArrayUtils;
import hear.app.util.LogUtil;
import hear.app.util.ToastUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.special.ResideMenu.ResideMenu;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.controller.UMServiceFactory;
import com.umeng.socialize.controller.UMSocialService;
import com.umeng.socialize.controller.listener.SocializeListeners.UMAuthListener;
import com.umeng.socialize.controller.listener.SocializeListeners.UMDataListener;
import com.umeng.socialize.exception.SocializeException;
import com.umeng.soexample.commons.Constants;
import com.umeng.soexample.widget.CustomShareBoard;

/**
 * Created by power on 14-8-11.
 */
public class HistoryActivity extends BaseFragmentActivity implements OnClickListener{
    private UMSocialService mController = UMServiceFactory.getUMSocialService(Constants.DESCRIPTOR);
	// Pager
	ViewPager mPage;

	List<Article> mArticles = null;

	private TextView mEmptyButton;
	
	ResideMenu resideMenu = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.history);
		mPage = (ViewPager) findViewById(R.id.vp_pages);
		mEmptyButton = (TextView) findViewById(R.id.empty_btn);
		init();
	}
	
	private void init() {
		Article[] articles = Article.getAllArticles();
		if (ArrayUtils.isEmpty(articles)) {
			showEmpty();
		} else {

			mArticles = ArrayUtils.from(articles);

			ArticlePageAdapter firstCategoryAdapter = new ArticlePageAdapter(
					getResources(), this.getSupportFragmentManager(),
					mArticles, this);
			mPage.setAdapter(firstCategoryAdapter);
			mPage.setCurrentItem(0);
			mPage.setVisibility(View.VISIBLE);
			mEmptyButton.setVisibility(View.GONE);
		}
		
        // attach to current activity;
        this.initMenu();
	}

    private void initMenu(){
        resideMenu = new ResideMenu(this);
        resideMenu.setBackground(R.drawable.play_bg);
        resideMenu.attachToActivity(this);
        resideMenu.setMenuListener(menuListener);
        resideMenu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
        resideMenu.setSwipeDirectionDisable(resideMenu.DIRECTION_LEFT);
        //valid scale factor is between 0.0f and 1.0f. leftmenu'width is 150dip.
        resideMenu.setScaleValue(0.9f);

        View menuView = this.getLayoutInflater().inflate(R.layout.slide_menu, null);

        menuView.findViewById(R.id.slide_login).setOnClickListener(this);
        menuView.findViewById(R.id.slide_memory).setOnClickListener(this);
        menuView.findViewById(R.id.slide_score).setOnClickListener(this);
        menuView.findViewById(R.id.check_update).setOnClickListener(this);
        menuView.findViewById(R.id.about_us).setOnClickListener(this);
        resideMenu.setMenuLayout(menuView, ResideMenu.DIRECTION_LEFT);
    }
    public void showMenu(View view) {
        LogUtil.d("show side menu view");
        resideMenu.openMenu( ResideMenu.DIRECTION_LEFT );

    }
	/**
	 * 展示为空，让重试
	 */
	protected void showEmpty() {
		mPage.setVisibility(View.GONE);
		mEmptyButton.setVisibility(View.VISIBLE);
		mEmptyButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				refreshRemoteArticle();
			}
		});
	}

	private boolean after22Refreshed = false;

	private InActivityHelper helper = new InActivityHelper(this);

	@Override
	protected void onResume() {
		super.onResume();
		// 推出去再回来
		Date d = new Date();
		if (d.getHours() >= 22 && !after22Refreshed) {
			LogUtil.d("get server result:");
			helper.getRemoteActicles(1,
					new InActivityHelper.OnFinishListener() {
						@Override
						public void onFinish() {
							after22Refreshed = true;
						}
					});
		}
	}

	/**
	 * 获取所有的文章
	 */
	public void refreshRemoteArticle() {

		String url = "http://www.hearheart.com/getlist";

		// 978,812

		BaseHttpAsyncTask asyncTask = new BaseHttpAsyncTask(url) {

			@Override
			public Class getRespClass() {
				return InActivityHelper.ArticleListWrapper.class;
			}

			@Override
			protected void onPostExecute(JsonRespWrapper jsonRespWrapper) {
				if (jsonRespWrapper.ret == 0) {
					InActivityHelper.ArticleListWrapper wrapper = (InActivityHelper.ArticleListWrapper) jsonRespWrapper;
					LogUtil.d("resp data:" + wrapper.data);
					Article.saveArtilcleList(AppContext.getGSON().toJson(
							wrapper.data));
					init();
				} else {
					LogUtil.d("get response fail:" + jsonRespWrapper.ret);
					ToastUtil.Short(jsonRespWrapper.reason);
				}
			}
		};

		asyncTask.get(null).execute();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
	
    private ResideMenu.OnMenuListener menuListener = new ResideMenu.OnMenuListener() {
        @Override
        public void openMenu() {
        }

        @Override
        public void closeMenu() {
        }
    };
    
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return resideMenu.dispatchTouchEvent(ev);
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
        int id = v.getId();
		
		switch (id) {
		case R.id.slide_login:
			
			break;
		case R.id.slide_memory:
			postShare();
			break;
		case R.id.slide_score:
			login(SHARE_MEDIA.SINA);
			break;
		case R.id.check_update:
			login(SHARE_MEDIA.QQ);
			break;
		case R.id.about_us:
			Intent i_show_about = new Intent( this, AboutUs.class );
            this.startActivity( i_show_about );

			break;

		default:
			break;
		}
	}
	 /**
     * 下面这段是测试代码，用于测试使用
     */
	
	
	 /**
     * 授权。如果授权成功，则获取用户信息</br>
     */
    private void login(final SHARE_MEDIA platform) {
        mController.doOauthVerify(HistoryActivity.this, platform, new UMAuthListener() {

            @Override
            public void onStart(SHARE_MEDIA platform) {
            }

            @Override
            public void onError(SocializeException e, SHARE_MEDIA platform) {
            }

            @Override
            public void onComplete(Bundle value, SHARE_MEDIA platform) {
                String uid = value.getString("uid");
                if (!TextUtils.isEmpty(uid)) {
                    getUserInfo(platform);
                } else {
                    Toast.makeText(HistoryActivity.this, "授权失败...", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancel(SHARE_MEDIA platform) {
            }
        });
    }
	
	 /**
     * 获取授权平台的用户信息</br>
     */
    private void getUserInfo(SHARE_MEDIA platform) {
        mController.getPlatformInfo(this, platform, new UMDataListener() {

            @Override
            public void onStart() {

            }

            @Override
            public void onComplete(int status, Map<String, Object> info) {
//                String showText = "";
//                if (status == StatusCode.ST_CODE_SUCCESSED) {
//                    showText = "用户名：" + info.get("screen_name").toString();
//                    Log.d("#########", "##########" + info.toString());
//                } else {
//                    showText = "获取用户信息失败";
//                }
                if ( info != null ) {
                    Toast.makeText(HistoryActivity.this, info.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
	
    /**
     * 调用postShare分享。跳转至分享编辑页，然后再分享。</br> [注意]<li>
     * 对于新浪，豆瓣，人人，腾讯微博跳转到分享编辑页，其他平台直接跳转到对应的客户端
     */
    private void postShare() {
        CustomShareBoard shareBoard = new CustomShareBoard(this);
        shareBoard.showAtLocation(this.getWindow().getDecorView(), Gravity.BOTTOM, 0, 0);
    }


}
