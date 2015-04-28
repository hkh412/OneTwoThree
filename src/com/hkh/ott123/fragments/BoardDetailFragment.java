package com.hkh.ott123.fragments;

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
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hkh.ott123.PagerActivity;
import com.hkh.ott123.R;
import com.hkh.ott123.ScaleImageActivity;
import com.hkh.ott123.adapter.PostViewAdapter;
import com.hkh.ott123.config.Config;
import com.hkh.ott123.config.Consts;
import com.hkh.ott123.config.HTMLAttrs;
import com.hkh.ott123.config.HTMLTags;
import com.hkh.ott123.config.Links;
import com.hkh.ott123.data.CityData;
import com.hkh.ott123.data.CommentData;
import com.hkh.ott123.data.PostItem;
import com.hkh.ott123.data.Session;
import com.hkh.ott123.data.UrlData;
import com.hkh.ott123.events.OnScaleImageViewListener;
import com.hkh.ott123.manager.AppDataManager;
import com.hkh.ott123.manager.SessionManager;
import com.hkh.ott123.manager.TimerManager;
import com.hkh.ott123.service.QueryDetailPageService;
import com.hkh.ott123.util.Util;

public class BoardDetailFragment extends Fragment 
	implements OnClickListener, OnScaleImageViewListener {

	private final String TAG = BoardDetailFragment.class.getSimpleName();
	private final String IMAGE_URL_KEY = "image_url_key";
	Context mContext;
	InputMethodManager imm;
	AppDataManager appDataMgr;
	
	/**
	 * 상세뷰를 기본이 되는 ListView
	 */
	PullToRefreshListView ptrListView;
	
	/**
	 * 상세뷰 ListView Adapter
	 */
	PostViewAdapter postAdapter;
	
	/**
	 * 댓글 입력 뷰
	 */
	LinearLayout layoutWriteCmt;
	
	/**
	 * 댓글 전송 버튼
	 */
	Button btnSendComment;
	
	/**
	 * 댓글 작성
	 */
	EditText etComment;
	
	/**
	 * 이미지 url 주소
	 */
	ArrayList<String> imageUrls;
	
	ArrayList<PostItem> items;
	PostItem headerItem;
	
	/**
	 * 지역 도메인 url
	 */
	String domainUrl;
	
	/**
	 * 게시글  링크 (board.php?act=view&id=159565&bo_table=10_21&)
	 * id=159565 -> 댓글등록시 board_id 임
	 */
	String link;
	
	/**
	 *  true: 작성자가 있는 게시글
	 */
	boolean isAuthor = false;
	
	public BoardDetailFragment() {
		items = new ArrayList<PostItem>();
		imageUrls = new ArrayList<String>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mContext = getActivity();
		appDataMgr = AppDataManager.getInstance();
		
		View rootView = inflater.inflate(R.layout.fragment_board_detail, container, false);
		ptrListView = (PullToRefreshListView) rootView.findViewById(R.id.layout_root);
		ptrListView.setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				requestPage(link);
			}
		});
		
		layoutWriteCmt = (LinearLayout) rootView.findViewById(R.id.layout_write_comment);
		etComment = (EditText) rootView.findViewById(R.id.et_comment);
		etComment.setOnFocusChangeListener(new OnFocusChangeListener() {
			/**
			 * 댓글입력 활성화 시 광고뷰를 감춘다
			 */
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					showAdView(false);
				}
			}
		});
		btnSendComment = (Button) rootView.findViewById(R.id.btn_send_comment);
		btnSendComment.setOnClickListener(this);
		
		postAdapter = new PostViewAdapter(mContext, R.layout.list_item_post, items);
		postAdapter.setOnScaleImageViewListener(this);
		ptrListView.setAdapter(postAdapter);
		
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		Bundle bundle = getArguments();
		link = bundle.getString("link");
		
		CityData cityData = appDataMgr.getCurrentCityData();
		if (cityData != null) {
			domainUrl = cityData.getUrl();
		}
		UrlData urlData = appDataMgr.getCurrentUrlData();
		if (urlData != null) {
			isAuthor = urlData.isAuthor();
		} else {
			isAuthor = false;
		}
		
		// 상세뷰 헤더 구성
		headerItem = new PostItem();
		headerItem.type = PostItem.TYPE_HEADER_VIEW;
		headerItem.authorNm = bundle.getString("authorNm");
		headerItem.lvImg = domainUrl+bundle.getString("lvImg");
		headerItem.title = bundle.getString("title");
		headerItem.viewCnt = bundle.getString("viewCnt");
		headerItem.isAuthor = isAuthor;
		
		requestPage(link);
	}
	
	public void requestPage(String link) {
		((PagerActivity)mContext).showLoadingIndicator();
		items.removeAll(items);
		
		QueryDetailPageService service = new QueryDetailPageService(mContext);
		service.setService(link, this, "onQueryResult");
		service.request();
	}

	public void onQueryResult(String url, String html, AjaxStatus ajaxStatus) {
		((PagerActivity)mContext).hideLoadingIndicator();
		ptrListView.onRefreshComplete();
		
		if (html == null) {
			if (Config.LogEnable) {
				Log.e(TAG, "상세페이지 내용이 null, 확인필요!");
			}
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
        Document document = Jsoup.parse(html);
		
		Element body = document.body();
		Elements infos = body.select("div.article-info div.info i");
		Elements contents = body.select("div.article-content");
		Elements comments = body.select("div.cmbox .item");
		Elements cmWrite = body.select("#comm");
		
		/**
		 *  헤더에 날짜시간 추가
		 */
		if (infos.size() > 0) {
			try {
				String[] dates = infos.get(0).text().split(" ");
				String date = dates[0]+" "+dates[1];
				headerItem.date = date;
			} catch (Exception e) {
			}
		}
		items.add(headerItem);
		
		/**
		 * 글 내용 파싱
		 */
		if (contents.size() > 0) {
			parseContentElement(contents.get(0));
		}

		/**
		 * 댓글 파싱
		 */
		parseCommentElements(comments);
		
		/**
		 * 댓글 기능 지원하지 않는 포스트 일경우
		 */
		if (cmWrite.size() > 0) {
			refreshWriteCommentView();
			showAdView(false);
		} else {
			layoutWriteCmt.setVisibility(View.GONE);
			showAdView(true);
		}
		
		// 리스트 display
		postAdapter.notifyDataSetChanged();
	}
	
	/**
	 * 내용 파싱
	 * @param element
	 */
	private void parseContentElement(Element element) {
		String[] lines = element.html().split("\n");
		
		StringBuffer plainText = new StringBuffer();
		for (int i=0; i<lines.length; i++) {
			Element body = Jsoup.parse(lines[i]).body();
			
			int size = body.children().size();
			if (size <= 0) {
				plainText.append(lines[i]);
				continue;
				
			} else if (size == 1) {
				Element e = body.child(0);
				// 태그 비교
				if (e.tagName().equals(HTMLTags.CENTER)) {
					// recursive
					parseContentElement(e);
				} else if (e.tagName().equals(HTMLTags.IMG)) {
					// 이미지 태그
					createTextView(plainText);
					String imgUrl = e.attr(HTMLAttrs.SRC);
					createImageView(domainUrl+imgUrl);
				} else if (e.tagName().equals(HTMLTags.P)) {
					// recursive
					parseContentElement(e);
				} else if (e.tagName().equals(HTMLTags.FONT)) {
					if (e.select("a").size() > 0) {
						createTextView(plainText);
						createHyperLinkView(e.select("a").get(0));
					} else {
						plainText.append(lines[i]);
					}
				} else if (e.tagName().equals(HTMLTags.A)) {
					createTextView(plainText);
					createHyperLinkView(e);
				} else {
					plainText.append(lines[i]);
				}
				
			} else {
				parseContentElement(body);
			}
		} // end for
		createTextView(plainText);
	}
	
	private void createTextView(StringBuffer plainText) {
		if (plainText.length() <= 0) {
			return;
		}
		PostItem item = new PostItem();
		item.type = PostItem.TYPE_TEXT_VIEW;
		item.contText = plainText.toString().trim();
		items.add(item);
		plainText.delete(0, plainText.length());
	}
	
	private void createImageView(String imageUrl) {
		Util.addUniqueItem(imageUrls, imageUrl);
		PostItem item = new PostItem();
		item.type = PostItem.TYPE_IMAGE_NO_LINK;
		item.imageUrl = imageUrl;
		items.add(item);
	}
	
	private void createHyperLinkView(Element e) {
		PostItem item = new PostItem();
		String link = e.attr(HTMLAttrs.HREF);
		item.type = PostItem.TYPE_HREF_LINK;
		item.hyperLink = link;
		item.contText = e.toString();
		items.add(item);
	}
	
	/**
	 * 댓글 파싱
	 * @param json
	 */
	private void parseCommentElements(Elements comments) {
		
		// 댓글수 추가
		String countText = mContext.getString(R.string.comment_count);
		PostItem info = new PostItem();
		info.type = PostItem.TYPE_COMMENT_INFO;
		info.cmtCntText = String.format(countText, comments.size());
		if (comments.size() == 0) {
			info.cmtCntText = mContext.getString(R.string.message_no_comment);
		}
		items.add(info);
		
		// 댓글 추가
		for (Element comment : comments) {
			CommentData cmtData = new CommentData();
			
			String imgUrl = comment.select(".meta img").attr("src");
			String[] dates = comment.select(".meta").text().split(" ");
			
			cmtData.imgUrl = domainUrl + imgUrl;
			cmtData.memo = comment.select(".text").html();
			cmtData.cmtDate = dates[1]+" "+dates[2];
			cmtData.author = comment.select(".name").text();
			cmtData.fullWidth = comment.select(".item").attr("style").equals("width:100%");
			
			PostItem item = new PostItem();
			item.type = PostItem.TYPE_COMMENT_VIEW;
			item.cmtData = cmtData;
			item.authorNm = headerItem.authorNm;
			items.add(item);
		}
	}

	@Override
	public void onClick(View v) {
		if (v == btnSendComment) {
			// 글쓰기 Timer reset 여부검사
			boolean isReset = TimerManager.getInstance().isTimerReset();
			if (!isReset) {
				Toast.makeText(mContext,
						mContext.getString(R.string.message_timer_not_reset), Toast.LENGTH_SHORT).show();
				return;
			}
			
			// 글자수 체크
			String comment = etComment.getText().toString().trim();
			if (comment.length() <= 10) {
				Toast.makeText(mContext,
						mContext.getString(R.string.message_too_short_comment), Toast.LENGTH_SHORT).show();
			} else {
				sendComment(comment);
			}
		}
	}
	
	/**
	 * 댓글 전송 서비스
	 * @param comment
	 */
	private void sendComment(String comment) {
		((PagerActivity)mContext).showLoadingIndicator();
		Session session = SessionManager.getInstance().getSession();
		
//		게시글  링크 (board.php?act=view&id=159561&bo_table=10_21&)
		String boardId = Util.getValueFromUrl(link, "id");
		String tableId = Util.getValueFromUrl(link, "bo_table");
		List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair("bo_table", tableId));                         
        pairs.add(new BasicNameValuePair("board_id", boardId));    
        pairs.add(new BasicNameValuePair("sunse", ""));    
        pairs.add(new BasicNameValuePair("be_id", ""));    
        pairs.add(new BasicNameValuePair("id", ""));    
        pairs.add(new BasicNameValuePair("content", comment));    
        HttpEntity entity = null;
		try {
			entity = new UrlEncodedFormEntity(pairs, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Content-Type", "application/x-www-form-urlencoded");
		headers.put("Referer", domainUrl+link);
		
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(AQuery.POST_ENTITY, entity);
        
        AQuery aq = new AQuery(mContext);
        AjaxCallback<String> cb = new AjaxCallback<String>();
        try {
			cb.url(domainUrl+Links.WRITE_COMMENT_URL)
				.params(params).type(String.class).weakHandler(this, "onCommentSent")
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
	
	public void onCommentSent(String url, String result, AjaxStatus status) {
		((PagerActivity)mContext).hideLoadingIndicator();
		if (status.getCode() == 200) {
			if (Config.LogEnable) {
				Log.d(TAG, "comment result: "+result);
			}
			Toast.makeText(mContext,
					mContext.getString(R.string.message_comment_sent),
					Toast.LENGTH_SHORT).show();
			TimerManager.getInstance().setCountDown(30);
			etComment.setText("");
			requestPage(link);
			
		} else {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_comment_sent_problem),
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public void hideSoftKeyboard(Context context) {
		if (context != null) {
			imm = (InputMethodManager)context.getSystemService(
					Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
		}
	}
	
	public void refreshWriteCommentView() {
		// 글쓰기 버튼 refresh
		boolean hasSession = SessionManager.getInstance().hasSession();
		int visibility = hasSession ? View.VISIBLE : View.GONE;
		layoutWriteCmt.setVisibility(visibility);
	}
	
	private void showAdView(boolean isShow) {
		if (isShow) {
			((PagerActivity)getActivity()).showAdView();
		} else {
			((PagerActivity)getActivity()).hideAdView();
		}
	}

	@Override
	public void onScaleImageView(String imageUrl) {
		Intent intent = new Intent(mContext, ScaleImageActivity.class);
		Bundle bundle = new Bundle();
		bundle.putStringArrayList(IMAGE_URL_KEY, imageUrls);
		bundle.putInt("position", imageUrls.indexOf(imageUrl));
		intent.putExtras(bundle);

		startActivity(intent);
	}
}
