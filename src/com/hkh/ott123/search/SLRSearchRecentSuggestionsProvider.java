package com.hkh.ott123.search;

import android.content.SearchRecentSuggestionsProvider;

public class SLRSearchRecentSuggestionsProvider extends
		SearchRecentSuggestionsProvider {

	public final static String AUTHORITY = "com.gstech.slroid.search.SLRSearchRecentSuggestionsProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;
    
    public SLRSearchRecentSuggestionsProvider() {
    	setupSuggestions(AUTHORITY, MODE);
    }
}
