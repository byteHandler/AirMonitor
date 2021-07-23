package nik.sociomind.airmonitor;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;
import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class dht extends Fragment {
    private final int[] colors = new int[] {
            Color.rgb(137, 230, 81),
            Color.rgb(240, 240, 30),
            Color.rgb(89, 199, 250),
            Color.rgb(250, 104, 104)
    };

    int count = 0;
    private BarChart chart;
    private BarChart chart2;
    TextView temperature;
    TextView humidity;
    ImageView t;
    ImageView h;
    Button humidity_flush;
    Button temperature_flush;
    JSONObject object;
    float PM25 =0;
    float PM10 =0;
   // TextView prog ;
    //ProgressBar progressBar;
    ArrayList<BarEntry> values = new ArrayList<>();
    ArrayList<BarEntry> values2 = new ArrayList<>();
    CardView cv;
    CardView cv2;
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "a2adq3e0j8pbg4-ats.iot.us-west-2.amazonaws.com";
    AWSIotMqttManager mqttManager;
    public static final String LOG_TAG = "DHT";
    public String clientId;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_dht,container,false);
        cv = view.findViewById(R.id.cardView);
        cv2 = view.findViewById(R.id.cardview2);
        t=view.findViewById(R.id.imageView);
        h =view.findViewById(R.id.imageView2);
       // progressBar = view.findViewById(R.id.progressBar2);
        //prog = view.findViewById(R.id.textView4);
        //modify the view
        chart = view.findViewById(R.id.tempchart);
        chart2 = view.findViewById(R.id.humidchart);
        clientId = UUID.randomUUID().toString();
        temperature = view.findViewById(R.id.temperature_text);
        humidity = view.findViewById(R.id.humid_text);
        temperature_flush = view.findViewById(R.id.temp_flush);
        humidity_flush = view.findViewById(R.id.humid_flush);
        //set_visiblity(true);
        temperature_flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values.clear();
            }
        });
        humidity_flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values2.clear();
            }
        });
//        mTf = Typeface.createFromAsset(((MainActivity)getActivity()).getAssets(), "OpenSans-Bold.ttf");
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dref = database.getReference();
        dref.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {


                try{
                    object = new JSONObject(dataSnapshot.getValue().toString());
                    PM25 =Float.parseFloat(object.getString("Temperature"));
                    if(PM25 >0)
                        temperature.setText(Float.toString(PM25)+" °C");

                    PM10 =Float.parseFloat(object.getString("Humidity"));
                    if(PM10 >0)
                        humidity.setText(Float.toString(PM10)+" g/m³");
                    if(PM25 >0)
                        values.add(new BarEntry(count , PM25));
                    if(PM10 >0)
                        values2.add(new BarEntry(count, PM10));
                    BarData data1 = getData1();
                    //  data1.setValueTypeface(mTf);
                    BarData data2 = getData2();
                    //  data2.setValueTypeface(mTf);
                    setupChart(chart,data1,colors[0]);
                    setupChart(chart2,data2,colors[1]);
                    count += 1;
                }
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
        final CountDownLatch latch = new CountDownLatch(1);
        AWSMobileClient.getInstance().initialize(
                getActivity().getApplicationContext(),
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
                                try {
                                    mqttManager.subscribeToTopic("aqi_thing/data", AWSIotMqttQos.QOS0,
                                            new AWSIotMqttNewMessageCallback() {
                                                @Override
                                                public void onMessageArrived(final String topic, final byte[] data) {
                                                    runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                //set_visiblity(false);
                                                                String message = new String(data, "UTF-8");//////////////////// THIS IS WHAT I NEED
                                                                Log.d(LOG_TAG, "Message arrived:");
                                                                Log.d(LOG_TAG, "   Topic: " + topic);
                                                                Log.d(LOG_TAG, " Message: " + message);
                                                           //     TextView msg = view.findViewById(R.id.pm10ac);
                                                                final JSONArray array;
                                                                final JSONObject object;
                                                                float PM25 =0;
                                                                float PM10 =0;
                                                                try{
                                                                    Log.d(TAG, "run: fieo"+message);
                                                                    object = new JSONObject(message);
                                                                    PM25 =Float.parseFloat(object.getString("Temperature"));
                                                                    if(PM25 >0)
                                                                    temperature.setText(Float.toString(PM25)+" °C");

                                                                    PM10 =Float.parseFloat(object.getString("Humidity"));
                                                                    if(PM10 >0)
                                                                    humidity.setText(Float.toString(PM10)+" g/m³");
                                                                    if(PM25 >0)
                                                                    values.add(new BarEntry(count , PM25));
                                                                    if(PM10 >0)
                                                                    values2.add(new BarEntry(count, PM10));
                                                                    BarData data1 = getData1();
                                                                    //  data1.setValueTypeface(mTf);
                                                                    BarData data2 = getData2();
                                                                    //  data2.setValueTypeface(mTf);
                                                                    setupChart(chart,data1,colors[0]);
                                                                    setupChart(chart2,data2,colors[1]);
                                                                    count += 1;
                                                                }
                                                                catch (Exception e){
                                                                    e.printStackTrace();
                                                                }

                                                                //msg.setText(current);

                                                            } catch (UnsupportedEncodingException e) {
                                                                Log.e(LOG_TAG, "Message encoding error.", e);
                                                            }
                                                        }
                                                    });
                                                }
                                            });
                                } catch (Exception e) {
                                    Log.e(LOG_TAG, "Subscription error.", e);
                                }
                                // navView.setEnabled(true);
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


        return view;
    }
   /* public void set_visiblity(boolean visiblity){
        if(visiblity)
        {
            cv.setVisibility(View.INVISIBLE);
            cv2.setVisibility(View.INVISIBLE);
            chart.setVisibility(View.INVISIBLE);
            chart2.setVisibility(View.INVISIBLE);
            humidity.setVisibility(View.INVISIBLE);
            temperature.setVisibility(View.INVISIBLE);
            temperature_flush.setVisibility(View.INVISIBLE);
            humidity_flush.setVisibility(View.INVISIBLE);
            t.setVisibility(View.INVISIBLE);
            h.setVisibility(View.INVISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            prog.setVisibility(View.VISIBLE);
        }
        else
        {
            cv.setVisibility(View.VISIBLE);
            cv2.setVisibility(View.VISIBLE);
            t.setVisibility(View.VISIBLE);
            h.setVisibility(View.VISIBLE);
            chart.setVisibility(View.VISIBLE);
            chart2.setVisibility(View.VISIBLE);
            temperature.setVisibility(View.VISIBLE);
            humidity.setVisibility(View.VISIBLE);
            temperature_flush.setVisibility(View.VISIBLE);
            humidity_flush.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);
            prog.setVisibility(View.INVISIBLE);
        }
    }
*/
    private BarData getData1() {

        List<BarEntry> values = this.values;

        // create a dataset and give it a type
        BarDataSet set1 = new BarDataSet(values, "Temperature");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

    //    set1.setLineWidth(1.75f);
     //   set1.setCircleRadius(5f);
     //   set1.setCircleHoleRadius(2.5f);
        set1.setColor(Color.RED);
     //   set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.BLACK);
        set1.setDrawValues(false);
        // create a data object with the data sets
        return new BarData(set1);
    }
    private BarData getData2() {

        ArrayList<BarEntry> values = this.values;

        // create a dataset and give it a type
        BarDataSet set1 = new BarDataSet(values2, "Humidity");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        //    set1.setLineWidth(1.75f);
        //   set1.setCircleRadius(5f);
        //   set1.setCircleHoleRadius(2.5f);
        set1.setColor(Color.BLUE);
        //   set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.BLACK);
        set1.setDrawValues(false);
        // create a data object with the data sets
        return new BarData(set1);
    }

    private void setupChart(BarChart chart, BarData data, int color) {

        //  ((LineDataSet) data.getDataSetByIndex(0)).setCircleHoleColor(color);

        // no description text
         chart.getDescription().setEnabled(false);
        // chart.setDrawHorizontalGrid(false);
        //
        // enable / disable grid background
        //  chart.setDrawGridBackground(false);
//        chart.getRenderer().getGridPaint().setGridColor(Color.WHITE & 0x70FFFFFF);

        // enable touch gestures
        //    chart.setTouchEnabled(false);

        // enable scaling and dragging
        //   chart.setDragEnabled(true);
        //   chart.setScaleEnabled(true);

        // if disabled, scaling can be done on x- and y-axis separately
        //   chart.setPinchZoom(false);
        //  chart.setBackgroundColor(color);

        // set custom chart offsets (automatic offset calculation is hereby disabled)
        //  chart.setViewPortOffsets(10, 0, 10, 0);

        // add data
        chart.setData(data);

        // get the legend (only possible after setting data)
        //    Legend l = chart.getLegend();
        //    l.setEnabled(false);
        //    chart.getAxisLeft().setEnabled(true);
        //   chart.getAxisLeft().setDrawLabels(true);
        //   chart.getAxisRight().setEnabled(true);
        //   chart.getAxisRight().setDrawLabels(true);
        //     chart.getAxisLeft().setSpaceTop(40);
        //   chart.getAxisLeft().setSpaceBottom(40);
    /*    chart.getAxisLeft().setLabelCount(10,true);
        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(true);
        chart.getXAxis().setDrawAxisLine(true);
        chart.getXAxis().setDrawLabels(true);
        chart.getXAxis().setLabelCount(10,true);*/
        // animate calls invalidate()...
        //   chart.animateX(2500);
        chart.invalidate();
    }

}
