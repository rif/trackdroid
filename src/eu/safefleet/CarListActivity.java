package eu.trackdroid;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.client.ClientProtocolException;

import utils.Dialogs;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public class CarListActivity extends ListActivity {
	//private static final String TAG = "ListActivity";
	private Handler handler = null;
	private CarListAdapter carListAdapter = null;
	private ProgressDialog dialog = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		ListView lv = getListView();
		lv.setTextFilterEnabled(true);
		carListAdapter = new CarListAdapter(this, new ArrayList<CarInfo>());
		setListAdapter(carListAdapter);
		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent mapIntent = new Intent(getApplicationContext(),
						GoogleMapsActivity.class);
				CarInfo ci = (CarInfo) carListAdapter.getItem(position);
				mapIntent.putExtra("vehicle_id", ci.getId());
				mapIntent.putExtra("vehicle_name", ci.getName());
				startActivity(mapIntent);
			}
		});
		handler = new Handler();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (carListAdapter.isEmpty()) {
			dialog = ProgressDialog.show(CarListActivity.this, "",
					"Loading. Please wait...", true);
			new Thread(carUpdaterRunnable).start();
		} else {
			carListAdapter.notifyDataSetChanged();
		}
	}

	private Runnable carUpdaterRunnable = new Runnable() {
		public void run() {
			try {
				final ArrayList<CarInfo> carList = WebService.getInstance()
						.getCars();
				if (carList == null || carList.isEmpty()) {
					// connected to server but could not obtain result: must
					// login
					Intent loginIntent = new Intent(getApplicationContext(),
							LoginActivity.class);
					startActivity(loginIntent);
				} else {
					handler.post(new Runnable() {
						public void run() {
							carListAdapter.setData(carList);
							carListAdapter.notifyDataSetChanged();
						}
					});
				}
			} catch (ClientProtocolException e) {
				handler.post(new Runnable() {
					public void run() {
						Dialogs.showExit(CarListActivity.this,
								R.string.servererror);
					}
				});

			} catch (IOException e) {
				handler.post(new Runnable() {
					public void run() {
						Dialogs.showExit(CarListActivity.this,
								R.string.nointernet);
					}
				});
			}
			handler.post(new Runnable() {
				public void run() {
					dialog.dismiss();
				}
			});
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.cars_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.reload_cars:
			dialog = ProgressDialog.show(CarListActivity.this, "",
					"Loading. Please wait...", true);
			new Thread(carUpdaterRunnable).start();
			return true;
		case R.id.exit:
			moveTaskToBack(true);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}		
}
