package com.anlytics.plug;

import android.util.Log;

import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class IOUtils {

    public static int TIMEOUT = 7000;
//    private static final int RETRY_TIMES = 3;
    public static final String TAG = "IOUtils";

    public static void cp(String from, String to) throws MalformedURLException, IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = getInputStream(from);
            out = getOutputStream(to);
            byte [] buffer = new byte[4 * 1024];
            for (int len; (len = in.read(buffer)) > 0; )
                out.write(buffer, 0, len);
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

    public static OutputStream getOutputStream(String to) throws FileNotFoundException {
        String path = to;
        if (to.charAt(0) == '~') {
            path = System.getProperty("user.home");
            path = path + to.substring(1);
        }

        return new FileOutputStream(path);
    }

    public static InputStream getInputStream(String from) throws MalformedURLException, IOException {
        if (from.startsWith("http://")) {
            HttpURLConnection conn = (HttpURLConnection) new URL(from).openConnection();
            conn.setRequestProperty("Accept-Encoding", "gzip");
            conn.setConnectTimeout(TIMEOUT - 500);
            conn.setReadTimeout(TIMEOUT);
            InputStream in = conn.getInputStream();
            if ("gzip".equals(conn.getContentEncoding())) {
                return new GZIPInputStream(in);
            }
            return in;
        }

        String path = from;
        if (from.charAt(0) == '~') {
            path = System.getProperty("user.home");
            path = path + from.substring(1);
        }

        InputStream in = new FileInputStream(path);
        return in;
    }

    public static Future<String> slurpAsync(final String url) {
        return slurpAsync(url, "utf-8");
    }

    public static Future<String> slurpAsync(final String url, final String encoding) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            return es.submit(new Callable<String>() {
                @Override
                public String call() throws Exception {
                    return slurp(url, encoding);
                }
            });
        } finally {
            es.shutdown();
        }
    }

    public static String slurp(String url) throws IOException {
        return slurp(url, "utf-8");
    }

    public static Future<Void> cpAsync(final String from, final String to) {
        ExecutorService es = Executors.newSingleThreadExecutor();
        try {
            return es.submit(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    cp(from, to);
                    return null;
                }
            });
        } finally {
            es.shutdown();
        }
    }

    public static String slurp(String url, String encoding) throws IOException {
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(getInputStream(url), encoding);
            StringBuilder sb = new StringBuilder(256);
            char[] buf = new char[4096];

            for(int n; (n = in.read(buf)) != -1; ) {
                sb.append(buf, 0, n);
            }
            /*
            for (int c; (c = in.read()) != -1;)
                sb.append((char) c);
            */
            return sb.toString();
        } catch (SocketTimeoutException e) {
        } catch (FileNotFoundException e) {
            Log.d(TAG, url + " ---> fetch failed!");
            throw e;
        } finally {
            if (in != null) in.close();
        }
        throw new SocketTimeoutException(url +  " retry failed!");
    }

    public static String curl(String url, String... options) throws IOException {
        String encoding = "UTF-8";
        String body = null;
        List<String> headers = new ArrayList<String>();
        String username = null;
        String password = null;
        boolean verbose = false;
        int follow = 0x0fffffff;
        for (int i = 0; i < options.length; i += 2) {
            if ("--data".equals(options[i]) || "-D".equals(options[i])) {
                body = options[i+1];
                continue;
            }
            if ("--encoding".equals(options[i])) {
                encoding = options[i+1];
                continue;
            }
            if ("--user-agent".equals(options[i]) || "-A".equals(options[i])) {
                headers.add("User-Agent");
                headers.add(options[i+1]);
                continue;
            }
            if ("-H".equals(options[i])) {
                String[] key_value = options[i+1].split(": ");
                headers.add(key_value[0]);
                headers.add(key_value[1]);
                continue;
            }

            if ("-L".equals(options[i]) || "--location".equals(options[i])) {
                follow = Integer.parseInt(options[i+1]);
                continue;
            }

            /*
            if ("-U".equals(options[i]) || "--proxy-user".equals(options[i])) {
                String user_pass[] = options[i+1].split("(?<!\\\\):");
                username = user_pass[0];
                password = user_pass[1];
                continue;
            }
            */

            if ("-u".equals(options[i]) || "--user".equals(options[i])) {
                String user_pass[] = options[i+1].split("(?<!\\\\):");
                username = user_pass[0];
                password = user_pass[1];
                continue;
            }

            if ("-v".equals(options[i]) || "--verbose".equals(options[i])) {
                verbose = true;
                continue;
            }
        }

        if (body != null) {
            return curlPost(url, username, password, follow, verbose, body, headers, encoding);
        } else {
            return curlGet(url, username, password, follow, verbose, headers, encoding);
        }
    }

    public static void addHeaders(HttpPost req, List<String> headers) {
        for (int i = 0, len = headers.size(); i < len; i += 2) {
            req.addHeader(headers.get(i), headers.get(i + 1));
        }
    }

    public static final String curlPost(String url, String username, String password, int follow,
                                        boolean verbose, String body, List<String> headers, String encoding) throws IOException {
        final HttpPost req = new HttpPost(url);
        addHeaders(req, headers);
        req.setEntity(new StringEntity(body));
        return request(req, username, password, follow, verbose, encoding);
    }

    public static final String curlGet(String url, String username, String password, int follow,
                                       boolean verbose, List<String> headers, String encoding) throws IOException {
        final HttpGet req = new HttpGet(url);
        addHeaders(req, headers);
        return request(req, username, password, follow, verbose, encoding);
    }

    private static void addHeaders(HttpGet req, List<String> headers) {
		// TODO Auto-generated method stub
    	 for (int i = 0, len = headers.size(); i < len; i += 2) {
             req.addHeader(headers.get(i), headers.get(i + 1));
         }
	}

	public static void printHeaders(Header[] headers) {
        for (int i = 0; i < headers.length; i++) {
            System.out.println(headers[i].toString());
        }
    }

    public static final String request(HttpUriRequest req, String username, String password,
                                       int follow, boolean verbose, String encoding) throws IOException {
        DefaultHttpClient hc = new DefaultHttpClient();
        if (username != null) {
            hc.getCredentialsProvider().setCredentials(
                    AuthScope.ANY, new UsernamePasswordCredentials(username, password));
        }
        final HttpParams params = hc.getParams();
        HttpClientParams.setRedirecting(params, false);
        final HttpResponse res = hc.execute(req);
        final HttpEntity entity = res.getEntity();
        final Header encHeader = entity.getContentEncoding();


        int status = res.getStatusLine().getStatusCode();
        if (verbose) {
            System.out.println("********request header********");
            printHeaders(req.getAllHeaders());

            System.out.println();
            System.out.println("********response header********");
            System.out.println("Status: " + status);
            printHeaders(res.getAllHeaders());
            System.out.println();
            System.out.println();
        }

        if (status == HttpStatus.SC_OK) {
            InputStream in0 = res.getEntity().getContent();
            if (encHeader != null) {
                final HeaderElement[] codecs = encHeader.getElements();
                for (int i = 0; i < codecs.length; i++) {
                    if ("gzip".equalsIgnoreCase(codecs[i].getName())) {
                        in0 = new GZIPInputStream(in0);
                    }
                }
            }

            InputStreamReader in = new InputStreamReader(in0, encoding);
            StringBuilder sb = new StringBuilder(256);
            char[] buf = new char[4096];
            for (int n ; (n = in.read(buf)) != -1;) {
                sb.append(buf, 0, n);
            }
            return sb.toString();
        } else if (status >= 300 && status < 400 && follow > 0) {
            Header[] headers = res.getHeaders("Location");
            String followUrl = headers[0].getValue();
            if (verbose) {
                System.out.println("[DEBUG] redirecting..." + followUrl);
            }
            HttpGet followReq = new HttpGet(followUrl);
            return request(followReq, username, password, follow - 1, verbose, encoding);
        } else {
            throw new IOException(req.getMethod() + " " + req.getURI() + " response status: " + res.getStatusLine().getStatusCode());
        }
    }

    public static void spit(String fileName, String content) throws IOException {
        spit(fileName, content, "utf-8");
    }

    public static void spit(String fileName, String content, String encoding) throws IOException {
        OutputStreamWriter out = null;
        try {
            out = new OutputStreamWriter(getOutputStream(fileName), encoding);
            out.write(content);
        } finally {
            if (out != null) out.close();
        }
    }

    public static String urlencode(String url) throws UnsupportedEncodingException {
        return URLEncoder.encode(url, "UTF-8");
    }

    public static String urldecode(String s) throws UnsupportedEncodingException {
        return URLDecoder.decode(s, "UTF-8");
    }
    
	public static boolean unzip(String zipPath,String outDir){
		try {
			ZipFile zipFile= new ZipFile(zipPath);
			Enumeration<? extends ZipEntry> enumeration=zipFile.entries();
			while (enumeration.hasMoreElements()) {
				ZipEntry zipEntry = (ZipEntry) enumeration.nextElement();
				InputStream inputStream=zipFile.getInputStream(zipEntry);
				BufferedInputStream bufferedInputStream=new BufferedInputStream(inputStream);
				File file=new File(outDir+File.separator+zipEntry.getName());
				if (file.exists()) {
					file.delete();
				}
				FileOutputStream fos = new FileOutputStream(file);
				byte[]buffer = new byte[1024*4];
				int count=0;
				while( (count=bufferedInputStream.read(buffer, 0, buffer.length)) != -1){
					fos.write(buffer,0,count);
				}
				if (fos!=null) {
					fos.close();
				}
			}
			if (zipFile!=null) {
				zipFile.close();
			}
			return true ;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
	public static boolean downFile(String urls, String dirs,String name) {
		File dir=new File(dirs);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File file = new File(dir.getAbsolutePath(),name);
		if (file.exists()) {
			file.delete();
		}
		URL url;
		HttpURLConnection connection;
		InputStream iStream = null;
		BufferedInputStream biStream = null;
		FileOutputStream foStream = null;
		BufferedOutputStream boStream = null;
		try {
			url = new URL(urls);
			connection = (HttpURLConnection) url.openConnection();
			connection.setConnectTimeout(3000);
			if (connection.getResponseCode()==200) {
				iStream = connection.getInputStream();
				biStream = new BufferedInputStream(iStream);
				foStream = new FileOutputStream(file);
				boStream = new BufferedOutputStream(foStream);
				byte[] buffer = new byte[1024];
				int readLenght;
				while ((readLenght = biStream.read(buffer)) != -1) {
					boStream.write(buffer, 0, readLenght);
				}
				boStream.flush();
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (boStream != null) {
					boStream.close();
				}if (foStream != null) {
					foStream.close();
				}if (biStream != null) {
					biStream.close();
				}if (iStream != null) {
					iStream.close();
				}
				return true;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		return false;
	}
}
