package nik.sociomind.airmonitor;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.github.anastr.speedviewlib.ProgressiveGauge;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;
import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class lpg extends Fragment {
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "a2adq3e0j8pbg4-ats.iot.us-west-2.amazonaws.com";
    AWSIotMqttManager mqttManager;
    ProgressiveGauge gauge;
    public static final String LOG_TAG = "lpg";
    public String clientId;
    ArrayList<Float> values;
    TextView conn;
    database db;
    JSONObject object;
    ProgressBar progressBar;
    EditText threshold;
    Button thresholdset;
    float actual_threshold;
    Vibrator vibrator;
    boolean subscribed;
    boolean view_created;
    float MQ135 =0;
    @Override
    public void onResume() {
        super.onResume();
        if(view_created)
            gauge.speedTo(MQ135);
        if(subscribed)

            if(MQ135 > actual_threshold && view_created)
            {
                gauge.setSpeedometerColor(Color.parseColor("#E91E63"));
            }
            else
            {
                if(view_created)
                    gauge.setSpeedometerColor(Color.parseColor("#4CAF50"));
            }
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate: lpg created");
        subscribed = false;
        view_created = false;
        clientId = UUID.randomUUID().toString();
        actual_threshold = 1000;
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dref = database.getReference();
        dref.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                subscribed = true;

                try{
                    object = new JSONObject(dataSnapshot.getValue().toString());

                    MQ135 =Float.parseFloat(object.getString("MQ135"));
                    conn.setText(object.getString("MQ135"));
                    if(view_created)
                        gauge.speedTo(MQ135);
                    if(MQ135 > actual_threshold && view_created)
                    {
                        gauge.setSpeedometerColor(Color.parseColor("#E91E63"));
                        vibrator.vibrate(900);
                        conn.setTextColor(Color.parseColor("FFC62828"));
                        conn.setText("SMOKE PRESENT");
                    }
                    else
                    {
                        if(view_created)
                            gauge.setSpeedometerColor(Color.parseColor("#4CAF50"));
                        conn.setTextColor(Color.parseColor("FF2E7D32"));
                        conn.setText("SMOKE ABSENT");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                //msg.setText(current);

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
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_lpg,container,false);
        Log.d(TAG, "onCreateView: view inflated");
        vibrator = (Vibrator) ((MainActivity)getActivity()).getSystemService(Context.VIBRATOR_SERVICE);
        gauge = view.findViewById(R.id.lpg_progressiveGauge2);
        conn = view.findViewById(R.id.lpgvalue);
        db = new database(view.getContext());
        gauge.setWithTremble(false);
        values = new ArrayList<>();
        if(!subscribed)
        {

            Log.d(TAG, "onCreateView: visiblity set to true");
        }
        view_created = true;

        actual_threshold =Float.parseFloat(db.return_names()[2]);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        view_created = false;
    }


}
