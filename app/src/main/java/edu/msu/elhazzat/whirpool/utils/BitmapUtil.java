package edu.msu.elhazzat.whirpool.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

/**
 * Created by christianwhite on 12/5/15.
 */
public class BitmapUtil {
    public static Bitmap resizeMapIcons(Context context, String iconName,
                                         int widthDip, int heightDip){
        int widthPx = (int)dipToPixels(context, widthDip);
        int heightPx = (int)dipToPixels(context, heightDip);

        Bitmap imageBitmap = BitmapFactory.decodeResource(context.getResources(),
                context.getResources().getIdentifier(iconName, "drawable", context.getPackageName()));
        Bitmap resizedBitmap = Bitmap.createScaledBitmap(imageBitmap, widthPx, heightPx, false);
        return resizedBitmap;
    }

    /**
     * Create a bitmap from string
     * @param text
     * @return
     */
    public static BitmapDescriptor getTextMarker(String text) {

        Paint paint = new Paint();

        /* Set text size, color etc. as needed */
        paint.setTextSize(24);

        int width = (int)paint.measureText(text);
        int height = (int)paint.getTextSize();

        paint.setTextAlign(Paint.Align.CENTER);

        // Create a transparent bitmap as big as you need
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(image);

        // During development the following helps to see the full
        // drawing area:
        canvas.drawColor(0x00000000);

        canvas.translate(width / 2f, height);
        canvas.drawText(text, 0, 0, paint);
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(image);
        return icon;
    }


    public static float dipToPixels(Context context, float dipValue) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics);
    }
}
