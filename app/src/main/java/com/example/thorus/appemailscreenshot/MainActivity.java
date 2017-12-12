package com.example.thorus.appemailscreenshot;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static int REQUEST_CODE_GALLERY = 10;
    @BindView(R.id.fab)
    FloatingActionButton fab;
    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setSupportActionBar(toolbar);

        // Bind views
        ButterKnife.bind(this);

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
                String[] filePathColumn = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();
                  // send Email right away
                sendEmail(this, picturePath);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Opens Gallery for specified
     *
     * @param activity context that is needed to open intent
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

    /**
     * Sends an email with "image" attachment
     *
     * @param activity context
     * @param filePath file path in string
     */
    public void sendEmail(Activity activity, String filePath) {
        try {
            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            // set the type to 'email'
            emailIntent.setType("message/rfc822");
            String to[] = {"ales.kavcic86@gmail.com"};
            emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
            // the attachment
            File file = new File(filePath);
            emailIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + file.getAbsolutePath()));
            // the mail subject
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
            emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                    getString(R.string.email_body_text) +  getDeviceInfo()
            );
            startActivity(Intent.createChooser(emailIntent, getString(R.string.email_choose_title)));
        } catch (Exception e) {
            Log.e("openGallery", e.getMessage());
            Toast.makeText(activity, R.string.email_send_msg, Toast.LENGTH_SHORT).show();
        }
    }

    public String getDeviceInfo() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int density= metrics.densityDpi;
        int width=metrics.widthPixels;
        int height=metrics.heightPixels;
        double wi=(double)width/(double)metrics.xdpi;
        double hi=(double)height/(double)metrics.ydpi;
        double x = Math.pow(wi,2);
        double y = Math.pow(hi,2);
        double screenInches = Math.sqrt(x+y);

        String deviceInfo="\n\nDevice Info:\n";
        deviceInfo += "\n OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")";
        deviceInfo += "\n Android SDK: " + android.os.Build.VERSION.SDK_INT + " (API level), " + Build.VERSION.RELEASE + " (Version)";
        deviceInfo += "\n Device: " + android.os.Build.DEVICE;
        deviceInfo += "\n Manufacturer: " + Build.MANUFACTURER;
        deviceInfo += "\n Model (and Product): " + android.os.Build.MODEL + " ("+ android.os.Build.PRODUCT + ")";
        deviceInfo += "\n density: " + density;
        deviceInfo += "\n widthPixels: " + width;
        deviceInfo += "\n heightPixels: " + height;
        deviceInfo += "\n screenInches: " + screenInches;

        return deviceInfo;
    }
}
