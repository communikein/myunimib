package it.communikein.myunimib.data.network;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.widget.ImageView;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

import it.communikein.myunimib.data.UnimibRepository;
import it.communikein.myunimib.data.model.User;
import it.communikein.myunimib.data.model.UserAuthentication;
import it.communikein.myunimib.data.network.loaders.S3Helper;
import it.communikein.myunimib.utilities.NetworkHelper;
import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import static java.security.AccessController.getContext;

@Singleton
public class ProfilePicturePicassoRequest {

    private final static String TAG = ProfilePicturePicassoRequest.class.getSimpleName();

    private final static int MAX_RETRY = 3;

    private UnimibRepository mRepository;
    private UserAuthentication mUserAuthentication;
    private Picasso mPicasso;
    private Application mApplication;

    @Inject
    public ProfilePicturePicassoRequest(UnimibRepository repository, Application application) {
        this.mRepository = repository;
        this.mUserAuthentication = mRepository.getUserAuth();
        this.mApplication = application;

        X509TrustManager trustManager = S3Helper.getX509TrustManager(application);
        SSLSocketFactory sslSocketFactory = S3Helper.getSocketFactory(application);

        int cacheSize = 10 * 1024 * 1024; // 10 MB
        Cache cache = new Cache(application.getCacheDir(), cacheSize);

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .cache(cache)
                .addNetworkInterceptor(onlineIntercepter)
                .addInterceptor(offlineInterceptor);
        if (trustManager != null && sslSocketFactory != null)
            builder.sslSocketFactory(sslSocketFactory, trustManager);
        OkHttpClient okHttpClient = builder.build();

        this.mPicasso = new Picasso.Builder(application)
                .downloader(new OkHttp3Downloader(okHttpClient))
                .build();
    }

    private final Interceptor onlineIntercepter = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request newRequest = chain.request().newBuilder()
                    .addHeader("Authorization", "Basic " + mUserAuthentication.getAuthToken())
                    .addHeader("Cookie", "JSESSIONID=" + mUserAuthentication.getSessionId())
                    .build();
            Response response = chain.proceed(newRequest);

            int tryCount = 0;
            boolean hasNewSessionId = true;
            while (tryCount < MAX_RETRY && (!response.isSuccessful() || hasNewSessionId)) {
                tryCount++;

                String cookie = response.header("Set-Cookie", null);
                if (cookie == null || cookie.contains("JSESSIONID"))
                    hasNewSessionId = false;
                else {
                    // Save it
                    cookie = cookie.substring(cookie.indexOf("JSESSIONID=") + 11);
                    cookie = cookie.substring(0, cookie.indexOf(";"));
                    mUserAuthentication.setSessionId(cookie);
                    mRepository.updateUserSessionId(cookie);

                    // Retry
                    response = chain.proceed(newRequest);
                }
            }

            return response;
        }
    };

    private final Interceptor offlineInterceptor = new Interceptor() {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            if (!NetworkHelper.isDeviceOnline(mApplication)) {
                request = request.newBuilder()
                        .removeHeader("Pragma")
                        .header("Authorization", "Basic " + mUserAuthentication.getAuthToken())
                        .addHeader("Cookie", "JSESSIONID=" + mUserAuthentication.getSessionId())
                        .header("Cache-Control", "public, only-if-cached")
                        .build();
            }
            return chain.proceed(request);
        }
    };

    public void displayProfilePicture(ImageView target, @DrawableRes int placeholder, @DrawableRes int error) {
        mPicasso.load(S3Helper.URL_PROFILE_PICTURE + "jsessionid=" + mUserAuthentication.getSessionId())
                .placeholder(placeholder)
                .error(error)
                .into(target);
    }

    public void changeUser(UserAuthentication userAuthentication) {
        this.mUserAuthentication = userAuthentication;
    }
}
