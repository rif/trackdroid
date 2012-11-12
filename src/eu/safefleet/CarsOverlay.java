package eu.trackdroid;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CarsOverlay extends ItemizedOverlay<OverlayItem> {
	private OverlayItem carOverlay = null;
	private Context mContext = null;

	public CarsOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public CarsOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return carOverlay;
	}

	@Override
	public int size() {
		return 1;
	}

	public void setCarOverlay(OverlayItem overlay) {
		carOverlay = overlay;
		populate();
	}

	@Override
	protected boolean onTap(int index) {
		OverlayItem item = carOverlay;
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}

}
