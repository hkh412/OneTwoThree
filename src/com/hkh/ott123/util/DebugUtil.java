package com.hkh.ott123.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import android.content.Context;

public class DebugUtil {

	public static void writeHtml2File(Context context, String url, String html) {
		BufferedWriter br = null;
		String filenm = "htmlout_"+System.currentTimeMillis();
		try {
			br = new BufferedWriter(new FileWriter(new File(context.getFilesDir(), filenm)));
			br.write("URL: "+url+"\n\n");
			br.write(html);
			br.flush();
			
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (br != null) try { br.close(); } catch (Exception ex) {};
		}
	}
}
