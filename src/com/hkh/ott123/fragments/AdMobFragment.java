package com.hkh.ott123.fragments;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

public class AdMobFragment extends Fragment {

	Context mContext;
	AdView mAdView;
	AdRequest adRequest;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mContext = getActivity();
		adRequest = new AdRequest.Builder()
    	.addTestDevice(AdRequest.DEVICE_ID_EMULATOR) 
    	.addTestDevice("356546051897777")
    	.build();
	}
	
	public void showAdView() {
		mAdView.loadAd(adRequest);
	}

	@Override
	public void onDestroy() {
		mAdView.destroy();
		super.onDestroy();
	}

	@Override
	public void onPause() {
		mAdView.pause();
		super.onPause();
	}

	@Override
	public void onResume() {
		mAdView.resume();
		super.onResume();
	}
}
