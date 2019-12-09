package com.egreen.egreenbeta05;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.egreen.egreenbeta05.Adapter.A7_ClassNotiAdapter;
import com.egreen.egreenbeta05.Adapter.A7_HomeAttendAdapter;
import com.egreen.egreenbeta05.Adapter.A7_JuchaListAdapter;
import com.egreen.egreenbeta05.Data.A7_ClassNotiData;
import com.egreen.egreenbeta05.Data.A7_JuchaData2;
import com.egreen.egreenbeta05.Data.StudyInfo;
import com.google.android.material.navigation.NavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class A7_ClassRoom extends AppCompatActivity implements View.OnClickListener,
        A7_Tab1_F.OnFragmentInteractionListener,
        A7_JuchaListAdapter.JuchaClickListener,
        A7_Home_F.OnFragmentHomeListener,
        A7_HomeAttendAdapter.AttendStateClickListener,
        A7_ClassNotiAdapter.ClassNotiListener,
        NavigationView.OnNavigationItemSelectedListener,
        NetworkAsyncTasker.AsyncResponse {

    private static final String TAG = A7_ClassRoom.class.getSimpleName();

    private static final int HOME = 1;
    private static final int NOW = 2;
    private static final String LOGOUT = "logout";

    int choiseJucha;
    int minJucha, maxJucha;
    Button btn_home, btn_course;
    ShowLoading loading;
    ArrayList<A7_ClassNotiData> arrClassNoti;
    ArrayList<Double> arrJuchaRatio;

    //AsyncTask 결과값을 받기위한 변수
    NetworkAsyncTasker asyncTask;

    //A7_StudyCenter >> A7_ClassRoom 으로 전달 받은 intent 정보
    StudyInfo si;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a7_class_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);        //액션바의 타이틀 삭제하고 툴바 아래의 텍스트 보이기
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);          //액션바의 왼쪽 버튼 사용여부
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.menu);    //액션바 왼쪽 버튼 이미지 설정
        getSupportActionBar().setHomeButtonEnabled(true);

        //A6_StudyCenter 또는 A8_Learning 에서 전달한 학습정보를 저장
        si = (StudyInfo) getIntent().getSerializableExtra("studyInfo");

//        Log.i(TAG, "전달받은 si.id ==> " + si.getUserId());

        //선택된 강좌이름으로 변경된다.
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(si.getClassTitle());

        btn_home = findViewById(R.id.btn_home);
        btn_course = findViewById(R.id.btn_course);
        ImageButton btn_logout = findViewById(R.id.btn_logout);

        btn_home.setOnClickListener(this);
        btn_course.setOnClickListener(this);
        btn_logout.setOnClickListener(this);

        final NetworkStateCheck netCheck = new NetworkStateCheck(this);
        if (netCheck.isConnectionNet()) {
            loading = new ShowLoading(this, "과목 정보를 가져오는 중입니다");
            loading.start();

            //주차리스트
            netConnForGetListData();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    /**
     * 목차 데이터를 가져오기 위한 네트워크 연결
     */
    private void netConnForGetListData() {
        String url = "http://cb.egreen.co.kr/mobile_proc/mypage/new/getClassRoomInfo_m2.asp";
        ContentValues cValues = new ContentValues();
        cValues.put("userId", si.getUserId());
        cValues.put("classId", si.getClassId());
        cValues.put("isAllList", "Y");

        //목차 데이터를 가져온다.
        asyncTask = new NetworkAsyncTasker(this, url, cValues);
        asyncTask.execute();
    }

    /**
     * 현재 출석체크 가능한 학습 목차를 가져오기 위한 AsyncTask
     */
    @Override
    public void processFinish(String result, String what) {
        if (loading != null) {
            loading.stop();
        }

        if (result.equals("FAIL")) {
            //네트워크 통신 오류
        }
        else if (result.equals("0")) {
            Log.i(TAG, result + " is empty");
        }
        else {
            if (what.equals(LOGOUT)) {
                initCertyAndFinish();
            }
            else {
                json_parsing(result);
            }
        }
    }

    /**
     * JSON parsing
     */
    private void json_parsing(String s) {
        A7_ClassNotiData a7_classNotiData;
        A7_JuchaData2 juchaData;

        /*
         * JSON 데이터를 파싱한다
         * */
        try {
            JSONObject jsObj = new JSONObject(s);
            JSONArray notiInfo = jsObj.getJSONArray("noti");

            arrClassNoti = new ArrayList<>();
            arrClassNoti.clear();

            //과목별 공지사항 3개

            for (int i=0; i<notiInfo.length(); i++) {
                JSONObject notiObj = notiInfo.getJSONObject(i);

                int notiId = notiObj.getInt("notiId");
                String title = notiObj.getString("title");
                String regDate = notiObj.getString("regDate");

                Log.i(TAG, "공지 등록일자 =====> " + regDate);

                if (!(regDate.equals("0") || regDate.equals("null"))) {
                    a7_classNotiData = new A7_ClassNotiData(notiId, title, regDate);
                    arrClassNoti.add(a7_classNotiData);
                }
            }

            int nDay = 0;

            //시작/마지막 주차, 팁활동 현황 정보
            JSONArray otherInfo = jsObj.getJSONArray("otherInfo");
            JSONObject otherInfoObj = otherInfo.getJSONObject(0);

            minJucha = otherInfoObj.getInt("minJucha");
            maxJucha = otherInfoObj.getInt("maxJucha");

            Log.i(TAG, "min jucha ==> " + minJucha);
            Log.i(TAG, "max jucha ==> " + maxJucha);

            //주차별 진도현황 리스트
            int ChasiNum = 0;
            ArrayList<A7_JuchaData2> arrAllJuchaData = new ArrayList<>();
            JSONArray jsArray = jsObj.getJSONArray("juChaInfo");
//            Log.i(TAG, jsArray.length() + "::jsArray 크기");
            for (int i=0; i<jsArray.length(); i++) {
                JSONObject attendListObj = jsArray.getJSONObject(i);

                String cid = attendListObj.getString("cid");
                nDay = attendListObj.getInt("nDay");
                int nHour = attendListObj.getInt("nHour");
                String totalRatio = attendListObj.getString("totalRatio");

                //데이터셋이 한 주차에 1, 2차시 데이터가 모두 들어가야 한다.
                //8 주차{1차시데이터}, 8주차 {2차시데이터}(X) / 8주차 {1차시데이터, 2차시데이터}
                //JSON 으로 온 데이터가 전자다.
                //후자 방식으로 다시 바꿔줘야 한다.
                if (jsArray.length() == 28) {
                    ChasiNum = 2;
                }

                if (jsArray.length() == 41) {
                    ChasiNum = 3;
                }

                juchaData = new A7_JuchaData2(cid, nDay, nHour, totalRatio);

                arrAllJuchaData.add(juchaData);
            }

            calculationRatio(arrAllJuchaData, ChasiNum);

            setTabHighlight(HOME);
            switchFragment(HOME);
        } catch (JSONException e) {
            Log.i(TAG, "notice JSON Exc: " + e.getMessage());
        }
    }

    /**
     * Home 화면의 전체 주차 수강 진행율을 보여주기 위해,
     * 각 차시의 진행율을 가져와 주차별로 합친 후, Home_F 로 보낸다.
     */
    private void calculationRatio(ArrayList<A7_JuchaData2> arrAllJuchaData, int ChasiNum) {
        int classCount = arrAllJuchaData.size();
        int count = (classCount / ChasiNum) + 2;        //2(중간고사, 기말고사) 를 넣어야 전체 주차 개수가 된다.

//        Log.i(TAG, "Count ===========> " + count);

        arrJuchaRatio = new ArrayList<>();
        double totalRatio;

        for (int i=1; i<=count; i++) {
            totalRatio = 0;

            for (int j=0; j<arrAllJuchaData.size(); j++) {
                int jucha = arrAllJuchaData.get(j).getJucha();
                double sRatio = Double.parseDouble(arrAllJuchaData.get(j).getTotalRatio());

                if (i == jucha) {
                    totalRatio += sRatio;
//                    Log.i(TAG, "일치 : i ==> " + i + " j ==> " + j + ", jucha ==> " + jucha + ", sRatio ==> " + sRatio);
                }
            }

            arrJuchaRatio.add(totalRatio/ChasiNum);
        }
    }

    /**
     * 탭 선택 하이라이트
     */
    private void setTabHighlight(int _what) {
        switch (_what) {
            case HOME:
                btn_home.setTextColor(ContextCompat.getColor(this, R.color.black));
                btn_course.setTextColor(ContextCompat.getColor(this, R.color.gray));

                btn_home.setBackgroundResource(R.drawable.border_black);
                btn_course.setBackgroundResource(R.drawable.border_gray);

                btn_home.setTypeface(null, Typeface.BOLD);
                btn_course.setTypeface(null, Typeface.NORMAL);
                break;
            case NOW:
                btn_home.setTextColor(ContextCompat.getColor(this, R.color.gray));
                btn_course.setTextColor(ContextCompat.getColor(this, R.color.black));

                btn_home.setBackgroundResource(R.drawable.border_gray);
                btn_course.setBackgroundResource(R.drawable.border_black);

                btn_course.setTypeface(null, Typeface.BOLD);
                btn_home.setTypeface(null, Typeface.NORMAL);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_home:
                switchFragment(HOME);
                setTabHighlight(HOME);
                break;
            case R.id.btn_course:
                if (si.getOrientation().equals("")) {
                    //오리엔테이션 열람 결과가 없으면 오리엔테이션을 먼저 선행 열람 해야한다.
                    showOrientationAlert();
                }
                else {
                    choiseJucha = 0;
                    switchFragment(NOW);
                    setTabHighlight(NOW);
                }
                break;
            case R.id.btn_logout:
                alertLogout();
                break;
        }
    }

    /**
     * 학습중인 목차, 전체 목차 Fragment 를 교체한다.
     */
    private void switchFragment(int whatIsFragment) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment fragment = null;

        if (loading != null) {
            loading.stop();
        }

        Bundle bundle = new Bundle();
        bundle.putString("UID", si.getUserId().trim());
        bundle.putString("CLASS_ID", si.getClassId().trim());

        if (whatIsFragment == HOME) {
            bundle.putSerializable("NOTI_INFO", arrClassNoti);
            bundle.putSerializable("JUCHA_RATIO", arrJuchaRatio);
            bundle.putInt("MIN_JUCHA", minJucha);
            bundle.putInt("MAX_JUCHA", maxJucha);

            bundle.putString("ORIENTATION", si.getOrientation().trim());
            bundle.putString("STUDY_AGREE", si.getDongPost().trim());
            bundle.putString("MY_GOAL", si.getMyGoal().trim());
            bundle.putInt("MY_NOTE", Integer.parseInt(si.getMyNote().trim()));
            bundle.putString("SELF_INT", si.getSelfIntro().trim());
            bundle.putString("DISCUSSION", si.getDiscussion().trim());

            fragment = new A7_Home_F();
        }
        else if (whatIsFragment == NOW) {
//            bundle.putSerializable("JUCHA_DATA", arrCurrJuchaData);
            bundle.putInt("MIN_JUCHA", minJucha);
            bundle.putInt("MAX_JUCHA", maxJucha);
            bundle.putInt("CHOISE_JUCHA", choiseJucha);

            fragment = new A7_Tab1_F();
        }

        fragment.setArguments(bundle);
//        ft.setCustomAnimations(R.anim.frag_open, R.anim.frag_close);
        ft.replace(R.id.fragmentLayout, fragment);
        ft.commitAllowingStateLoss();

//        ft.commit();
        //.commit() 사용시, Can not perform this action after onSaveInstanceState 오류 발생
        //https://gogorchg.tistory.com/entry/Android-Can-not-perform-this-action-after-onSaveInstanceState 참고
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
     * 로그아웃 Alert을 띄운다.
     */
    public void alertLogout() {
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
     * 오리엔테이션 열람여부에 따라 알럿을 보여준다.
     */
    private void showOrientationAlert() {
        //오리엔테이션 열람여부 확인 후,
        //미완료 했다면, 알럿으로 안내한다.
        //"본 학습전, 오리엔테이션을 먼저 연람해주셔야 합니다"
        //"오리엔테이션을 보시겠습니까?"
        //"예, 아니오"
        //"예" -> 오리엔테이션 강좌 열기
        //"아니오" -> finish()
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setTitle("오리엔테이션");
        ab.setMessage("본 학습전, 오리엔테이션을 먼저 열람해주셔야 합니다.\n오리엔테이션을 보시겠습니까?");
        ab.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //열람 처리 우선 해준다.
                netConnForWriteOrientation();
            }
        });
        ab.setNegativeButton("아니오", null);
        ab.show();
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
     * 오리엔테이션 열람 상태를 DB에 기록하기 위한 통신이다.
     */
    private void netConnForWriteOrientation() {
        String url = "http://cb.egreen.co.kr/mobile_proc/mypage/new/readOrientation_m2.asp";

        ContentValues cValues = new ContentValues();
        cValues.put("userId", si.getUserId());
        cValues.put("classId", si.getClassId());

        WriteOrientaionNetTask writeOrientaionNT = new WriteOrientaionNetTask(url, cValues);
        writeOrientaionNT.execute();
    }

    /**
     * 오리엔테이션 열람 기록을 위한 AsyncTask
     */
    public class WriteOrientaionNetTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public WriteOrientaionNetTask(String url, ContentValues values) {
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

            //DB에 insert하는 일아라 오류만 처리하면 된다.
            if (result.equals("FAIL")) {
                //네트워크 통신 오류
            }
            else {
                String[] arrS = null;
                try {
                    arrS = s.split(",");
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i(TAG, "Orientation Err : " + e.getMessage());
                }

                if (arrS[0].equals("OK")) {
                    goOrientation();
                }
                else if (arrS[0].equals("Err")) {
                    AlertDialog.Builder ab = new AlertDialog.Builder(A7_ClassRoom.this);
                    ab.setMessage("오류가 발생했습니다. 잠시 후, 다시 시도해주세요\n==오류 코드==\n" + arrS[1]);
                    ab.setPositiveButton("확인", null);
                    ab.show();
                }
            }
        }
    }

    /**
     * A7_HOME_F -> A7_ClassRoom
     */
    @Override
    public void onFragmentHome(String s) {
        //여기서 중복로그인 체크!
        //중복 로그인 체크
        String result;
        result = netConnForOverlapCheck();
        Log.i(TAG, "중복 로그인 결과 ===> " + result);

        if (result != "") {
            logoutByLoginOverlap(result);
        }
        else {
            if (s.equals("ori")) {
                if (si.getOrientation().equals("")) {
                    netConnForWriteOrientation();
                } else {
                    goOrientation();
                }
            }
        }
    }

    /**
     * 오리엔테이션을 들으러 간다. -> A8_Learning
     */
    private void goOrientation() {
        if (si.getOrientation().equals("")) {
            SharedPreferences savedOrien = getSharedPreferences("PARTI_ACT", MODE_PRIVATE);
            SharedPreferences.Editor editor = savedOrien.edit();
            editor.putString("SAVE_ORIEN", "True");
            editor.commit();
        }

        try {
            Log.i(TAG, "getDirectoryName =========> " + si.getDirectoryName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Intent intent = new Intent(A7_ClassRoom.this, A8_Learning.class);
        intent.putExtra("JUCHA", 0);
        intent.putExtra("CHASI", 0);
        intent.putExtra("CID", "0000");
        intent.putExtra("WATCHED_TIME", "00");
        intent.putExtra("FULL_TIME", "00");
        intent.putExtra("FILE_ROOT", "");
        intent.putExtra("ENABLE", false);
        intent.putExtra("studyInfo", si);

        startActivity(intent);
        finish();
    }

    /**
     * A7_JuchaListAdapter -> A7_ClassRoom
     * A7_Tab1_F 의 리스트에서 실행되지만 결국은 이 Activity로 결과값이 온다.
     */
    @Override
    public void onItemClicked(String sCid, String eCid, int jucha, int chasi,
                              String watchedTiem, String fullTime, String fileRoot, boolean enable) {
        //여기서 중복로그인 체크!
        //중복 로그인 체크
        String result;
        result = netConnForOverlapCheck();
        Log.i(TAG, "중복 로그인 결과 ===> " + result);

        if (result != "") {
            logoutByLoginOverlap(result);
        }
        else {
            Log.i(TAG, "차시 선택 => " + si.getClassId() + ", " + sCid + ", " + eCid + ", " + fileRoot);

            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setPositiveButton("알겠습니다", null);

            if (jucha == 7 || jucha == 15) {
                ab.setTitle("시험안내");
                ab.setMessage("시험은 PC로만 응시가능합니다.");
                ab.show();
            }
            else if (jucha > maxJucha) {
                ab.setTitle("열람불가");
                ab.setMessage("이 강의는 아직 열리지 않았습니다.");
                ab.show();
            }
            else {
                Log.i(TAG, "go Learning");
//            loading = new ShowLoading(this, "강의를 불러오는 중입니다");
//            loading.start();

                goLearning(jucha, chasi, eCid, watchedTiem, fullTime, fileRoot, enable);
            }
        }
    }

    /**
     * A8_Learning 으로 이동
     */
    private void goLearning(int jucha, int chasi, String eCid, String watchedTime,
                            String fullTime, String fileRoot, boolean enable) {
        Log.i(TAG, "학점 인정 가능한 수업 ==> " + enable);

        Intent intent = new Intent(this, A8_Learning.class);
        intent.putExtra("CLASS_ID", si.getClassId());
        intent.putExtra("JUCHA", jucha);
        intent.putExtra("CHASI", chasi);
        intent.putExtra("CID", eCid);
        intent.putExtra("WATCHED_TIME", watchedTime);
        intent.putExtra("FULL_TIME", fullTime);
        intent.putExtra("DIRECTORY_NAME", si.getDirectoryName());
        intent.putExtra("FILE_ROOT", fileRoot);
        intent.putExtra("ENABLE", enable);
        intent.putExtra("studyInfo", si);

        startActivity(intent);
        finish();
    }

    /**
     * AttendHome -> A7_ClassRoom
     * 리사이클러뷰 아이템 터치시
     */
    @Override
    public void onItemClicked_home(final int jucha) {
        //여기서 중복로그인 체크!
        //중복 로그인 체크
        String result;
        result = netConnForOverlapCheck();
        Log.i(TAG, "중복 로그인 결과 ===> " + result);

        if (result != "") {
            logoutByLoginOverlap(result);
        }
        else {
            Log.i(TAG, "jucha ==>> " + jucha);
            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setPositiveButton("알겠습니다", null);

            if (jucha == 7 || jucha == 15) {
                ab.setTitle("시험안내");
                ab.setMessage("시험은 PC로만 응시가능합니다.");
                ab.show();
            }
            else if (jucha > maxJucha) {
                ab.setTitle("열람불가");
                ab.setMessage("이 강의는 아직 열리지 않았습니다.");
                ab.show();
            }
            else if (jucha < minJucha) {
                ab.setTitle("학점인정 기간 초과");
                ab.setMessage("학점인정 기간이 지난강의에요\n복습하시겠어요?");
                ab.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        reviewLessions(jucha);
                    }
                });
                ab.setNegativeButton("아니요", null);
                ab.show();
            }
            else {
                if (si.getOrientation() == "") {
                    showOrientationAlert();
                }
                else {
                    reviewLessions(jucha);
//                switchFragment(NOW);
//                setTabHighlight(NOW);
                }
            }
        }
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
                //moveToA2_Login
                initCertyAndFinish();
            }
        }).show();
    }

    /**
     * 공인인증서 상태 초기화 및 finish()
     */
    public void initCertyAndFinish() {
        SharedPreferences savedCertyState = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);
        SharedPreferences.Editor editor = savedCertyState.edit();
        editor.putBoolean("certyState", false);
        editor.commit();

        finish();
    }

    /**
     * 학습인정기간이 지난 강의 보기 로직
     */
    private void reviewLessions(int _jucha) {
//        Log.i(TAG, "choise jucha ====> " + _jucha);
        choiseJucha = _jucha;

        loading = new ShowLoading(this, "강의를 불러오는 중입니다");
        loading.start();

        switchFragment(NOW);
        setTabHighlight(NOW);
    }

    /**
     * A7_Tab1_F -> A7_ClassRoom
     */
    @Override
    public void onFragmentInteraction(String s) {
        Log.i(TAG, "from Frag : " + s);
    }

    /**
     * A7_Home_F 의 공지사항을 터치
     */
    @Override
    public void onItemClicked_noti(int notiId) {
        Log.i(TAG, "noti ID ==> " + notiId);

        Intent intent = new Intent(A7_ClassRoom.this, ClassNotiContents.class);
        intent.putExtra("NOTI_ID", notiId);
        startActivity(intent);
    }

    /**
     * 뒤로가기 이벤트
     */
//    @Override
    public void onBackPressed() {
        //탭간 이동
//        super.onBackPressed();
        for (Fragment fragment: getSupportFragmentManager().getFragments()) {
            if (fragment.isVisible()) {
                if (fragment instanceof A7_Home_F) {
                    Intent intent = new Intent(this, A6_StudyCenter.class);
                    intent.putExtra("studyInfo", si);
                    startActivity(intent);
                    finish();
                }
                else {
                    switchFragment(HOME);
                    setTabHighlight(HOME);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loading != null) {
            loading.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
