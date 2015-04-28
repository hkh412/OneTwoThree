package com.hkh.ott123.data;

import org.jsoup.nodes.Element;

/**
 * 게시글 화면 구성 데이터
 * @author hkh
 *
 */
public class PostItem {

	public static final int TYPE_HEADER_VIEW = 1;
	public static final int TYPE_IMAGE_NO_LINK = 2;
	public static final int TYPE_IMAGE_WITH_LINK = 3;
	public static final int TYPE_HREF_LINK = 4;
	public static final int TYPE_VIDEO_VIEW = 5;
	public static final int TYPE_TEXT_VIEW = 6;
	public static final int TYPE_WEB_VIEW = 7;
	
	/**
	 * 댓글수 표시 LAYOUT
	 */
	public static final int TYPE_COMMENT_INFO = 8;
	
	/**
	 * 실제 댓글 목록 ITEM LAYOUT
	 */
	public static final int TYPE_COMMENT_VIEW = 9;
	
	/**
	 * 댓글 쓰기 LAYOUT
	 */
	public static final int TYPE_COMMENT_WRITE = 10;
	
	public CommentData cmtData;
	
	public String imageUrl;
	public String cmtCntText;
	public String contText;
	public String hyperLink;
	public boolean isAuthor;

	public String authorNm;
	public String lvImg;
	public String date;
	public String title;
	public String viewCnt;
	public Element element;
	
	public int type = -1;
}
