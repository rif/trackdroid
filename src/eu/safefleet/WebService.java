package eu.trackdroid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class WebService {
	// private static final String TAG = "WebService";
	private static final String SERVER = "https://portal.trackdroid.eu/trackdroid/webservice";
	public static final int RESPONSE_OK = 200;
	private HttpClient httpclient = null;
	private static WebService instance = null;

	private WebService() {
		httpclient = getNewHttpClient();
	}

	public static WebService getInstance() {
		if (instance == null) {
			instance = new WebService();
		}
		return instance;
	}

	public HttpClient getNewHttpClient() {
		try {			
			KeyStore trustStore = KeyStore.getInstance(KeyStore
					.getDefaultType());
			trustStore.load(null, null);

			SSLSocketFactory sf = new PermissiveSSLSocketFactory(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
			HttpProtocolParams.setContentCharset(params, HTTP.UTF_8);			
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(params, 3000);
			// Set the default socket timeout (SO_TIMEOUT) 
			// in milliseconds which is the timeout for waiting for data.			
			HttpConnectionParams.setSoTimeout(params, 5000);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory
					.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(
					params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultHttpClient();
		}
	}

	public boolean login(String user, String pass)
			throws ClientProtocolException, IOException {
		HttpPost httpost = new HttpPost(SERVER + "/authenticate/?username="
				+ user + "&password=" + pass);
		HttpResponse response = httpclient.execute(httpost);
		HttpEntity entity = response.getEntity();

		if (entity != null) {
			entity.consumeContent();
		}
		return response.getStatusLine().getStatusCode() == (RESPONSE_OK);
	}

	public ArrayList<CarInfo> getCars() throws ClientProtocolException,
			IOException {
		HttpGet httpget = new HttpGet(SERVER + "/get_vehicles/");
		HttpResponse response = httpclient.execute(httpget);
		String dataAsString = getResponseAsString(response);
		// Load the requested page converted to a string into a JSONObject.
		ArrayList<CarInfo> cars = new ArrayList<CarInfo>();
		try {
			JSONObject respJson = new JSONObject("{'result' :" + dataAsString
					+ "}");

			JSONArray items = respJson.getJSONArray("result");
			for (int i = 0; i < items.length(); i++) {
				JSONObject r = items.getJSONObject(i);
				CarInfo info = new CarInfo(r.getString("vehicle_id"),
						r.getString("name"), r.getDouble("lat"),
						r.getDouble("lng"), 0);
				cars.add(info);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cars;
	}

	public synchronized CarInfo getVehicleDynamicInfo(String vehicleId,
			String number) throws ClientProtocolException, IOException,
			JSONException {
		HttpGet httpget = new HttpGet(SERVER
				+ "/get_vehicle_dynamic_info/?vehicle_id=" + vehicleId);
		// Log.d(TAG, httpclient.toString());
		HttpResponse response = httpclient.execute(httpget);
		String dataAsString = getResponseAsString(response);
		// Load the requested page converted to a string into a JSONObject.
		JSONObject object = new JSONObject(dataAsString);
		return new CarInfo(vehicleId, number, object.getDouble("lat"),
				object.getDouble("lng"), object.getInt("speed"));
	}

	private String getResponseAsString(HttpResponse response)
			throws IOException {
		HttpEntity entity = response.getEntity();

		InputStream inputStream = entity.getContent();
		ByteArrayOutputStream content = new ByteArrayOutputStream();
		int readBytes = 0;
		byte[] sBuffer = new byte[512];
		while ((readBytes = inputStream.read(sBuffer)) != -1) {
			content.write(sBuffer, 0, readBytes);
		}
		// Close the stream.
		inputStream.close();
		if (entity != null) {
			entity.consumeContent();
		}

		// Return result from buffered stream
		return new String(content.toByteArray());
	}
}

/**
 * SSL Socket factory that will not check the certificate for any site.
 * Acceotable in our case as we connect only to trusted sites (trackdroid.eu and
 * google.com)
 * 
 * @author rif
 * 
 */
final class PermissiveSSLSocketFactory extends SSLSocketFactory {
	SSLContext sslContext = SSLContext.getInstance("TLS");

	public PermissiveSSLSocketFactory(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {
			public void checkClientTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public void checkServerTrusted(X509Certificate[] chain,
					String authType) throws CertificateException {
			}

			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}
		};

		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}
