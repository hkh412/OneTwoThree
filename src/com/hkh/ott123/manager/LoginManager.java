package com.hkh.ott123.manager;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.cookie.Cookie;
import org.apache.http.message.BasicNameValuePair;

import android.content.Context;
import android.util.Log;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.hkh.ott123.config.Consts;
import com.hkh.ott123.config.Links;
import com.hkh.ott123.data.Session;
import com.hkh.ott123.fragments.LoginDialogFragment.LoginListener;

public class LoginManager {
	private static LoginManager instance = null;
	
	private LoginManager() {
	}
	
	public static LoginManager getInstance() {
		if (instance == null) {
			instance = new LoginManager();
		}
		return instance;
	}
	
	/**
	 * 123 로그인 함수
	 * @param context
	 * @param username
	 * @param password
	 * @param listener
	 */
	public void doLogin(Context context,
			String username, String password,
			final LoginListener listener) {
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("mb_user", username));                         
        pairs.add(new BasicNameValuePair("mb_pass", password));    
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", "http://shanghai.123123.net/main/");
		
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        AQuery aq = new AQuery(context);
        aq.ajax(Links.LOGIN_URL, params, String.class, new AjaxCallback<String>(){
        	@Override
	        public void callback(String url, String result, AjaxStatus status) {
				if (status.getCode() == 200) {
					List<Cookie> cookies = status.getCookies();
					String userName = null;
					String sessionId = null;
					String nickName = null;
					String point = null;
					String email = null;
					String md5 = null;
					String level = null;
					for (Cookie cookie : cookies) {
						if (cookie.getName().equals(Consts.MB_ID_KEY)) {
							userName = cookie.getValue();
						} else if (cookie.getName().equals(Consts.SESSION_ID_KEY)) {
							sessionId = cookie.getValue();
							continue;
						} else if (cookie.getName().equals(Consts.MB_NICK_KEY)) {
							try {
								nickName = URLDecoder.decode(cookie.getValue(), "UTF-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
							}
							continue;
						} else if (cookie.getName().equals(Consts.MB_POINT_KEY)) {
							point = cookie.getValue();
							continue;
						} else if (cookie.getName().equals(Consts.MB_EMAIL_KEY)) {
							email = cookie.getValue();
							continue;
						} else if (cookie.getName().equals(Consts.MB_LEVEL_KEY)) {
							level = cookie.getValue();
							continue;
						} else if (cookie.getName().equals(Consts.MD5_KEY)) {
							md5 = cookie.getValue();
							continue;
						}
					}
					Log.d("LoginManager", userName+" | "+sessionId+" | "+nickName+" | "+point);
					
					if (userName != null) {
						Session session = new Session(sessionId, userName);
						session.setNickName(nickName);
						session.setPoint(point);
						session.setEmail(email);
						session.setLevel(level);
						session.setMd5(md5);
						SessionManager.getInstance().setSession(session);
						
						if (listener != null) {
							listener.onLoginComplete();
						}
					} else if (result != null){
						// 아이디 비밀번호가 맞지 않는 경우
						int index = result.indexOf("alert('");
						String message = null;
						if (index >= 0) {
							message = result.substring(index+7);
							String[] tmps = message.split("'");
							if (tmps.length > 0) {
								message = tmps[0];
							}
						}
						listener.onLoginFailure(message);
					} else {
						// 기타 에러
						listener.onLoginFailure(null);
					}
				}
			}
        }.headers(headers));
	}
	
	/**
	 * 로그아웃 호출
	 */
	public void doLogout(Context context, final LoginListener listener) {
		AQuery aq = new AQuery(context);
		Session session = SessionManager.getInstance().getSession();
        aq.ajax(Links.LOGIN_URL, String.class, new AjaxCallback<String>(){
        	@Override
	        public void callback(String url, String result, AjaxStatus status) {
        		if (status.getCode() == 200) {
        			listener.onLogoutComplete();
        		} else {
        			listener.onLogoutFailure();
        		}
        	}
        }.cookie(Consts.SESSION_ID_KEY, session.getSessionId()));
	}
}
