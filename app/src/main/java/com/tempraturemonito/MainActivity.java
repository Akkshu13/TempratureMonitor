package com.tempraturemonito;

import android.app.Dialog;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.tempraturemonitor.R;

/*
    original Banner ID: ca-app-pub-9666108206323574/4188387797

        -->Use in production only

    Test Banner Id: ca-app-pub-3940256099942544/6300978111
        -->use for testing purpose

    Other Test IDs:
    Interstitial 	ca-app-pub-3940256099942544/1033173712
    Interstitial Video 	ca-app-pub-3940256099942544/8691691433
    Rewarded Video 	ca-app-pub-3940256099942544/5224354917h
    Native Advanced 	ca-app-pub-3940256099942544/2247696110
    Native Advanced Video 	ca-app-pub-3940256099942544/1044960115

*/

public class MainActivity extends AppCompatActivity implements batReceiver.MyBroadcastListener {
    TextView text, textPer, tempT, note;
    batReceiver bat;
    DecoView batdecoView;
    int series1Index, IconResId;
    SeriesItem seriesItem1;
    CardView tempCard;
    ImageView chargingIcon;
    Float battTemp = 0f, battLevel = 0f;
    boolean doubleBackToExitPressedOnce = false;
    String CHANNEL_ID = "1234";
    boolean isNight;
    String PREF_NAME = "Temperature App Night Mode";
    String PREF_KEY = "darkState";
    String PREF_ICON_KEY = "darkModeToggler";
    String PREF_DIALOG_KEY = "IsDialogShown";
    String message;
    boolean flag = false;
    Double a;
    String r;
    ToggleButton tb;

    boolean dialogShown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView bottomBanner = findViewById(R.id.bottomBanner);
        AdRequest adRequest = new AdRequest.Builder().build();
        bottomBanner.loadAd(adRequest);

        //====================================Ads code above this=============================================

        note = findViewById(R.id.note);
        tempT = findViewById(R.id.temp);
        text = findViewById(R.id.text);
        textPer = findViewById(R.id.text_percent);
        batdecoView = findViewById(R.id.DynamicBatteryPercentage);
        bat = new batReceiver(this);
        tempCard = findViewById(R.id.tempCard);
        chargingIcon = findViewById(R.id.ch_icon);


        SharedPreferences sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        isNight = sharedPreferences.getBoolean(PREF_KEY, false);
        IconResId = sharedPreferences.getInt(PREF_ICON_KEY, R.drawable.ic_moon);
        dialogShown = sharedPreferences.getBoolean(PREF_DIALOG_KEY, false);

        if (!dialogShown && !isNight) {
            //Displaying first visit dialog
            Dialog d = new Dialog(MainActivity.this);
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);
            d.setContentView(R.layout.firstdialog);
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.transparent));
            d.setCancelable(true);
            d.show();

            d.findViewById(R.id.d_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    toggleDarkTheme();
                }
            });
        }

        if (isNight)
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);


        chargingIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        });

        MainActivity.this.registerReceiver(bat,
                new IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        );


// Create background track
        batdecoView.configureAngles(280, 0);
        batdecoView.addSeries(new SeriesItem.Builder(getResources().getColor(R.color.trackBg))
                .setRange(0, 100, 100)
                .setInitialVisibility(true)
                .setLineWidth(42f)
                .build());

//Create data series track
        seriesItem1 = new SeriesItem.Builder(Color.argb(255, 64, 196, 0))
                .setRange(0, 100, 0)
                .setLineWidth(40f)
                .build();

        series1Index = batdecoView.addSeries(seriesItem1);


        tb=(ToggleButton) findViewById(R.id.toggleButton);





    }


    @Override
    public void tempChanged(final float temp, float level, boolean isCharging, float f) {
        battLevel = level;
        battTemp = temp;

        //Toast.makeText(MainActivity.this,"charging? "+ isCharging, Toast
        //       .LENGTH_SHORT).show();
        if (isCharging) {
            message = "Device is Charging";
            chargingIcon.setAnimation(AnimationUtils.loadAnimation(MainActivity.this, R.anim.blink));
            chargingIcon.setVisibility(View.VISIBLE);
        } else {
            chargingIcon.setAnimation(null);
            chargingIcon.setVisibility(View.INVISIBLE);
        }


        if (battLevel == 100) {
            if (isCharging) {
                message = "Device is Fully charged, You should Unplug charger now";
                chargingIcon.setAnimation(null);
                if (chargingIcon.getVisibility() == View.INVISIBLE)
                    chargingIcon.setVisibility(View.VISIBLE);
            }
            textPer.setTextColor(getResources().getColor(R.color.full_battery));
            seriesItem1.setColor(getResources().getColor(R.color.full_battery));

        } else if (battLevel < 20) {
            textPer.setTextColor(getResources().getColor(R.color.low_battery));
            seriesItem1.setColor(getResources().getColor(R.color.low_battery));
        } else if (battLevel < 50) {
            textPer.setTextColor(getResources().getColor(R.color.medium_battery));
            seriesItem1.setColor(getResources().getColor(R.color.medium_battery));
        } else {
            textPer.setTextColor(getResources().getColor(R.color.good_battery));
            seriesItem1.setColor(getResources().getColor(R.color.good_battery));
        }

        String battPer = battLevel.intValue() + "%";
        textPer.setText(battPer);

        series1Index = batdecoView.addSeries(seriesItem1);
        batdecoView.addEvent(new DecoEvent.Builder(battLevel).setIndex(series1Index).setDuration(2000).build());

        textPer.setText(battPer);


        tempT.setText(String.format("%s%s", battTemp, getString(R.string.celcius)));


        tb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    // The toggle is enabled

                    a=Double.parseDouble(String.valueOf(battTemp));
                    Double b=a*9/5+32;
                    r=String.valueOf(b);
                    Toast.makeText(MainActivity.this,r+"°F",Toast.LENGTH_SHORT).show();
                    tempT.setText(r);
                    tempT.setText(String.format("%s%s", r, getString(R.string.celcius)));
                    battTemp = Float.parseFloat(r);

                } else {
                    // The toggle is disabled


                    a=Double.parseDouble(String.valueOf(battTemp));
                    Double b=a-32;
                    Double c=b*5/9;
                    r=String.valueOf(c);
                    Toast.makeText(MainActivity.this,r+"°C",Toast.LENGTH_SHORT).show();
                    tempT.setText(r);
                    tempT.setText(String.format("%s%s", r, getString(R.string.fahrenheit)));

                }
            }
        });


        if (battTemp >= 47) {
            note.setText(R.string.overHeat);
            note.setTextColor(getResources().getColor(R.color.overHeating_text));
            tempT.setTextColor(getResources().getColor(R.color.moderate));
            Toast.makeText(MainActivity.this, "Phone Is Overheating", Toast.LENGTH_LONG).show();
            tempCard.setCardBackgroundColor(getResources().getColor(R.color.overHeating));

        } else if (battTemp >= 40) {
            note.setText(R.string.moderate_temp);
            tempT.setTextColor(getResources().getColor(R.color.moderate));

        } else {
            note.setText(R.string.coolBattery);
            tempT.setTextColor(getResources().getColor(R.color.cool));
        }

    }

    //========================================================
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e("activity ", "destroy");
        this.unregisterReceiver(bat);
        startService(new Intent(getApplicationContext(), MyService.class));
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("activity ", "STOP");
        startService(new Intent(getApplicationContext(), MyService.class));

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }

    public void rateMe(View view) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=" + this.getPackageName())));
        } catch (android.content.ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar_menu, menu);
        menu.findItem(R.id.toggleDark).setIcon(IconResId);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {


        // Handle item selection
        if (item.getItemId() == R.id.toggleDark) {

//            if (isNight) {
//                item.setIcon(R.drawable.ic_sun);
//            } else {
//                item.setIcon(R.drawable.ic_moon);
//            }


            toggleDarkTheme();
        }

        return super.onOptionsItemSelected(item);
    }

    private void toggleDarkTheme() {
        if (isNight) {
            //set light mode and save state
            isNight = false;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            IconResId = R.drawable.ic_moon;
        } else {
            //set dark mode and save state
            isNight = true;
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            IconResId = R.drawable.ic_sun;
        }

        SharedPreferences.Editor editor = getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_KEY, isNight);
        editor.putInt(PREF_ICON_KEY, IconResId);
        editor.putBoolean(PREF_DIALOG_KEY, true);
        editor.apply();
    }

}