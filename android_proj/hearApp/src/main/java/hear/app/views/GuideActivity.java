package hear.app.views;

import hear.app.R;
import hear.app.helper.LogUtil;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Window;
import android.widget.LinearLayout;

/**
 * Created by power on 14-8-11.
 */
public class GuideActivity extends FragmentActivity {

	private InActivityHelper helper = new InActivityHelper(this);

	// Pager
	ViewPager mPage;
	LinearLayout mDotsLayout;

	private boolean started = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.guide);

		mPage = (ViewPager) findViewById(R.id.guide_viewpager);
		mDotsLayout = (LinearLayout) findViewById(R.id.guide_dots);
		GuideFragmentAdapter adapter = new GuideFragmentAdapter(
				this.getSupportFragmentManager());
		mPage.setAdapter(adapter);

		// mPage.setOnPageChangeListener();
		mPage.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				for (int i = 0; i < mDotsLayout.getChildCount(); i++) {
					if (i == arg0) {
						mDotsLayout.getChildAt(i).setSelected(true);
					} else {
						mDotsLayout.getChildAt(i).setSelected(false);
					}
				}
				currentPage = arg0;
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				LogUtil.d("current page:" + currentPage);
				// TODO Auto-generated method stub
				LogUtil.d("arg0:" + arg0);
				LogUtil.d("arg1:" + arg1);
				LogUtil.d("arg2:" + arg2);
				if (currentPage == 3 && arg0 == 3 && arg1 == 0.0 && arg2 == 0
						&& scrollState == 1) {
					if (!started) {
						started = true;
						helper.initEntranceActivity();
					}
				}
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
				// TODO Auto-generated method stub
				LogUtil.d("on page scroll state changed:" + arg0);
				scrollState = arg0;
			}
		});
	}

	private int currentPage = 0;
	private int scrollState = 0;

	@Override
	public void onBackPressed() {
	}

	public class GuideFragmentAdapter extends FragmentPagerAdapter {

		public GuideFragmentAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Fragment getItem(int position) {
			final Fragment result;
			result = new GuideFragment();
			Bundle args = new Bundle();
			args.putInt(GuideFragment.KEY_POSITION, position);
			result.setArguments(args);
			return result;
		}
	}

}
