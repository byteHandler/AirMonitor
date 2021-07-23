package nik.sociomind.airmonitor;

import android.content.Context;
import android.database.DataSetObserver;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttNewMessageCallback;
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarEntry;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.xml.datatype.Duration;

import static android.content.ContentValues.TAG;
import static com.amazonaws.mobile.auth.core.internal.util.ThreadUtils.runOnUiThread;

public class settingsActivity extends AppCompatActivity {
    private static final String TAG ="setactiv" ;
    CheckBox pm25;
    CheckBox pm10;
    CheckBox mq135;
    CheckBox mq02;
    CheckBox temp;
    CheckBox humid;
    EditText pm25e;
    EditText pm10e;
    EditText mq135e;
    EditText mq02e;
    EditText tempe;
    EditText humide;
    Button apply;
    Spinner spinner1;
    Spinner spinner2;
    CheckBox backlight;
    Button display_button;
    boolean message_sent;
    Runnable runnable;
    Handler handler;
    private static final String CUSTOMER_SPECIFIC_IOT_ENDPOINT = "a2adq3e0j8pbg4-ats.iot.us-west-2.amazonaws.com";
    AWSIotMqttManager mqttManager;
    public static final String LOG_TAG = "DHT";
    public String clientId;

    @Override
    public View onCreateView(String name, Context context, AttributeSet attrs) {
        message_sent = false;
        return super.onCreateView(name, context, attrs);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        final String[] message= new String[2];
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        handler = new Handler();
        spinner1 = findViewById(R.id.f_spinner);
        spinner2 = findViewById(R.id.s_spinner);
        clientId = UUID.randomUUID().toString();
        display_button = findViewById(R.id.Display_button);
        backlight = findViewById(R.id.backlight_checkbox);
        pm25 = findViewById(R.id.PM25_checkBox);
        pm10 = findViewById(R.id.PM10_checkbox);
        mq135 = findViewById(R.id.mq135_checkbox);
        mq02 = findViewById(R.id.mq02_checkbox);
        temp = findViewById(R.id.temp_checkbox);
        humid = findViewById(R.id.humidity_checkbox);
        pm25e = findViewById(R.id.PM25_editText);
        pm10e = findViewById(R.id.PM10_edittext);
        mq135e = findViewById(R.id.mq135_exittext);
        mq02e = findViewById(R.id.mq02_edittext);
        tempe = findViewById(R.id.temp_edittext);
        humide = findViewById(R.id.humidity_edittext);
        final int[] attribute_positions = {0,0};
        final database db = new database(this);
        //     boolean bl = db.insertdata("1000","1000","1000","1000","1000","1000");
        String[] values = db.return_names();
        pm25e.setText(values[0]);
        pm10e.setText(values[1]);
        mq135e.setText(values[2]);
        mq02e.setText(values[3]);
        tempe.setText(values[4]);
        humide.setText(values[5]);
        apply = findViewById(R.id.apply_button);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(pm25.isChecked()){
                    db.updatedata("pm25",pm25e.getText().toString());
                }
                if(pm10.isChecked())
                {
                    db.updatedata("pm10",pm10e.getText().toString());
                }
                if(mq135.isChecked())
                {
                    db.updatedata("mq135",mq135e.getText().toString());
                }
                if(mq02.isChecked()){
                    db.updatedata("mq02",mq02e.getText().toString());
                }
                if(temp.isChecked()){
                    db.updatedata("temp",tempe.getText().toString());
                }
                if(humid.isChecked()){
                    db.updatedata("humid",humide.getText().toString());
                }
                Toast.makeText(getApplicationContext(),"Conditions Applied", Toast.LENGTH_SHORT).show();
            }
        });
        String[] attributes = {"PM 2.5","PM 10","Gas PPM","LPG PPM","Temperature","Humidity"};
        //Creating the ArrayAdapter instance having the country list
        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,attributes);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        spinner1.setAdapter(aa);
        spinner2.setAdapter(aa);
        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                attribute_positions[0] = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                attribute_positions[1] = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        if(!backlight.isChecked()){
            spinner1.setEnabled(false);
            spinner2.setEnabled(false);
        }
        backlight.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    spinner1.setEnabled(true);
                    spinner2.setEnabled(true);
                }
                else
                {
                    spinner1.setEnabled(false);
                    spinner2.setEnabled(false);
                }
            }
        });
        display_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(backlight.isChecked())
                    message[0] = "t" + Integer.toString(attribute_positions[0])+Integer.toString(attribute_positions[1]);
                else
                    message[0] = "f" + Integer.toString(attribute_positions[0])+Integer.toString(attribute_positions[1]);
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

                            runOnUiThread(runnable = new Runnable() {
                                @Override
                                public void run() {
                                    if(status.toString().toLowerCase().equals("connected"))
                                    {
                                        //Toast.makeText(getApplicationContext(),"Sending Message to Device", Toast.LENGTH_SHORT).show();
                                        try {
                                            mqttManager.publishString(message[0],"aqi_thing/data",AWSIotMqttQos.QOS0);
                                            //Toast.makeText(getApplicationContext(),"Message Sent", Toast.LENGTH_SHORT).show();
                                        } catch (Exception e) {
                                            Log.e(LOG_TAG, "Publishing error.", e);
                                        }

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

            }
        });
    }
}
