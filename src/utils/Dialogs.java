package utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import eu.trackdroid.R;

public class Dialogs {
	public static void showExit(final Context context, final int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId)
				.setCancelable(false)
				.setPositiveButton(R.string.exit,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								((Activity) context).finish();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}
}
