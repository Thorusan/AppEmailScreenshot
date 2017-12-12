package com.example.thorus.appemailscreenshot;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import java.io.ByteArrayOutputStream;
import java.io.File;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_CODE_GALLERY = 10;
    private Bitmap thumbnail = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery(MainActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_GALLERY) {
            if (data != null) {

                Uri selectedImage = data.getData();
                String[] filePathColumn = { MediaStore.Images.Media.DATA };
                Cursor cursor = getContentResolver().query(selectedImage,filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                thumbnail = (BitmapFactory.decodeFile(picturePath));

                File bitmapFile = new File(Environment.getExternalStorageDirectory()+
                        "/"+selectedImage+"/picture.jpg");


                sendEmail(this, picturePath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }

    /**
     * Odpre galerijo za podan ContentType (prikaže vse galerije)
     *
     * @param activity
     */
    public void openGallery(Activity activity) {
        try {
            //File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            if (!folder.exists()) {
                return;
            }
            //Intent intent = new Intent(Intent.ACTION_VIEW);
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            // intent.setType("image/*");
            //Uri uri = Uri.fromFile(folder);
            Uri uri = FileProvider.getUriForFile(
                    activity, activity.getApplicationContext().
                            getPackageName() + ".com.example.thorus.appemailscreenshot", folder);

            intent.setDataAndType(uri, "image/*");
            activity.startActivityForResult(intent, REQUEST_CODE_GALLERY);

        } catch (Exception e) {
            Log.e("openGallery", e.getMessage());
        }
    }

    public void sendEmail(Activity activity, String filePath) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            // set the type to 'email'
            emailIntent.setType("message/rfc822");
            String to[] = {"ales.kavcic86@gmail.com"};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            // the attachment
            File file = new File(filePath);
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+file.getAbsolutePath()));
            // the mail subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Attention: Bug found!!!");
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    "The details about error you can find in attachment!");
            startActivity(Intent.createChooser(emailIntent, "Send email..."));
        } catch (Exception e) {
            Log.e("openGallery", e.getMessage());
        }
    }
}
