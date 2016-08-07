package br.redcode.pokecamerago;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.AsyncTask;

public class AsyncTaskCombineBitmap extends AsyncTask<byte[], Void, Bitmap> {

    private Context context;
    private byte[] byteArrayImagemDaCamera;
    private AsyncTaskCombineBitmap.Callback callback;

    public AsyncTaskCombineBitmap(Context context, byte[] byteArrayImagemDaCamera, AsyncTaskCombineBitmap.Callback callback) {
        this.context = context;
        this.byteArrayImagemDaCamera = byteArrayImagemDaCamera;
        this.callback = callback;
    }

    @Override
    protected Bitmap doInBackground(byte[]... params) {
        Bitmap imagemDaCamera = BitmapFactory.decodeByteArray(byteArrayImagemDaCamera, 0, byteArrayImagemDaCamera.length);

        imagemDaCamera = rotate(imagemDaCamera, 90);

        Bitmap icon = BitmapFactory.decodeResource(context.getResources(), R.drawable.template_novo);
        return combineImages(icon, imagemDaCamera);
    }

    public Bitmap rotate(Bitmap b, int degrees) {
        if (degrees != 0 && b != null) {
            Matrix m = new Matrix();

            m.setRotate(degrees, (float) b.getWidth() / 2, (float) b.getHeight() / 2);
            try {
                Bitmap b2 = Bitmap.createBitmap(
                        b, 0, 0, b.getWidth(), b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                throw ex;
            }
        }
        return b;
    }


    public Bitmap combineImages(Bitmap frame, Bitmap image) {
        Bitmap cs = null;
        Bitmap rs = null;

        rs = Bitmap.createScaledBitmap(frame, image.getWidth(),
                image.getHeight(), true);

        cs = Bitmap.createBitmap(rs.getWidth(), rs.getHeight(),
                Bitmap.Config.RGB_565);

        Canvas comboImage = new Canvas(cs);
        comboImage.setMatrix(new Matrix());

        comboImage.drawBitmap(image, 25, 25, null);
        comboImage.drawBitmap(rs, 0, 0, null);
        if (rs != null) {
            rs.recycle();
            rs = null;
        }
        Runtime.getRuntime().gc();
        return cs;
    }


    @Override
    protected void onPostExecute(Bitmap bitmapOverlayed) {
        super.onPostExecute(bitmapOverlayed);
        callback.onBitmapOverlayed(bitmapOverlayed);
    }

    public interface Callback {
        void onBitmapOverlayed(Bitmap bitmapOverlayed);
    }
}