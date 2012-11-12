package eu.trackdroid;

import java.io.IOException;
import java.util.List;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONException;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GoogleMapsActivity extends MapActivity {
	//private static final String TAG = "GoogleMapsActivity";
	private static final int ZOOM_LEVEL = 16;
	private static final int UPDATE_INTERVAL =  60 * 1000; // 1 minute
	private Handler handler = null;
	private MapView mapView = null;
	private List<Overlay> mapOverlays = null;
	private CarsOverlay itemizedoverlay = null;
	private String vehicleId = null;
	private String vehicleName = null;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);
		handler = new Handler();
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.getController().setZoom(ZOOM_LEVEL);
		mapView.setBuiltInZoomControls(true);
		mapView.setTraffic(true);	
		mapOverlays = mapView.getOverlays();
		Drawable drawable = GoogleMapsActivity.this.getResources().getDrawable(
				R.drawable.androidmarker);
		itemizedoverlay = new CarsOverlay(drawable, GoogleMapsActivity.this);
	}

	@Override
	protected void onResume() {
		super.onResume();		
		vehicleId = getIntent().getExtras().getString("vehicle_id");
		vehicleName = getIntent().getExtras().getString("vehicle_name");		
		handler.post(mUpdateTimeTask);
	}

	@Override
	protected void onPause() {
		super.onPause();
		handler.removeCallbacks(mUpdateTimeTask);
	}

	private Runnable mUpdateTimeTask = new Runnable() {
		public void run() {
			mapOverlays.clear();
			try {
				CarInfo carInfo = WebService.getInstance()
						.getVehicleDynamicInfo(vehicleId, vehicleName);
				//Log.d(TAG, "updating... " + carInfo.getLat() + " : " + carInfo.getLng());
				GeoPoint point = new GeoPoint((int) (carInfo.getLat() * 1E6),
						(int) (carInfo.getLng() * 1E6));
				OverlayItem overlayitem = new OverlayItem(point, vehicleName
						+ ":", "Hola, Mundo!");
				mapView.getController().setCenter(point);
				itemizedoverlay.setCarOverlay(overlayitem);
				mapOverlays.add(itemizedoverlay);
			} catch (ClientProtocolException e) {
				
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			handler.postDelayed(this, UPDATE_INTERVAL);
			//Log.d(TAG, handler.toString());
		}
	};
}
