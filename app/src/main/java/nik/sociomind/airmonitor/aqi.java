package nik.sociomind.airmonitor;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.client.internal.oauth2.OAuth2Client;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.github.anastr.speedviewlib.SpeedView;
import com.github.anastr.speedviewlib.util.OnPrintTickLabel;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;
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
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import static android.content.ContentValues.TAG;
import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class aqi extends Fragment{
    private final int[] colors = new int[] {
            Color.rgb(137, 230, 81),
            Color.rgb(240, 240, 30),
            Color.rgb(89, 199, 250),
            Color.rgb(250, 104, 104)
    };

    int count = 0;
    private LineChart chart;
    private LineChart chart2;
    TextView pm25ac;
    TextView pm10ac;
    CardView cv;
    CardView cv2;
    CardView cv3;
    CardView cv4;
    CardView cv5;
    TextView comments;
    Button pm25flush;
    JSONObject object;
    Button pm10flush;

    SpeedView scale;
    SpeedView scale2;

    Handler handler;
    Runnable subscribe;
    Runnable connect;
    boolean subscribed;
    boolean view_created;
    float PM25 =0;
    float PM10 =0;
    ArrayList<Entry> values = new ArrayList<>();
    ArrayList<Entry> values2 = new ArrayList<>();
    Typeface mTf;
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "a2adq3e0j8pbg4-ats.iot.us-west-2.amazonaws.com";
    AWSIotMqttManager mqttManager;
    public static final String LOG_TAG = "AQI";
    public String clientId;

    @Override
    public void onResume() {
        super.onResume();
        if(PM10 <= 50)
        {
            if(view_created)
                comments.setText("Air quality is satisfactory, and air pollution poses little to no risk.");
            if(view_created)
                cv5.setBackgroundColor(Color.GREEN);
        }
        else if (PM10 <= 100)
        {
            if(view_created)
                comments.setText("Air quality is acceptable, however for some pollutants there may be a moderate health concern for a very small number of people who are usually sensitive to air pollution");
            if(view_created)
                cv5.setBackgroundColor(Color.YELLOW);
        }
        else if(PM10 <= 150)
        {
            if(view_created)
                comments.setText("Members of sensitive groups may experience health effects. The general public is not likely to be affected.");
            if(view_created)
                cv5.setBackgroundColor(Color.rgb(255,165,0));
        }
        else if(PM10 <= 200)
        {
            if(view_created)
                comments.setText("Everyone may begin to experience health effects, members of sensitive groups may experience more serious health effects.");
            if(view_created)
                cv5.setBackgroundColor(Color.RED);
        }
        else if(PM10 <= 300)
        {
            if(view_created)
                comments.setText("Health Alert! Everyone may experience more serious health effects");
            if(view_created)
                comments.setTextColor(Color.WHITE);
            if(view_created)
                cv5.setBackgroundColor(Color.rgb(85,26,139));
        }
        else
        {
            if(view_created)
                comments.setText("Health warnings of emergency conditions. The entire population is more likely to be affected.");
            if(view_created)
                comments.setTextColor(Color.WHITE);
            if(view_created)
                cv5.setBackgroundColor(Color.parseColor("#4a2c36"));
        }
        LineData data1 = getData1();
        //  data1.setValueTypeface(mTf);
        LineData data2 = getData2();
        if(view_created)
            setupChart(chart,data1,colors[0]);
        if(view_created)
            setupChart(chart2,data2,colors[1]);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        view_created = false;
        subscribed = false;
        clientId = UUID.randomUUID().toString();
        handler = new Handler();
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
       // mqttManager = new AWSIotMqttManager(clientId, CUSTOMER_SPECIFIC_IOT_ENDPOINT);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference dref = database.getReference();
        dref.orderByKey().limitToLast(1).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                subscribed = true;

                try{
                    String timestamp;
                    object = new JSONObject(dataSnapshot.getValue().toString());
                    PM25 =Float.parseFloat(object.getString("PM2.5"));
                    PM10 = Float.parseFloat(object.getString("PM10"));
                    Log.d(TAG, "onChildAdded: fuck");
                    if(view_created && PM25 > 0)
                        scale.speedTo(PM25);
                    //  pm25ac.setText("PM 2.5 :"+Float.toString(PM25));
                    PM10 =Float.parseFloat(object.getString("PM10"));
                    timestamp = object.getString("timestamp");
                    if(PM10 <= 50)
                    {
                        if(view_created)
                            comments.setText("Air quality is satisfactory, and air pollution poses little to no risk.");
                        if(view_created)
                            cv5.setBackgroundColor(Color.GREEN);
                    }
                    else if (PM10 <= 100)
                    {
                        if(view_created)
                            comments.setText("Air quality is acceptable, however for some pollutants there may be a moderate health concern for a very small number of people who are usually sensitive to air pollution");
                        if(view_created)
                            cv5.setBackgroundColor(Color.YELLOW);
                    }
                    else if(PM10 <= 150)
                    {
                        if(view_created)
                            comments.setText("Members of sensitive groups may experience health effects. The general public is not likely to be affected.");
                        if(view_created)
                            cv5.setBackgroundColor(Color.rgb(255,165,0));
                    }
                    else if(PM10 <= 200)
                    {
                        if(view_created)
                            comments.setText("Everyone may begin to experience health effects, members of sensitive groups may experience more serious health effects.");
                        if(view_created)
                            cv5.setBackgroundColor(Color.RED);
                    }
                    else if(PM10 <= 300)
                    {
                        if(view_created)
                            comments.setText("Health Alert! Everyone may experience more serious health effects");
                        if(view_created)
                            comments.setTextColor(Color.WHITE);
                        if(view_created)
                            cv5.setBackgroundColor(Color.rgb(85,26,139));
                    }
                    else
                    {
                        if(view_created)
                            comments.setText("Health warnings of emergency conditions. The entire population is more likely to be affected.");
                        if(view_created)
                            comments.setTextColor(Color.WHITE);
                        if(view_created)
                            cv5.setBackgroundColor(Color.parseColor("#4a2c36"));
                    }
                    if(view_created && PM10 > 0)
                        scale2.speedTo(PM10);
                    //  pm10ac.setText("PM 10 :"+Float.toString(PM10));
                    if(PM25 >0)
                        values.add(new Entry(count , PM25,new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(timestamp)));
                    if(PM10 > 0)
                        values2.add(new Entry(count, PM10,new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(timestamp)));
                    LineData data1 = getData1();
                    //  data1.setValueTypeface(mTf);
                    LineData data2 = getData2();
                    //  data2.setValueTypeface(mTf);
                    if(view_created)
                        setupChart(chart,data1,colors[0]);
                    if(view_created)
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
        /*
        try {
            mqttManager.connect(AWSMobileClient.getInstance(), new AWSIotMqttClientStatusCallback() {
                @Override
                public void onStatusChanged(final AWSIotMqttClientStatus status,
                                            final Throwable throwable) {

                    runOnUiThread(connect =new Runnable() {
                        @Override
                        public void run() {
                            //    tvStatus.setText(status.toString());
                            if(status.toString().toLowerCase().equals("connected"))
                            {
                                subscribed= true;
                                set_visiblity(false);
                                try {
                                    mqttManager.subscribeToTopic("aqi_thing/data", AWSIotMqttQos.QOS0,
                                            new AWSIotMqttNewMessageCallback() {
                                                @Override
                                                public void onMessageArrived(final String topic, final byte[] data) {
                                                    runOnUiThread(subscribe = new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            try {
                                                                String message = new String(data, "UTF-8");//////////////////// THIS IS WHAT I NEED
                                                                Log.d(LOG_TAG, "Message arrived:");
                                                                Log.d(LOG_TAG, "   Topic: " + topic);
                                                                Log.d(LOG_TAG, " Message: " + message);
                                                                //     TextView msg = view.findViewById(R.id.pm10ac);
                                                                final JSONArray array;
                                                                final JSONObject object;

                                                                String timestamp;
                                                                try{
                                                                    Log.d(TAG, "run: fieo"+message);
                                                                    object = new JSONObject(message);
                                                                    PM25 =Float.parseFloat(object.getString("PM2.5"));
                                                                    if(view_created && PM25 >0)
                                                                    scale.speedTo(PM25);
                                                                    //  pm25ac.setText("PM 2.5 :"+Float.toString(PM25));
                                                                    PM10 =Float.parseFloat(object.getString("PM10"));
                                                                    timestamp = object.getString("timestamp");
                                                                    if(PM10 <= 50)
                                                                    {
                                                                        if(view_created)
                                                                        comments.setText("Air quality is satisfactory, and air pollution poses little to no risk.");
                                                                        if(view_created)
                                                                        cv5.setBackgroundColor(Color.GREEN);
                                                                    }
                                                                    else if (PM10 <= 100)
                                                                    {
                                                                        if(view_created)
                                                                        comments.setText("Air quality is acceptable, however for some pollutants there may be a moderate health concern for a very small number of people who are usually sensitive to air pollution");
                                                                        if(view_created)
                                                                        cv5.setBackgroundColor(Color.YELLOW);
                                                                    }
                                                                    else if(PM10 <= 150)
                                                                    {
                                                                        if(view_created)
                                                                        comments.setText("Members of sensitive groups may experience health effects. The general public is not likely to be affected.");
                                                                        if(view_created)
                                                                        cv5.setBackgroundColor(Color.rgb(255,165,0));
                                                                    }
                                                                    else if(PM10 <= 200)
                                                                    {
                                                                        if(view_created)
                                                                        comments.setText("Everyone may begin to experience health effects, members of sensitive groups may experience more serious health effects.");
                                                                        if(view_created)
                                                                        cv5.setBackgroundColor(Color.RED);
                                                                    }
                                                                    else if(PM10 <= 300)
                                                                    {
                                                                        if(view_created)
                                                                        comments.setText("Health Alert! Everyone may experience more serious health effects");
                                                                        if(view_created)
                                                                        comments.setTextColor(Color.WHITE);
                                                                        if(view_created)
                                                                        cv5.setBackgroundColor(Color.rgb(85,26,139));
                                                                    }
                                                                    else
                                                                    {
                                                                        if(view_created)
                                                                        comments.setText("Health warnings of emergency conditions. The entire population is more likely to be affected.");
                                                                        if(view_created)
                                                                        comments.setTextColor(Color.WHITE);
                                                                        if(view_created)
                                                                        cv5.setBackgroundColor(Color.parseColor("#4a2c36"));
                                                                    }
                                                                    if(view_created && PM10 > 0)
                                                                    scale2.speedTo(PM10);
                                                                    //  pm10ac.setText("PM 10 :"+Float.toString(PM10));
                                                                    if(PM25 >0)
                                                                    values.add(new Entry(count , PM25,new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(timestamp)));
                                                                    if(PM10 > 0)
                                                                    values2.add(new Entry(count, PM10,new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(timestamp)));
                                                                    LineData data1 = getData1();
                                                                    //  data1.setValueTypeface(mTf);
                                                                    LineData data2 = getData2();
                                                                    //  data2.setValueTypeface(mTf);
                                                                    if(view_created)
                                                                    setupChart(chart,data1,colors[0]);
                                                                    if(view_created)
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
*/


    }
    public aqi(){

    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view =  inflater.inflate(R.layout.fragment_aqi,container,false);
        scale = view.findViewById(R.id.pm25sv);
        scale.setWithTremble(false);

        scale2 = view.findViewById(R.id.pm10sv);
        scale2.setWithTremble(false);
        cv = view.findViewById(R.id.PM25cv);
        cv2 = view.findViewById(R.id.pm10cv);
        cv3 = view.findViewById(R.id.pm25chartcv);
        cv4 = view.findViewById(R.id.pm10chartcv);
        comments = view.findViewById(R.id.comments);
        cv5 = view.findViewById(R.id.cv6);
        OnPrintTickLabel label =new OnPrintTickLabel() {
            @Override
            public CharSequence getTickLabel(int tickPosition, float tick) {
                switch (tickPosition){
                    case 0:
                        return "Good";
                    case 1:
                        return "Moderate";
                    case 2:
                        return " ";
                    case 3:
                        return " ";
                    case 4:
                        return "Unhealthy";
                    case 5:
                        return " ";
                }
                return null;
            }

        };
        scale.setOnPrintTickLabel(label);
        scale2.setOnPrintTickLabel(label);
        // If you set the value from the xml that not produce an event so I will change the

        //modify the view
        chart = view.findViewById(R.id.pm25);
        MyMarkerView markerView = new MyMarkerView(getContext(),R.layout.aqimarker);
        chart2 = view.findViewById(R.id.pm10);
        chart.setMarkerView(markerView);
        chart2.setMarkerView(markerView);

       // pm25ac = view.findViewById(R.id.pm25ac);
       // pm10ac = view.findViewById(R.id.pm10ac);
        pm25flush = view.findViewById(R.id.button2);
        pm10flush = view.findViewById(R.id.button3);
        pm25flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values.clear();
            }
        });
        pm10flush.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                values2.clear();
            }
        });
//        mTf = Typeface.createFromAsset(((MainActivity)getActivity()).getAssets(), "OpenSans-Bold.ttf");
        view_created = true;
        return view;
    }
    public void set_visiblity(boolean visiblity){
        if(visiblity)
        {
            cv.setVisibility(View.INVISIBLE);
            cv2.setVisibility(View.INVISIBLE);
            cv3.setVisibility(View.INVISIBLE);
            cv4.setVisibility(View.INVISIBLE);
            cv5.setVisibility(View.INVISIBLE);
            chart.setVisibility(View.INVISIBLE);
            chart2.setVisibility(View.INVISIBLE);
//            pm10ac.setVisibility(View.INVISIBLE);
  //          pm25ac.setVisibility(View.INVISIBLE);
            pm10flush.setVisibility(View.INVISIBLE);
            pm25flush.setVisibility(View.INVISIBLE);
        //    progressBar.setVisibility(View.VISIBLE);
        //    prog.setVisibility(View.VISIBLE);
        }
        else
        {
            cv.setVisibility(View.VISIBLE);
            cv2.setVisibility(View.VISIBLE);
            cv3.setVisibility(View.VISIBLE);
            cv4.setVisibility(View.VISIBLE);
            cv5.setVisibility(View.VISIBLE);
            chart.setVisibility(View.VISIBLE);
            chart2.setVisibility(View.VISIBLE);
       //     pm10ac.setVisibility(View.VISIBLE);
      //      pm25ac.setVisibility(View.VISIBLE);
            pm10flush.setVisibility(View.VISIBLE);
            pm25flush.setVisibility(View.VISIBLE);
           // progressBar.setVisibility(View.INVISIBLE);
           // prog.setVisibility(View.INVISIBLE);
        }
    }
    private LineData getData1() {

        ArrayList<Entry> values = this.values;

        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "PM 2.5");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(2.5f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.BLACK);
        set1.setDrawValues(false);
        // create a data object with the data sets
        return new LineData(set1);
    }
    private LineData getData2() {

        ArrayList<Entry> values =this.values2;
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "PM 10");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(2.5f);
        set1.setColor(Color.BLACK);
        set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.BLACK);
        set1.setDrawValues(false);

        // create a data object with the data sets
        return new LineData(set1);
    }

    private void setupChart(LineChart chart, LineData data, int color) {

      //  ((LineDataSet) data.getDataSetByIndex(0)).setCircleHoleColor(color);

        // no description text
     //   chart.getDescription().setEnabled(false);

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

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: "+"Paused");
        view_created = false;
     //   handler.removeCallbacks(connect);
    //    handler.removeCallbacks(subscribe);
    }

    public class MyMarkerView extends MarkerView{
        private TextView tstamp;
        private TextView val;
        public MyMarkerView(Context context, int layoutResource){
            super(context,layoutResource);
            tstamp = (TextView) findViewById(R.id.timestamp);
            val = (TextView) findViewById(R.id.value);
        }

        @Override
        public void refreshContent(Entry e, Highlight highlight) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
            String time = dateFormat.format((Date)e.getData());
            val.setText(Float.toString(e.getY()));
            tstamp.setText(time);
        }
        @Override
        public MPPointF getOffsetForDrawingAtPoint(float posX, float posY) {
            float x = (float)5;
            float y = (float) 0.9;
            return new MPPointF(x,y);
        }
    }
}
