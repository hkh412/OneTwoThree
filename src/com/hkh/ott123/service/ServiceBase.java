package com.hkh.ott123.service;

import java.util.Map;

import android.content.Context;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.hkh.ott123.data.Session;
import com.hkh.ott123.manager.SessionManager;

public class ServiceBase {

	Context mContext;
	AjaxCallback<String> cb;
	
	public ServiceBase(Context context) {
		mContext = context;
		cb = new AjaxCallback<String>();
	}
	
	public <T> void attachSession() {
		Session session = SessionManager.getInstance().getSession();
		if (session != null) {
//			Map<String, String> cookies = new HashMap<String, String>();
//			cookies.put(Consts.C_PASSPORT, session.getPassPort());
//			cookies.put(Consts.M_SESSION_ID, session.getSessionId());
//			cb.cookies(cookies);
		}
	}
	
	/**
	 * 서비스 설정
	 * @param url 
	 * @param handler - callback 메서드를 포함한 클래스 인스턴스
	 * @param handlerName - callback 메서드명
	 */
	public void setService(String url, Object handler, String handlerName) {
		cb.url(url).type(String.class).weakHandler(handler, handlerName);
	}
	
	/**
	 * http post body params
	 * @param params
	 */
	public void setParams(Map<String, Object> params) {
		cb.params(params);
	}
	
	/**
	 * http request headers
	 * @param headers
	 */
	public void setHeaders(Map<String, String> headers) {
		if (headers != null) {
			cb.headers(headers);
		}
	}
	
	/**
	 * http request 실행
	 */
	public void request() {
		if (cb != null) {
			AQuery aq = new AQuery(mContext);
			attachSession();
			aq.ajax(cb);
		}
	};
	
}
