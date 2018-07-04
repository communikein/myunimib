package it.communikein.myunimib.data.network;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ImageView;

/*
import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestHandler;
*/

import java.io.IOException;
import java.util.Arrays;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.data.network.loaders.S3Helper;

/*
import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
*/

@Singleton
public class ProfilePicturePicassoRequest {

    private final static String TAG = ProfilePicturePicassoRequest.class.getSimpleName();

    private final static int MAX_RETRY = 3;

    private UnimibRepository mRepository;
    private UserAuthentication mUserAuthentication;

    /*
    private OkHttpClient mOkHttpClient;
    private Picasso mPicasso;
    private Request mRequest;
    */

    private ImageDownloadCallback mImageDownloadCallback;
    public interface ImageDownloadCallback {
        void onImageReady(Bitmap bitmap);
        void onImageError(Exception e);
    }

    @Inject
    public ProfilePicturePicassoRequest(UnimibRepository repository, Application application) {
        this.mRepository = repository;
        this.mUserAuthentication = mRepository.getUserAuth();

        /*
        X509TrustManager trustManager = S3Helper.getX509TrustManager(application, true);
        SSLSocketFactory sslSocketFactory = S3Helper.getSocketFactory(application, true);

        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(cache);
        if (trustManager != null && sslSocketFactory != null)
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        mOkHttpClient = builder.build();

        mRequest = new Request.Builder()
                .get()
                .url(S3Helper.URL_PROFILE_PICTURE + "JSESSIONID=" + mUserAuthentication.getSessionId())
                .addHeader("Authorization", "Basic " + mUserAuthentication.getAuthToken())
                .addHeader("Cookie", "JSESSIONID=" + mUserAuthentication.getSessionId())
                .addHeader("Connection", "keep-alive")
                .build();
        */
    }

    public void setImageDownloadCallback(ImageDownloadCallback callback) {
        this.mImageDownloadCallback = callback;
    }

    /*
    private final Interceptor onlineInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();

            Log.i(TAG, "\n\n" + String.format("Sending request %s on %s%n%s",
                    request.toString(), chain.connection(), request.headers()));

            request = request.newBuilder()
                    .get()
                    .url(S3Helper.URL_PROFILE_PICTURE + "JSESSIONID=" + mUserAuthentication.getSessionId())
                    .addHeader("Authorization", "Basic " + mUserAuthentication.getAuthToken())
                    .addHeader("Cookie", "JSESSIONID=" + mUserAuthentication.getSessionId())
                    //.addHeader("Connection", "keep-alive")
                    .build();

            Log.i(TAG, String.format("Sending request %s on %s%n%s",
                    request.toString(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            Log.i(TAG, String.format("Received response for %s%n%s",
                    response.toString(), response.headers()));

            int tryCount = 0;
            boolean hasNewSessionId = true;
            while (tryCount < MAX_RETRY && (!response.isSuccessful() || hasNewSessionId)) {
                tryCount++;

                String cookie = response.header("Set-Cookie", null);
                if (cookie == null || !cookie.contains("JSESSIONID"))
                    hasNewSessionId = false;
                else {
                    // Save it
                    cookie = cookie.substring(cookie.indexOf("JSESSIONID=") + 11);
                    cookie = cookie.substring(0, cookie.indexOf(";"));
                    mUserAuthentication.setSessionId(cookie);
                    mRepository.updateUserSessionId(cookie);

                    // Retry
                    request = chain.request().newBuilder()
                            .get()
                            .url(S3Helper.URL_PROFILE_PICTURE + "JSESSIONID=" + mUserAuthentication.getSessionId())
                            .addHeader("Authorization", "Basic " + mUserAuthentication.getAuthToken())
                            .addHeader("Cookie", "JSESSIONID=" + mUserAuthentication.getSessionId())
                            //.addHeader("Connection", "keep-alive")
                            .build();

                    response = chain.proceed(request);
                }
            }

            return response;
        }
    };

    public void displayProfilePicture() {

        mOkHttpClient.newCall(mRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                if (mImageDownloadCallback != null) mImageDownloadCallback.onImageError(e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) {
                if (!response.isSuccessful()) {
                    String cookie = response.header("Set-Cookie", null);
                    if (cookie != null && cookie.contains("JSESSIONID")) {
                        // Save it
                        cookie = cookie.substring(cookie.indexOf("JSESSIONID=") + 11);
                        cookie = cookie.substring(0, cookie.indexOf(";"));
                        mUserAuthentication.setSessionId(cookie);
                        mRepository.updateUserSessionId(cookie);

                        displayProfilePicture();
                    }
                } else {
                    Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    if (mImageDownloadCallback != null) mImageDownloadCallback.onImageReady(bitmap);
                }
            }
        });

    }

    public void displayProfilePicturePicasso(Context application, ImageView target) {

        mOkHttpClient = getOkHttpClient(application, onlineInterceptor, true);

        Picasso picasso = new Picasso.Builder(application)
                .downloader(new OkHttp3Downloader(mOkHttpClient))
                .build();
        picasso.setIndicatorsEnabled(true);
        picasso.setLoggingEnabled(true);

        picasso.load(S3Helper.URL_PROFILE_PICTURE + "jsessionid=" + mUserAuthentication.getAuthToken())
            .into(target);

    }

    public void changeUser(UserAuthentication userAuthentication) {
        this.mUserAuthentication = userAuthentication;
    }


    public static OkHttpClient getOkHttpClient(Context application, Interceptor onlineInterceptor, boolean secure) {
        try {
            X509TrustManager trustManager = S3Helper.getX509TrustManager(application, secure);
            SSLSocketFactory sslSocketFactory = S3Helper.getSocketFactory(application, secure);

            int cacheSize = 10 * 1024 * 1024; // 10 MB
            Cache cache = new Cache(application.getCacheDir(), cacheSize);

            OkHttpClient.Builder builder = new OkHttpClient.Builder()
                    .cache(cache)
                    //.addInterceptor(new LoggingInterceptor());
                    .addInterceptor(onlineInterceptor);
            if (trustManager != null && sslSocketFactory != null)
                builder.sslSocketFactory(sslSocketFactory, trustManager);

            OkHttpClient okHttpClient = builder.build();

            return okHttpClient;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static class LoggingInterceptor implements Interceptor {
        @Override public Response intercept(Interceptor.Chain chain) throws IOException {
            Request request = chain.request();

            long t1 = System.nanoTime();
            Log.i(TAG, String.format("Sending request %s on %s%n%s",
                    request.url(), chain.connection(), request.headers()));

            Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            Log.i(TAG, String.format("Received response for %s in %.1fms%n%s",
                    response.request().url(), (t2 - t1) / 1e6d, response.headers()));

            return response;
        }
    }
    */

}
