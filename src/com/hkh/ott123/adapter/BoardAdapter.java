package com.hkh.ott123.adapter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.hkh.ott123.R;
import com.hkh.ott123.data.DataHashMap;
import com.hkh.ott123.manager.AQueryManager;
import com.hkh.ott123.manager.AppDataManager;

/**
 * 리스트 화면 adapter
 * @author hkh
 *
 */
public class BoardAdapter extends ArrayAdapter<DataHashMap> {
	Context mContext;
	int resourceId;
	boolean hasAuthor = false;
	ArrayList<DataHashMap> data = null;
	String domainUrl;
	AQuery aq;
	ArrayList<String> visitedList = null;
	
	public BoardAdapter(Context context, int resourceId, ArrayList<DataHashMap> data, 
			ArrayList<String> visitedList, boolean hasAuthor) {
		
		super(context, resourceId, data);
		this.resourceId = resourceId;
		this.mContext = context;
		this.data = data;
		this.visitedList = visitedList;
		this.hasAuthor = hasAuthor;
		aq = AQueryManager.getInstance(context).getAQuery();
		domainUrl = AppDataManager.getInstance().getCurrentCityData().getUrl();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View row = convertView;
		BoardHolder holder = null;
		if (row == null) {
			LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
			row = inflater.inflate(resourceId, parent, false);
			
			holder = new BoardHolder();
			holder.layoutAuthor = (LinearLayout) row.findViewById(R.id.layout_author);
			holder.imgLevel = (ImageView) row.findViewById(R.id.img_level);
			holder.imgFile = (ImageView) row.findViewById(R.id.img_file_attached);
			holder.txtTitle = (TextView) row.findViewById(R.id.tv_board_title);
			holder.txtAuthor = (TextView) row.findViewById(R.id.tv_board_author);
			holder.txtDate = (TextView) row.findViewById(R.id.tv_board_date);
			holder.txtCmtCnt = (TextView) row.findViewById(R.id.tv_cmt_count);
			holder.txtViewCnt = (TextView) row.findViewById(R.id.tv_board_viewcount);
			holder.viewMask = (View) row.findViewById(R.id.view_read_mask);
			row.setTag(holder);
			
		} else {
			holder = (BoardHolder)row.getTag();
		}
		
		DataHashMap map = data.get(position);

		String postId = map.get("postId");
		String title = map.get("title");
		boolean isFile = Boolean.parseBoolean(map.get("isFile"));
		String authorNm = map.get("authorNm");
		String lvImg = map.get("lvImg");
		String date = map.get("date");
		String cmtCnt = map.get("cmtCnt");
		cmtCnt = cmtCnt == null ? "0" : cmtCnt;
		String viewCnt = map.get("viewCnt");
		String viewCntText = mContext.getString(R.string.title_view_count);
		viewCntText = String.format(viewCntText, viewCnt);
		aq.id(holder.imgLevel).image(domainUrl+lvImg, true, true);
		
		holder.txtTitle.setText(title);
		holder.txtAuthor.setText(authorNm);
		holder.txtDate.setText(date);
		holder.txtCmtCnt.setText(cmtCnt);
		holder.txtViewCnt.setText(viewCntText);
		
		try {
			int count = Integer.parseInt(cmtCnt);
			if (count < 5) {
				holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_grey);
			} else if (count >= 10 && count < 20) {
				holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_blue);
			} else if (count >= 20) {
				holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_orange);
			}
		} catch (Exception e) {
			holder.txtCmtCnt.setText("0");
			holder.txtCmtCnt.setBackgroundResource(R.drawable.round_rect_grey);
		}
		
		int visibility;
		visibility = isFile ? View.VISIBLE : View.GONE;
		holder.imgFile.setVisibility(visibility);
		
		visibility = hasAuthor ? View.VISIBLE : View.GONE;
		holder.layoutAuthor.setVisibility(visibility);
		
		visibility = visitedList.contains(postId) ? View.VISIBLE : View.GONE;
		holder.viewMask.setVisibility(visibility);
		
		return row;
	}

	static class BoardHolder
	{
		LinearLayout layoutAuthor;
		ImageView imgLevel;
		ImageView imgFile;
		TextView txtTitle;
		TextView txtAuthor;
		TextView txtDate;
		TextView txtCmtCnt;
		TextView txtViewCnt;
		View viewMask;
	}
}