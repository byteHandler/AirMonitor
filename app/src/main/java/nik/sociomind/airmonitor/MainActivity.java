package nik.sociomind.airmonitor;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

public class MainActivity extends AppCompatActivity {

    private TextView mTextMessage;
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "a2adq3e0j8pbg4-ats.iot.us-west-2.amazonaws.com";

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this , settingsActivity.class);
                MainActivity.this.startActivity(intent);
                break;
            case R.id.pastdata:
                Intent intent1 = new Intent(MainActivity.this,Main2Activity.class);
                MainActivity.this.startActivity(intent1);
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu,menu);
        return true;
    }
    AWSIotMqttManager mqttManager;
    Toolbar toolbar;
    String clientId;
    Fragment aqi1;
    Fragment dht1;
    Fragment mq135;
    Fragment mq02;
    public static final String LOG_TAG = "MainActivity";
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {

            switch (item.getItemId()) {
                case R.id.aqi:
                    loadFragment(aqi1);
                    return true;
                case R.id.lpg:
                   loadFragment(mq02);
                    return true;
                case R.id.gas:

                  loadFragment(mq135);
                    return true;
                case R.id.dht11:
                    loadFragment(dht1);
                    return true;
            }
            return false;
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent serviceIntent = new Intent(this, noti_service.class);
        ContextCompat.startForegroundService(this, serviceIntent);
        database db = new database(this);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Air Monitor");
        setSupportActionBar(toolbar);
        final BottomNavigationView navView = findViewById(R.id.nav_view);
        clientId = UUID.randomUUID().toString();
        navView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        navView.setEnabled(false);
        aqi1 = new aqi();
        dht1 = new dht();
      mq02 = new lpg();
      mq135 = new gas();
        loadFragment(aqi1);
/*
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(
                getApplicationContext(),
                new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails result) {
                        latch.countDown();
                    }

                    @Override
                    public void onError(Exception e) {
                        latch.countDown();
                        Log.e(LOG_TAG, "onError: ", e);
                    }
                }
        );
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);
        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {
                    Log.d(LOG_TAG, "Status = " + String.valueOf(status));

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        //    tvStatus.setText(status.toString());
                            if(status.toString().toLowerCase().equals("connected"))
                            {
                                loadFragment(new aqi());
                                navView.setEnabled(true);
                            }
                            if (throwable != null) {
                                Log.e(LOG_TAG, "Connection error.", throwable);
                            }
                        }
                    });
                }
            });
        } catch (final Exception e) {
            Log.e(LOG_TAG, "Connection error.", e);
         //   tvStatus.setText("Error! " + e.getMessage());
        }
*/



    }
    public  void loadFragment(Fragment fragment){
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frameLayout,fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public AWSIotMqttManager getMqttManager() {
        return mqttManager;
    }
}
