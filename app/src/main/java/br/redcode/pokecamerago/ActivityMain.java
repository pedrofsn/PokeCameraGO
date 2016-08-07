package br.redcode.pokecamerago;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

/**
 * Created by pedrofsn on 06/08/16.
 */
public class ActivityMain extends AppCompatActivity implements AsyncTaskSaveImage.Callback, AsyncTaskCombineBitmap.Callback, View.OnClickListener {

    private static int rotation;
    private SurfaceHolder previewHolder;
    private Camera camera;
    private boolean safeToTakePicture = false;
    private ProgressDialog dialog;
    private Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, final Camera camera) {
            dialog = ProgressDialog.show(ActivityMain.this, getString(R.string.aviso), getString(R.string.salvando_imagem));
            onPictureTake(data, camera);
            safeToTakePicture = true;
        }
    };
    private ImageView imageViewTemplate;
    private Camera.Size result;
    SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            } catch (Throwable t) {
                Toast.makeText(ActivityMain.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = getBestPreviewSize(width, height, parameters);

            if (size != null) {
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                safeToTakePicture = true;
            }

            setCameraDisplayOrientation(camera);
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    public static void setCameraDisplayOrientation(android.hardware.Camera camera) {
        try {
            int cameraId = -1;
            int numberOfCameras = Camera.getNumberOfCameras();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(i, info);
                if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraId = i;
                    break;
                }
            }

            android.hardware.Camera.CameraInfo info =
                    new android.hardware.Camera.CameraInfo();

            android.hardware.Camera.getCameraInfo(cameraId, info);

            int degrees = 0;

            switch (rotation) {
                case Surface.ROTATION_0:
                    degrees = 0;
                    break;
                case Surface.ROTATION_90:
                    degrees = 90;
                    break;
                case Surface.ROTATION_180:
                    degrees = 180;
                    break;
                case Surface.ROTATION_270:
                    degrees = 270;
                    break;
            }

            int result;
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }

            camera.setDisplayOrientation(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        rotation = getWindowManager().getDefaultDisplay().getRotation();
        setContentView(R.layout.activity_main);

        imageViewTemplate = (ImageView) findViewById(R.id.imageViewTemplate);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);

        imageViewTemplate.setBackgroundColor(0x00000000);

        previewHolder = surfaceView.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        previewHolder.setFixedSize(getWindow().getWindowManager().getDefaultDisplay().getWidth(), getWindow().getWindowManager().getDefaultDisplay().getHeight());
        imageViewTemplate.setOnClickListener(this);
        exibirAviso();
    }

    private void exibirAviso() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.aviso))
                .setMessage(getString(R.string.mensagem_como_faz))
                .setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                })
                .create().show();
    }

    @Override
    public void onResume() {
        super.onResume();
        camera = Camera.open();
    }

    @Override
    public void onPause() {
        if (safeToTakePicture) {
            camera.stopPreview();
        }

        camera.release();
        camera = null;
        safeToTakePicture = false;
        super.onPause();
    }

    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result = size;
                } else {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;
                    if (newArea > resultArea) {
                        result = size;
                    } else if (size.width == 640 && size.height == 480) {
                        result = size;
                        break;
                    } else if (size.width == 1280 && size.height == 720) {
                        result = size;
                        break;
                    }
                }
            }
        }
        return result;
    }

    public void onPictureTake(byte[] data, Camera camera) {
        new AsyncTaskCombineBitmap(this, data, this).execute();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == imageViewTemplate.getId() && safeToTakePicture) {
            camera.takePicture(null, null, photoCallback);
            safeToTakePicture = false;
        } else {
            camera = Camera.open();
        }
    }

    @Override
    public void onBitmapOverlayed(Bitmap bitmapOverlayed) {
        imageViewTemplate.setVisibility(View.GONE);
        new AsyncTaskSaveImage(ActivityMain.this, bitmapOverlayed, this).execute();
    }

    @Override
    public void onImageSaved(File file) {
        dialog.dismiss();
        String absolutePath = file.getAbsolutePath();
        Intent intent = new Intent(ActivityMain.this, ActivityPreview.class);
        intent.putExtra("path", absolutePath);
        startActivity(intent);
        finish();
    }
}
