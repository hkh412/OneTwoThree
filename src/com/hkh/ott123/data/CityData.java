package com.hkh.ott123.data;

import java.io.Serializable;

public class CityData implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String url = null;
	private String city = null;
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	
}
