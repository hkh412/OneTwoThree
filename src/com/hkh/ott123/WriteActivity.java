package com.hkh.ott123;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.hkh.ott123.config.Consts;
import com.hkh.ott123.config.Links;
import com.hkh.ott123.data.CityData;
import com.hkh.ott123.data.Session;
import com.hkh.ott123.data.UrlData;
import com.hkh.ott123.manager.AppDataManager;
import com.hkh.ott123.manager.SessionManager;
import com.hkh.ott123.util.Util;

public class WriteActivity extends Activity {
	
	Context mContext = null;
	
	UrlData urlData = null;
	String domainUrl = null;
	
	LinearLayout mLoadingLayout;
	
	LinearLayout layoutRegion;
	LinearLayout layoutCategory;
	
	Spinner spinnerRegion;
	Spinner spinnerCategory;
	
	Button btnOk;
	Button btnCancel;
	
	EditText etTitle;
	EditText etContent;

	TextView tvMenuNm;
	
	boolean hasRegion = false;
	boolean hasCategory = false;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_write);
		
		mContext = this;
		mLoadingLayout = (LinearLayout) findViewById(R.id.layout_indicator);
		layoutRegion = (LinearLayout)findViewById(R.id.layout_region);
		layoutCategory = (LinearLayout)findViewById(R.id.layout_category);
		
		tvMenuNm = (TextView)findViewById(R.id.tv_menu_nm);
		etTitle = (EditText)findViewById(R.id.et_title);
		etContent = (EditText)findViewById(R.id.et_content);
		
		spinnerRegion = (Spinner)findViewById(R.id.spinner_region);
		spinnerCategory = (Spinner)findViewById(R.id.spinner_category);
		
		btnOk = (Button)findViewById(R.id.btn_send_write);
		btnCancel = (Button)findViewById(R.id.btn_cancel_write);
		
		// 확인
		btnOk.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// validation check
				if (hasRegion && spinnerRegion.getSelectedItemPosition() <= 0) {
					Toast.makeText(mContext,
							mContext.getString(R.string.message_select_region),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (hasCategory && spinnerCategory.getSelectedItemPosition() <= 0) {
					Toast.makeText(mContext,
							mContext.getString(R.string.message_select_category),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (etTitle.getText().length()<=0) {
					Toast.makeText(mContext,
							mContext.getString(R.string.message_put_title),
							Toast.LENGTH_SHORT).show();
					return;
				}
				if (etContent.getText().length()<=0) {
					Toast.makeText(mContext,
							mContext.getString(R.string.message_put_content),
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				sendPost();
			}
		});
		
		// 취소
		btnCancel.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		urlData = AppDataManager.getInstance().getCurrentUrlData();
		if (urlData == null) {
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		tvMenuNm.setText(urlData.getName());
		
		// spinner visible 처리
		String[] params = urlData.getParam().split("[|]");
		for (int i=0; i<params.length; i++) {
			if (params[i].length()>0 && params[i].equals("region")) {
				hasRegion = true;
				layoutRegion.setVisibility(View.VISIBLE);
			}
			if (params[i].length()>0 && params[i].equals("category")) {
				hasCategory = true;
				layoutCategory.setVisibility(View.VISIBLE);
			}
		}
		
		showLoadingIndicator();
		queryWritePage();
	}
	
	private void sendPost() {
		showLoadingIndicator();
		
		String bo_table = Util.getValueFromUrl(urlData.getUrl(), "bo_table");
		Session session = SessionManager.getInstance().getSession();
		
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        
        if (hasRegion) {
        	String region = (String)spinnerRegion.getSelectedItem();
        	pairs.add(new BasicNameValuePair("diqu_cate2", region)); 
        }
        if (hasCategory) {
        	String category = (String)spinnerCategory.getSelectedItem();
        	pairs.add(new BasicNameValuePair("cate1", category)); 
        }
        if (urlData.getUid() == 4 || urlData.getUid() == 5) {
        	// 부동산 OR 벼룩시장
        	pairs.add(new BasicNameValuePair("cate2", "기타")); 
        }
        
        pairs.add(new BasicNameValuePair("bo_table", bo_table));
        pairs.add(new BasicNameValuePair("s_url", ""));
        pairs.add(new BasicNameValuePair("id", ""));
        pairs.add(new BasicNameValuePair("file2", ""));    
        pairs.add(new BasicNameValuePair("ssid", session.getSessionId()));    
        pairs.add(new BasicNameValuePair("title", etTitle.getText().toString()));
        pairs.add(new BasicNameValuePair("content", etContent.getText().toString()));    
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", domainUrl+urlData.getUrl());
		
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        AQuery aq = new AQuery(mContext);
        AjaxCallback<String> cb = new AjaxCallback<String>();
        try {
			cb.url(domainUrl+Links.SEND_POST_URL)
				.params(params).type(String.class).weakHandler(this, "onPostSent")
				.headers(headers)
				.cookie(Consts.SESSION_ID_KEY, session.getSessionId())
				.cookie(Consts.MB_ID_KEY, session.getUserName())
				.cookie(Consts.MB_NICK_KEY, URLEncoder.encode(session.getNickName(), "UTF-8"))
				.cookie(Consts.MB_POINT_KEY, session.getPoint())        	
				.cookie(Consts.SEND_TIME_KEY, String.valueOf(System.currentTimeMillis()))        	
	        	.cookie(Consts.MB_EMAIL_KEY, session.getEmail())        	
	        	.cookie(Consts.MB_LEVEL_KEY, session.getLevel())        	
	        	.cookie(Consts.MD5_KEY, session.getMd5());
	        aq.ajax(cb);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void onPostSent(String url, String html, AjaxStatus ajaxStatus) {
		hideLoadingIndicator();
		if (ajaxStatus.getCode() == 200) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_post_sent),
					Toast.LENGTH_LONG).show();
		}
		setResult(RESULT_OK);
		finish();
	}
	
	private void queryWritePage() {
		CityData currentCityData = AppDataManager.getInstance().getCurrentCityData();
		if (currentCityData != null) {
			domainUrl = currentCityData.getUrl();
		} else {
			domainUrl = Links.DEFAULT_CITY_URL;
		}
		String bo_table = Util.getValueFromUrl(urlData.getUrl(), "bo_table");
		String url = domainUrl+Links.WRITE_POST_URL+"&bo_table="+bo_table;
		
		Session session = SessionManager.getInstance().getSession();
		AQuery aq = new AQuery(mContext);
		AjaxCallback<String> cb = new AjaxCallback<String>();           
		try {
			cb.url(url)
				.cookie(Consts.SESSION_ID_KEY, session.getSessionId())
				.cookie(Consts.MB_ID_KEY, session.getUserName())
				.cookie(Consts.MB_NICK_KEY, URLEncoder.encode(session.getNickName(), "UTF-8"))
				.cookie(Consts.MB_POINT_KEY, session.getPoint())        	
				.cookie(Consts.SEND_TIME_KEY, String.valueOf(System.currentTimeMillis()))        	
				.cookie(Consts.MB_EMAIL_KEY, session.getEmail())        	
				.cookie(Consts.MB_LEVEL_KEY, session.getLevel())        	
				.cookie(Consts.MD5_KEY, session.getMd5())
				.type(String.class).weakHandler(this, "onQueryResult");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		aq.ajax(cb);
	}
	
	public void onQueryResult(String url, String html, AjaxStatus ajaxStatus) {
//		System.out.println(html);
		hideLoadingIndicator();
		if (html == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		Element element = Jsoup.parse(html).body();
		// 서버 alert 체크 (글쓰기 빠름 등)
		Elements alertElm = element.select("div.pop-3 div.text");
		if (alertElm.size()>0) {
			Toast.makeText(mContext,
					alertElm.text(), Toast.LENGTH_LONG).show();
			setResult(RESULT_CANCELED);
			finish();
			return;
		}
		
		if (hasRegion) {
			ArrayList<String> options1 = new ArrayList<String>();
			Elements regionElm = element.select("#diqu option");
			for (int i=0; i<regionElm.size(); i++) {
				String value = regionElm.get(i).attr("value");
				if (value.equals("")) {
					value = "선택";
				}
				options1.add(value);
			}
			ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(mContext,
			        android.R.layout.simple_list_item_1, options1);
			spinnerRegion.setAdapter(adapter1);
		}
		if (hasCategory) {
			ArrayList<String> options2 = new ArrayList<String>();
			Elements regionElm = element.select("#cate1 option");
			for (int i=0; i<regionElm.size(); i++) {
				String value = regionElm.get(i).attr("value");
				if (value.equals("")) {
					value = "선택";
				}
				options2.add(value);
			}
			ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(mContext,
			        android.R.layout.simple_list_item_1, options2);
			spinnerCategory.setAdapter(adapter2);
		}
	}
	
	public void showLoadingIndicator() {
		if (mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.VISIBLE);
		}
	}
	
	public void hideLoadingIndicator() {
		if (mLoadingLayout != null) {
			mLoadingLayout.setVisibility(View.INVISIBLE);
		}
	}
}
