package it.communikein.myunimib.data.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Network;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;

import it.communikein.myunimib.AppExecutors;
import it.communikein.myunimib.data.User;
import it.communikein.myunimib.data.network.S3Helper;
import it.communikein.myunimib.utilities.UserUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;


public class ProfilePictureVolleyRequest implements ImageLoader.ImageCache{

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static ProfilePictureVolleyRequest sInstance;
    private final Context mContext;

    private RequestQueue mRequestQueue;
    private final ProfilePictureLoader mImageLoader;

    private final LruCache<String, Bitmap> cache = new LruCache<>(1);

    private ProfilePictureVolleyRequest(Context context, User user) {
        this.mContext = context;
        this.mRequestQueue = getRequestQueue();

        mImageLoader = new ProfilePictureLoader(user, mRequestQueue, this);
    }

    public static ProfilePictureVolleyRequest getInstance(Context context, User user) {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new ProfilePictureVolleyRequest(context, user);
            }
        }
        return sInstance;
    }


    @Override
    public Bitmap getBitmap(String url) {
        Bitmap match = cache.get(url);
        if (match != null)
            Log.d(ProfilePictureRequest.class.getSimpleName(), "Found image in cache.");

        return match;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        Log.d(ProfilePictureRequest.class.getSimpleName(), "Saving bitmap in cache (" + url + ").");
        cache.put(url, bitmap);
    }


    private RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            Cache cache = new DiskBasedCache(mContext.getCacheDir(), 1024 * 1024);
            Network network = new BasicNetwork(
                    new HurlStack(null, S3Helper.getSocketFactory(mContext)));
            mRequestQueue = new RequestQueue(cache, network);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public ProfilePictureLoader getImageLoader() {
        return mImageLoader;
    }



    public final class ProfilePictureLoader extends ImageLoader {

        private User mUser;

        /**
         * Constructs a new ImageLoader.
         *
         * @param queue      The RequestQueue to use for making image requests.
         * @param imageCache The cache to use as an L1 cache.
         */
        ProfilePictureLoader(User user, RequestQueue queue, ImageCache imageCache) {
            super(queue, imageCache);

            this.mUser = user;
        }

        @Override
        protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth,
                                                   int maxHeight, ImageView.ScaleType scaleType,
                                                   final String cacheKey) {
            ProfilePictureRequest request = new ProfilePictureRequest(mUser,
                    response ->
                        onGetImageSuccess(cacheKey, response),
                    maxWidth, maxHeight, scaleType, Bitmap.Config.RGB_565,
                    error -> {
                        onGetImageError(cacheKey, error);

                        String cookie = error.networkResponse.headers.get("Set-Cookie");
                        mUser = UserUtils.updateSessionId(mUser, cookie, mContext);
                    });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            return request;
        }
    }

    public final class ProfilePictureRequest extends ImageRequest {

        private final User mUser;

        ProfilePictureRequest(User user, Response.Listener<Bitmap> listener, int maxWidth,
                                int maxHeight, ImageView.ScaleType scaleType,
                                Bitmap.Config decodeConfig, Response.ErrorListener errorListener) {
            super(S3Helper.URL_PROFILE_PICTURE, listener, maxWidth, maxHeight, scaleType,
                    decodeConfig, errorListener);

            this.mUser = user;
        }

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + mUser.getAuthToken());
            headers.put("Cookie", "JSESSIONID=" + mUser.getSessionID());
            return headers;
        }

        @Override
        protected Response<Bitmap> parseNetworkResponse(NetworkResponse response) {
            String last_modified = response.headers.get("Last-Modified");
            last_modified = last_modified.substring(0, last_modified.length() - 4) + "GMT";

            response.headers.put("Last-Modified", last_modified);

            return super.parseNetworkResponse(response);
        }
    }

}
