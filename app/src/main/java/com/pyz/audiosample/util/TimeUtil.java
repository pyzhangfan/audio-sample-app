package com.pyz.audiosample.util;

import java.text.SimpleDateFormat;

public class TimeUtil {
	private TimeUtil(){
	}

	public static String getDuration(long ms){
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("mm:ss");
		return simpleDateFormat.format(ms);
	}
}
