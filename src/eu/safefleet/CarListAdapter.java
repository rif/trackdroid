package eu.trackdroid;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class CarListAdapter extends BaseAdapter {

	private Activity activity;
	private ArrayList<CarInfo> data = null;
	private static LayoutInflater inflater = null;
	private Handler handler = null;

	public CarListAdapter(Activity a, ArrayList<CarInfo> d) {
		activity = a;
		data = d;
		inflater = (LayoutInflater) activity
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		handler = new Handler();
	}

	public void setData(ArrayList<CarInfo> d) {
		data = d;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		View vi = convertView;
		if (convertView == null) {
			vi = inflater.inflate(R.layout.row, null);
		}

		CarInfo ci = data.get(position);
		if (ci != null) {
			TextView numberView = (TextView) vi.findViewById(R.id.carNumber);
			TextView locationView = (TextView) vi
					.findViewById(R.id.carLocation);
			TextView statusView = (TextView) vi.findViewById(R.id.carSpeed);
			if (numberView != null) {
				numberView.setText(ci.getName());
			}
			if (locationView != null) {
				ci.obtainLocality(parent.getContext(), handler, locationView);
				// locationView.setText(ci.getLocality(parent.getContext()));
			}

			if (statusView != null) {
				ci.obtainSpeed(handler, statusView);
				// statusView.setText(ci.getSpeed() + "km/h");
			}
		}
		return vi;
	}

	public int getCount() {
		return data.size();
	}

	public Object getItem(int position) {
		return data.get(position);
	}

	public long getItemId(int position) {
		return position;
	}
}
