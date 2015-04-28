// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: braces fieldsfirst space lnc 

package com.hkh.ott123.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.hkh.ott123.data.DataHashMap;

public class ParseUtil {

	String TAG = ParseUtil.class.getSimpleName();

	/**
	 * 일반 게시판 파싱
	 * @param rows
	 * @return
	 */
	public static ArrayList<DataHashMap> parseBoardElements(List<Element> rows) {
		ArrayList<DataHashMap> list = new ArrayList<DataHashMap>();
		DataHashMap map;
		for (Iterator<Element> iterator=rows.iterator(); iterator.hasNext();) {
			Element row = iterator.next();
			map = new DataHashMap();
			
			String postId = getBoardPostId(row);
			String cmtCnt = null;
			Elements span = row.select("dd.title span");
			if (span.size()>0) {
				cmtCnt = span.get(0).text().replace("(", "").replace(")", "");
			}
			span.remove();
			
			String title = row.select("dd.title").text();
			String imgSrc = row.select("dd.title img").attr("src");
			boolean isFile = imgSrc.length()>0 && imgSrc.indexOf("ico_file.gif")>=0;
			String authorNm = row.select("dd.writer").text();
			String lvImg = row.select("dd.writer img").attr("src");
			String date = row.select("dd.time").text();
			String link = row.select("dd.title a").attr("href");
			String viewCnt = row.select("dd.clicks").text();
			
			// 이얼싼 공지사항 숨김
//			if (authorNm.equals("이얼싼")) {
//				continue;
//			}
			
			map.put("postId", postId);
			map.put("title", title);
			map.put("authorNm", authorNm);
			map.put("lvImg", lvImg);
			map.put("isFile", String.valueOf(isFile));
			map.put("date", date);
			map.put("link", link);
			map.put("cmtCnt", cmtCnt);
			map.put("viewCnt", viewCnt);
			list.add(map);
		}
		return list;
	}
	/**
	 * 일반 게시판 postId 파싱
	 * @param row
	 * @return
	 */
	private static String getBoardPostId(Element row) {
		Elements matched = row.select("dt");
		if (matched.size()>0) {
			return matched.get(0).text();
		}
		return "";
	}
	
	public static int mergeList(ArrayList<DataHashMap> data, ArrayList<DataHashMap> newList) {
		int duplicateCnt = 0;
		for (Iterator<DataHashMap> iterator = newList.iterator();iterator.hasNext();) {
			DataHashMap map = iterator.next();
			String postId = map.get("postId");
			boolean duplicate = checkContain(data, postId);
			if (duplicate) {
				duplicateCnt++;
			} else {
				data.add(map);
			}
		}
		return duplicateCnt;
	}
	
	public static boolean checkContain(ArrayList<DataHashMap> data, String postId) {
		boolean contain = false;
		for (DataHashMap map : data) {
			if (map.get("postId")!=null && map.get("postId").equals(postId)) {
				contain = true;
				break;
			}
		}
		return contain;
	}
}
