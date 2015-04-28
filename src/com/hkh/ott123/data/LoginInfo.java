package com.hkh.ott123.data;

public class LoginInfo {

	private static LoginInfo defaultValue = null;
	public static LoginInfo getDefault() {
		if (defaultValue == null) {
			defaultValue = new LoginInfo();
		}
		return defaultValue;
	}
}
