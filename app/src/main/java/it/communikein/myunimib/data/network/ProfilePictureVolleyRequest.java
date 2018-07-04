package it.communikein.myunimib.data.network;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

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

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.data.network.loaders.S3Helper;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ProfilePictureVolleyRequest implements ImageLoader.ImageCache{

    private final static String LOG_TAG = ProfilePictureVolleyRequest.class.getSimpleName();

    private final UnimibRepository mRepository;

    private RequestQueue mRequestQueue;
    private final ProfilePictureLoader mImageLoader;

    private final LruCache<String, Bitmap> cache = new LruCache<>(1);
    private UserAuthentication userAuthentication;

    @Inject
    public ProfilePictureVolleyRequest(UnimibRepository repository, Context context) {
        this.mRepository = repository;
        this.mRequestQueue = getRequestQueue(context);
        this.userAuthentication = mRepository.getUserAuth();

        this.mImageLoader = new ProfilePictureLoader(mRequestQueue, this);
    }


    @Override
    public Bitmap getBitmap(String url) {
        Bitmap match = cache.get(url);
        if (match != null)
            Log.d(LOG_TAG, "Found image in cache.");

        return match;
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        Log.d(LOG_TAG, "Saving bitmap in cache (" + url + ").");
        cache.put(url, bitmap);
    }

    public void clearCache() {
        cache.evictAll();
    }

    public void changeUser(UserAuthentication userAuthentication) {
        clearCache();
        this.userAuthentication = userAuthentication;
    }


    private RequestQueue getRequestQueue(Context context) {
        if (mRequestQueue == null) {
            Cache cache = new DiskBasedCache(context.getCacheDir(), 1024 * 1024);
            Network network = new BasicNetwork(
                    new HurlStack(null, S3Helper.getSocketFactory(context, true)));
            mRequestQueue = new RequestQueue(cache, network);
            mRequestQueue.start();
        }
        return mRequestQueue;
    }

    public ProfilePictureLoader getImageLoader() {
        return mImageLoader;
    }



    public final class ProfilePictureLoader extends ImageLoader {

        /**
         * Constructs a new ImageLoader.
         *
         * @param queue      The RequestQueue to use for making image requests.
         * @param imageCache The cache to use as an L1 cache.
         */
        ProfilePictureLoader(RequestQueue queue, ImageCache imageCache) {
            super(queue, imageCache);
        }

        @Override
        protected Request<Bitmap> makeImageRequest(String requestUrl, int maxWidth,
                                                   int maxHeight, ImageView.ScaleType scaleType,
                                                   final String cacheKey) {
            ProfilePictureRequest request = new ProfilePictureRequest(
                    response ->
                        onGetImageSuccess(cacheKey, response),
                    maxWidth, maxHeight, scaleType,
                    error -> {
                        onGetImageError(cacheKey, error);

                        String cookie = error.networkResponse.headers.get("Set-Cookie");
                        if (cookie != null && cookie.contains("JSESSIONID")) {
                            // Save it
                            cookie = cookie.substring(cookie.indexOf("JSESSIONID=") + 11);
                            cookie = cookie.substring(0, cookie.indexOf(";"));
                            userAuthentication.setSessionId(cookie);
                            mRepository.updateUserSessionId(cookie);
                        }
                    });
            request.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    3,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

            return request;
        }
    }

    final class ProfilePictureRequest extends ImageRequest {

        ProfilePictureRequest(Response.Listener<Bitmap> listener, int maxWidth,
                              int maxHeight, ImageView.ScaleType scaleType,
                              Response.ErrorListener errorListener) {
            super(S3Helper.URL_PROFILE_PICTURE, listener, maxWidth, maxHeight, scaleType,
                    Bitmap.Config.RGB_565, errorListener);
        }

        @Override
        public Map<String, String> getHeaders() {
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Basic " + userAuthentication.getAuthToken());
            headers.put("Cookie", "JSESSIONID=" + userAuthentication.getSessionId());
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
