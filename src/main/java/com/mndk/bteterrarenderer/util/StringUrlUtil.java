package com.mndk.bteterrarenderer.util;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUrlUtil {

	public static String replaceRandoms(String url) {
		Matcher m = Pattern.compile("\\{random:([^{}]+)\\}").matcher(url);
		StringBuffer buffer = new StringBuffer();
		Random r = new Random();
		while(m.find()) {
			String[] randoms = m.group(1).split(",");
			m.appendReplacement(buffer, randoms[r.nextInt(randoms.length)]);
		}
		m.appendTail(buffer);
		return buffer.toString();
	}

}
