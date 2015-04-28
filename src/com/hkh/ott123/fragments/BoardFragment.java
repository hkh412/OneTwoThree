package com.hkh.ott123.fragments;

import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnLastItemVisibleListener;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.hkh.ott123.MainActivity;
import com.hkh.ott123.PagerActivity;
import com.hkh.ott123.R;
import com.hkh.ott123.adapter.BoardAdapter;
import com.hkh.ott123.config.Config;
import com.hkh.ott123.config.Links;
import com.hkh.ott123.data.CityData;
import com.hkh.ott123.data.DataHashMap;
import com.hkh.ott123.data.UrlData;
import com.hkh.ott123.manager.AQueryManager;
import com.hkh.ott123.manager.AppDataManager;
import com.hkh.ott123.manager.PostStateManager;
import com.hkh.ott123.service.QueryListService;
import com.hkh.ott123.util.DebugUtil;
import com.hkh.ott123.util.ParseUtil;
import com.hkh.ott123.util.Util;

public class BoardFragment extends SearchableFragment implements OnItemClickListener {
	
	private static String TAG = BoardFragment.class.getSimpleName();
	Context mContext;
	AQuery aq;
	
	PullToRefreshListView boardList;
	ArrayList<DataHashMap> data = null;
	BoardAdapter boardAdapter = null;
	boolean dataLoaded = false;
	int currentPage=1;
	boolean mergeData = false;
	UrlData currentUrlData;
	CityData currentCityData;
	String domainUrl = null;
	String boardUrl = null;
	
	ArrayList<String> visitedList = null;
	
	public BoardFragment() {
		data = new ArrayList<DataHashMap>();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		mContext = getActivity();
		aq = AQueryManager.getInstance(mContext).getAQuery();
		
		if (savedInstanceState != null) {
			CityData city = (CityData)savedInstanceState.get("currentCity");
			UrlData urlData = (UrlData)savedInstanceState.get("currentUrl");
			AppDataManager.getInstance().setCurrentCityData(city);
			AppDataManager.getInstance().setCurrentUrlData(urlData);
		}
		currentCityData = AppDataManager.getInstance().getCurrentCityData();
		currentUrlData = AppDataManager.getInstance().getCurrentUrlData();
		if (currentCityData != null) {
			domainUrl = currentCityData.getUrl();
		} else {
			domainUrl = Links.DEFAULT_CITY_URL;
		}
		if (currentUrlData != null) {
			boardUrl = currentUrlData.getUrl();
		} else {
			boardUrl = Links.DEFAULT_BOARD_URL;
		}
		
		View rootView = inflater.inflate(R.layout.fragment_board, container, false);
		boardList = (PullToRefreshListView) rootView.findViewById(R.id.list_board);
		boardList.setOnRefreshListener(new OnRefreshListener2<ListView>() {
			@Override
			public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
				mergeData = false;
				currentPage = 1;
				if (searchMode) {
					querySearchData();
				} else {
					queryData(boardUrl);
				}
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
				mergeData = true;
				currentPage++;
				if (searchMode) {
					querySearchData(currentPage);
				} else {
					queryData(currentPage);
				}
			}
		});
		boardList.setOnLastItemVisibleListener(new OnLastItemVisibleListener() {
			@Override
			public void onLastItemVisible() {
				boardList.setCurrentMode(Mode.PULL_FROM_END);
				boardList.setRefreshing(true);
			}
		});
		boardList.setOnItemClickListener(this);
		return rootView;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (dataLoaded) {
			boardList.setAdapter(boardAdapter);
			return;
		}
		PostStateManager psmgr = PostStateManager.getInstance(mContext);
		visitedList = psmgr.getVisitedList(boardUrl);
		boardAdapter = new BoardAdapter(mContext,
				R.layout.list_item_board, data, visitedList, currentUrlData.isAuthor());
		boardList.setAdapter(boardAdapter);
		
		if (searchMode) {
			querySearchData();
		} else {
			queryData(boardUrl);
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("currentCity", currentCityData);
        outState.putSerializable("currentUrl", currentUrlData);
    }
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// go to detail with url link;
		DataHashMap map = data.get(position-1);
		
		String postId = map.get("postId");
		String link = map.get("link");
		String title = map.get("title");
		String authorNm = map.get("authorNm");
		String lvImg = map.get("lvImg");
		String viewCnt = map.get("viewCnt");
		String viewCntText = mContext.getString(R.string.title_view_count);
		viewCntText = String.format(viewCntText, viewCnt);
		
		BoardDetailFragment detailFragment = new BoardDetailFragment();
		Bundle bundle = new Bundle();
		bundle.putString("link", domainUrl+link);
		bundle.putString("title", title);
		bundle.putString("viewCnt", viewCntText);
		if (currentUrlData.isAuthor()) {
			bundle.putString("authorNm", authorNm);
			bundle.putString("lvImg", lvImg);
		}
		
		// 방문한 post id 저장
		Util.addUniqueItem2LimitedSize(visitedList, postId,
				Config.LIST_LIMIT);
		
		detailFragment.setArguments(bundle);
		((PagerActivity)mContext).setDetailFragment(detailFragment);
	}
	
	@Override
	public void queryData(int page) {
		String url = boardUrl+"&page="+String.valueOf(page);
		queryData(url);
    }
	
	@Override
	public void queryData(String url) {
		((PagerActivity)mContext).showLoadingIndicator();
        url = domainUrl+url;
		QueryListService service = new QueryListService(mContext);
		service.setService(url, this, "onQueryResult");
		service.request();
	}
	
	public void onQueryResult(String url, String html, AjaxStatus ajaxStatus) {
		((PagerActivity)mContext).hideLoadingIndicator();
		boardList.onRefreshComplete();
		boardList.setCurrentMode(Mode.PULL_FROM_START);
		
		if (Config.DebugEnable) {
			DebugUtil.writeHtml2File(mContext, url, html);
		}
		
		if (html == null) {
			Toast.makeText(mContext,
					mContext.getString(R.string.message_network_unavailable), Toast.LENGTH_SHORT).show();
			return;
		}
		
		dataLoaded = true;
		Element element = Jsoup.parse(html).body();
        Elements elements = element.select("div.txtlist dl").not(".hd");
        ArrayList<DataHashMap> newList = ParseUtil.parseBoardElements(elements);	
        if (mergeData) {
            ParseUtil.mergeList(data, newList);
        } else {
            data.clear();
            data.addAll(newList);
        }
        boardAdapter.notifyDataSetChanged();
        
        // 좌측메뉴 최초 한번 로딩
        if (getActivity() instanceof MainActivity) {
        	((MainActivity) getActivity()).openDrawerList();
        }
	}

	@Override
	public void querySearchData() {
		mergeData = false;
		String url = boardUrl+"&sf=0&stx="+searchQuery;
		querySearchData(url);
	}

	@Override
	public void querySearchData(int page) {
		String url = boardUrl+"&sf=0&stx="+searchQuery+"&page="+page;
		querySearchData(url);
	}

	@Override
	public void querySearchData(String url) {
		((PagerActivity)mContext).showLoadingIndicator();
		url = domainUrl+url;
		QueryListService service = new QueryListService(mContext);
		service.setService(url, this, "onQueryResult");
		service.request();
	}
}
