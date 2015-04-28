package com.hkh.ott123.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.hkh.ott123.R;
import com.hkh.ott123.data.CityData;
import com.hkh.ott123.data.UrlData;

public class Util {

	public static int convertDipToPixel(Context context, int dip) {
		Resources r = context.getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, r.getDisplayMetrics());
		return (int)px;
	}
	
	public static int findFirstMatch(ArrayList<CityData> cityList, String value) {
		if (cityList == null) {
			return -1;
		}
		for (int i=0; i<cityList.size(); i++) {
			if (cityList.get(i).getCity().equals(value)) {
				return i;
			}
		}
		return -1;
	}
	
	public static UrlData getMatchedUrlDataByUid(ArrayList<UrlData> urlList, int uid) {
		UrlData matched = null;
		for (UrlData urlData : urlList) {
			if (urlData.getUid() == uid) {
				matched = urlData;
				break;
			}
		}
		return matched;
	}
	
	public static String getValueFromUrl(String url, String key) {
		String value = null;
		if (url != null) {
			String[] keyValues = url.split("&");
			for (int i=0; i<keyValues.length; i++) {
				String keyValue = keyValues[i];
				if (keyValue.indexOf(key) >= 0) {
					value = keyValue.split("=")[1];
					break;
				}
			}
		}
		return value;
	}
	
	public static void addUniqueItem(ArrayList<String> list, String str) {
		if (list == null || str == null) {
			return;
		}
		if (str.isEmpty() || list.contains(str)) {
			return;
		}
		list.add(str);
	}
	
	/**
	 * 저장할 수 있는 최대한도 설정
	 * limit을 초과할 경우 FIFO방식으로 첫번째 item을 삭제후 뒤에 추가한다.
	 * @param list
	 * @param str
	 * @param limit
	 */
	public static void addUniqueItem2LimitedSize(ArrayList<String> list, String str, int limit) {
		if (list == null || str == null) {
			return;
		}
		if (str.isEmpty() || list.contains(str)) {
			return;
		}
		if (list.size() >= limit) {
			list.remove(0);
		}
		list.add(str);
//		Collections.sort(list);
	}
	
//	public static void writeToSDFile(String dump){
//	    File root = android.os.Environment.getExternalStorageDirectory(); 
//	    File dir = new File (root.getAbsolutePath() + "/download/OneTwoThreeDump/");
//	    dir.mkdirs();
//	    File file = new File(dir, "dump"+new Date().toString()+".txt");
//
//	    try {
//	        FileOutputStream f = new FileOutputStream(file);
//	        PrintWriter pw = new PrintWriter(f);
//	        pw.print(dump);
//	        pw.flush();
//	        pw.close();
//	        f.close();
//	    } catch (FileNotFoundException e) {
//	        e.printStackTrace();
//	    } catch (IOException e) {
//	        e.printStackTrace();
//	    }   
//	}
	
	/**
	 * Bitmap 이미지를 외장 스토리지에 파일로 저장
	 * @param bitmap
	 */
	public static boolean saveImageToExternalStorage(Context context, Bitmap bitmap, String fname) {
		String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
	    File myDir = new File(root+"/"+context.getString(R.string.image_folder_name));
	    myDir.mkdirs();
	    File file = new File(myDir, fname+".jpg");
	    if (file.exists())
	        file.delete();
	    try {
	        FileOutputStream out = new FileOutputStream(file);
	        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
	        out.flush();
	        out.close();
	    }
	    catch (Exception e) {
	        e.printStackTrace();
	        return false;
	    }
	    // 갤러리에서 보이도록 새로고침 한다.
	    MediaScannerConnection.scanFile(context, new String[] { file.toString() }, null,
	            new MediaScannerConnection.OnScanCompletedListener() {
	                public void onScanCompleted(String path, Uri uri) {
	                }
	    });
	    
	    return true;
	}
	
	/**
	 * 게시판 Menu 의 목록을 Model (UrlData) 에 담아서 리턴한다.
	 * @return List<UrlData>
	 */
	public static ArrayList<UrlData> loadUrlMapData(Context context,
			List<UrlData> parentNodes, List<ArrayList<UrlData>> childNodes) {
		AssetManager assetManager = context.getAssets();
		ArrayList<UrlData> urlList = new ArrayList<UrlData>();
		// parentNode, childNode 구성 favorite 추가
		parentNodes.clear();
		childNodes.clear();
		
		Gson gson = new GsonBuilder().create();
		try {
			InputStream in = assetManager.open("sitemap.json");
			JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
			reader.beginArray();
			ArrayList<UrlData> children = new ArrayList<UrlData>();
			while (reader.hasNext()) {
				UrlData urlMap = gson.fromJson(reader, UrlData.class);
				if (urlMap.getDepth()==0) {
					// parent node
					parentNodes.add(urlMap);
					children = new ArrayList<UrlData>();
					childNodes.add(children);
				} else {
					// child node
					children.add(urlMap);
				}
				urlList.add(urlMap);
			}
			reader.endArray();
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("loadUrlMapData: ", e.getMessage());
		}
		
		return urlList;
	}
}
