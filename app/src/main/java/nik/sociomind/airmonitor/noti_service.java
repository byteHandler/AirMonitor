package nik.sociomind.airmonitor;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import static com.amazonaws.mobile.client.internal.oauth2.OAuth2Client.TAG;
import static nik.sociomind.airmonitor.app.CHANNEL_ID;
public class noti_service extends Service {
    database db;
    JSONObject object;
    Float pm25_threshold;
    Float pm10_threshold;
    Float humidity_threshold;
    Float temperature_threshold;
    Float gas_threshold;
    Float lpg_threshold;
    @Override
    public void onCreate() {
        super.onCreate();
        db = new database(getBaseContext());
        pm25_threshold = (float)1000.0;
        pm10_threshold = (float)1000.0;
        humidity_threshold = (float) 1000;
        temperature_threshold = (float) 1000;
        gas_threshold = (float) 1000;
        lpg_threshold = (float) 1000;
    }
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        final Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);

        final Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Air Monitor")
                .setContentText("Fetching realtime data from Firebase")
                .setSmallIcon(R.drawable.ic_android)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dref = database.getReference();
        dref.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    Log.d(TAG, "onDataChange: CGUK");
                    String[] vals;
                    if (!db.isempty()) {
                        Log.d(TAG, "onDataChange: took values");
                        vals = db.return_names();
                        pm25_threshold = Float.parseFloat(vals[0]);
                        pm10_threshold = Float.parseFloat(vals[1]);
                        gas_threshold = Float.parseFloat(vals[2]);
                        lpg_threshold = Float.parseFloat(vals[3]);
                        temperature_threshold = Float.parseFloat(vals[4]);
                        humidity_threshold = Float.parseFloat(vals[5]);
                    }
                    try{
                        object = new JSONObject(dataSnapshot.getValue().toString());
                        Log.d(TAG, "onDataChange: uih"+object.getString("Humidity"));
                        if (Float.parseFloat(object.getString("PM2.5")) > pm25_threshold)
                            notifyy("PM2.5 Alert", "PM2.5 levels above specified levels");
                        if (Float.parseFloat(object.getString("PM10")) > pm10_threshold)
                            notifyy("PM10 Alert", "PM10 levels above specified levels");
                        if (Float.parseFloat(object.getString("Temperature")) > temperature_threshold)
                            notifyy("Temperature Alert", "Temperature exceeded specified limits");
                        if (Float.parseFloat(object.getString("Humidity")) > humidity_threshold)
                            notifyy("Humidity Alert", "Humidity exceeded specified limits");
                        if (Float.parseFloat(object.getString("MQ135")) > gas_threshold)
                            notifyy("Smoke Alert", "ALERT ! Smoke detected");
                        if (Float.parseFloat(object.getString("MQ02")) > lpg_threshold)
                            notifyy("LPG Alert", "ALERT ! Gas leakage detected");}
                    catch (Exception e){
                        e.printStackTrace();
                    }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return START_NOT_STICKY;
    }
public void notifyy(String topic , String message){
    try{
        final Intent notificationIntent = new Intent(this, MainActivity.class);
        final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, 0);



            final Notification notificationn = new NotificationCompat.Builder(getBaseContext(), CHANNEL_ID)
                    .setContentTitle(topic)
                    .setContentText(message)
                    .setSmallIcon(R.drawable.ic_android)
                    .setContentIntent(pendingIntent)
                    .build();
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
            notificationManager.notify(2, notificationn);
       //     Log.d(TAG, "onChildAdded: NOTIFICATION RAISED H="+ Float.toString(humidity));
    }
    catch (Exception e)
    {
        Log.d(TAG, "onChildAdded: JSON ERROR");
    }
}
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
