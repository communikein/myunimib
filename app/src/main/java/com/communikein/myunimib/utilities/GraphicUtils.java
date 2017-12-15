package com.communikein.myunimib.utilities;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.communikein.myunimib.utilities.Utils;

import java.io.File;
import java.io.InputStream;

/**
 * Created by eliam on 12/4/2017.
 */

public class GraphicUtils {

    static void loadImage(final String imagePath, final ImageView target,
                          final ProgressBar progress){
        if (progress != null) {
            progress.setVisibility(View.VISIBLE);
            target.setVisibility(View.GONE);
        }

        (new AsyncTask<Void, Void, Bitmap>(){
            @Override
            protected Bitmap doInBackground(Void... params) {
                File file = new File(imagePath);

                Bitmap bmp = null;
                if (file.exists())
                    bmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                return bmp;
            }

            @Override
            protected void onPostExecute(Bitmap image) {
                target.setImageBitmap(image);
                if (progress != null) {
                    progress.setVisibility(View.GONE);
                    target.setVisibility(View.VISIBLE);
                }
            }
        }).execute();
    }

    public static Bitmap scaleImage(Context ctx, int resource){
        Bitmap img = BitmapFactory.decodeResource(ctx.getResources(), resource);

        return scaleImage(ctx, img, 180);
    }

    public static Bitmap scaleImage(Context ctx, Bitmap bitmap, int bound){
        try{
            if (bitmap == null) {
                return null; // Checking for null & return, as suggested in comments
            }

            // Get current dimensions AND the desired bounding box
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int bounding = Utils.dpToPx(ctx, bound);

            // Determine how much to scale: the dimension requiring less scaling is
            // closer to the its side. This way the image always stays inside your
            // bounding box AND either x/y axis touches it.
            float xScale = ((float) bounding) / width;
            float yScale = ((float) bounding) / height;
            float scale = (xScale <= yScale) ? xScale : yScale;

            // Create a matrix for the scaling and add the scaling data
            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);

            // Create a new bitmap and convert it to a format understood by the ImageView
            return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        }catch (Exception e){
            //Utils.sendExceptionInfo(context, e);
            //Utils.saveLog("ERROR ESSE3HANDLER - scaleImage", e.getMessage());

            return null;
        }
    }

    private static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId,
                                                         int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromStream(Context ctx, InputStream stream,
                                                       int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, Utils.dpToPx(ctx, reqWidth), Utils.dpToPx(ctx, reqHeight));

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(stream, new Rect(0, 0, 0, 0), options);
    }

}
