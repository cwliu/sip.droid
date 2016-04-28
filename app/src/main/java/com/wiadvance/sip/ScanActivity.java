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
import android.text.TextUtils;
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

import ezvcard.Ezvcard;
import ezvcard.VCard;
import ezvcard.property.Telephone;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class ScanActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static String BCR_SERVICE_URL_FORMAT = "http://bcr1.intsig.net/BCRService/BCR_VCF2?user=%s&pass=%s&lang=7";
    private static String BCR_SERVICE_URL = String.format(BCR_SERVICE_URL_FORMAT, BuildConfig.CAMCARD_API_EMAIL, BuildConfig.CAMCARD_API_KEY);

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


        final View progress_bar = findViewById(R.id.add_contact_progress_bar);
        if (progress_bar == null) {
            return;
        }
        progress_bar.setVisibility(View.VISIBLE);
        progress_bar.bringToFront();

        RequestBody requestBody;
        try {
            requestBody = RequestBody.create(MediaType.parse("image/jpg"), createImageFile(mResizeImageFileName));
        } catch (IOException e) {
            Log.e(TAG, "Name card recognition error:", e);
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
                progress_bar.setVisibility(View.GONE);
                NotificationUtil.displayStatus(ScanActivity.this, getString(R.string.bcr_error) + ":" + e);
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call call, final okhttp3.Response response) throws IOException {
                Log.d(TAG, "onResponse() called with: " + "call = [" + call + "], response = [" + response + "]");

                final String bodyString = response.body().string();

                if (!response.isSuccessful()) {
                    NotificationUtil.displayStatus(ScanActivity.this,
                            getString(R.string.bcr_error) + ": " + response.message());
                    finish();
                    return;
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progress_bar.setVisibility(View.GONE);

                        VCard vcard = Ezvcard.parse(bodyString).first();
                        String name = null;
                        if (vcard.getFormattedName() != null) {
                            name = vcard.getFormattedName().getValue();
                        }

                        String phone = null;
                        String listString = "辨識到的\n姓名:\n" + name + "\n電話:\n";
                        if (vcard.getTelephoneNumbers().size() != 0) {
                            phone = vcard.getTelephoneNumbers().get(0).getText();
                            for (Telephone s : vcard.getTelephoneNumbers()) {
                                listString += s.getText() + " " + TextUtils.join(", ", s.getTypes()) + "\n";
                            }
                        }
                        NotificationUtil.displayStatus(ScanActivity.this, listString);
                        TextView view = (TextView) findViewById(R.id.scan_result_textview);
                        //noinspection ConstantConditions
                        view.setText(listString);

                        if (name == null && phone == null) {
                            NotificationUtil.displayStatus(ScanActivity.this, "名片辨識失敗");
                        } else {
                            Intent intent = AddContactActivity.newIntent(ScanActivity.this, name, phone);
                            startActivity(intent);
                        }
                    }
                });
            }
        });
    }
}
