package com.hkh.ott123.manager;

import com.hkh.ott123.data.CityData;
import com.hkh.ott123.data.UrlData;

public class AppDataManager {
	private static AppDataManager instance = null;
	private CityData cityData;
	private UrlData urlData;
	
	private AppDataManager() {
	}
	
	public static AppDataManager getInstance() {
		if (instance == null) {
			instance = new AppDataManager();
		}
		return instance;
	}
	
	/**
	 * 현재 선택된 지역정보
	 */
	public void setCurrentCityData(CityData cityData) {
		this.cityData = cityData;
	}
	public CityData getCurrentCityData() {
		return this.cityData;
	}
	
	/**
	 * 현재 선택된 메뉴 (left drawer)
	 */
	public void setCurrentUrlData(UrlData urlData) {
		this.urlData = urlData;
	}
	
	public UrlData getCurrentUrlData() {
		if (this.urlData == null) {
			
		}
		return this.urlData;			
	}
}
