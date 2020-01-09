package com.anlytics.plug.demo;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import com.anlytics.plug.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    //没有混淆的
    public static final String DEX_NAME = "plugin_dex.jar";
    public static final String DEX_GUARD_NAME = "plugin_dex_guard.jar";

    private String destFilePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        destFilePath = getCacheDir().getAbsolutePath() + File.separator + DEX_GUARD_NAME;
        boolean isCPSuccess = copyFileFromAssets(DEX_GUARD_NAME, destFilePath);

        if (isCPSuccess) {
            Log.i(TAG, "onCreate: 复制dex文件成功");
            loadDex();
        } else {
            Log.e(TAG, "onCreate: 复制dex文件失败");
        }
    }

    public void loadDex() {
        try {
            for (int i = 0; i < 3; i++) {
                DexClassLoader dexClassLoader = new DexClassLoader(destFilePath,
                        getCacheDir().getAbsolutePath(),
                        null,
                        getClassLoader());

                Class<?> classLoader = dexClassLoader.loadClass("com.anlytics.plug.ParserUtils");
                Method dexInit = classLoader.getMethod("AnalyticsHelper", String.class);
                dexInit.setAccessible(true);
                dexInit.invoke(null, "test");

                if (dexInit != null) {
                    Log.i(TAG, "loadDex: 加载dex成功");
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "loadDex: 加载dex失败", e);
        }
    }

    public boolean copyFileFromAssets(final String assetsFilePath, final String destFilePath) {
        boolean res = true;
        try {
            InputStream is = getAssets().open(assetsFilePath);
            File newFile = new File(destFilePath);
            FileOutputStream fos = new FileOutputStream(newFile);

            int len = -1;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
            fos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            res = false;
        }
        return res;
    }
}
