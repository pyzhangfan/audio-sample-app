/*
 * Copyright 2018 Dmitriy Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.dimowner.audiorecorder.util;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.view.Display;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.pyz.audiosample.R;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;


/**
 * Android related utilities methods.
 */
public class AndroidUtils {

	//Prevent object instantiation
	private AndroidUtils() {}

	/**
	 * Convert density independent pixels value (dip) into pixels value (px).
	 * @param dp Value needed to convert
	 * @return Converted value in pixels.
	 */
	public static float dpToPx(int dp) {
		return dpToPx((float) dp);
	}

	/**
	 * Convert density independent pixels value (dip) into pixels value (px).
	 * @param dp Value needed to convert
	 * @return Converted value in pixels.
	 */
	public static float dpToPx(float dp) {
		return (dp * Resources.getSystem().getDisplayMetrics().density);
	}

	/**
	 * Returns display pixel density.
	 * @return display density value in pixels (pixel count per one dip).
	 */
	public static float getDisplayDensity() {
		return Resources.getSystem().getDisplayMetrics().density;
	}

	/**
	 * Convert pixels value (px) into density independent pixels (dip).
	 * @param px Value needed to convert
	 * @return Converted value in pixels.
	 */
	public static float pxToDp(int px) {
		return pxToDp((float) px);
	}

	/**
	 * Convert pixels value (px) into density independent pixels (dip).
	 * @param px Value needed to convert
	 * @return Converted value in pixels.
	 */
	public static float pxToDp(float px) {
		return (px / Resources.getSystem().getDisplayMetrics().density);
	}

	public static int screenWidth() {
		return Resources.getSystem().getDisplayMetrics().widthPixels;
	}

	public static int screenHeight() {
		return Resources.getSystem().getDisplayMetrics().heightPixels;
	}

	public static int convertMillsToPx(long mills, float pxPerSec) {
		// 1000 is 1 second evaluated in milliseconds
		return (int) (mills * pxPerSec / 1000);
	}

	public static int convertPxToMills(long px, float pxPerSecond) {
		return (int) (1000 * px / pxPerSecond);
	}

	// A method to find height of the status bar
	public static int getStatusBarHeight(Context context) {
		int result = 0;
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = context.getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	// A method to find height of the navigation bar
	public static int getNavigationBarHeight(Context context) {
		int result = 0;
		try {
			if (hasNavBar(context)) {
				int resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
				if (resourceId > 0) {
					result = context.getResources().getDimensionPixelSize(resourceId);
				}
			}
		} catch (Resources.NotFoundException e) {
			e.printStackTrace();
			return 0;
		}
		return result;
	}

	//This method works not correctly
	public static boolean hasNavBar (Context context) {
//		int id = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
//		return id > 0 && context.getResources().getBoolean(id);
//		boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
//		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
//		return !hasMenuKey && !hasBackKey;

		boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		boolean hasHomeKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_HOME);
		return !hasHomeKey && !hasBackKey;
	}

	public static void setTranslucent(Activity activity, boolean translucent) {
		Window w = activity.getWindow();
		if (translucent) {
			w.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		} else {
			w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
		}
	}

	/**
	 * Convert int array to byte array
	 */
	public static byte[] int2byte(int[] src) {
		ByteBuffer byteBuffer = ByteBuffer.allocate(src.length * 4);
		IntBuffer intBuffer = byteBuffer.asIntBuffer();
		intBuffer.put(src);
		return byteBuffer.array();
	}

	/**
	 * Convert byte array to int array
	 */
	public static int[] byte2int(byte[]src) {
		int dstLength = src.length >>> 2;
		int[]dst = new int[dstLength];

		for (int i=0; i<dstLength; i++) {
			int j = i << 2;
			int x = 0;
			x += (src[j++] & 0xff) << 0;
			x += (src[j++] & 0xff) << 8;
			x += (src[j++] & 0xff) << 16;
			x += (src[j++] & 0xff) << 24;
			dst[i] = x;
		}
		return dst;
	}

	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.x;
	}

	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		return size.y;
	}

	/**
	 * Read sound file duration.
	 * @param file Sound file
	 * @return Duration in microseconds.
	 */
	public static long readRecordDuration(File file) {
		try {
			MediaExtractor extractor = new MediaExtractor();
			MediaFormat format = null;
			int i;

			extractor.setDataSource(file.getPath());
			int numTracks = extractor.getTrackCount();
			// find and select the first audio track present in the file.
			for (i = 0; i < numTracks; i++) {
				format = extractor.getTrackFormat(i);
				if (format.getString(MediaFormat.KEY_MIME).startsWith("audio/")) {
					extractor.selectTrack(i);
					break;
				}
			}

			if (i == numTracks) {
				throw new IOException("No audio track found in " + file.toString());
			}
			if (format != null) {
				try {
					return format.getLong(MediaFormat.KEY_DURATION);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return -1;
	}

	/**
	 * @return true if the menu has at least one MenuItem with an icon.
	 */
	private static boolean hasIcon(Menu menu) {
		for (int i = 0; i < menu.size(); i++) {
			if (menu.getItem(i).getIcon() != null) return true;
		}
		return false;
	}

	public static void showDialog(Activity activity, int positiveBtnTextRes, int negativeBtnTextRes, int resTitle, int resContent, boolean cancelable,
								  final View.OnClickListener positiveBtnListener, final View.OnClickListener negativeBtnListener){
		final Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setCancelable(cancelable);
		View view = activity.getLayoutInflater().inflate(R.layout.dialog_layout, null, false);
		((TextView)view.findViewById(R.id.dialog_title)).setText(resTitle);
		((TextView)view.findViewById(R.id.dialog_content)).setText(resContent);
		if (negativeBtnListener != null) {
			Button negativeBtn = view.findViewById(R.id.dialog_negative_btn);
			if (negativeBtnTextRes >=0) {
				negativeBtn.setText(negativeBtnTextRes);
			}
			negativeBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					negativeBtnListener.onClick(v);
					dialog.dismiss();
				}
			});
		} else {
			view.findViewById(R.id.dialog_negative_btn).setVisibility(View.GONE);
		}
		if (positiveBtnListener != null) {
			Button positiveBtn = view.findViewById(R.id.dialog_positive_btn);
			if (positiveBtnTextRes >=0) {
				positiveBtn.setText(positiveBtnTextRes);
			}
			positiveBtn.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					positiveBtnListener.onClick(v);
					dialog.dismiss();
				}
			});
		} else {
			view.findViewById(R.id.dialog_positive_btn).setVisibility(View.GONE);
		}
		dialog.setContentView(view);
		dialog.show();
	}

	public static void showInfoDialog(Activity activity, int resContent){
		showDialog(activity, -1, -1, R.string.info, resContent, true,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {}
				}, null);
	}

	public static String getAppVersion(Context context) {
		String versionName;
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			versionName = info.versionName;
		} catch (PackageManager.NameNotFoundException e) {
			versionName = "N/A";
		}
		return versionName;
	}
}
