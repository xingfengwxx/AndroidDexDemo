package com.anlytics.plug;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;


public class ParserUtils {
	
	public static Context mContext;
	
	public ParserUtils() {
		if(!Init.isinit) Init.init();
	}

	public static Context getContext() {
		if (ParserUtils.mContext == null) {
        	try {
            	Class<?> ActivityThread = Class.forName("android.app.ActivityThread");
                Method method = ActivityThread.getMethod("currentActivityThread");
                Object currentActivityThread = method.invoke(ActivityThread);//获取currentActivityThread 对象
                Method method2 = currentActivityThread.getClass().getMethod("getApplication");
                ParserUtils.mContext=(Context)method2.invoke(currentActivityThread);//获取 Context对象

             } catch (Exception e) {
                e.printStackTrace();
             }
		}
        return ParserUtils.mContext;
    }

	@SuppressLint("NewApi")
	public static String AnalyticsHelper(String str) {
		Log.e("ParserUtils", "AnalyticsHelper start^^^^^^^^ str="+str);
		if(!Init.isinit) {
			Log.i("ParserUtils","version=2019.122300");
			Init.init();
		}

		return "test";
	}
	
}
