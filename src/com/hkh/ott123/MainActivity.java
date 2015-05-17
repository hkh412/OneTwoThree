package com.hkh.ott123;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.hkh.ott123.AnalyticsApplication.TrackerName;
import com.hkh.ott123.adapter.ActionBarDropDownAdapter;
import com.hkh.ott123.adapter.ExpandableDrawerListAdapter;
import com.hkh.ott123.adapter.ListDetailPagerAdapter;
import com.hkh.ott123.config.Config;
import com.hkh.ott123.data.CityData;
import com.hkh.ott123.data.Session;
import com.hkh.ott123.data.UrlData;
import com.hkh.ott123.fragments.BoardFragment;
import com.hkh.ott123.fragments.LoginDialogFragment;
import com.hkh.ott123.fragments.LoginDialogFragment.LoginListener;
import com.hkh.ott123.manager.AppDataManager;
import com.hkh.ott123.manager.LoginManager;
import com.hkh.ott123.manager.PostStateManager;
import com.hkh.ott123.manager.SessionManager;
import com.hkh.ott123.manager.SharedPreferenceManager;
import com.hkh.ott123.util.Util;
import com.purplebrain.adbuddiz.sdk.AdBuddiz;

public class MainActivity extends PagerActivity 
	implements ActionBar.OnNavigationListener,
	View.OnClickListener, LoginListener, OnChildClickListener {

	private static String TAG = MainActivity.class.getSimpleName();
	Context mContext;
	ActionBarDrawerToggle mDrawerToggle;
	DrawerLayout mDrawerLayout;
	
	/**
	 * 왼쪽 슬라이드 메뉴관련
	 */
	LinearLayout mLeftDrawer;
	FrameLayout mLayoutHead;
	FrameLayout mLayoutProfile;
	TextView tvLogin;
	ExpandableListView mDrawerList;
	TextView tvNickname;
	TextView tvLevel;
	TextView tvPoint;
	
	ActionBarDropDownAdapter mActionBarAdapter;
	ExpandableDrawerListAdapter mDrawerListAdapter;
	ActionBar actionBar;
	
	
	/**
	 * 지역별 도메인 정보
	 */
	ArrayList<CityData> cityList = new ArrayList<CityData>();

	/**
	 * 메뉴 데이터 (상위, 하위 포함) 
	 */
	ArrayList<UrlData> urlList = new ArrayList<UrlData>();
	
	/**
	 * 좌측 상위 메뉴 데이터 (지역정보, 이야기방..)
	 */
	ArrayList<UrlData> parentNodes = new ArrayList<UrlData>();
	
	/**
	 * 메뉴하위 게시글 목록 (구인정보, 친구사귀기 등..)
	 */
	ArrayList<ArrayList<UrlData>> childNodes = new ArrayList<ArrayList<UrlData>>();
	
	/**
	 * 좌측메뉴 최초 열림
	 */
	boolean initialMenuOpen = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mContext = this;
		setContentView(R.layout.activity_main);
		mLoadingLayout = (LinearLayout) findViewById(R.id.layout_indicator);
		
		// 지역 정보 데이터 로딩
		cityList = loadCityData();
		
		// 메뉴 데이터 로딩 / 즐겨찾기 불러오기
		urlList = Util.loadUrlMapData(mContext, parentNodes, childNodes);
		restoreFavoriteMenu(urlList);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		
		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {
			@Override
			public void onDrawerClosed(View drawerView) {
				super.onDrawerClosed(drawerView);
				invalidateOptionsMenu();
			}

			@Override
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);
				hideSoftKeyboard();
				invalidateOptionsMenu();
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
        
		mLeftDrawer = (LinearLayout) findViewById(R.id.left_drawer);
		mLayoutHead = (FrameLayout) findViewById(R.id.layout_head);
		mLayoutProfile = (FrameLayout) findViewById(R.id.layout_profile);
		tvLogin = (TextView) findViewById(R.id.tv_login_text);
		tvNickname = (TextView) findViewById(R.id.tv_profile_nickname);
		tvLevel = (TextView) findViewById(R.id.tv_profile_level);
		tvPoint = (TextView) findViewById(R.id.tv_profile_point);
		mLayoutHead.setOnClickListener(this);
		
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(this);
		
		mDrawerList = (ExpandableListView) findViewById(R.id.drawer_list);
		mDrawerListAdapter = new ExpandableDrawerListAdapter(mContext, parentNodes, childNodes);
		mDrawerList.setAdapter(mDrawerListAdapter);
		
		/**
		 * 좌측 메뉴 child node 클릭 listener 설정
		 */
		mDrawerList.setOnChildClickListener(this);
		
		// 즐겨찾기 메뉴는 기본으로 Expand 상태
		mDrawerList.expandGroup(0);
		
		int lastUid = SharedPreferenceManager.getInstance(mContext).getInt("last-menu-uid");
		UrlData lastUrlData = null;
		if (lastUid < 0) {
			// 저장된 메뉴 없음, 도움요청
			lastUrlData = Util.getMatchedUrlDataByUid(urlList, 11);
		} else {
			lastUrlData = Util.getMatchedUrlDataByUid(urlList, lastUid);
		}
		AppDataManager.getInstance().setCurrentUrlData(lastUrlData);
		
		setActionBar();
		setProfile();
		setAdView();
		sendAnalytics();
		checkAutoLogin();
	}

	@Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

	/**
	 * 액션바 관련 설정
	 */
	private void setActionBar() {
		actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setHomeButtonEnabled(true);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		mActionBarAdapter = new ActionBarDropDownAdapter(mContext,
				android.R.layout.simple_spinner_dropdown_item, cityList);
		actionBar.setListNavigationCallbacks(mActionBarAdapter, this);
		String city = SharedPreferenceManager.getInstance(mContext).getString("city");
		if (city != null) {
			int position = Util.findFirstMatch(cityList, city);
			if (position >= 0) {
				actionBar.setSelectedNavigationItem(position);
			}
		}
	}
	
	@Override
	public void setAdView() {
		if (!Config.AdEnable) {
			return;
		}
		mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new ToastAdListener(this));

        String deviceId = Secure.getString(getContentResolver(), Secure.ANDROID_ID); 
        Log.d(TAG, "DeviceId: "+deviceId);
        
        AdRequest.Builder builder = new AdRequest.Builder();
        if (Config.ADVIEW_TEST) {
        	builder.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
        	.addTestDevice(deviceId);
        }
        adRequest = builder.build();
        mAdView.loadAd(adRequest);
        
     // AD provider - adbuddiz.com
        SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
        int viewCnt = spm.getInt("view_count");
        if (viewCnt >= Config.AD_THRESHOLD) {
        	spm.putInt("view_count", 0);
            AdBuddiz.setPublisherKey(mContext.getString(R.string.adbuddiz_pub_key));
            AdBuddiz.cacheAds((Activity)mContext);
            AdBuddiz.showAd(this);
        } else {
        	viewCnt++;
        	spm.putInt("view_count", viewCnt);
        }
	}
	
	public void sendAnalytics() {
        Tracker t = ((AnalyticsApplication) getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
	}
	
	/**
	 * 도시 데이터 로딩
	 * @return
	 */
	private ArrayList<CityData> loadCityData() {
		AssetManager assetManager = mContext.getAssets();
		ArrayList<CityData> cityList = new ArrayList<CityData>();
		Gson gson = new Gson();
		try {
			InputStream in = assetManager.open("city.json");
			JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
			reader.beginArray();
			while (reader.hasNext()) {
				CityData cityData = gson.fromJson(reader, CityData.class);
				cityList.add(cityData);
			}
			reader.endArray();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return cityList;
	}
	
	private void loadFragment(UrlData urlData) {
		actionBar.setTitle(urlData.getName());
		Fragment fragment = new BoardFragment();
		mListDetailAdapter = null;
		mListDetailAdapter = new ListDetailPagerAdapter(getSupportFragmentManager(), fragment);
		mViewPager.setAdapter(mListDetailAdapter);
		mViewPager.setOffscreenPageLimit(mListDetailAdapter.getCount());
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		MenuItem searchItem = menu.findItem(R.id.action_search);
		searchItem.setVisible(false);
		
		SearchView searchView = (SearchView) searchItem.getActionView();
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		int id = item.getItemId();
		if (id==R.id.action_write) {
			// 글쓰기
			Session session = SessionManager.getInstance().getSession();
			if (session == null) {
				Toast.makeText(mContext,
						mContext.getString(R.string.message_write_login), Toast.LENGTH_LONG).show();
				return true;
			}
			Intent intent = new Intent(mContext, WriteActivity.class);
			startActivityForResult(intent, 0);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 0) {
			if (resultCode == RESULT_OK) {
				UrlData urlData = AppDataManager.getInstance().getCurrentUrlData();
				loadFragment(urlData);
			}
		}
	}
	
	@Override
	public boolean onChildClick(ExpandableListView parent, View v,
			int groupPosition, int childPosition, long id) {
		mDrawerLayout.closeDrawer(mLeftDrawer);
		mBackBtnClickCount = 0;
		mViewPager.setAdapter(null);
		
		// 선택한 메뉴 인덱스 저장
		UrlData urlData = mDrawerListAdapter.getChild(groupPosition, childPosition);
		AppDataManager.getInstance().setCurrentUrlData(urlData);
		int parentIndex = groupPosition;
		if (parentIndex == 0) {
			// 즐겨찾기 클릭
			for (int i=0; i<childNodes.size(); i++) {
				ArrayList<UrlData> list = childNodes.get(i);
				if (list.indexOf(urlData) >= 0) {
					parentIndex = i;
					break;
				}
			}
		}
		SharedPreferenceManager.getInstance(mContext).putInt("last-menu-uid", urlData.getUid());
		loadFragment(urlData);
		return true;
	}
	
	@Override
	public boolean onNavigationItemSelected(int position, long itemId) {
		hideSoftKeyboard();
		CityData cityData = mActionBarAdapter.getItem(position);
		AppDataManager.getInstance().setCurrentCityData(cityData); 
		SharedPreferenceManager.getInstance(mContext).putString("city", cityData.getCity());
		mDrawerLayout.closeDrawer(mLeftDrawer);
		
		UrlData urlData = AppDataManager.getInstance().getCurrentUrlData();
		loadFragment(urlData);
		return true;
	}
	
	/**
	 * 좌측 사용자 정보 설정
	 */
	private void setProfile() {
		Session session = SessionManager.getInstance().getSession();
		if (session != null) {
			tvLogin.setVisibility(View.GONE);
			mLayoutProfile.setVisibility(View.VISIBLE);
			tvNickname.setText(session.getNickName());
			tvLevel.setText(session.getLevel());
			tvPoint.setText(session.getPoint());
		} else {
			tvLogin.setVisibility(View.VISIBLE);
			mLayoutProfile.setVisibility(View.GONE);
		}
	}
	
	/**
	 * 자동 로그인 설정여부를 체크하고 자동 로그인 수행
	 */
	private void checkAutoLogin() {
		SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		boolean isAutoLogin = spm.getBoolean("auto-login");
		if (isAutoLogin) {
			String username = spm.getString("username");
			String password = spm.getString("password");
			if (username == null || password == null) {
				Log.e(TAG, "자동로그인이 체크되었지만 저장된 계정에 문제가 발생함.");
			} else {
				LoginManager.getInstance().doLogin(mContext,
						username, password, this);
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		if (mLayoutHead == v) {
			mDrawerLayout.closeDrawer(mLeftDrawer);
			// 로그인
			LoginDialogFragment dialog = new LoginDialogFragment();
			dialog.setLoginListener(this);
	        dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
		}
	}

	@Override
	public void onLoginComplete() {
		// 로그인 완료
		setProfile();
		refreshWritePermission();
		SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		
		// 자동로그인 설정
		spm.putBoolean("auto-login", true);
		Toast.makeText(mContext, 
				mContext.getString(R.string.message_login_success), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLoginFailure(String message) {
		if (message != null && message.length() > 0) {
			Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(mContext, 
					mContext.getString(R.string.message_login_problem), Toast.LENGTH_SHORT).show();
		}
	}
	
	@Override
	public void onLogoutComplete() {
		// session 지움
		SessionManager.getInstance().setSession(null);
		// 자동로그인  false
		SharedPreferenceManager spm = SharedPreferenceManager.getInstance(mContext);
		spm.putBoolean("auto-login", false);
		// setProfile 호출
		setProfile();
		// Toast
		Toast.makeText(mContext,
				mContext.getString(R.string.message_logout_success), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onLogoutFailure() {
		Toast.makeText(mContext,
				mContext.getString(R.string.message_logout_problem), Toast.LENGTH_SHORT).show();
	}
	
	/**
	 * 저장된 즐겨찾기 불러오기
	 * @param urlList
	 */
	private void restoreFavoriteMenu(List<UrlData> urlList) {
		// restore favorite menu
		PostStateManager psm = PostStateManager.getInstance(mContext);
		List<Integer> favoriteList = psm.restoreFavoriteMenu();
		// 저장된 favoriteList가 있으면 즐겨찾기메뉴에 추가
		if (favoriteList != null) {
			ArrayList<UrlData> favorites = childNodes.get(0);
			for (UrlData menuData : urlList) {
				if (favoriteList.contains(menuData.getUid())) {
					favorites.add(menuData);
				}
			}
		}
	}
	
	/**
	 * 처음 게시글목록 로딩완료후 side menu 열림
	 */
	public void openDrawerList() {
		if (!initialMenuOpen) {
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					runOnUiThread(new Runnable(){
						@Override
						public void run() {
							mDrawerLayout.openDrawer(mLeftDrawer);
						}
					});
				}
			};
			Timer timer = new Timer();
			timer.schedule(timerTask, 300);
			initialMenuOpen = true;
		}
	}

	@Override
	public void onShowBackButtonToast() {
		Toast.makeText(mContext, mContext.getString(R.string.message_back_button_toast),
				Toast.LENGTH_SHORT).show();		
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    if (newConfig.keyboardHidden == Configuration.KEYBOARDHIDDEN_NO) {
	    	Log.d(TAG, "keyboard hidden");
	    } else {
	    	Log.d(TAG, "keyboard showed up");
	    }
	}
}
