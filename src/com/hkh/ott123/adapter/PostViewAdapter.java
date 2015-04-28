package com.hkh.ott123.adapter;

import java.util.List;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.hkh.ott123.R;
import com.hkh.ott123.data.CommentData;
import com.hkh.ott123.data.PostItem;
import com.hkh.ott123.events.OnScaleImageViewListener;
import com.hkh.ott123.views.CommentRow;
import com.hkh.ott123.views.CustomImageView;

public class PostViewAdapter extends ArrayAdapter<PostItem> {

	private static String TAG = PostViewAdapter.class.getSimpleName();
	Context mContext;
	int resourceId;
	AQuery aq;
	OnScaleImageViewListener mScaleImageListener;
	
	public PostViewAdapter(Context context, int resource, List<PostItem> objects) {
		super(context, resource, objects);
		this.resourceId = resource;
		this.mContext = context;
		aq = new AQuery(mContext);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		View view = convertView;
		
		if (view == null) {
			view = ((Activity) mContext).getLayoutInflater().inflate(resourceId, parent, false);
			holder = new ViewHolder();
			holder.textView = (TextView) view.findViewById(R.id.textview);
			holder.layoutCmtCnt = (LinearLayout) view.findViewById(R.id.layout_cmtcnt);
			holder.commentRow = (CommentRow) view.findViewById(R.id.comment_row);
			holder.headerView = (LinearLayout) view.findViewById(R.id.layout_header);
			holder.imageView = (CustomImageView) view.findViewById(R.id.imageview);
			holder.imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (v instanceof CustomImageView && mScaleImageListener != null) {
						String imageUrl = ((CustomImageView)v).getImageUrl();
						mScaleImageListener.onScaleImageView(imageUrl);
					}
				}
			});			
			view.setTag(holder);
		} else {
			holder = (ViewHolder) view.getTag();
		}
		
		holder.imageView.setVisibility(View.GONE);
		holder.textView.setVisibility(View.GONE);
		holder.layoutCmtCnt.setVisibility(View.GONE);
		holder.commentRow.setVisibility(View.GONE);
		holder.headerView.setVisibility(View.GONE);
		
		PostItem item = this.getItem(position);
		
		switch (item.type) {
		case PostItem.TYPE_HEADER_VIEW:
			holder.headerView.setVisibility(View.VISIBLE);
			LinearLayout layoutAuthor = (LinearLayout) holder.headerView.findViewById(R.id.layout_detail_author);
			ImageView imgLevel = (ImageView) holder.headerView.findViewById(R.id.img_detail_level);
			TextView tvContAuthor = (TextView) holder.headerView.findViewById(R.id.tv_board_cont_author);
			TextView tvContTitle = (TextView) holder.headerView.findViewById(R.id.tv_board_cont_title);
			TextView tvContDate = (TextView) holder.headerView.findViewById(R.id.tv_board_cont_date);
			TextView tvViewCnt = (TextView) holder.headerView.findViewById(R.id.tv_head_viewcnt);
			
			if (item.isAuthor) {
				layoutAuthor.setVisibility(View.VISIBLE);
				tvContAuthor.setText(item.authorNm);
				aq.id(imgLevel).image(item.lvImg);
			} else {
				layoutAuthor.setVisibility(View.GONE);
			}
			tvContTitle.setText(item.title);
			tvContDate.setText(item.date);
			tvViewCnt.setText(item.viewCnt);
			break;
			
		case PostItem.TYPE_TEXT_VIEW:
			holder.textView.setVisibility(View.VISIBLE);	
			holder.textView.setText(Html.fromHtml(item.contText));
			holder.textView.setOnClickListener(null);
			break;
			
		case PostItem.TYPE_IMAGE_NO_LINK:
			holder.imageView.setVisibility(View.VISIBLE);
			holder.imageView.loadImage(item.imageUrl);
			break;
			
		case PostItem.TYPE_HREF_LINK:
			holder.textView.setVisibility(View.VISIBLE);
			holder.textView.setText(Html.fromHtml(item.contText));
			final String link = item.hyperLink;
			holder.textView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
						mContext.startActivity(intent);
					} catch (ActivityNotFoundException e) {
						Log.e(TAG, e.getMessage());
					}
				}
			});
			break;
			
		case PostItem.TYPE_COMMENT_INFO:
			holder.layoutCmtCnt.setVisibility(View.VISIBLE);
			// 총 댓글 수량 문구
			TextView tvCmtCnt = (TextView) holder.layoutCmtCnt.findViewById(R.id.tv_detail_cmtcnt);
			tvCmtCnt.setText(Html.fromHtml(item.cmtCntText));
			break;
			
		case PostItem.TYPE_COMMENT_VIEW:
			holder.commentRow.setVisibility(View.VISIBLE);
			CommentData cmtData = item.cmtData;
			holder.commentRow.setCommentImg(cmtData.imgUrl);
			holder.commentRow.setCommentMemo(cmtData.memo);
			holder.commentRow.setCommentAuthor(cmtData.author);
			holder.commentRow.setCommentDate(cmtData.cmtDate);
			holder.commentRow.setReplyIntent(cmtData.fullWidth);
			
			// 글 작성자 코멘트 여부
			boolean isAuthorCmt = item.cmtData.author.equals(item.authorNm);
			int cmtBgColor = isAuthorCmt ? 
					R.color.comment_bg_highlight_wh : R.color.comment_bg_normal_wh;
			holder.commentRow.setBackgroundResource(cmtBgColor);
			break;
			
		default:
			break;
		}
		return view;
	}
	
	static class ViewHolder {
		LinearLayout headerView;
		CustomImageView imageView;
		TextView textView;
		LinearLayout layoutCmtCnt;
		CommentRow commentRow;
	}
	
	public void setOnScaleImageViewListener(OnScaleImageViewListener listener) {
		mScaleImageListener = listener;
	}
}
