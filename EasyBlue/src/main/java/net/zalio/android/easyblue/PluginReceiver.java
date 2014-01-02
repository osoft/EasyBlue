package net.zalio.android.easyblue;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * Created by Henry on 12/14/13.
 */
public class PluginReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Received action!", Toast.LENGTH_SHORT).show();
        Intent i = new Intent();
        i.setClass(context, EasyBlueControlService.class);
        i.setAction(EasyBlueControlService.ACTION_FIRE);
        i.putExtra(EditActivity.EXTRA_BUNDLE, intent.getBundleExtra(EditActivity.EXTRA_BUNDLE));
        context.startService(i);
    }
}
