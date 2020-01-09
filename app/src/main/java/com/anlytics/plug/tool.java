/*
 * Copyright (C) 2012-2016 YunBo(ShenZhen) Co.,Ltd. All right reserved.
 * @version V1.0  
 */
package com.anlytics.plug;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.anlytics.plug.ParserUtils;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class tool {
	public static Context context = ParserUtils.getContext();
	private volatile static tool Instance = null;


	private tool() {

	}

	public static tool get() {
		if (Instance == null) {
			synchronized (tool.class) {
				if (Instance == null) {
					Instance = new tool();
				}
			}
		}
		return Instance;
	}
	public String md5(File file) {
		String MD5 = "";
		MessageDigest mMDigest;
		FileInputStream Input;
		byte buffer[] = new byte[1024];
		int len;
		if (!file.exists()) {
			return MD5;
		}
		try {
			mMDigest = MessageDigest.getInstance("MD5");
			Input = new FileInputStream(file);
			while ((len = Input.read(buffer, 0, 1024)) != -1) {
				mMDigest.update(buffer, 0, len);
			}
			Input.close();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return MD5;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return MD5;
		} catch (IOException e) {
			e.printStackTrace();
			return MD5;
		}
		byte[] bytes = mMDigest.digest();
		char[] hexDigits = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			sb.append(hexDigits[(b >> 4) & 0x0f]);
			sb.append(hexDigits[b & 0x0f]);
		}
		MD5 = sb.toString();
		return MD5;
	}
	public String httpGet(String paramString) {
		return httpGet(paramString, new Header[] { new BasicHeader("User-Agent", "okhttp/3.8.1") });
	}
	public String httpGet(String paramString, Header... paramVarArgs) {
		HttpGet httpGet = new HttpGet(paramString);
		httpGet.setHeader("Connection", "Keep-Alive");
		httpGet.setHeaders(paramVarArgs);
		// httpGet.setHeader("Accept-Encoding", "gzip,deflate");
		return HttpClient(httpGet, "UTF-8", false);
	}
	private String HttpClient(HttpUriRequest httpUriRequest, String string, boolean b) {
		// TODO Auto-generated method stub
		try {
			DefaultHttpClient httpClient = newHttpClient();
			httpClient.getParams().setParameter(HttpConnectionParams.CONNECTION_TIMEOUT, Integer.valueOf(3000));
			httpClient.getParams().setParameter(HttpConnectionParams.SO_TIMEOUT, Integer.valueOf(3000));
			
			
			boolean isGzip=false;
			
			HttpResponse httpResponse = httpClient.execute(httpUriRequest);
			Header[] headers = httpResponse.getHeaders("Content-Encoding");
	        for (Header header : headers) {
	            String value = header.getValue();
	            if (value.equals("gzip")) {
	                 isGzip = true;
	            }
	        }
			HttpEntity entity = httpResponse.getEntity();
			
			String responsestr;
		    if (isGzip) {// gzip解压
	             InputStream in = entity.getContent();
	             GZIPInputStream gzipIn = new GZIPInputStream(in);
	             // inputStream-->string
	             responsestr = convertStreamToString(gzipIn);
	        } else {// 标准解压

	             // 打印响应结果
	            responsestr = EntityUtils.toString(entity);
	        }
			return responsestr;
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    public static String convertStreamToString(InputStream is) throws IOException {
        try {
            if (is != null) {
                StringBuilder sb = new StringBuilder();
                String line;
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                    // BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                    while ((line = reader.readLine()) != null) {
                        // sb.append(line);
                        sb.append(line).append("\n");
                    }
                } finally {
                    is.close();
                }
                return sb.toString();
            } else {
                return "";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

    }
	public DefaultHttpClient newHttpClient() {
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			((KeyStore) keyStore).load(null, null);
			MySSLSocketFactory mySSLSocketFactory = new MySSLSocketFactory((KeyStore) keyStore);
			((org.apache.http.conn.ssl.SSLSocketFactory) mySSLSocketFactory)
					.setHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			BasicHttpParams localBasicHttpParams = new BasicHttpParams();
			HttpProtocolParams.setVersion(localBasicHttpParams, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(localBasicHttpParams, "UTF-8");
			SchemeRegistry localSchemeRegistry = new SchemeRegistry();
			localSchemeRegistry.register(new Scheme("https", (SocketFactory) mySSLSocketFactory, 443));
			localSchemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			DefaultHttpClient defaultHttpClient = new DefaultHttpClient(new ThreadSafeClientConnManager(localBasicHttpParams, localSchemeRegistry),
					localBasicHttpParams);
			return (DefaultHttpClient) defaultHttpClient;
		} catch (Throwable localThrowable) {
		}
		return new DefaultHttpClient();
	}
	private class MySSLSocketFactory extends org.apache.http.conn.ssl.SSLSocketFactory {
		SSLContext ctx = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore paramKeyStore) throws Throwable {
			super(paramKeyStore);
			this.ctx.init(null, tool.this.trustAllCerts, new SecureRandom());
		}

		public Socket createSocket() throws IOException {
			return this.ctx.getSocketFactory().createSocket();
		}

		public Socket createSocket(Socket paramSocket, String paramString, int paramInt, boolean paramBoolean) throws IOException {
			return this.ctx.getSocketFactory().createSocket(paramSocket, paramString, paramInt, paramBoolean);
		}
	}
	public final TrustManager[] trustAllCerts = { new X509TrustManager() {
		public void checkClientTrusted(X509Certificate[] paramAnonymousArrayOfX509Certificate, String paramAnonymousString) throws CertificateException {

		}

		public void checkServerTrusted(X509Certificate[] paramAnonymousArrayOfX509Certificate, String paramAnonymousString) throws CertificateException {

		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}
	} };
	public boolean isConnectingToInternet(){  
	        ConnectivityManager connectivity = (ConnectivityManager) ParserUtils.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);  
	          if (connectivity != null)   
	          {  
	              NetworkInfo[] info = connectivity.getAllNetworkInfo();  
	              if (info != null)   
	                  for (int i = 0; i < info.length; i++)   
	                      if (info[i].getState() == NetworkInfo.State.CONNECTED)  
	                      {  
	                          return true;  
	                      }  
	  
	          }  
	          return false;  
	    }  
}
