package com.hkh.ott123.config;

public class Config {

	/**
	 *  Log 메시지 출력 여부
	 */
	public static boolean LogEnable = false;
	
	/**
	 * Debug 모드
	 */
	public static boolean DebugEnable = true;
	
	/**
	 * 광고뷰 테스트모드
	 */
	public static boolean ADVIEW_TEST = false;
	
	/**
	 * 광고 로딩여부
	 */
	public static boolean AdEnable = true;
	
	/**
	 * 광고 Toast 여부
	 */
	public static boolean AdToast = false;
	
	/**
	 * 방문한 게시글 저장 리스트의 크기
	 */
	public static int LIST_LIMIT = 100;
	
	public static final String USER_AGENT = 
			"Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_3_2 like Mac OS X; en-us)"+
			"AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8H7 Safari/6533.18.5";
}
