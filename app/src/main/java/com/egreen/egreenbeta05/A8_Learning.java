package com.egreen.egreenbeta05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AbsoluteLayout;
import android.widget.LinearLayout;

import com.egreen.egreenbeta05.Data.StudyInfo;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;

public class A8_Learning extends AppCompatActivity implements NetworkAsyncTasker.AsyncResponse {
    private static final String TAG = A8_Learning.class.getSimpleName();

    WebView wv;

    int jucha = 0, chasi = 0;
    String contentsPage;
    String eCid = "", fileRoot = "";
    String watchedTime = "", fullTime = "";
    private boolean enable;

    Timer jindoTimer = new Timer();
    TimerTask jindoTimerTask = null;

    int viewTime, maxTime, minutes, second;
    ShowLoading loading;

    //AsyncTask 결과값을 받기위한 변수
    NetworkAsyncTasker asyncTask;
    NetworkStateCheck netCheck;
    Handler loginOverHelper;

    //A7_StudyCenter >> A7_ClassRoom 으로 전달 받은 intent 정보
    StudyInfo si;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a8_learning);

        if (Build.VERSION.SDK_INT <= 23) {
            Log.i(TAG, "This is API Level 23");
            getWindow().getAttributes().width = WindowManager.LayoutParams.MATCH_PARENT;
            getWindow().getAttributes().height = WindowManager.LayoutParams.MATCH_PARENT;
        }

        wv = findViewById(R.id.wv);

        //웹뷰 설정
        WebSettings wvSet = wv.getSettings();
        wvSet.setJavaScriptEnabled(true);       //자바스크립트 사용 가능하다.
        wvSet.setUseWideViewPort(true);         //뷰포트 사용 가능하다.
        wvSet.setLoadWithOverviewMode(true);    //웹뷰 화면에 맞게 출력한다.
        wvSet.setAllowContentAccess(false);     //컨텐츠 프로바이더(CP)를 이용할때는 true로 설정, 컨텐츠 프로바이더가 제공하는 컨텐츠를 읽을 수 있다.
        wvSet.setBuiltInZoomControls(false);    //줌 기능 설정이다.
        wvSet.setDisplayZoomControls(false);    //내장 줌 기능을 사용할때, 웹뷰가 화면에 줌 컨트롤러를 표시할지 설정한다.
        wvSet.setSupportZoom(false);            //줌 컨트롤과 제스처를 사용하여 확대 여부를 결정한다.
        wvSet.setCacheMode(WebSettings.LOAD_NO_CACHE);  //캐시를 사용할지 결정한다. (배포할때 주석처리 해야함)
        wv.addJavascriptInterface(new JSCall(), "jscall");      //웹 -> 앱 함수를 호출한다.
        wv.setWebViewClient(new WebBrowserClient());
        wv.setWebChromeClient(new WebChromeClient());

//        a6_studyRoom.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);       //자동 줄바꿈 방지, 안드로이드 3.x 이하에서는 안해도 된다.

        //A7_StudyCenter에서 전달한 학습정보를 저장
        si = (StudyInfo) getIntent().getSerializableExtra("studyInfo");

        /*
         * A6_MyClass에서 전달한 과목 ID 를 가져온다.
         */
        Intent intent = getIntent();
        try {
            jucha = intent.getIntExtra("JUCHA", 0);     //주차
            chasi = intent.getIntExtra("CHASI", 0);     //차시
            eCid = intent.getStringExtra("CID");            //cid
            watchedTime = intent.getStringExtra("WATCHED_TIME");    //학습 누적시간
            fullTime = intent.getStringExtra("FULL_TIME");      //컨텐츠 전체시간
            fileRoot = intent.getStringExtra("FILE_ROOT");      //컨텐츠 파일 경로 ex)01/01.html
            enable = intent.getBooleanExtra("ENABLE", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //위 값들을 잘 가져왔는지 그냥.. 체크한다. 잘 가져왔겠지만.
        checkGetIntent();

        //강의를 열었을때, 마지막으로 본 페이지로 이동한다.(예정)
//        SharedPreferences savedContents = getSharedPreferences("CONTENTS", MODE_PRIVATE);
//        SharedPreferences.Editor editor = savedContents.edit();
//        editor.putString("D_NAME", directoryName);
//        editor.putInt("CHASI", chasi);
//        editor.commit();

        //학습시간을 초기화한다.
        viewTime = Integer.parseInt(watchedTime);
        maxTime = Integer.parseInt(fullTime);

        int _viewTime = 0;
        if (viewTime < maxTime) {
            //학습한 시간이 총 학습시간보다 작을때,
            //남은 시간에서부터 - 하기 위해,
            //총 학습시간에서 학습누적시간을 - 해준다.
            _viewTime = maxTime;
            _viewTime -= viewTime;
        }
        else if (viewTime >= maxTime) {
            enable = false;
        }
        else {
            //총 학습시간보다 크거나 같을때,
            _viewTime = maxTime;
        }

        minutes = _viewTime;
        second = 60;

        Log.i(TAG, "onCreate :: " + viewTime + ", " + maxTime + ", " + minutes + ", " + second);

//        Handler handler = new Handler();
//        handler.post(new Runnable() {
//            @Override
//            public void run() {

                //마지막으로 본 화면을 불러온다.
//                SharedPreferences savedPage = getSharedPreferences("CONTENTS", MODE_PRIVATE);
//                String page = savedPage.getString("SAVED_PAGE", "");
//
//                String[] splitRoot, splitRoot2;

                //저장된 페이지가 있는지 확인, 마지막 페이지인지 확인(마지막 페이지는 09.html)
//                if (!page.equals("")) {
//                    splitRoot = page.split("/");
//                    splitRoot2 = splitRoot[2].split(".");
//
//                    if (!splitRoot2[0].equals("09")) {
//                        fileRoot = page;
//                    }
//                }

        //중복 로그인 객체
        loginOverHelper = new Handler();

        //강의를 웹뷰에 띄운다.
        String contentsUrl;
        String domain = "http://cb.egreen.co.kr/contents_m/android/" + si.getDirectoryName();
        if (jucha == 0) {
            contentsUrl = domain + "/00/orientation.html";
//                contentsUrl = "http://cb.egreen.co.kr/contents_m/tmp/2018_34ina/01/01.html";
        }
        else {
            contentsUrl = domain + "/" + fileRoot;
//                contentsUrl = "http://cb.egreen.co.kr/contents_m/tmp/2018_34ina/01/01.html";
        }

        netCheck = new NetworkStateCheck(this);
        if (netCheck.isConnectionNet()) {
            wv.loadUrl(contentsUrl);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.i(TAG, "onStart :: " + viewTime + ", " + maxTime + ", " + minutes + ", " + second);

        if (netCheck.isConnectionNet()) {
            if (enable) {
                Log.i(TAG, "시작!" + minutes + ", " + second);
                jindoCheck_proc(1);
                startTimerTask();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 중복 로그인 체크
     */
    public String netConnForOverlapCheck() {
        String url = "http://cb.egreen.co.kr/mobile_proc/loginOverlap_check_m2.asp";
        ContentValues cValues = new ContentValues();
        cValues.put("userId", si.getUserId());
        cValues.put("ulNum", si.getLoginNumber());

//        Log.i(TAG, "전달하는 값 ====> " + cValues.toString());

        //네트워크 통신으로 데이터를 가져온다.
        asyncTask = new NetworkAsyncTasker(this, url, cValues);
        try {
            return  asyncTask.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return "";
    }

    /**
     * Async 결과 전달 받음
     */
    @Override
    public void processFinish(String result, String what) { }

    /**
     * 브라우저
     */
    private class WebBrowserClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //페이지 로딩 시작
            loading = new ShowLoading(A8_Learning.this, "강의를 가져오는 중입니다!");
            loading.start();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            return super.shouldOverrideUrlLoading(view, request);
            //컨텐츠 실행중
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            //페이지 로딩 완료
            if (loading != null) {
                loading.stop();
            }
        }
    }

    /**
     * 2분마다 체크, 진도처리를 한다.
     */
    private void startTimerTask() {
        stopTimerTask();

        jindoTimerTask = new TimerTask() {
            @Override
            public void run() {
                second--;
                if (second == -1) {
                    second = 59;
                    minutes--;
                }

                Log.i(TAG, "돈다!!" + minutes + ", " + second);

                //1분 마다 로그인 중복 체크, 5초에 한번씩
                if (second % 5 == 0) {
                    //여기서 중복로그인 체크!
                    //중복 로그인 체크
                    final String result;
                    result = netConnForOverlapCheck();
                    Log.i(TAG, "중복 로그인 조회 결과 ===> " + result);

                    if (result != "") {
                        Log.i(TAG, "진도 스레드 자원 정리");
                        Log.i(TAG, "진도 스레드 종료");

                        stopTimerTask();

                        loginOverHelper.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                logoutByLoginOverlap(result);
                            }
                        }, 0);
                    }
                }

                if ((minutes % 2 == 0) && (second == 0)) {
                    if (minutes > 0) {
                        //여기서 '처리'
                        Log.i(TAG, "처리!" + minutes + ", " + second);
                        jindoCheck_proc(2);
                    }
                }

                if ((minutes == 0) && (second == 0)) {
                    //여기서 '충족'
                    Log.i(TAG, "충족!" + minutes + ", " + second);
                    jindoCheck_proc(4);
                }

                if (minutes > maxTime) {
                    Log.i(TAG, "진도 스레드 자원 정리");
                    Log.i(TAG, "진도 스레드 종료");

                    stopTimerTask();
                }
            }
        };

        jindoTimer.schedule(jindoTimerTask, 0, 1000);
    }

    /**
     * 진도체크 쓰레드를 종료한다.
     */
    private void stopTimerTask() {
        if (jindoTimerTask != null) {
            jindoTimerTask.cancel();
            jindoTimerTask = null;
        }
    }

    /**
     * 웹 -> 안드로이드 함수 호출
     */
    private class JSCall {
        @JavascriptInterface
        public void toAndroid(final String msg) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "JS 메세지: " + msg);

//                    SharedPreferences savedPage = getSharedPreferences("CONTENTS", MODE_PRIVATE);
//                    SharedPreferences.Editor editor = savedPage.edit();
//                    editor.putString("SAVED_PAGE", msg);
//                    editor.commit();

                    contentsPage = msg;
                    if (enable) {
                        jindoCheck_proc(4);
                    }
                }
            }).start();
        }
    }

    /**
     * 진도처리를 한다.
     */
    public void jindoCheck_proc(int what) {
        String jindoDomain;

        ContentValues cValues = new ContentValues();
        cValues.put("cUserid", si.getUserId());
        cValues.put("cClassid", si.getClassId());

        if (what == 4) {
            //페이지 체크
            Log.i(TAG, "페이지 체크");
            jindoDomain = "http://cb.egreen.co.kr/mobile_proc/mypage/course/studyPageSave_Proc2.asp";

            cValues.put("strUrl", contentsPage);
        }
        else {
            //시간 체크
            Log.i(TAG, "시간 체크");
            jindoDomain = "http://cb.egreen.co.kr/mobile_proc/mypage/course/studyTimeSave_Proc2.asp";

            cValues.put("nIs_Start", what);
            cValues.put("nDay", jucha);
            cValues.put("nHour", chasi);
            cValues.put("nViewCid", eCid);
        }

        SetJindoNetTask setJindoNT = new SetJindoNetTask(jindoDomain, cValues);
        setJindoNT.execute();
    }

    /**
     * 과목 리스트를 가져오기 위한 AsyncTask
     */
    public class SetJindoNetTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public SetJindoNetTask(String url, ContentValues values) {
            this.url = url;
            this.values = values;
        }

        @Override
        protected String doInBackground(Void... voids) {
            String result;

            NetworkConnection nc = new NetworkConnection();
            result = nc.request(url, values);

            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            String result = s;

            Log.i(TAG, "결과 :" + result);

            //DB에 insert하는 일이라 오류 처리만 하면 된다.
            if (result.equals("FAIL")) {
                //네트워크 통신 오류
                Log.i(TAG, "진도처리 통신 오류 발생!!");
            }
        }
    }

    /**
     * 그냥 intent 로 잘 넘어왔나 체크...
     */
    private void checkGetIntent() {
        try {
            if (jucha == 0) {
                Log.i(TAG, "주차가 없다.");
            }

            if (chasi == 0) {
                Log.i(TAG, "차시가 없다.");
            }

            if (eCid.equals("")) {
                Log.i(TAG, "cId가 없다.");
            }

            if (watchedTime.equals("")) {
                Log.i(TAG, "watchedTime가 없다.");
            }

            if (fullTime.equals("")) {
                Log.i(TAG, "fullTime가 없다.");
            }

            if (fileRoot.equals("")) {
                Log.i(TAG, "파일경로가 없다.");
            }
            else {
                Log.i(TAG, fileRoot + "");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 뒤로가기 버튼 클릭시, 정말 졸료할것인지를 묻는다.
     */
    @Override
    public void onBackPressed() {
//        super.onBackPressed();
        final Handler handler = new Handler();

        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage("정말 강의를 종료하시겠어요?");
        ab.setTitle("강의 종료");
        ab.setPositiveButton("네", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, final int which) {
                if (enable) {
                    loading = null;
                    loading = new ShowLoading(A8_Learning.this, "학습시간을 저장하고있습니다.\n잠시만 기다려주세요.");
                    loading.start();

                    new Thread(new Runnable() {
                        int count = 0;

                        @Override
                        public void run() {
                            while(count < 2) {
                                ++count;

                                if (count == 2) {
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            moveToA7_ClassRoom();
                                        }
                                    });
                                }

                                try {
                                    Thread.sleep(750);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }).start();
                }
                else {
                    moveToA7_ClassRoom();
                }
            }
        });
        ab.setNegativeButton("아니오", null);
        ab.show();
    }

    /**
     * A7_ClassRoom으로 이동
     */
    public void moveToA7_ClassRoom() {
        Intent intent = new Intent(this, A7_ClassRoom.class);
        intent.putExtra("studyInfo", si);
        startActivity(intent);
        finish();
    }

    /**
     * 중복 로그인 알림
     */
    private void logoutByLoginOverlap(String result) {
        String [] arrResult = result.split(",");
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage("중복 로그인이 감지되어 로그아웃 되었습니다.\n\n" +
                "접 속 시 간 : " + arrResult[1] + "\n" +
                "접 속 IP : " + arrResult[2] + "\n" +
                "접 속 OS : " + arrResult[3] + "\n" +
                "접속Browser : " + arrResult[4]);
        ab.setPositiveButton("알겠습니다", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                moveToA2_Login();
            }
        }).show();
    }

    /**
     * A2_Login으로 이동
     */
    public void moveToA2_Login() {
        SharedPreferences savedCertyState = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);
        SharedPreferences.Editor editor = savedCertyState.edit();
        editor.putBoolean("certyState", false);
        editor.commit();

        startActivity(new Intent(this, A2_Login.class));
        finish();
    }

    /**
     * 이 화면을 벗어나게 될때 강의가 동영상 재생중이었다면, 멈추도록 한다.
     * 전화 왔을때, 카톡할때 등등
     */
    @Override
    protected void onPause() {
        super.onPause();

        Log.i(TAG, "onPause :: " + viewTime + ", " + maxTime + ", " + minutes + ", " + second);

        if (netCheck.isConnectionNet()) {
            // 동영상 플레이어를 일시정지 한다.
            if (wv != null) {
                wv.loadUrl("javascript:background_pause();");
            }

            if (enable) {
                //진도체크 - 종료
                jindoCheck_proc(3);
                stopTimerTask();
            }
        }

        if (loading != null) {
            loading.stop();
        }
    }

    @Override
    protected void onDestroy() {
        //액티비티 종료될때 반드시 제거
        jindoTimer.cancel();

        Log.i(TAG, "onDestroy()");

        super.onDestroy();
    }

//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Log.i(TAG, "가로");
//        }
//        else {
//            Log.i(TAG, "세로");
//        }
//    }
}
