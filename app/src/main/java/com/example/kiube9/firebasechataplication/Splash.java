package com.example.kiube9.firebasechataplication;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by Kiube9 on 10/26/2016.
 */

public class Splash extends Activity {
    public ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    public static int Device_Width;
    public static FirebaseDatabase mDatabase;
    static String LoggedIn_User_Email;

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    Thread splashTread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        progressDialog = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        if (mDatabase == null) {
            mDatabase = FirebaseDatabase.getInstance();
            //mDatabase.setPersistenceEnabled(true);
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }
        if(mAuth.getCurrentUser() != null)
        {
            //User  logged In
            finish();
            startActivity(new Intent(getApplicationContext(),ChatActivity.class));

        }

//        FirebaseUser user = mAuth.getCurrentUser();
//        Log.d("LOGGED", "FirebaseUser: " + user);
//        if (user != null) {
//           LoggedIn_User_Email =user.getEmail();
//        }

//        if (MainActivity.mDatabase == null) {
//            MainActivity.mDatabase = FirebaseDatabase.getInstance();
//            //mDatabase.setPersistenceEnabled(true);
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        }



    }


    private final IntentFilter filter = new IntentFilter();
    private BroadcastReceiver networkStateReceiver;

    @Override
    public void onResume() {
        super.onResume();
        // Defining broadcast receiver in onResume()
        networkStateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //Do what you want
                final String status = NetworkUtil.getConnectivityStatusString(context);

                Toast.makeText(context, status, Toast.LENGTH_LONG).show();
                Animation anim = AnimationUtils.loadAnimation(Splash.this, R.anim.alpha);
                anim.reset();
                LinearLayout l = (LinearLayout) findViewById(R.id.lin_lay);
                l.clearAnimation();
                l.startAnimation(anim);
                anim = AnimationUtils.loadAnimation(Splash.this, R.anim.translate);
                anim.reset();
                ImageView iv = (ImageView) findViewById(R.id.img_spalsh);
                iv.clearAnimation();
                iv.startAnimation(anim);


                splashTread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            int waited = 0;
                            while (waited < 5000) {
                                sleep(100);
                                waited += 100;
                            }
                            if (status.equals("Not connected to Internet")) {
                                finish();
                            } else {
                                Intent intent = new Intent(Splash.this,
                                        MainActivity.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(intent);
                                Splash.this.finish();
                            }
                        } catch (InterruptedException e) {

                        } finally {
                            Splash.this.finish();
                        }
                    }
                };
                splashTread.start();


            }
        };
        // Registering receiver with intent filter, here intent filter can be changed
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkStateReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister receiver in onStop to avoid any runtime exception
        unregisterReceiver(networkStateReceiver);
    }




}
