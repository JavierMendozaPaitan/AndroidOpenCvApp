package com.example.androidopencvapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    Button buttxt;
    ImageView imgv;
    private static final int PICK_IMAGE = 100;
    Uri imguri;

    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
    }

    public native void galleryImageToOcv(long mat,long matout);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        buttxt  = findViewById(R.id.button);
        imgv = findViewById(R.id.imageView);

        buttxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            if (data == null) throw new AssertionError("Data cannot be null");
            imguri = data.getData();
            BitmapFactory.Options bmpf = new BitmapFactory.Options();
            bmpf.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imguri);
                Mat obj = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8U);
                Mat objout = new Mat(bmp.getWidth(), bmp.getHeight(), CvType.CV_8U);
                Utils.bitmapToMat(bmp, obj);
                galleryImageToOcv(obj.getNativeObjAddr(), objout.getNativeObjAddr());
                Utils.matToBitmap(objout, bmp);
                imgv.setImageBitmap(bmp);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private void OpenGallery(){
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }
}
