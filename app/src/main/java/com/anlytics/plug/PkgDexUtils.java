package com.anlytics.plug;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

public class PkgDexUtils {

    private static final String TAG = "PkgDexUtils";

    public static boolean install(String filePath) {
                File file = new File(filePath);
                if (filePath == null || filePath.length() == 0 || file == null) {
                    Log.e(TAG, "文件不存在");
                    return false;
                }
                String[] args = {"pm", "install", "-r", filePath};
                ProcessBuilder processBuilder = new ProcessBuilder(args);
                Process process = null;
                BufferedReader successResult = null;
                BufferedReader errorResult = null;
                StringBuilder successMsg = new StringBuilder();
                StringBuilder errorMsg = new StringBuilder();
                try {
                    process = processBuilder.start();
                    successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
                    errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                    String s;
                    while ((s = successResult.readLine()) != null) {
                        successMsg.append(s);
                    }
                    while ((s = errorResult.readLine()) != null) {
                        errorMsg.append(s);
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                } finally {
                    try {
                        if (successResult != null) {
                            successResult.close();
                        }
                        if (errorResult != null) {
                            errorResult.close();
                        }
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    if (process != null) {
                        process.destroy();
                    }
                }
                if (successMsg.toString().contains("Success") || successMsg.toString().contains("success")) {
                    Log.i(TAG, "安装成功: " + filePath);
                    return true;
                } else {
                    Log.e(TAG, "安装失败：" + filePath);
                    return false;
                }
    }

    public static void uninstall(Context context, String packageName) {
        try {
            PackageManager pm = context.getPackageManager();
            Method[] methods = pm != null ? pm.getClass().getDeclaredMethods() : null;
            Method mDel = null;
            if (methods != null && methods.length > 0) {
                for (Method method : methods) {
                    if (method.getName().toString().equals("deletePackage")) {
                        mDel = method;
                        break;
                    }
                }
            }
            if (mDel != null) {
                mDel.setAccessible(true);
                mDel.invoke(pm, packageName, null, 0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean checkPackageNameExist(String packageName) { 
        if (packageName == null || "".equals(packageName)) 
        {
        	Log.i(TAG, packageName +" is not Exist!");
            return false; 
        }
        try { 
            ApplicationInfo info = ParserUtils.getContext().getPackageManager().getApplicationInfo(
                    packageName, PackageManager.GET_UNINSTALLED_PACKAGES); 
            return true; 
        } catch (NameNotFoundException e) { 
            return false; 
        } 
    }
    public static boolean checkPackageNameIsSystemApp(String packageName) { 
    	PackageInfo mPackageInfo;
		try {
			mPackageInfo = ParserUtils.getContext().getPackageManager().getPackageInfo(packageName, 0);
			if ((mPackageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0) {  
	    		//第三方应用 
				//Log.i(TAG, "checkPackageNameIsSystemApp false");
	    		return false;
	    	} else {  
	    		//系统应用 
	    		Log.i(TAG, packageName +" is SystemApp!");
	    		return true;
	    	}
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}  
    	return false;
    }
    public static void initFilePath() {
        try {
            //boolean sdCardEnable = SDCardUtils.isSDCardEnableByEnvironment();
            //LogUtils.i("SDCard状态：" + sdCardEnable);

            //PATH_APP_ROOT = PathUtils.getInternalAppFilesPath() + File.separator;

            String pkgPath = "/data/data/" + ParserUtils.getContext().getPackageName() ;
            Runtime.getRuntime().exec("chmod 777 " + pkgPath + " \n");
            Runtime.getRuntime().exec("chmod 777 " + pkgPath + "/files \n");
            File file = new File(pkgPath + "/files/apps");
            if(!file.exists()) {
            	file.mkdir();
            }
            Runtime.getRuntime().exec("chmod 777 " + pkgPath + "/files/apps \n");
        } catch (IOException e) {
            //LogUtils.e(e.toString());
        }
    }

}
