package com.communikein.myunimib.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

@SuppressWarnings("unused")
public class NetworkUtils {
    private static final int TYPE_CONNECTION_ERROR = -1;

    private static int getNetworkAvailableType(Context context){
        int type = TYPE_CONNECTION_ERROR;
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            // If I'm connected
            if (networkInfo != null && networkInfo.isConnected()) {
                //For Mobile check
                boolean isMobile = networkInfo.getType() == ConnectivityManager.TYPE_MOBILE;
                //For Wifi Check
                boolean isWifi = networkInfo.getType() == ConnectivityManager.TYPE_WIFI;

                if (isMobile) type = ConnectivityManager.TYPE_MOBILE;
                if (isWifi) type = ConnectivityManager.TYPE_WIFI;
            }
        }

        return type;
    }

    public static boolean isWifiConnected(Context context) {
        return getNetworkAvailableType(context) == ConnectivityManager.TYPE_WIFI;
    }

    public static boolean isMobileConnected(Context context) {
        return getNetworkAvailableType(context) == ConnectivityManager.TYPE_MOBILE;
    }

    public static boolean isDeviceOnline(Context context){
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();

            return (networkInfo != null && networkInfo.isConnected());
        }
        else
            return false;
    }
}
