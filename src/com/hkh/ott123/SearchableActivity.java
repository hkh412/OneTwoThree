package com.hkh.ott123;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;

import com.androidquery.AQuery;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.hkh.ott123.AnalyticsApplication.TrackerName;
import com.hkh.ott123.adapter.ListDetailPagerAdapter;
import com.hkh.ott123.config.Config;
import com.hkh.ott123.fragments.BoardFragment;

public class SearchableActivity extends PagerActivity {

	private static String TAG = SearchableActivity.class.getSimpleName();
	AQuery aq;
	boolean mergeData = false;
	
	public SearchableActivity() {
		backCountForFinish = 0;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_search);
	    aq = new AQuery(getApplicationContext());
	    mViewPager = (ViewPager) findViewById(R.id.search_pager);
	    getActionBar().setTitle(mContext.getString(R.string.title_search));
	    handleIntent(getIntent());
	    setAdView();
	    sendAnalytics();
	}

	@Override
	protected void onNewIntent(Intent intent) {
	    setIntent(intent);
	    handleIntent(intent);
	}

	private void handleIntent(Intent intent) {
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
		    Fragment fragment = new BoardFragment();
		    Bundle bundle = new Bundle();
		    bundle.putAll(intent.getExtras());
		    fragment.setArguments(bundle);
		    mListDetailAdapter = new ListDetailPagerAdapter(getSupportFragmentManager(), fragment);
			mViewPager.setAdapter(mListDetailAdapter);
			mViewPager.setOffscreenPageLimit(mListDetailAdapter.getCount());
			mViewPager.setOnPageChangeListener(this);
	    }
	}

	@Override
	public void setAdView() {
		if (!Config.AdEnable) {
			return;
		}
		mAdView = (AdView) findViewById(R.id.adView);
        mAdView.setAdListener(new ToastAdListener(this));
        adRequest = new AdRequest.Builder()
//        	.addTestDevice(AdRequest.DEVICE_ID_EMULATOR) 
//        	.addTestDevice("356546051897777")
        	.build();
        mAdView.loadAd(adRequest);
	}
	
	public void sendAnalytics() {
        Tracker t = ((AnalyticsApplication) getApplication()).getTracker(
                TrackerName.APP_TRACKER);
        t.setScreenName(TAG);
        t.send(new HitBuilders.AppViewBuilder().build());
	}
	
	@Override
	public void onShowBackButtonToast() {
	}
}
