package it.communikein.myunimib.utilities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import it.communikein.myunimib.R;


class PreferenceUtils {

    public static int getPreferredSyncFrequency(Context context) {
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        String syncKey = context.getResources()
                .getString(R.string.pref_sync_frequency_key);
        String syncFrequencyDefault = context.getResources()
                .getString(R.string.pref_sync_frequency_5);

        return Integer.parseInt(sharedPreferences.getString(syncKey, syncFrequencyDefault));
    }

}
