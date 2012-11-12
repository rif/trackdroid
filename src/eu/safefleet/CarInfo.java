package eu.trackdroid;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import org.json.JSONException;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.widget.TextView;

public class CarInfo {
	private String id;
	private String name = null;
	private double lat = 0;
	private double lng = 0;
	private int speed = 0;

	public CarInfo(String id, String name, double lat, double lng, int speed) {
		super();
		this.id = id;
		this.name = name;
		this.lat = lat;
		this.lng = lng;
		this.speed = speed;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public int getSpeed() {
		return speed;
	}

	public void obtainSpeed(int speed) {
		this.speed = speed;
	}

	public void obtainSpeed(final Handler handler, final TextView label) {
		new Thread() {
			public void run() {
				try {
					final CarInfo carInfo = WebService.getInstance()
							.getVehicleDynamicInfo(id, name);

					handler.post(new Runnable() {
						public void run() {
							label.setText(carInfo.speed + " km/h");
						}
					});
				} catch (IOException e) {
					// TODO: implement exception
					e.printStackTrace();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void obtainLocality(final Context context, final Handler handler,
			final TextView label) {
		new Thread() {
			public void run() {
				Geocoder gc = new Geocoder(context, Locale.getDefault());

				List<Address> addresses = null;
				try {
					addresses = gc.getFromLocation(lat, lng, 1);
				} catch (IOException e) {
					e.printStackTrace();
				}
				final String locality = addresses != null && addresses.size() > 0? addresses.get(0)
						.getLocality() : "N/A";

				final String finalLocality = (locality == null || locality
						.equals("")) ? "N/A" : locality;
				handler.post(new Runnable() {
					public void run() {
						label.setText(finalLocality);
					}
				});
			}
		}.start();
	}

	@Override
	public String toString() {
		return name;
	}

}
