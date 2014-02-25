package com.athlete.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.zip.GZIPInputStream;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.json.JSONObject;

import android.util.Log;

/**
 * @author edBaev
 * */
public final class HttpUtil {
	private static final String TAG = HttpUtil.class.toString();
	public static final int error404 = 404;
	public static final int error500 = 500;
	public static final int error204 = 204;
	public static final int error200 = 200;
	public static final int error202 = 202;
	public static final int error201 = 201;
	// duplicate status code
	public static final int error206 = 206;
	private static final HttpClient sClient;
	private static final int timeout = 10000;
	private static final int httpPort = 80;
	private static final int httpsPort = 443;
	private static final int bufferSize = 10240;
	private static final String headerNameContent = "Content-type";
	private static final String headerValueJson = "application/json";
	private static final String headerNameAccept = "Accept";
	private static final String headerNameAuthorixation = "Authorization";
	private static final String headerValueGPX = "application/X.athlete-GPX+xml";
	private static final String headerNameLocation = "Location";
	static {
		final HttpParams params = new BasicHttpParams();
		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, "UTF-8");

		KeyStore trustStore = null;
		try {
			trustStore = KeyStore.getInstance(KeyStore.getDefaultType());

			trustStore.load(null, null);
		} catch (Exception e) {
		}
		HttpConnectionParams.setStaleCheckingEnabled(params, false);
		HttpConnectionParams.setConnectionTimeout(params, timeout);
		HttpConnectionParams.setSoTimeout(params, timeout);
		HttpConnectionParams.setSocketBufferSize(params, bufferSize);
		HttpClientParams.setRedirecting(params, false);
		SSLSocketFactory sf = null;
		try {
			sf = new MySSLSocketFactory(trustStore);
		} catch (KeyManagementException e) {
		} catch (UnrecoverableKeyException e) {
		} catch (NoSuchAlgorithmException e) {
		} catch (KeyStoreException e) {
		}
		sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		SchemeRegistry schemeRegistry = new SchemeRegistry();
		schemeRegistry.register(new Scheme("http", PlainSocketFactory
				.getSocketFactory(), httpPort));
		schemeRegistry.register(new Scheme("https", sf, httpsPort));

		ClientConnectionManager manager = new ThreadSafeClientConnManager(
				params, schemeRegistry);
		sClient = new DefaultHttpClient(manager, params);
	}

	public static String get(String url, String authHash) {
		HttpGet httpGet = new HttpGet(url);
		String result = null;
		try {
			httpGet.setHeader(headerNameContent, headerValueJson);
			httpGet.addHeader(headerNameAccept, headerValueJson);
			if (authHash != null) {
				httpGet.addHeader(headerNameAuthorixation, authHash);
			}
			HttpResponse response = sClient.execute(httpGet);
			if (response.getStatusLine().getStatusCode() == error404) {
				return String.valueOf(error404);
			}
			return readResponse(response);

		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

	public static String getGPX(String url, String authHash) {
		HttpGet httpGet = new HttpGet(url);

		String result = null;
		try {
			httpGet.setHeader(headerNameContent, headerValueJson);
			httpGet.addHeader(headerNameAccept, headerValueGPX);
			if (authHash != null)
				httpGet.addHeader(headerNameAuthorixation, authHash);
			HttpResponse response = sClient.execute(httpGet);

			return readResponse(response);
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

	private static String readResponse(HttpResponse response)
			throws IllegalStateException, IOException {
		StringBuffer builder = new StringBuffer();
		BufferedReader reader = null;
		Header acceptEnc = response.getFirstHeader("Content-Encoding");
		if (acceptEnc != null && "gzip".equalsIgnoreCase(acceptEnc.getValue())) {
			reader = new BufferedReader(new InputStreamReader(
					new GZIPInputStream(response.getEntity().getContent())));
		} else {
			reader = new BufferedReader(new InputStreamReader(response
					.getEntity().getContent()));
		}
		String line = null;
		while ((line = reader.readLine()) != null) {
			builder.append(line);
		}
		return builder.toString();
	}

	public static String put(String url, JSONObject data, String authHash) {
		HttpPut httpPut = new HttpPut(url);

		String result = null;
		try {
			httpPut.setHeader(headerNameContent, headerValueJson);
			httpPut.addHeader(headerNameAccept, headerValueJson);
			if (authHash != null) {
				httpPut.addHeader(headerNameAuthorixation, authHash);
			}
			if (data != null) {
				httpPut.setEntity(new StringEntity(data.toString(), "UTF8"));
			}
			HttpResponse response = sClient.execute(httpPut);
			return readResponse(response);
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

	public static boolean delete(String url, String authHash) {
		HttpDelete httpDelete = new HttpDelete(url);
		boolean result = false;
		try {
			httpDelete.setHeader(headerNameContent, headerValueJson);
			httpDelete.addHeader(headerNameAccept, headerValueJson);
			if (authHash != null)
				httpDelete.addHeader(headerNameAuthorixation, authHash);
			HttpResponse response = sClient.execute(httpDelete);
			if (response.getStatusLine().getStatusCode() == error204
					|| response.getStatusLine().getStatusCode() == error404) {
				return true;
			}

		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

	public static String post(String url, JSONObject data, String authHash,
			boolean statusLine) {
		HttpPost httppost = new HttpPost(url);
		String result = "";
		try {

			httppost.setHeader(headerNameContent, headerValueJson);
			httppost.addHeader(headerNameAccept, headerValueJson);
			if (authHash != null) {
				httppost.addHeader(headerNameAuthorixation, authHash);
			}
			if (data != null) {
				httppost.setEntity(new StringEntity(data.toString(), "UTF8"));
			}
			HttpResponse response = sClient.execute(httppost);
			Header[] location = response.getHeaders(headerNameLocation);
			if (response.getStatusLine().getStatusCode() == error404) {
				String.valueOf(error404);
			}
			if (statusLine) {
				if (response.getStatusLine().getStatusCode() == error202) {
					return String.valueOf(error202);
				}
				if (response.getStatusLine().getStatusCode() == error200) {
					return String.valueOf(error200);
				}
				if (response.getStatusLine().getStatusCode() == error204) {
					return String.valueOf(error204);
				}
				if (response.getStatusLine().getStatusCode() == error201) {
					return String.valueOf(error201);
				}
			}
			if (location != null && location.length > 0) {
				return location[0].getValue();
			}

			return readResponse(response);
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

	public static String postJSONTrack(String url, JSONObject data,
			String authHash) {
		HttpPost httppost = new HttpPost(url);
		String result = null;
		try {

			httppost.setHeader(headerNameContent, headerValueJson);
			httppost.addHeader(headerNameAccept, headerValueJson);
			if (authHash != null) {
				httppost.addHeader(headerNameAuthorixation, authHash);
			}
			if (data != null) {
				httppost.setEntity(new StringEntity(data.toString(), "UTF8"));
			}
			HttpResponse response = sClient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == error206) {
				return String.valueOf(error206);
			}
			Header[] location = response.getHeaders(headerNameLocation);

			if (location != null && location.length > 0) {
				return location[0].getValue();
			}

		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

	public static String postPhoto(String link, String authHash, File file) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		FileEntity entity = new FileEntity(file, "image/png");
		HttpPost httppost = new HttpPost(link);
		httppost.setHeader(headerNameContent, "image/jpeg");
		httppost.addHeader(headerNameAuthorixation, authHash);
		httppost.setEntity(entity);
		try {
			HttpResponse response = httpclient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == error201) {
				return String.valueOf(error201);
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			return String.valueOf(error500);
		}

	}

	public static String postWorkout(String link, String authHash, File file) {

		DefaultHttpClient httpclient = new DefaultHttpClient();
		FileEntity entity = new FileEntity(file, "application/gpx+xml");
		HttpPost httppost = new HttpPost(link);
		httppost.setHeader(headerNameContent, headerValueGPX);
		httppost.addHeader(headerNameAuthorixation, authHash);
		httppost.setEntity(entity);
		try {
			HttpResponse response = httpclient.execute(httppost);
			Header[] location = response.getHeaders("Location");
			if (response.getStatusLine().getStatusCode() == error206) {
				return String.valueOf(error206);
			}
			if (location != null && location.length > 0) {
				return location[0].getValue();
			}
			return null;

		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String patch(String url, JSONObject data, String authHash) {
		HttpPost httppost = new HttpPost(url);

		String result = null;
		try {
			httppost.setHeader(headerNameContent, headerValueJson);
			httppost.addHeader(headerNameAccept, headerValueJson);
			httppost.addHeader("X-HTTP-Method-Override", "PATCH");

			if (authHash != null)
				httppost.addHeader(headerNameAuthorixation, authHash);
			if (data != null)
				httppost.setEntity(new StringEntity(data.toString(), "UTF8"));
			HttpResponse response = sClient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == error202)
				return String.valueOf(error202);
			if (response.getStatusLine().getStatusCode() == error200)
				return String.valueOf(error200);
			if (response.getStatusLine().getStatusCode() == error204)
				return String.valueOf(error204);

			return readResponse(response);
		} catch (Exception e) {
			Log.e(TAG, "" + e.getMessage(), e);
		}

		return result;
	}

}