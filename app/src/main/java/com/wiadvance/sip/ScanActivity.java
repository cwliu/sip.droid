package com.wiadvance.sip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static String BCR_SERVICE_URL = "http://bcr1.intsig.net/BCRService/BCR_VCF2?user=liu@codylab.com&pass=YNW8DX3Q9YTAD94X&lang=7";


    private String mFullImagePath;
    private String mFullImageFileName = "image.jpg";
    private String mResizeImageFileName = "image_resize.jpg";

    private String TAG = "ScanActivity";

    public static Intent newIntent(Context context) {
        return new Intent(context, ScanActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Button button = (Button) findViewById(R.id.scan_take_photo_button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchTakePictureIntent();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setImage();
            resizeImage();
            namecardRecognition();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast.makeText(this, R.string.no_camera_app, Toast.LENGTH_LONG).show();
            finish();
        }

        File imageFile = null;
        try {
            imageFile = createImageFile(mFullImageFileName);
            mFullImagePath = imageFile.getAbsolutePath();

        } catch (IOException e) {
            Toast.makeText(this, R.string.create_image_failed, Toast.LENGTH_LONG).show();
            finish();
        }
        if (imageFile != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void setImage() {
        ImageView imageView = (ImageView) findViewById(R.id.scan_image_imageview);
        if (imageView == null) {
            return;
        }

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = 5;

        Bitmap bitmap = BitmapFactory.decodeFile(mFullImagePath, bmOptions);
        imageView.setImageBitmap(bitmap);
    }

    private void resizeImage() {
        Bitmap b = BitmapFactory.decodeFile(mFullImagePath);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mFullImagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        float scaleFactor = Math.max(photoW / 1024, photoH / 768);

        Bitmap out = Bitmap.createScaledBitmap(b,
                Math.round(photoW / scaleFactor), Math.round(photoH / scaleFactor), false);

        try {
            File file = createImageFile(mResizeImageFileName);
            FileOutputStream fOut;
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.JPEG, 70, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {
        }
    }

    private File createImageFile(String fileName) throws IOException {
        return new File(Environment.getExternalStorageDirectory(), fileName);
    }

    private void namecardRecognition() {
        OkHttpClient client = new OkHttpClient();
        OkHttpClient clientWith60sTimeout = client.newBuilder()
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MediaType.parse("image/jpg"), createImageFile(mResizeImageFileName));
        } catch (IOException e) {
            Log.e(TAG, "namecardRecognition: ", e);
            return;
        }

        MultipartBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("upfile", mResizeImageFileName, requestBody)
                .build();

        Request request = new Request.Builder()
                .url(BCR_SERVICE_URL)
                .post(body)
                .build();

        clientWith60sTimeout.newCall(request).enqueue(new okhttp3.Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure() called with: " + "call = [" + call + "], e = [" + e + "]");
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");

                final String rawBodyString = response.body().string();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        TextView textView = (TextView) findViewById(R.id.scan_result_textview);
                        Log.d(TAG, rawBodyString);
                        if (textView != null) {
                            textView.setText(response.code() + "\n" + rawBodyString);
                        }

                    }
                });
            }
        });
    }
}
