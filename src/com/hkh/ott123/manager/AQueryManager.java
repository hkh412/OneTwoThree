package com.hkh.ott123.manager;

import android.content.Context;

import com.androidquery.AQuery;

public class AQueryManager {
	private static AQueryManager instance = null;
	Context mContext;
	AQuery aq = null;
	private AQueryManager(Context context) {
		mContext = context;
		aq = new AQuery(mContext);
	}
	
	public static AQueryManager getInstance(Context context) {
		if (instance == null) {
			instance = new AQueryManager(context);
		}
		return instance;
	}
	
	public AQuery getAQuery() {
		return aq;
	}
}
