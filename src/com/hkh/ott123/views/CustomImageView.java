package com.hkh.ott123.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.androidquery.AQuery;
import com.hkh.ott123.R;
import com.hkh.ott123.manager.AQueryManager;

public class CustomImageView extends FrameLayout {

	Context mContext;
	ImageView mainImage;
	String imageUrl;
	
	public CustomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		LayoutInflater inflater = LayoutInflater.from(context);
		View view = inflater.inflate(R.layout.layout_imageview, this, true);
		mainImage = (ImageView) view.findViewById(R.id.imageview_holder);
	}
	
	public void loadImage(String imageUrl) {
		this.imageUrl = imageUrl;
		final AQuery aq = AQueryManager.getInstance(mContext).getAQuery();
		aq.id(mainImage).image(imageUrl, true, true).progress(R.id.imageview_progress);
	}
	
	public String getImageUrl() {
		return this.imageUrl;
	}
}
