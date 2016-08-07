package br.redcode.pokecamerago;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.File;

/**
 * Created by pedrofsn on 06/08/16.
 */
public class ActivityPreview extends AppCompatActivity {

    private String path;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);

        path = getIntent().getStringExtra("path");

        final ImageView imageView = (ImageView) findViewById(R.id.imageView);

        new Thread(new Runnable() {
            @Override
            public void run() {
                File imgFile = new File(path);

                Bitmap myBitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());

                imageView.setImageBitmap(myBitmap);
            }
        }).run();

        MobileAds.initialize(this, Constantes.ID_BANNER_APP);
        InterstitialAd mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(Constantes.BLOCO_ANUNCIO);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, ActivityMain.class));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_preview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuItemCompartilhar:
                share();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void share() {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("image/jpeg");
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(path));
        startActivity(Intent.createChooser(share, getString(R.string.compartilhar)));
    }
}
