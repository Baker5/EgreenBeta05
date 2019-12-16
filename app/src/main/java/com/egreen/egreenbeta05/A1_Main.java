package com.egreen.egreenbeta05;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.egreen.egreenbeta05.Adapter.A1_NoticeAdapter;
import com.egreen.egreenbeta05.Data.A1_NoticeListData;
import com.egreen.egreenbeta05.Dialog.UpdateNotify;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/*
* 2017.1.12
* 작성자 : 조윤성
*
* 배너와 주요학사일정 등 공지사항을 보여주는 Activity
* */

public class A1_Main extends AppCompatActivity implements NetworkAsyncTasker.AsyncResponse {
    private static final String TAG = A1_Main.class.getSimpleName();

    /* View 변수 선언 */
    ViewFlipper a1_bannerFlipper;       //배너
    ImageView a1_bannerImg1, a1_bannerImg2, a1_bannerImg3;     //Flipper에 들어가는 배너 이미지
    ListView a1_noticeList;      //공지사항 리스트

    //앱 버전
    String storeVersion = "", appVersion = "";
    ShowLoading showLoading;

    //AsyncTask 결과값을 받기위한 변수
    NetworkAsyncTasker asyncTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a1_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        /* View 연결 */
        a1_bannerFlipper = findViewById(R.id.a1_bannerFlipper);        //배너 Flipper
        a1_noticeList = findViewById(R.id.a1_noticeList);      //공지사항

        /* 배너 이미지를 자동으로 넘김 */
        a1_bannerFlipper.setFlipInterval(2000);
        a1_bannerFlipper.startFlipping();

        /* 네트워크 연결 상태 확인 */
        NetworkStateCheck netCheck = new NetworkStateCheck(this);
        if(netCheck.isConnectionNet()) {
            showLoading = new ShowLoading(this, "앱 버전을 확인합니다.");
            showLoading.start();

            /* 앱 버전체크 */
            new ThreadForNewAppInstall().start();

            /* 배너 */
            showBanner();

            /* 공지사항 */
            netConnForGetNotify();
        }

        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM Log", "getInstanceId failed", task.getException());
                            return;
                        }

                        String token = task.getResult().getToken();
                        Log.d("FCM Log", "FCM 토큰 ==> " + token);
//                        Toast.makeText(A1_Main.this, token, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 공지사항을 받아오는 웹 서버의 통신이다.
     */
    private void netConnForGetNotify() {
        String url = "http://cb.egreen.co.kr/mobile_proc/index_m2.asp";

        asyncTask = new NetworkAsyncTasker(this, url, null);
        asyncTask.execute();
    }

    /**
     * 공지사항 AsyncTask 결과
     */
    @Override
    public void processFinish(String result, String what) {
        Log.i(TAG, "======>>>>>>>>>" + result);

        if (result.equals("FAIL")) {
            //통신 오류
        }
        else {
            parsing_json(result);
        }
    }

    private void parsing_json(String result) {
        JSONObject jsObj = new JSONObject();
        final ArrayList<A1_NoticeListData> noticeListData = new ArrayList<A1_NoticeListData>();

        try {
            JSONArray jsArray = new JSONArray(result);

            for (int i = 0; i < jsArray.length(); i++) {
                jsObj = jsArray.getJSONObject(i);

                String flag = jsObj.getString("Flag");
                String noticeTitle = jsObj.getString("strTitle");
                String noticeCid = jsObj.getString("cId");

                int noticeFlag = Integer.parseInt(flag);
                int noticeimportance = 0;

                if (noticeFlag != 0) {
                    noticeimportance = 0;
                } else {
                    noticeimportance = 1;
                }

                /* ListView 에 data를 넣는 작업 */
                A1_NoticeListData data = new A1_NoticeListData(noticeimportance, noticeCid, noticeTitle);
                noticeListData.add(data);
            }

            /* ListView 에 data를 넣기 위한 Adapter 설정 */
            A1_NoticeAdapter a1_noticeAdapter = new A1_NoticeAdapter(getApplicationContext(), R.layout.a1_cs_notice_list, noticeListData);
            a1_noticeAdapter.notifyDataSetChanged();                 //변경되는 data가 있다면 새로고침이 자동으로 됨
            a1_noticeList.setAdapter(a1_noticeAdapter);
            a1_noticeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Log.i(TAG, "공지사항 URL: " + noticeListData.get(position).getNoticeCid());
                    Intent intent = new Intent(A1_Main.this, A1_NoticeWebView.class);
                    intent.putExtra("NOTICE", noticeListData.get(position).getNoticeCid());
                    startActivity(intent);
                }
            });
        } catch (JSONException e) {
            Log.i(TAG, "notice JSON Exc: " + e.getMessage());
        }
    }

    /**
     * 상단 배너 이미지를 보여준다.
     */
    private void showBanner() {
        /* 배너 ImageView를 ArrayList에 추가 */
        a1_bannerImg1 = findViewById(R.id.a1_bannerImg1);     //배너 이미지1
        a1_bannerImg2 = findViewById(R.id.a1_bannerImg2);     //배너 이미지2
        a1_bannerImg3 = findViewById(R.id.a1_bannerImg3);     //배너 이미지3

        Glide.with(this)
                .load("http://cb.egreen.co.kr/banner/images/image01.jpg")
                .skipMemoryCache(true)                              //메모리 캐싱 끄기
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)      //변형된 이미지만 캐싱
                .into(a1_bannerImg1);
        Glide.with(this)
                .load("http://cb.egreen.co.kr/banner/images/image02.jpg")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(a1_bannerImg2);
        Glide.with(this)
                .load("http://cb.egreen.co.kr/banner/images/image03.jpg")
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                .into(a1_bannerImg3);
    }

    /**
     * 스토어의 앱버전을 가져오기 위한 쓰레드 and 핸들러
     */
    private class ThreadForNewAppInstall extends Thread {
        public void run() {
//            Log.i(TAG, "getPackageName => " + getPackageName());
            GetMarketVersion j_arketVersion = new GetMarketVersion();
            storeVersion = j_arketVersion.getMarketVersion(getPackageName());

            try {
                appVersion = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception e) {
                Log.i(TAG, "앱버전 가져오기 오류: " + e.getMessage());
            }

            handler.sendEmptyMessage(0);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            if (msg.what == 0) {
                //Message Id 가 0이면,
                //앱 버전체크
                versionCheck();
            }
        }
    };

    /**
     * 플레이스토어에서 앱 버전 가져오기
     * 이건 반드시 메인쓰레드와 별개의 쓰레드로 작동시켜야한다.
     */
    private void versionCheck() {
        if (showLoading != null) {
            showLoading.stop();
        }

        Log.i(TAG, "스토어버전: " + storeVersion + ", 앱 버전: " + appVersion);
        final SharedPreferences updateRead = getSharedPreferences("UPDATE_READ", MODE_PRIVATE);

        if (appVersion.equals(storeVersion)) {
            Log.i(TAG, "READ ====> " + updateRead.getBoolean("READ", false));
            if (updateRead.getBoolean("READ", false) == false) {
                UpdateNotify upNotiDig = new UpdateNotify(this, R.layout.update_notify_dig);
                upNotiDig.callResultDialog();
                upNotiDig.setCsDialogListener(new UpdateNotify.CsDialogListener() {
                    @Override
                    public void onPositiveClicked(String s) {
                        if (s.equals("OK")) {
                            SharedPreferences.Editor editor = updateRead.edit();
                            editor.putBoolean("READ", true);
                            editor.commit();
                        }
                    }
                });
            }
            else {
                Toast.makeText(A1_Main.this, "최신 버전입니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else {
            //최신버전이 아닐때
            SharedPreferences.Editor editor = updateRead.edit();
            editor.putBoolean("READ", false);
            editor.commit();

            AlertDialog.Builder ab = new AlertDialog.Builder(this);
            ab.setMessage("*업데이트 알림*\n새로운 버전이 업데이트 되었습니다.\n업데이트 하시겠어요?");
            ab.setPositiveButton("업데이트", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    goMarket(A1_Main.this, "com.egreen.egreenbeta05");
                }
            });
            ab.setNegativeButton("그냥 쓸래요", null);
            ab.show();
        }
    }

    /**
     * 플레이스토어로 이동~~~
     */
    public static void goMarket(Activity caller, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        caller.startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_c, menu);
        return true;
    }

    /**
     * 오른쪽 로그인 아이콘 액션
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.login:
                moveActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * 모바일 로그인인지 아닌지 체크 완료 로직
     */
    private void moveActivity() {
        Intent intent = new Intent(A1_Main.this, A2_Login.class);
        startActivity(intent);        //A2_Login 으로 이동
//        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (showLoading != null) {
            showLoading.stop();
        }
    }
}
