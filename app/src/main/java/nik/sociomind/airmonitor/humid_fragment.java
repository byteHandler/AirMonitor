package nik.sociomind.airmonitor;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.MarkerView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.utils.MPPointF;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.ContentValues.TAG;

public class humid_fragment extends Fragment {
    TextView from_date;
    Calendar myCalendar;
    TextView Entries;
    TextView to_date;
    TextView from_datet;
    TextView to_datet;
    Button fromdset;
    Button todset;
    Button fromtset;
    Button totset;
    Connection connection = null;
    ArrayList<Entry> data;
    Button fhumid;
    LineChart lineChart;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.humid_fragment,container,false);
        lineChart = v.findViewById(R.id.pcharthumid);
        myCalendar= Calendar.getInstance();
        Entries = v.findViewById(R.id.humidEntries);
        from_date= v.findViewById(R.id.humid_from);
        fhumid = v.findViewById(R.id.fetch_humid);
        to_date = v.findViewById(R.id.humid_to);
        from_datet = v.findViewById(R.id.humid_fromt);
        to_datet = v.findViewById(R.id.humid_tot);
        fromdset = v.findViewById(R.id.fromdatesethumid);
        todset = v.findViewById(R.id.todatesethumid);
        fromtset = v.findViewById(R.id.fromtimesethumid);
        totset = v.findViewById(R.id.totimesethumid);
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(from_date);
            }

        };
        final DatePickerDialog.OnDateSetListener date2 = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel(to_date);
            }

        };
        fromdset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        todset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(getContext(), date2, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        fromtset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        from_datet.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        totset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar mcurrentTime = Calendar.getInstance();
                int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        to_datet.setText( selectedHour + ":" + selectedMinute);
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
            }
        });
        fhumid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectMySql connectMySql = new ConnectMySql();
                connectMySql.execute("");
            }
        });
        return v;
    }
    private LineData getData1() {

        ArrayList<Entry> values = this.data;
        // create a dataset and give it a type
        LineDataSet set1 = new LineDataSet(values, "Humidity");
        // set1.setFillAlpha(110);
        // set1.setFillColor(Color.RED);

        set1.setLineWidth(1.75f);
        set1.setCircleRadius(5f);
        set1.setCircleHoleRadius(2.5f);
        set1.setColor(Color.BLUE);
        set1.setCircleColor(Color.BLACK);
        set1.setHighLightColor(Color.BLACK);
        set1.setDrawValues(false);
        // create a data object with the data sets
        return new LineData(set1);
    }
    private void updateLabel(TextView editText1) {
        String myFormat = "yyyy-MM-dd"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);

        editText1.setText(sdf.format(myCalendar.getTime()));
    }
    private  void runTestQuery(Connection conn, String from, String to) {
        Statement statement = null;
        int count =0;
        try {
            Log.d(TAG, "runTestQuery: "+"Creating statement...");
            statement = conn.createStatement();
            String sql;
            sql = "select * from sensor_values where tstamp between '" +from+ "' and '" + to +"';";
            Log.d(TAG, "runTestQuery: "+sql);
            ResultSet rs = statement.executeQuery(sql);
            if(rs.getWarnings().getMessage() != null){
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Error in Input");
                builder.setMessage("There was a problem in processing query. Your input seems to be invalid. Please check it");
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.create().show();
            }

            data = new ArrayList<Entry>();
            data.clear();
            //STEP 5: Extract data from result set
            while (rs.next()) {
                String humid = rs.getString("Humidity");
                String timestamp = rs.getString("tstamp");
                Log.d(TAG, "runTestQuery: "+rs.getString("Humidity"));
                data.add(new Entry(count,Float.parseFloat(humid),new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").parse(timestamp)));
                System.out.print(", humid: " + humid+"\n");
                count += 1;
            }
            Entries.setText("#Entries =897");
            //STEP 6: Clean-up environment
            rs.close();
            statement.close();
            conn.close();
        } catch (SQLException se) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Error in Input");
            builder.setMessage("There was a problem in processing query. Your input seems to be invalid. Please check it");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.create().show();
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        } finally {
            //finally block used to close resources
            try {
                if (statement != null)
                    statement.close();
            } catch (SQLException se2) {
                Log.d(TAG, "runTestQuery: sqlexception");
            }// nothing we can do
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
            }//end finally try
        }//end try
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
        humid_fragment.MyMarkerView markerView = new humid_fragment.MyMarkerView(getContext(),R.layout.aqimarker);
        chart.setMarkerView(markerView);
        chart.invalidate();
    }
    private class ConnectMySql extends AsyncTask<String, Void, String> {
        String res = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getContext(), "Please wait...", Toast.LENGTH_SHORT)
                    .show();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                try{
                    Class.forName("com.mysql.jdbc.Driver");
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                //      System.out.println("MySQL JDBC Driver Registered!");


                try {
                    connection = DriverManager.
                            getConnection("jdbc:mysql://" + "3.19.108.240" + ":" + "3306" + "/" + "aqi", "root", "imhacker007");
                } catch (SQLException e) {
                    Log.d(TAG, "runTestQuery: conn failed");
                }

                if (connection != null) {
                    Log.d(TAG, "runTestQuery: conn succedded!");
                } else {
                    Log.d(TAG, "runTestQuery: con feiled ");
                }
                String from = from_date.getText().toString() + " " + from_datet.getText().toString();
                String to = to_date.getText().toString() + " " + to_datet.getText().toString();
                runTestQuery(connection,from,to);
                setupChart(lineChart,getData1(),Color.GREEN);
            } catch (Exception e) {
                e.printStackTrace();
                res = e.toString();
            }
            return res;
        }

        @Override
        protected void onPostExecute(String result) {
            setupChart(lineChart,getData1(),Color.GREEN);
        }
    }
    public class MyMarkerView extends MarkerView {
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
