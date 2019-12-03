package com.egreen.egreenbeta05;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Activity;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.egreen.egreenbeta05.Adapter.A6_MyClassListAdapter;
import com.egreen.egreenbeta05.Data.A6_MyClassListData;
import com.egreen.egreenbeta05.Data.StudyInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class A6_StudyCenter extends AppCompatActivity implements View.OnClickListener,
        A6_MyClassListAdapter.MyClassListClickListener,
        NetworkAsyncTasker.AsyncResponse {
    private static final String TAG = A6_StudyCenter.class.getSimpleName();
    private static final String MAIN_LIST = "mainList";
    private static final String OVERLAP = "overlap";
    private static final String LOGOUT = "logout";

    RecyclerView myClassList;
    SwipeRefreshLayout swipeRefresh;
    ShowLoading loading;

    //AsyncTask 결과값을 받기위함
    NetworkAsyncTasker asyncTask;

    //학습에 필요한 정보
    StudyInfo si;
    SharedPreferences savedCertyState;
    boolean isEnable;
    String isThereHighSchoolName, isSurvey;

    /* 공인인증서 변수 */
    private String appKey = "2O4zh+t/DZJR0ifytsXeZw==";     //정식 라이센스
    private String[] rtnParams = {"a", "b", "c"};

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onReStart()");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume()");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_study_center);

        Log.i(TAG, "onCreate()");

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        swipeRefresh = findViewById(R.id.swipeRefresh);
        myClassList = findViewById(R.id.myClassList);
        TextView userName = findViewById(R.id.userName);
        Button btn_request = findViewById(R.id.btn_request);

        btn_request.setOnClickListener(this);

        savedCertyState = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);

        //A7_StudyCenter 또는 A8_Learning 에서 전달한 학습정보를 저장
        si = (StudyInfo) getIntent().getSerializableExtra("studyInfo");

        try {
            Log.i(TAG, "A2_Login 에서 전달 받은 =====> " + si.getUserId());
        } catch (Exception e) {
            Log.i(TAG, "A2_Login 에서 전달 받은 =====> 없음");
        }

        if (si.getUserName().equals("")) {
            userName.setText("나의 강의실");
        }
        else {
            userName.setText(Html.fromHtml("<Strong>" + si.getUserName() + "</Strong>님의 강의실"));
        }

        final NetworkStateCheck netCheck = new NetworkStateCheck(this);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (netCheck.isConnectionNet()) {
                    netConnForGetMyClassListData();
                }
            }
        });

        if (netCheck.isConnectionNet()) {
            netConnForGetMyClassListData();
        }
    }

    @Override
    public void onClick(View v) {
        //로그인 상태 확인 필요..
        switch (v.getId()) {
            case R.id.btn_request:
                startActivity(new Intent(this, A6_RequestLecture.class));
                break;
        }
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

        loading = new ShowLoading(A6_StudyCenter.this, "잠시만 기다려주세요.");
        loading.start();

        asyncTask = new NetworkAsyncTasker(this, url, cValues, OVERLAP);
        asyncTask.execute();

        return "";
    }

    /**
     * 과정정보를 가져오는 네트워크 통신
     */
    private void netConnForGetMyClassListData() {
        String url = "http://cb.egreen.co.kr/mobile_proc/mypage/new/main_m2.asp";
        ContentValues cValues = new ContentValues();
        cValues.put("userId", si.getUserId());

//        Log.i(TAG, "전달하는 값 ====> " + cValues.toString());

        //과목 가져오기
        loading = new ShowLoading(A6_StudyCenter.this, "과목정보를 가져오는 중입니다.");
        loading.start();

        asyncTask = new NetworkAsyncTasker(this, url, cValues, MAIN_LIST);
        asyncTask.execute();
    }

    /**
     * 로그아웃을 위한 통신이다.
     */
    private void netConnForLogout() {
        String url = "http://cb.egreen.co.kr/mobile_proc/login/new/logout_proc_m2.asp";
        ContentValues cValues = new ContentValues();
        cValues.put("userId", si.getUserId());
        cValues.put("ulNum", si.getLoginNumber());

        asyncTask = new NetworkAsyncTasker(this, url, cValues, LOGOUT);
        asyncTask.execute();
    }

    /**
     * 과목 리스트를 가져는 AsyncTask 결과 처리
     */
    @Override
    public void processFinish(String result, String what) {
        if (loading != null) {
            loading.stop();
        }

        if (result.equals("FAIL")) {
            //네트워크 통신 오류
        }
        else if (what.equals(MAIN_LIST)) {
            json_parsing(result);
        }
        else if (what.equals(OVERLAP)) {
            overlap_proc(result);
        }
        else if (what.equals(LOGOUT)) {
            finish();
        }
    }

    /**
     * 터치 액션시, 중복 로그인 체크
     * 중복 로그인이 아니면 학습동의서, 사전설문, 나의 강의실로 이동
     */
    private void overlap_proc(String result) {
        if (result != "") {
            //중복 로그인
            String [] arrResult = result.split(",");

            AlertDialog.Builder ab = new AlertDialog.Builder(A6_StudyCenter.this);
            ab.setMessage("중복 로그인이 감지되어 로그아웃 되었습니다.\n\n" +
                    "접 속 시 간 : " + arrResult[1] + "\n" +
                    "접 속 IP : " + arrResult[2] + "\n" +
                    "접 속 OS : " + arrResult[3] + "\n" +
                    "접속Browser : " + arrResult[4]);
            ab.setPositiveButton("알겠습니다", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            }).show();
        }
        else {
            Log.i(TAG, "공인인증서 유예자 인가?" + getIsNeedCerty());
            Log.i(TAG, "인증서 로그인 성공 상태를 유지 중인가?" + savedCertyState.getBoolean("certyState", false));
            if (isEnable) {
                if (getIsNeedCerty() == false && savedCertyState.getBoolean("certyState", false) == false) {
                    isLoginCerty();
                }
                else {
                    checkDong_Servey();
                }
            }
            else {
                //모바일 수강 불가능한 과목이면 Alert를 띄운다.
                AlertDialog.Builder ab = new AlertDialog.Builder(A6_StudyCenter.this);
                ab.setTitle("모바일 수강불가 과목");
                ab.setMessage("죄송합니다.\n[" + si.getClassTitle().trim() + "]은 모바일 강의를 지원하지 않습니다.\nPC를 이용해주세요.");
                ab.setPositiveButton("알겠습니다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        netConnForGetMyClassListData();
                    }
                });
                ab.show();
            }
        }
    }

    /**
     * JSON parsing
     */
    private void json_parsing(String s) {
        A6_MyClassListData a6_myClassListData;
        ArrayList<A6_MyClassListData> myClassListData = new ArrayList<>();

        /*
         * JSON 데이터를 파싱한다
         * */
        try {
            JSONObject jsObj = new JSONObject(s);
//            JSONArray jsArray = new JSONArray(s);
            JSONArray jsArray = jsObj.getJSONArray("classInfo");

            for (int i = 0; i < jsArray.length(); i++) {
//            for (int i = 0; i < 8; i++) {
                jsObj = jsArray.getJSONObject(i);

                String classId = jsObj.getString("classId");
                String classTitle = jsObj.getString("classTitle");
                String currJucha = jsObj.getString("currJucha");
                String attendRatio = jsObj.getString("attendRatio");
                String isReadOrientation = jsObj.getString("isReadOrientation");
                String isEnable = jsObj.getString("isEnable");
                String isSurvey = jsObj.getString("isSurvey");
                String isThereHighSchoolName = jsObj.getString("isThereHighSchoolName");
                String isDongPost = jsObj.getString("isDongPost");
                String directoryName = jsObj.getString("strUrl");     //디렉토리 명
                String myGoal = jsObj.getString("myGoal");
                String myNote = jsObj.getString("myNote");
                String selfInt = jsObj.getString("selfIntroduce");
                String discussion = jsObj.getString("discussion");

                Boolean _isEnable;
                if (isEnable.equals("Y")) {
                    _isEnable = true;
                }
                else {
                    _isEnable = false;
                }

                a6_myClassListData = new A6_MyClassListData(_isEnable, classId, classTitle, currJucha, attendRatio,
                        isReadOrientation, isSurvey, isThereHighSchoolName, isDongPost, directoryName,
                        myGoal, myNote, selfInt, discussion);
                myClassListData.add(a6_myClassListData);
            }
        } catch (JSONException e) {
            Log.i(TAG, "notice JSON Exc: " + e.getMessage());
        }

        setListView(myClassListData);
    }

    /**
     * NetConnForgetMyClassList() 로 받아온 데이터를 그린다.
     */
    private void setListView(ArrayList<A6_MyClassListData> myClassListData) {
        /*
         * 각 Item 들이 RecyclerView 의 전체크기를 변경하지 않는다면,
         * setHasFixedSize() 함수를 사용해서 성능을 개선할 수 있다.
         * 전체크기가 변경될 가능성이 있다면 false, 아니면 true
         * */
        LinearLayoutManager listViewLayout = new LinearLayoutManager(A6_StudyCenter.this);
        A6_MyClassListAdapter myClassListAdapter = new A6_MyClassListAdapter(A6_StudyCenter.this, myClassListData);

        myClassList.setHasFixedSize(true);
        myClassList.setLayoutManager(listViewLayout);
        myClassListAdapter.setOnClickListener(A6_StudyCenter.this);

        myClassList.setAdapter(myClassListAdapter);
        myClassListAdapter.notifyDataSetChanged();

        if (loading != null) {
            loading.stop();
        }

        swipeRefresh.setRefreshing(false);      //해주지 않으면 리스레쉬 아이콘이 사라지지 않는다.
        Toast.makeText(this, "동기화 완료", Toast.LENGTH_SHORT).show();
    }

    /**
     * A6_MyClassListAdapter -> A7_StudyCenter
     */
    @Override
    public void onItemClicked(boolean isEnable, String classId, String classTitle, String strUrl, String isReadOrientation,
                              String isSurvey, String isThereHighSchoolName, String isDongPost, String myGoal, String myNote,
                              String studyDate, String selfIntro, String discussion) {

        //체크순서
        //1. 모바일 수강 가능한지.
        //2. 출석 인증 기간인지.
        //3. 학습동의서를 완료 했는지.
        //4. 설문조사는 완료 했는지.
        si.setClassId(classId);
        si.setStudyDate(studyDate);
        si.setClassTitle(classTitle);
        si.setDirectoryName(strUrl);

        si.setDongPost(isDongPost);
        si.setMyGoal(myGoal);
        si.setMyNote(myNote);
        si.setSelfIntro(selfIntro);
        si.setOrientation(isReadOrientation);
        si.setDiscussion(discussion);

        this.isEnable = isEnable;
        this.isThereHighSchoolName = isThereHighSchoolName;
        this.isSurvey = isSurvey;

        //여기서 중복로그인 체크!
        //중복 로그인 체크
        netConnForOverlapCheck();
    }

    /**
     * 학습동의서 및 사전설문조사 확인, 화면 이동
     */
    private void checkDong_Servey() {
        Intent intent;
        if (si.getStudyDate().equals("0")) {
            showDoNotStudyDate(si.getStudyDate());
        }
        else if (si.getDongPost().equals("")) {
            //학습동의서 미완료시,
            //학습동의서 StudyAgreement Activity 로 이동한다.
            intent = new Intent(this, StudyAgreement.class);

            intent.putExtra("CLASS_ID", si.getClassId());
            intent.putExtra("IS_THERE_HIGHSCHOOL_NAME", isThereHighSchoolName);
            intent.putExtra("studyInfo", si);
            startActivity(intent);
            finish();
        }
        else if (isSurvey.equals("False")) {
            //설문조사 미완료시,
            //설문조사 Survey Activity 로 이동한다.
            intent = new Intent(this, Survey.class);
            intent.putExtra("CLASS_ID", si.getClassId());
            intent.putExtra("studyInfo", si);
            startActivity(intent);
            finish();
        }
        else {
            //학습동의서와 설문조사 완료시,
            //과목 목차 리스트 Activity로 이동한다.
            SharedPreferences.Editor editor = savedCertyState.edit();
            editor.putBoolean("certyState", true);
            editor.commit();

            goClassRoom();
        }
    }

    /**
     * A7_ClassRoom 으로 이동
     */
    private void goClassRoom() {
        Intent intent = new Intent(this, A7_ClassRoom.class);
        intent.putExtra("studyInfo", si);
        startActivity(intent);
        finish();
    }

    /**
     *  학습이 시작되기 전이거나 종료했을때, 알럿을 띄워 보여준다.
     */
    private void showDoNotStudyDate(String _studyDate) {
        try {
            if (_studyDate.equals("0")) {
                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setMessage("출석인증 기간이 아닙니다.");
                ab.setPositiveButton("확인", null);
                ab.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, e.getMessage());
        }
    }

    ////////////////////// ----------------------- 공인인증서 -------------------------------///////////////////
    /*
     * 라이선스 등록을 위해 꼭 실행하여야 함.
     */
    private void doLicense() {
        //라이선스 정보 추출
        Intent intent = new Intent();
        intent.setData(Uri.parse("crosscert://licenseinfo"));
        intent.putExtra("requestCode", 2);
        startActivityForResult(intent, 2);
    }

    //공인인증서 로그인 유무 확인
    protected void isLoginCerty() {
        if (isCertyApp() == false) {
            android.app.AlertDialog.Builder ab = new android.app.AlertDialog.Builder(this);
            ab.setCancelable(false);

            ab.setTitle("공인인증서 로그인")
                    .setMessage("범용공인인증서 로그인을 위해서는 한국전자인증 앱이 필요합니다.\n설치 화면으로 이동 하시겠어요?")
                    .setPositiveButton("예", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            searchMarket(A6_StudyCenter.this, "com.crosscert.android");
                        }
                    })
                    .setNegativeButton("아니오", null)
                    .show();
        } else {
            // 전자인증서
            //doEsign("abcd", 1, null);
            String inputData = "abcd";
            String inputbase64 = null;
            try {
                inputbase64 = Base64Coder.encodeString(inputData);
            } catch (Exception e1) {
                e1.printStackTrace();
            }

            // 전자서명
            doEsign(inputbase64, 1, null);
        }
    }

    //인증서 설치유무 확인
    protected boolean isCertyApp() {
        //한국전자인증 공인인증센터 App 설치 유무 확인
        PackageManager pm = getPackageManager();
        Boolean isCrosscertInstalled = false;

        //flags Additional option flags.  Currently should always be 0.
        List<ApplicationInfo> appList = pm.getInstalledApplications(0);
        Iterator<ApplicationInfo> i = appList.iterator();

        while (i.hasNext()) {
            ApplicationInfo app = i.next();
            if (!(app.packageName.equals("com.crosscert.android"))) {
                isCrosscertInstalled = false;
            } else {
                isCrosscertInstalled = true;
                break;
            }
        }

        return isCrosscertInstalled;
    }

    /**
     * 액티비티 결과 : 하위 액티비티가 리턴하는 결과 처리하기
     * 하위 액티비티가 종료되면, 해당 액티비티를 시작시킨
     * 호출 액티비티의 onActivityResult 이벤트 핸들러가 호출된다.
     * 하위 액티비티가 리턴하는 결과를 처리하려면 이 메서드를 재정의 해야한다.
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

//        Log.i(TAG, "requestCode : " + requestCode);
//        Log.i(TAG, "resultCode : " + resultCode);

        if (resultCode == RESULT_OK) {
            Log.i(TAG, "어디서 돌아오셨나요? " + requestCode);

            int resultState = 1;
            if (requestCode == 2) {
                resultState = data.getIntExtra("resultstate", resultState);
                String resultData = data.getStringExtra("result");

                if (resultState == 0) {
                    Log.i(TAG, "LicenseInfo::" + resultData);
                } else {
                    Log.i(TAG, "error: " + "(" + resultData + ")");
                }
            }
            else if (requestCode == 8) {    //전자서명
                resultState = data.getIntExtra("resultstate", resultState);
                String resultData = data.getStringExtra("result");
                String[] rtnParamsData = data.getStringArrayExtra("rtnParam");

//                Log.v("clientApp", "resultState : " + resultState);

                String result = "";
                if (resultState == 0) {
                    if (resultData != null) {
                        String sidCheck = data.getStringExtra("sidCheck");
                        String sidResult = data.getStringExtra("sidResult");
                        if (sidCheck != null && sidResult != null) {
//                            resultData = resultData + "\nsidcheck : " + sidCheck;
//                            resultData = resultData + "\nsidResult : " + sidResult;
                        }
                        String certiResultData = resultData;
//                        alertDialog("result", resultData, this);
//                        Log.i(TAG, "공인인증서 서명 :" + tResultData);

                        // 공인인증서 로그인, cb2 로 고정
                        String url = "http://cb2.egreen.co.kr/mobile_proc/login/new/new_verify_m.asp";

                        ContentValues cValue = new ContentValues();
                        cValue.put("cuserid", si.getUserId());
                        cValue.put("uMobileBrand", Build.BRAND + "::" + Build.MODEL);
                        cValue.put("result", certiResultData);

                        VerifyNetTask verifyNT = new VerifyNetTask(url, cValue);
                        verifyNT.execute();
                    }
                } else {
                    Log.i(TAG, "error: " + "(" + resultData + ")");
                }
            }
        }
    }

    /**
     * 공인인증서 로그인 결과처리 AsyncTask
     */
    private class VerifyNetTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public VerifyNetTask(String url, ContentValues values) {
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

            if (result.equals("FAIL")) {
                //네트워크 통신 오류
            } else {
                Log.i(TAG, "인증서 결과 : " + result);
                loginCertyResult_proc(result);
            }
        }
    }

    private void loginCertyResult_proc(String result) {
        android.app.AlertDialog.Builder ab = new android.app.AlertDialog.Builder(A6_StudyCenter.this);
        ab.setCancelable(false);

        if (result.equals("OK")) {
            checkDong_Servey();
        }
        else {
            if (result.equals("FAIL")) {
                //DB에 저장된 인증서와 다름
                ab.setMessage("e그린원격평생교육원 홈페이지에서 사용하는 인증서와 동일한 인증서를 사용해주세요.");
                ab.setPositiveButton("확인", null);
                ab.show();
            }
            else if (result.equals("Err")) {
                ab.setMessage("공인인증서 조회에 실패했습니다\n본 원으로 연락주세요!");
                ab.setPositiveButton("확인", null);
                ab.show();
            }
            else if (result.equals("1358954498")) {
                ab.setMessage("범용 공인인증서가 아닙니다.\n인증서를 다시한번 확인해주세요.");
                ab.setPositiveButton("확인", null);
                ab.show();
            }
            else if (result.equals("1342177281")) {
                ab.setMessage("본인 확인에 실패했습니다.\n인증서를 다시한번 확인해주세요.");
                ab.setPositiveButton("확인", null);
                ab.show();
            }
            else if (result.equals("23068686")) {
                ab.setMessage("전자서명 형식이 유효하지 않습니다.\n인증서를 다시한번 확인해주세요.");
                ab.setPositiveButton("확인", null);
                ab.show();
            }
            else {
                //위 사항 모두 해당 없을때
                ab.setMessage("예상치 못한 오류가 발생했습니다.\n오류내용을 본 교육원에 알려주세요\n오류내용 : " + result);
                ab.setPositiveButton("확인", null);
                ab.show();
            }
        }
    }

    /**
     * 전자서명 (PKCS#7 SignedData)
     * @param inputbase64 base64 인코딩된 평문 데이터
     */
    private void doEsign(String inputbase64, int sidOption, String sidValue) {
        // 앱에서 한국전자인증의 공인인증센터 전자서명 모듈 링크 걸기
        Intent intent = new Intent();

        intent.setData(Uri.parse("crosscert://esign"));
        intent.putExtra("requestCode", 8);
        intent.putExtra("data", inputbase64); // base64 인코딩된 평문 데이터
        intent.putExtra("appkey", appKey);  //App 서비스 라이선스
        intent.putExtra("rtnParam", rtnParams);

        // verify sid
        if (sidOption == 1) {
            intent.putExtra("sidOption", sidOption);
        } else if (sidOption == 2) {
            intent.putExtra("sidOption", sidOption);
            intent.putExtra("sidValue", sidValue);
        }

        startActivityForResult(intent, 8);
    }

    //한국전자인증 공인인증센터 App 설치
    public static void searchMarket(Activity caller, String packagename) {
        Uri uri = Uri.parse("market://details?id=" + packagename);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        caller.startActivity(intent);
    }
    ////////////////////// ----------------------- 공인인증서 -------------------------------///////////////////

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();

        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage("로그아웃 하시겠어요?");
        ab.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                netConnForLogout();
            }
        });
        ab.setNegativeButton("아니오", null);
        ab.show();
    }

    /**
     * 회원 LoginNumber를 가져온다.
     */
    private boolean getIsNeedCerty() {
        /*
         * 저장되어 있는 회원 ID를 가져온다.
         */
        boolean isNeedCerty = false;
        try {
            isNeedCerty = savedCertyState.getBoolean("isNeedCerty", false);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isNeedCerty;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loading != null) {
            loading.stop();
        }

        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.i(TAG, "onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "onDestroy()");
    }
}
