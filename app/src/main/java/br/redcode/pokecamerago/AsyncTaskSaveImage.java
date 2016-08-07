package br.redcode.pokecamerago;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Calendar;

public class AsyncTaskSaveImage extends AsyncTask<byte[], Void, File> {

    private Bitmap bitmap;
    private AsyncTaskSaveImage.Callback callback;
    private Context context;

    public AsyncTaskSaveImage(Context context, Bitmap bitmap, AsyncTaskSaveImage.Callback callback) {
        this.callback = callback;
        this.context = context;
        this.bitmap = bitmap;
    }

    @Override
    protected File doInBackground(byte[]... arg0) {
        File imageFileFolder = new File(Environment.getExternalStorageDirectory(), "pokécamera_go");
        imageFileFolder.mkdir();
        FileOutputStream out = null;
        Calendar c = Calendar.getInstance();
        String date = "d" + fromInt(c.get(Calendar.DAY_OF_MONTH)) + "_" + "m" + fromInt(c.get(Calendar.MONTH)) + "_" + "y" + fromInt(c.get(Calendar.HOUR_OF_DAY)) + "_" + "min" + fromInt(c.get(Calendar.MINUTE)) + "_" + "sec" + fromInt(c.get(Calendar.SECOND));
        File imageFileName = new File(imageFileFolder, "pokécamera_go" + "_" + date.toString() + ".jpg");
        try {
            out = new FileOutputStream(imageFileName);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
            out = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        addNewImageToGallery(context, imageFileName.getAbsolutePath());

        return imageFileName;
    }

    @Override
    protected void onPostExecute(File result) {
        super.onPostExecute(result);
        callback.onImageSaved(result);
    }

    public String fromInt(int val) {
        return String.valueOf(val);
    }

    private Uri addNewImageToGallery(Context context, String filePath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(filePath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        context.sendBroadcast(mediaScanIntent);
        return contentUri;
    }

    public interface Callback {

        void onImageSaved(File file);

    }
}