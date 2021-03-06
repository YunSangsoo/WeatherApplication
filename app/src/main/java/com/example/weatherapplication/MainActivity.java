package com.example.weatherapplication;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    GpsService gpsService = null;

    public LatXLonY gridgps;

    LocalBroadcastManager mLocalBroadcastManager;
    BroadcastReceiver mReceiver;

    //지역 정보 저장을 위한 변수 선언
    String adminArea;
    String subLocality;
    List<Address> addresses = null;

    MainBackView mainframe;
    TextView adminAreaView,subLocalityView;

    TextView nowTemp,nowRain,nowMax,nowMin,nowSky;
    ImageView nowIcon;
    ImageView settingBtn;
    Button airPollutionBtn;


    float initialX=0;
    float pointCurX=0;
    Boolean scrollswt=false;
    int SET_X=300;

    Context mainC;

    LinearLayout mainFrame;

    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    private boolean isPermission = false;

    final String[] cityList = {"서울특별시","인천광역시","부산광역시","울산광역시","대구광역시","광주광역시","대전광역시","경기도","경상남도","경상북도","전라남도","전라북도","충청남도","충청북도","강원도"};
    final String[] cityCode = {"11B10101","11B20201","11H20201","11H20101","11H10701","11F20503","11C20401","11B20701","11H20301","11H10501","11F20503","11F10201","11C20404","11C10301","11D20501"};
    final String[] weatherCode = {"11B00000","11B00000","11H20000","11H20000","11H10000","11F20000","11C20000","11B00000","11H20000","11H10000","11F20000","11F10000","11C20000","11C10000","11D20000"};

    private ServiceConnection mConnection = new ServiceConnection(){
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {

            gpsService=new GpsService();
            gpsService=((GpsService.LocalBinder)iBinder).getCountService();

            double latitude = gpsService.getLocation().getLatitude();
            double longitude = gpsService.getLocation().getLongitude();

            Geocoder coder = new Geocoder(getApplicationContext(), Locale.KOREA);
            try {
                addresses = coder.getFromLocation(latitude, longitude, 5); // 첫번째 파라미터로 위도, 두번째로 경도, 세번째 파라미터로 리턴할 Address객체의 개수
                adminArea = addresses.get(0).getAdminArea();
                subLocality = addresses.get(0).getLocality();
                for(int i=0;i<7;i++)
                {
                    if(adminArea.equals(cityList[i]))
                    {
                        subLocality=addresses.get(0).getSubLocality();
                        break;
                    }
                }
                adminAreaView.setText(adminArea);
                subLocalityView.setText((subLocality));

                gridgps=new LatXLonY();

                gridgps = gridgps.convertGrid(latitude,longitude);


                NetworkTask networkTask = new NetworkTask(null);
                networkTask.execute();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callPermission();
        if(isPermission)
        {
            adminAreaView=(TextView)findViewById(R.id.cityName);
            subLocalityView=(TextView)findViewById(R.id.localityName);

            nowTemp=(TextView)findViewById(R.id.tempnowin);
            nowRain=(TextView)findViewById(R.id.rainperin);
            nowMax=(TextView)findViewById(R.id.maxtempin);
            nowMin=(TextView)findViewById(R.id.mintempin);
            nowSky=(TextView)findViewById(R.id.statenow);
            nowIcon=(ImageView)findViewById(R.id.stateicon);
            settingBtn=(ImageView)findViewById(R.id.setting);
            mainframe=(MainBackView)findViewById(R.id.mainFrame);
            mainC=this;


            HorizontalScrollView hs = (HorizontalScrollView)findViewById(R.id.scroll);
            ScrollView sv = (ScrollView)findViewById(R.id.scroll2);

            hs.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 1) 터치 다운 위치의 Y 위치를 기억해 둔다.
                            initialX = motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            pointCurX = motionEvent.getX();
                            scrollswt=true;
                    }
                    return false;
                }
            });
            sv.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 1) 터치 다운 위치의 Y 위치를 기억해 둔다.
                            initialX = motionEvent.getX();
                            break;
                        case MotionEvent.ACTION_UP:
                            pointCurX = motionEvent.getX();
                            scrollswt=true;
                    }
                    return false;
                }
            });

            mainframe.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent){
                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_DOWN:
                            // 1) 터치 다운 위치의 Y 위치를 기억해 둔다.
                            initialX = motionEvent.getRawX();
                            return true;
                        case MotionEvent.ACTION_UP:
                            pointCurX = motionEvent.getRawX();
                            Intent intent;
                            //터치 다운 X 위치에서 300픽셀을 초과 이동되면 애니매이션 실행
                            if(scrollswt)
                                SET_X=800;
                            else
                                SET_X=400;
                            scrollswt=false;
                            if(pointCurX- initialX> SET_X){
                                intent = new Intent(getApplicationContext(), ClothesRecommendationActivity.class);
                                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                                finish();
                                return true;
                            }
                            else if(pointCurX- initialX< 0-SET_X){
                                intent =new Intent(getApplicationContext(), RealTimeWeather.class);
                                startActivity(intent);
                                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                                finish();
                                return true;
                            }
                    }
                    return false;
                }
            });

            settingBtn.setOnTouchListener(new View.OnTouchListener(){
                Intent intent;
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    initialX = motionEvent.getRawX();
                    pointCurX = motionEvent.getRawX();
                    System.out.println(motionEvent.getAction());
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP||motionEvent.getAction()==MotionEvent.ACTION_CANCEL) {
                        intent = new Intent(getApplicationContext(), SettingsActivity.class);
                        startActivity(intent);
                    }
                    return false;
                }
            });

            airPollutionBtn=(Button)findViewById(R.id.airpollution);
            airPollutionBtn.setOnTouchListener(new View.OnTouchListener(){
                Intent intent;
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    initialX = motionEvent.getRawX();
                    pointCurX = motionEvent.getRawX();
                    if(motionEvent.getAction()==MotionEvent.ACTION_UP||motionEvent.getAction()==MotionEvent.ACTION_CANCEL) {
                        intent = new Intent(getApplicationContext(), AirPollutionDetailInfoActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                    }
                    return false;
                }
            });

            /*GPS 서비스 시작*/
            Intent intent = new Intent(getApplicationContext(), GpsService.class);
            startService(intent);

            Intent bIntent = new Intent();
            ComponentName componentName = new ComponentName("com.example.weatherapplication","com.example.weatherapplication.GpsService");
            bIntent.setComponent(componentName);
            bindService(bIntent,mConnection,0);

            mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
            mReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    double latitude = intent.getDoubleExtra(GpsService.EXTRA_LATITUDE,0);
                    double longitude = intent.getDoubleExtra(GpsService.EXTRA_LONGITUDE,0);

                    Geocoder coder = new Geocoder(getApplicationContext(), Locale.KOREA);
                    try {
                        addresses = coder.getFromLocation(latitude, longitude, 5); // 첫번째 파라미터로 위도, 두번째로 경도, 세번째 파라미터로 리턴할 Address객체의 개수
                        adminArea = addresses.get(0).getAdminArea();
                        subLocality = addresses.get(0).getLocality();
                        for(int i=0;i<7;i++)
                        {
                            if(adminArea.equals(cityList[i]))
                            {
                                subLocality=addresses.get(0).getSubLocality();
                                break;
                            }
                        }
                        adminAreaView.setText(adminArea);
                        subLocalityView.setText((subLocality));

                        gridgps=new LatXLonY();

                        gridgps = gridgps.convertGrid(latitude,longitude);

                        NetworkTask networkTask = new NetworkTask(null);
                        networkTask.execute();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        }
    }


    @Override
    public void onStart(){
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocalBroadcastManager.registerReceiver(mReceiver, new IntentFilter(GpsService.ACTION_LOCATION_BROADCAST));

    }

    @Override
    protected void onPause() {
        super.onPause();
        //mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }

    @Override
    protected void onDestroy() {
        unbindService(mConnection);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            isAccessFineLocation = true;
            isPermission=true;
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
        /*if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }*/
    }

    private void callPermission() {
// Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        }
        /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);

        } */else {
            isPermission = true;
        }
    }

    public class NetworkTask extends AsyncTask<Void,Void,String> {
        String url;
        WeatherUrlParser parsing;
        WeatherWeekUrlParser weekparsing;
        String citycode;
        String weathercode;
        ProgressDialog asyncDialog;

        NetworkTask(String url){
            this.url = url;
        }
        @Override
        protected void onPreExecute(){
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(Void... params){
            for(int i=0;i<cityList.length;i++)
            {
                if(adminArea.equals(cityList[i]))
                {
                    citycode=cityCode[i];
                    weathercode=weatherCode[i];
                    break;
                }
            }
            parsing = new WeatherUrlParser(gridgps.x,gridgps.y);
            weekparsing=new WeatherWeekUrlParser(citycode,weathercode);
            return null;
        }
        @Override
        protected void onPostExecute(String result){
            changeNow(parsing.POP,parsing.SKY,parsing.PTY,parsing.T3H,parsing.TMX,parsing.TMN);
            changeAfter(parsing.POPL,parsing.T3HL,parsing.SKYL,parsing.timeList,parsing.PTYL);
            changeWeek(parsing,weekparsing);
            super.onPostExecute(result);

        }


    }
    public void changeNow(String POP,String SKY,String PTY,String T3H,String TMX,String TMN)
    {
        nowTemp.setText(T3H+"℃");
        nowRain.setText(POP+"%");
        nowMax.setText(Math.round(Double.parseDouble(TMX))+"℃");
        nowMin.setText(Math.round(Double.parseDouble(TMN))+"℃");
        int PTYcheck=0;
        if(!PTY.equals(""))
            PTYcheck=Integer.parseInt(PTY);
        int SKYcheck=0;
        if(!SKY.equals(""))
            SKYcheck=Integer.parseInt(SKY);

        if(PTYcheck==0)
        {
            switch(SKYcheck){
                case 1 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sun));
                    nowSky.setText("맑음");
                }break;
                case 3 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clouds_sun));
                    nowSky.setText("구름많음");
                }break;
                case 4 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cloud));
                    nowSky.setText("흐림");
                }break;
            }
        }
        else
        {
            switch(PTYcheck){
                case 1 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rain));
                    nowSky.setText("비");
                }break;
                case 2 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rain));
                    nowSky.setText("비/눈");
                }break;
                case 3 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.snow));
                    nowSky.setText("눈");
                }break;
                case 4 : {
                    nowIcon.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rain));
                    nowSky.setText("소나기");
                }break;
            }
        }
    }
    public void changeAfter(List<String> POP, List<String> L3H,List<String> SKY,List<String> timeList,List<String> PTY)
    {
        List<TextView> tempafterView = new ArrayList<TextView>();
        List<TextView> rainafterView = new ArrayList<TextView>();
        List<TextView> timeafterView = new ArrayList<TextView>();
        List<ImageView> imageafterView = new ArrayList<ImageView>();
        TextView inputT;
        inputT=(TextView)findViewById(R.id.tempafter3);
        tempafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.tempafter6);
        tempafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.tempafter9);
        tempafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.tempafter12);
        tempafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.tempafter15);
        tempafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.tempafter18);
        tempafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.tempafter21);
        tempafterView.add(inputT);
        for(int i=0;i<tempafterView.size();i++){
            inputT=tempafterView.get(i);
            inputT.setText(L3H.get(i)+"℃");
        }

        inputT=(TextView)findViewById(R.id.rainafter3);
        rainafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.rainafter6);
        rainafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.rainafter9);
        rainafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.rainafter12);
        rainafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.rainafter15);
        rainafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.rainafter18);
        rainafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.rainafter21);
        rainafterView.add(inputT);
        for(int i=0;i<rainafterView.size();i++){
            inputT=rainafterView.get(i);
            inputT.setText(POP.get(i)+"%");
        }

        inputT=(TextView)findViewById(R.id.timeafter3);
        timeafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.timeafter6);
        timeafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.timeafter9);
        timeafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.timeafter12);
        timeafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.timeafter15);
        timeafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.timeafter18);
        timeafterView.add(inputT);
        inputT=(TextView)findViewById(R.id.timeafter21);
        timeafterView.add(inputT);
        for(int i=0;i<rainafterView.size();i++){
            inputT=timeafterView.get(i);
            String timeS=timeList.get(i);
            int timeD=(Integer.parseInt(timeS))/100;
            inputT.setText(timeD+"시");
        }

        ImageView inputI;

        inputI=(ImageView)findViewById(R.id.stateicon3);
        imageafterView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon6);
        imageafterView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon9);
        imageafterView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon12);
        imageafterView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon15);
        imageafterView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon18);
        imageafterView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon21);
        imageafterView.add(inputI);


        for(int i=0;i<rainafterView.size();i++){
            inputI=imageafterView.get(i);
            int SKYcheck = Integer.parseInt(SKY.get(i));
            int PTYcheck = Integer.parseInt(PTY.get(i));
            if(PTY.get(i).equals("0"))
            {
                switch(SKYcheck){
                    case 1 : {
                        inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sun));
                    }break;
                    case 3 : {
                        inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clouds_sun));
                    }break;
                    case 4 : {
                        inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cloud));
                    }break;
                }
            }
            else
            {
                if(PTYcheck==3)
                    inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.snow));
                else
                    inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.rain));
            }
        }
    }
    public void changeWeek(WeatherUrlParser parsing,WeatherWeekUrlParser weekparsing){
        List<TextView> mintempView = new ArrayList<TextView>();
        List<TextView> maxtempView = new ArrayList<TextView>();
        List<TextView> rainweekView = new ArrayList<TextView>();
        List<TextView> weekdayView = new ArrayList<TextView>();
        List<ImageView> imageweekView = new ArrayList<ImageView>();
        TextView inputT;
        inputT=(TextView)findViewById(R.id.week1day);
        weekdayView.add(inputT);
        inputT=(TextView)findViewById(R.id.week2day);
        weekdayView.add(inputT);
        inputT=(TextView)findViewById(R.id.week3day);
        weekdayView.add(inputT);
        inputT=(TextView)findViewById(R.id.week4day);
        weekdayView.add(inputT);
        inputT=(TextView)findViewById(R.id.week5day);
        weekdayView.add(inputT);
        inputT=(TextView)findViewById(R.id.week6day);
        weekdayView.add(inputT);
        for(int i=0;i<6;i++)
        {
            inputT=weekdayView.get(i);
            inputT.setText(weekparsing.dayNum.get(i)+"요일");
        }

        inputT=(TextView)findViewById(R.id.mintemp1day);
        inputT.setText(Math.round(Double.parseDouble(parsing.taMIN1day))+"℃");
        mintempView.add(inputT);
        inputT=(TextView)findViewById(R.id.mintemp2day);
        inputT.setText(Math.round(Double.parseDouble(parsing.taMIN2day))+"℃");
        mintempView.add(inputT);
        inputT=(TextView)findViewById(R.id.mintemp3day);
        mintempView.add(inputT);
        inputT=(TextView)findViewById(R.id.mintemp4day);
        mintempView.add(inputT);
        inputT=(TextView)findViewById(R.id.mintemp5day);
        mintempView.add(inputT);
        inputT=(TextView)findViewById(R.id.mintemp6day);
        mintempView.add(inputT);
        for(int i=0;i<4;i++)
        {
            inputT=mintempView.get(i+2);
            int inputnum=Integer.parseInt(weekparsing.taMIN.get(i));
            inputT.setText(inputnum+"℃");
        }

        inputT=(TextView)findViewById(R.id.maxtemp1day);
        inputT.setText(Math.round(Double.parseDouble(parsing.taMAX1day))+"℃");
        maxtempView.add(inputT);
        inputT=(TextView)findViewById(R.id.maxtemp2day);
        inputT.setText(Math.round(Double.parseDouble(parsing.taMAX2day))+"℃");
        maxtempView.add(inputT);
        inputT=(TextView)findViewById(R.id.maxtemp3day);
        maxtempView.add(inputT);
        inputT=(TextView)findViewById(R.id.maxtemp4day);
        maxtempView.add(inputT);
        inputT=(TextView)findViewById(R.id.maxtemp5day);
        maxtempView.add(inputT);
        inputT=(TextView)findViewById(R.id.maxtemp6day);
        maxtempView.add(inputT);
        for(int i=0;i<4;i++)
        {
            inputT=maxtempView.get(i+2);
            int inputnum=Integer.parseInt(weekparsing.taMAX.get(i));
            inputT.setText(inputnum+"℃");
        }


        inputT=(TextView)findViewById(R.id.rain1day);
        inputT.setText(parsing.rnSt1day+"%");
        rainweekView.add(inputT);
        inputT=(TextView)findViewById(R.id.rain2day);
        inputT.setText(parsing.rnSt2day+"%");
        rainweekView.add(inputT);
        inputT=(TextView)findViewById(R.id.rain3day);
        rainweekView.add(inputT);
        inputT=(TextView)findViewById(R.id.rain4day);
        rainweekView.add(inputT);
        inputT=(TextView)findViewById(R.id.rain5day);
        rainweekView.add(inputT);
        inputT=(TextView)findViewById(R.id.rain6day);
        rainweekView.add(inputT);
        for(int i=0;i<4;i++)
        {
            inputT=rainweekView.get(i+2);
            inputT.setText(weekparsing.rnSt.get(i)+"%");
        }

        ImageView inputI;

        inputI=(ImageView)findViewById(R.id.stateicon1day);
        imageweekView.add(inputI);
        int SKYcheck = Integer.parseInt(parsing.sky1day);
        switch(SKYcheck){
            case 1 : {
                inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sun));
            }break;
            case 3 : {
                inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clouds_sun));
            }break;
            case 4 : {
                inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cloud));
            }break;
        }
        inputI=(ImageView)findViewById(R.id.stateicon2day);
        SKYcheck = Integer.parseInt(parsing.sky2day);
        switch(SKYcheck){
            case 1 : {
                inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sun));
            }break;
            case 3 : {
                inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clouds_sun));
            }break;
            case 4 : {
                inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cloud));
            }break;
        }
        imageweekView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon3day);
        imageweekView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon4day);
        imageweekView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon5day);
        imageweekView.add(inputI);
        inputI=(ImageView)findViewById(R.id.stateicon6day);
        imageweekView.add(inputI);


        for(int i=0;i<4;i++){
            inputI=imageweekView.get(i+2);
            switch(weekparsing.wfkor.get(i)){
                case "맑음" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sun));
                }break;
                case "구름많음" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.clouds_sun));
                }break;
                case "흐림" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.cloud));
                }break;
                case "비" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.rain));
                }break;
                case "눈" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.snow));
                }break;
                case "비/눈" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.rain));
                }break;
                case "눈/비" : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this,R.drawable.snow));
                }break;
                default : {
                    inputI.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.sun));
                }break;
            }
        }

    }


}
