package com.example.ruben.sportsballbounceapp;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;

import android.graphics.Color;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;


import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.AsyncTask;
import android.os.Environment;
import android.os.Bundle;
import android.os.Handler;

import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import static android.Manifest.permission.RECORD_AUDIO;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static java.lang.Math.abs;
import static java.lang.Math.log;
import static java.lang.Math.log10;


//TODO opslaan van de metingen adhv de juiste naam (Datum (J-M-D) - B/G - M1/M2/M3/M4/M5)


public class Football1Test extends AppCompatActivity implements AdapterView.OnItemClickListener, AdapterView.OnItemSelectedListener {

    // buttons element
    Button buttonStart, buttonStop, buttonPlayLastRecordAudio, buttonStopPlayingRecording, buttonUpdate, buttonTest ;
    // Spinner element
    Spinner OndergrondKeuze,MetingKeuze;
    String AudioSavePathInDevice    = "SportsballsBounceApp/";
    String csvFile = "SportsballsBounceApp/file/test.txt";
    String RandomAudioFileName      = "FootBallTest";
    MediaRecorder   mediaRecorder ;
    MediaPlayer     mediaPlayer ;
    Random random ;
    public static final int RequestPermissionCode = 1;
    private PointsGraphSeries<DataPoint> series1;
    private LineGraphSeries<DataPoint> series2;
    private LineGraphSeries<DataPoint> series3;
    private LineGraphSeries<DataPoint> Peaks;

    DateFormat df = new SimpleDateFormat("EEE d MMM yyyy");
    String Ondergrond, Meting, Date, Path, DirectoryName, DirectoryPath, Filename, ZscoreFilename, PeakFilename;

    int intAmplitude =0;
    int intTime = 0;
    long longTime = 0;
    long startTime = 0;
    List<Integer> listAmplitude = new ArrayList<Integer>();
    List<Integer> listTime = new ArrayList<Integer>();

    List<Integer> listAmplitudeTest = new ArrayList<Integer>();

    int PeakAmplitude1 = 0;
    int PeakAmplitude2 = 0;
    int PeakTime1 = 0;
    int PeakTime2 = 0;

    Handler handler= new Handler();
    boolean SoundRecording=false;

    public Football1Test() {
    }

    public void MediaRecorderReady(){
        mediaRecorder=new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.UNPROCESSED);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mediaRecorder.setOutputFile(AudioSavePathInDevice);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_football1_test);

        buttonTest = findViewById(R.id.buttonTest);

        buttonStart = findViewById(R.id.buttonStart);
        buttonStop = findViewById(R.id.buttonStop);
        buttonUpdate = findViewById(R.id.buttonUpdate);

        buttonPlayLastRecordAudio = findViewById(R.id.buttonPlay);
        buttonStopPlayingRecording = findViewById(R.id.buttonStopPlay);

        // Spinner Element connecting with layout
        OndergrondKeuze = findViewById(R.id.spinnerOndergrond);
        MetingKeuze = findViewById(R.id.spinnerMeting);
        // Spinner click listener
        OndergrondKeuze.setOnItemSelectedListener(this);
        MetingKeuze.setOnItemSelectedListener(this);
        /*
        //Get array out of resources
        String[] OndergrondArray= getResources().getStringArray(R.array.OndergrondArray);
        String[] MetingArray= getResources().getStringArray(R.array.MetingArray);
        String OndergrondPrompt=getResources().getString(R.string.OndergrondPrompt);
        //Make adapter for spinner
        ArrayAdapter OndergrondAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,OndergrondArray);
        ArrayAdapter MetingAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,MetingArray);
        //dropdownview
        OndergrondAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        MetingAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Set adapter of spinner
        OndergrondKeuze.setAdapter(OndergrondAdapter);
        MetingKeuze.setAdapter(MetingAdapter);
        //set prompt of spinner
        OndergrondKeuze.setPrompt(OndergrondPrompt);
        */

        final TextView textTime = findViewById(R.id.TextTime);
        TextView textAmplitude = findViewById(R.id.TextAmplitude);

        final TextView TextTime1 = findViewById(R.id.TextTime1);
        final TextView TextTime2 = findViewById(R.id.TextTime2);

        final TextView TextHeight1 = findViewById(R.id.TextHeight1);
        final TextView TextHeight2 = findViewById(R.id.TextHeight2);

        //initialize buttons
        buttonStart.setEnabled(true);
        buttonStop.setEnabled(false);
        buttonUpdate.setEnabled(false);
        buttonTest.setEnabled(false);
        buttonPlayLastRecordAudio.setEnabled(false);
        buttonStopPlayingRecording.setEnabled(false);

        final GraphView graph = findViewById(R.id.graph);

        graph.removeAllSeries();
        // customize a little bit viewport
        Viewport viewport = graph.getViewport();
        viewport.setYAxisBoundsManual(true);
        viewport.setXAxisBoundsManual(true);
        viewport.setMinY(0);
        viewport.setMaxY(120);
        viewport.setMinX(0);
        viewport.setMaxX(4000);
        viewport.setScrollable(true);
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(false); // enables vertical zooming and scrolling

        final Runnable updateSoundLevel = new Runnable() {
            @Override
            public void run() {
                if (SoundRecording==true){
                    handler.post(this);

                    intAmplitude = mediaRecorder.getMaxAmplitude();
                    longTime= System.currentTimeMillis()-startTime;
                    int intTime=(int) longTime;
                    if (intAmplitude!=0)
                    {
                        double test =20*log10(intAmplitude);
                        int test2=(int) test;
                        listAmplitude.add(test2);
                        listTime.add(intTime);
                        addEntry(intTime,test2);
                    }

                    //textTime.setText("Amplitude:  "+String.valueOf(Amplitude));
                    //textAmplitude.setText("Time:  "+String.valueOf(intTime)+"ms");

                }
                else
                {
                    //intTime=0;
                    //Amplitude =0;
                }
            }
        };

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //initialize buttons
                buttonStart.setEnabled(false);
                buttonStop.setEnabled(true);
                buttonUpdate.setEnabled(false);
                buttonTest.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(false);

                SoundRecording=true;

                graph.removeAllSeries();
                listAmplitude.clear();
                listTime.clear();
                PeakTime1=0;
                PeakTime2=0;
                PeakAmplitude1=0;
                PeakAmplitude2=0;

                // data pointgraph
                series1 = new PointsGraphSeries<DataPoint>();
                graph.addSeries(series1);
                series1.setShape(PointsGraphSeries.Shape.POINT);
                series1.setSize(4);
                series1.setColor(Color.RED);
                PointsGraphSeries<DataPoint> series1 = new PointsGraphSeries<>(new DataPoint[] {
                        new DataPoint(0, 0)
                });

                // data linegraph
                series2 = new LineGraphSeries<DataPoint>();
                graph.addSeries(series2);
                series2.setThickness(2);
                series2.setColor(Color.BLUE);
                LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                        new DataPoint(0, 0)
                });

                if(checkPermission())
                {
                    String FileName = getfilename();
                    AudioSavePathInDevice = FileName;
                    MediaRecorderReady();
                    try {
                        mediaRecorder.setAudioSamplingRate(96000);
                        mediaRecorder.setAudioEncodingBitRate(384000);
                        mediaRecorder.prepare();
                        mediaRecorder.start();
                    } catch (IllegalStateException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    startTime= System.currentTimeMillis();

                    Toast.makeText(Football1Test.this, "Recording started",Toast.LENGTH_LONG).show();
                }
                else
                {
                    requestPermission();
                }
                handler.post(updateSoundLevel);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //initialize buttons
                buttonStart.setEnabled(true);
                buttonStop.setEnabled(false);
                buttonUpdate.setEnabled(true);
                buttonTest.setEnabled(true);
                buttonPlayLastRecordAudio.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);

                mediaRecorder.stop();
                intTime=0;
                intAmplitude =0;

                Toast.makeText(Football1Test.this, "Stop Recording", Toast.LENGTH_LONG).show();
                SoundRecording=false;

            }
        });

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // data linegraph peaks
                for (int i = 0; i < listAmplitude.size(); i++) {
                    int ampl = listAmplitude.get(i);
                    if(ampl > PeakAmplitude1){
                        PeakAmplitude1 = ampl;
                        PeakTime1 = listTime.get(i);
                    }else if(ampl <= PeakAmplitude1 && ampl > PeakAmplitude2){
                        PeakAmplitude2 = ampl;
                        PeakTime2 = listTime.get(i);
                    }
                }
                int Peaktime=PeakTime2-PeakTime1;

                TextTime1.setText("T2-T1:  "+String.valueOf(Peaktime)+" ms");
                int height = (int) (1.23*((Peaktime-25)*(Peaktime-25))/10000);
                TextHeight1.setText("Height:  "+String.valueOf(height)+" cm");

                try
                {
                    getfilename();
                    PrintWriter Peakwriter = new PrintWriter(PeakFilename, "UTF-8");
                    Peakwriter.println("T2-T1:  "+String.valueOf(Peaktime)+" ms");
                    Peakwriter.println("Height:  "+String.valueOf(height)+" cm");
                    Peakwriter.close();
                    //display file saved message
                    //Toast.makeText(getBaseContext(), "File saved successfully!",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                LineGraphSeries<DataPoint> Peaks = new LineGraphSeries<>(new DataPoint[] {
                        new DataPoint(0, 0),
                        new DataPoint(PeakTime1, 0),
                        new DataPoint(PeakTime1, 20000),
                        new DataPoint(PeakTime1, 0),
                        new DataPoint(PeakTime2, 0),
                        new DataPoint(PeakTime2, 20000),
                        new DataPoint(PeakTime2, 0),
                        new DataPoint(PeakTime2+10000, 0)
                });
                graph.addSeries(Peaks);
                Peaks.setThickness(8);
                Peaks.setColor(Color.rgb(0, 151, 68));
            }
        });


        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //graph.removeAllSeries();
                PeakTime1=0;
                PeakTime2=0;
                PeakAmplitude1=0;
                PeakAmplitude2=0;
                int[] AmplitudeArray1 = new int[listAmplitude.size()];
                int[] SmoothedZScoreArray;

                for (int i = 0; i < listAmplitude.size(); i++)
                {
                    AmplitudeArray1[i]=listAmplitude.get(i);
                }

                SmoothedZScoreArray=smoothedZScore(AmplitudeArray1,10,20,4);

                for (int i = 0; i < SmoothedZScoreArray.length; i++)
                {
                    if (i<30)
                    {
                        listAmplitudeTest.add(0);
                    }
                    else
                    {
                        listAmplitudeTest.add(SmoothedZScoreArray[i]);
                    }
                }

                // data linegraph
                series2 = new LineGraphSeries<DataPoint>();
                graph.addSeries(series2);
                series2.setThickness(2);
                series2.setColor(Color.BLUE);
                LineGraphSeries<DataPoint> series2 = new LineGraphSeries<>(new DataPoint[] {
                        new DataPoint(0, 0)
                });

                //int lag=5;
                //int sumAmplitude=0;
                for (int i = 0; i < listAmplitudeTest.size(); i++)
                {
                    series2.appendData(new DataPoint(listTime.get(i), listAmplitudeTest.get(i)), true, 500000);
                }

                graph.addSeries(series2);

                int[] peaks=new int[10];
                int j=0;
                for (int i = 1; i < listAmplitudeTest.size(); i++)
                {
                    if (listAmplitudeTest.get(i - 1) < listAmplitudeTest.get(i)) {
                        peaks[j] = listTime.get(i);
                        j++;
                    }
                }
                int Peaktime=peaks[1]-peaks[0];

                TextTime2.setText("T2-T1:  "+String.valueOf(Peaktime)+" ms");
                int height = (int) (1.23*((Peaktime-25)*(Peaktime-25))/10000);
                TextHeight2.setText("Height:  "+String.valueOf(height)+" cm");

                try
                {
                    getfilename();
                    PrintWriter Zscorewriter = new PrintWriter(ZscoreFilename, "UTF-8");
                    Zscorewriter.println("T2-T1:  "+String.valueOf(Peaktime)+" ms");
                    Zscorewriter.println("Height:  "+String.valueOf(height)+" cm");
                    Zscorewriter.close();
                    //display file saved message
                    //Toast.makeText(getBaseContext(), "File saved successfully!",Toast.LENGTH_SHORT).show();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });

        buttonPlayLastRecordAudio.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) throws IllegalArgumentException,
                    SecurityException, IllegalStateException {

                buttonStop.setEnabled(false);
                buttonStart.setEnabled(false);
                buttonStopPlayingRecording.setEnabled(true);

                mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(AudioSavePathInDevice);
                    mediaPlayer.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mediaPlayer.start();
                Toast.makeText(Football1Test.this, "Recording Playing", Toast.LENGTH_LONG).show();

            }
        });
        buttonStopPlayingRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buttonStop.setEnabled(false);
                buttonStart.setEnabled(true);
                buttonStopPlayingRecording.setEnabled(false);
                buttonPlayLastRecordAudio.setEnabled(true);

                if(mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                    MediaRecorderReady();
                }
            }
        });

    }

    public String getfilename() {
        //find all variables to make the filename
        Path =Environment.getExternalStorageDirectory().getAbsolutePath();
        Date = df.format(Calendar.getInstance().getTime());
        String[] OndergrondArray= getResources().getStringArray(R.array.OndergrondArray);
        String[] MetingArray= getResources().getStringArray(R.array.MetingArray);
        Ondergrond=OndergrondArray[OndergrondKeuze.getSelectedItemPosition()];
        Meting=MetingArray[MetingKeuze.getSelectedItemPosition()];
        DirectoryName="BBT";
        DirectoryPath=Path + "/"+DirectoryName;
        Filename= Path + "/"+DirectoryName+"/" + Date + Ondergrond + Meting+ ".mp4";
        ZscoreFilename= Path + "/"+DirectoryName+"/" + Date + Ondergrond + Meting+ "Zscore.txt";
        PeakFilename= Path + "/"+DirectoryName+"/" + Date + Ondergrond + Meting+ "Peak.txt";
        // create a File object for the parent directory
        File audioDirectory = new File(DirectoryPath);
        // have the object build the directory structure, if needed.
        audioDirectory.mkdirs();

        return (Filename);
    }

    private void addEntry(int a, int b) {
        // here, we choose to display max 100 points on the viewport and we scroll to end
        series1.appendData(new DataPoint(a, b), true, 500000);
        series2.appendData(new DataPoint(a, b), true, 500000);
    }

    public String CreateRandomAudioFileName(int string){
        StringBuilder stringBuilder = new StringBuilder( string );
        int i = 0 ;
        while(i < string )
        {
            stringBuilder.append(RandomAudioFileName.charAt(random.nextInt(RandomAudioFileName.length())));
            i++ ;
        }
        return stringBuilder.toString();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(Football1Test.this, new String[]{WRITE_EXTERNAL_STORAGE, RECORD_AUDIO}, RequestPermissionCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RequestPermissionCode:
                if (grantResults.length> 0) {
                    boolean StoragePermission = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean RecordPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;

                    if (StoragePermission && RecordPermission) {
                        Toast.makeText(Football1Test.this, "Permission Granted",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(Football1Test.this,"Permission Denied",Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }

    public boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(getApplicationContext(),WRITE_EXTERNAL_STORAGE);
        int result1 = ContextCompat.checkSelfPermission(getApplicationContext(),RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED;
    }

    public int[] smoothedZScore(int[] y,int lag, int threshold, int influence)
    {
        lag=10;
        threshold =20;
        influence =1;
        int[] filteredY=new int[y.length], signals=new int[y.length], avgFiltered=new int[y.length], stdFiltered=new int[y.length], partialY;

        //initialization of the arrays
        for (int i=0; i<lag; i++)
        {
            filteredY[i]=y[i];
            avgFiltered[i]=0;
            stdFiltered[i]=0;
            signals[i]=0;
        }

        for (int i=lag; i<y.length; i++)
        {
            partialY=Arrays.copyOfRange(y, i-lag, i);
            avgFiltered[i]=FuncMean(partialY);
            stdFiltered[i]=FuncStd(partialY);
            //int testint=abs(y[i]-avgFiltered[i-1]);
            //int testint=threshold*stdFiltered[i-1];
            if (abs(y[i]-avgFiltered[i-1])>threshold*stdFiltered[i-1])
            {
                if (y[i]>1.2*avgFiltered[i-1])
                {
                    signals[i]=50;
                }
                else
                {
                    signals[i]=0;
                }
                //int testint =(1/influence)*y[i];
                //int testint2 =(1-(1/influence))*filteredY[i-1];
                filteredY[i]=influence*y[i]+(1-influence)*filteredY[i-1];
            }
            else
            {
                signals[i]=0;
                filteredY[i]=y[i];
            }
        }

        return signals;
    }

    public  int FuncMean(int[] signal)
    {
        int mean=0;
        int sum=0;

        for (int i=0; i<signal.length; i++)
        {
            sum=sum+signal[i];
        }
        mean= Math.round(sum/signal.length);

        return mean;
    }

    public int FuncStd(int[] signal)
    {
        int mean, std;
        int sumStd=0;
        int sumMean=0;

        mean=FuncMean(signal);

        for (int i=0; i<signal.length; i++)
        {
            sumStd=sumStd+(signal[i]-mean)^2;
        }
        std=sumStd/signal.length;

        return std;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}


