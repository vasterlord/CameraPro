package com.example.yulian.camerapro;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Context;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    final int CAMERA_CAPTURE = 1;
    final int PIC_CROP = 2;
    private static final int REQUEST = 1;
    private Uri picUri;
    TextView hellotxt;
    EditText txtName;
    ImageView picture;
    String empt = "Hello_Unnamed:(";
  //  Button btnGal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        picture = (ImageView) findViewById(R.id.picture);
        hellotxt = (TextView) findViewById(R.id.hellotxt);
        txtName = (EditText) findViewById(R.id.txtName);
        setTitle("Photo from you :)");
        hellotxt.setText("Hello!!!:)");

    }
    private void dispatchTakePictureIntent(int actionCode) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePictureIntent, actionCode);
    }

    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
    public void onClick(View v) {
        if (txtName.getText().toString().isEmpty())
        {
            hellotxt.setText("Hello unnamed :(");
        }     else
        {
            hellotxt.setText("Hello" + " " + txtName.getText().toString());
        }

        try {
            // Намерение для запуска камеры
            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(captureIntent, CAMERA_CAPTURE);
        }
        catch (ActivityNotFoundException e) {
            // Выводим сообщение об ошибке
            String errorMessage = "Your device does not support shooting";
            Toast toast = Toast
                    .makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
        Context context = getApplicationContext();
        Intent notificationIntent = new Intent();
        PendingIntent contentIntent = PendingIntent.getActivity(context,
                0, notificationIntent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationManager nm = (NotificationManager) context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        Resources res = context.getResources();
        Notification.Builder builder = new Notification.Builder(context);
        Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        long[] vibrate = new long[] { 1000, 1000, 1000, 1000 };
        builder.setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.cannotific)
                .setWhen(System.currentTimeMillis())
                .setSound(ringURI)
                .setVibrate(vibrate)
                .setContentTitle(" Notification ")
                .setContentText(" The best app ;) ");
        Notification notification = builder.build();
        notification.defaults = Notification.DEFAULT_SOUND |
                Notification.DEFAULT_VIBRATE;
        notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;
        notification.flags = notification.flags | Notification.FLAG_INSISTENT;
        Notification n = builder.getNotification();
        nm.notify(1, n);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap img = null;
        if (resultCode == RESULT_OK) {
            // Вернулись от приложения Камера
            if (requestCode == CAMERA_CAPTURE) {
                // Получим Uri снимка
                picUri = data.getData();
                // кадрируем его
                performCrop();
            }
            // Вернулись из операции кадрирования
            else if(requestCode == PIC_CROP){
                Bundle extras = data.getExtras();
                // Получим кадрированное изображение
                Bitmap thePic = extras.getParcelable("data");
                // передаём его в ImageView
                ImageView picView = (ImageView)findViewById(R.id.picture);
                picView.setImageBitmap(thePic);
            }
        }
        else if (requestCode == REQUEST && resultCode == RESULT_OK) {
            Uri selectedImage = data.getData();
            try {
                img = MediaStore.Images.Media.getBitmap(getContentResolver(), selectedImage);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            picture.setImageBitmap(img);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void performCrop(){
        try {
            // Намерение для кадрирования. Не все устройства поддерживают его
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            cropIntent.setDataAndType(picUri, "image/*");
            cropIntent.putExtra("crop", "true");
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            cropIntent.putExtra("return-data", true);
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            String errorMessage = "Sorry, but your device does not support framing";
            Toast toast = Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
           finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_gallery) {
            if (txtName.getText().toString().isEmpty())
            {
                hellotxt.setText("Hello unnamed :(");
            }     else
            {
                hellotxt.setText("Hello" + " " + txtName.getText().toString());
            }
            Context context = getApplicationContext();
            Intent notificationIntent = new Intent();
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, notificationIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);
            NotificationManager nm = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            Resources res = context.getResources();
            Notification.Builder builder = new Notification.Builder(context);
            Uri ringURI = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            long[] vibrate = new long[] { 1000, 1000, 1000, 1000 };
            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.cannotific)
                    .setWhen(System.currentTimeMillis())
                    .setSound(ringURI)
                    .setVibrate(vibrate)
                    .setContentTitle(" Notification ")
                    .setContentText(" The best app ;) ");
            Notification notification = builder.build();
            notification.defaults = Notification.DEFAULT_SOUND |
                    Notification.DEFAULT_VIBRATE;
            notification.flags = notification.flags | Notification.FLAG_SHOW_LIGHTS;
            notification.flags = notification.flags | Notification.FLAG_INSISTENT;
            Notification n = builder.getNotification();
            nm.notify(1, n);
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");
            startActivityForResult(i, REQUEST);
        }
        else if (id == R.id.nav_dev) {
            Intent intent = new Intent(this, Developer.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
