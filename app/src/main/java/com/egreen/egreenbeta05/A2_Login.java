package com.egreen.egreenbeta05;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.egreen.egreenbeta05.Data.StudyInfo;
import com.egreen.egreenbeta05.Dialog.Notify;
import com.egreen.egreenbeta05.Dialog.RegDevice;

import java.io.UnsupportedEncodingException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

/*
 * 2017.1.12
 * 작성자 : 조윤성
 *
 * 로그인, 학번/비밀번호 찾기, 회원가입을 할 수 있는 Activity
 * */

public class A2_Login extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = A2_Login.class.getSimpleName();

    private final int PERMISSIONS_REQUEST_RESULT = 1;
    private static final String AF_ASK_LOGOUT = "ASK_LOGOUT";
    private static final String AF_LOGOUT_SUCCESS = "LOGOUT_SUCCESS";

    private static final String AF_NOID = "01";
    private static final String AF_NO_MATCH_PW = "02";
    private static final String AF_NO_MATCH_MKEY = "03";
    private static final String AF_STOP_USER = "11";
    private static final String AF_NO_DATA = "14";

    /* View 변수 목록 */
    private static EditText a2_inputID, a2_inputPASS;      //학번,비밀번호 입력 필드

    TextView version;           //버전
    Button a2_findID, a2_findPASS;  //학번찾기, 비번찾기
    Button a2_loginBt, a2_joinBt; //로그인, 회원가입
    Button btn_quick, btn_call; //빠른문의, 전화문의
    CheckBox chk_savedLoginInfo;        //학번, 비밀번호 저장

    String id, pw, userName, loginNumber;
    String serial;
    boolean _isNeedCertyLogin;
    SharedPreferences savedID_PW;

    /* 공인인증서 변수 */
    private String appKey = "2O4zh+t/DZJR0ifytsXeZw==";     //정식 라이센스
    private String[] rtnParams = {"a", "b", "c"};

    /* 로딩을 표시하는 Dialog */
    ShowLoading loading;

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.v(TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart()");
    }

    ////////////////////// ----------------------- onCreate -------------------------------///////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a2_login);

        /* View 연결 */
        a2_loginBt = findViewById(R.id.a2_loginBt);
        a2_inputID = findViewById(R.id.a2_inputID);         //학번 입력 필드
        a2_inputPASS = findViewById(R.id.a2_inputPASS);       //비밀번호 입력 필드
        a2_findID = findViewById(R.id.a2_findID);        //학번 찾기
        a2_findPASS = findViewById(R.id.a2_findPASS);      //비밀번호 찾기
        a2_joinBt = findViewById(R.id.a2_joinBt);        //회원가입
        version = findViewById(R.id.version);            //버전
        chk_savedLoginInfo = findViewById(R.id.chk_savedLoginInfo);     //학번, 비밀번호 저장 체크박스
        btn_quick = findViewById(R.id.btn_quick);       //빠른상담
        btn_call = findViewById(R.id.btn_call);        //전화문의

        /* View.setOnClickListner */
        a2_loginBt.setOnClickListener(this);
        a2_findID.setOnClickListener(this);
        a2_findPASS.setOnClickListener(this);
        a2_joinBt.setOnClickListener(this);
        btn_quick.setOnClickListener(this);
        btn_call.setOnClickListener(this);

        //저장된 학번 비번을 가져온다.
        String _id = "", _pw = "" ;
        savedID_PW = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);
        try {
            _id = savedID_PW.getString("UID", "");
            _pw = savedID_PW.getString("UPW", "");
        } catch (Exception e) {
            Log.i(TAG, e.getMessage());
        }

//        Log.i(TAG, "로그인 정보 => " + _id + "//" + _pw);

        if (_id.equals("")) {
            a2_inputID.setText("");
        }
        else {
            a2_inputID.setText(_id);
        }

        if (_pw.equals("")) {
            a2_inputPASS.setText("");
            chk_savedLoginInfo.setChecked(false);
        }
        else {
            a2_inputPASS.setText(_pw);
            chk_savedLoginInfo.setChecked(true);
        }

        //앱 버전 가져온다
        try {
            version.setText("V " + getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.i(TAG, "앱버전 가져오기 오류: " + e.getMessage());
        }

        /* Test Setup */
//        a2_inputID.setText("15200002");
//        a2_inputPASS.setText("19870203");

        checkPermissions();
    }

    /**
     * 권한 체크
     */
    private void checkPermissions() {
        /**
         * Pie버전 이상에서 기기 일련번호를 가져오기 위해서는
         * 권한 요청을 하고, 승인을 받아야 한다.
         * 사용자에게 직접 승인받아야 하는 요청은 인터넷으로 확인하자
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_PHONE_STATE)) {
//                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE, Manifest.permission.BLUETOOTH}, PERMISSIONS_REQUEST_RESULT);
//                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, PERMISSIONS_REQUEST_RESULT);
//                }

                return;
            } else {
                serial = Build.getSerial();
                Log.i(TAG, "serial : " + serial);
            }
        }
    }

    /**
     * 권한 요청 승인 결과 처리
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSIONS_REQUEST_RESULT:
                if (grantResults.length > 0) {
//                    for (int i=0; i<grantResults.length; i++) {
                        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                            //하나라도 거부한다면,
                            new AlertDialog.Builder(this).setTitle("알림")
                                    .setMessage("권한을 허용해 주셔야 앱을 이용할 수 있습니다")
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                            finish();
                                        }
                                    }).show();
                        }
                        else {
                            Toast.makeText(this, "승인되었습니다", Toast.LENGTH_SHORT).show();
                            checkPermissions();
                        }
//                    }
                }

                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a2_loginBt:
                NetworkStateCheck netCheck = new NetworkStateCheck(this);
                if (netCheck.isConnectionNet()) {
                    //인터넷이 연결되어 있는 상태
                    if (isEmpty()) {
                        doLogin();
//                    moveActivity(A6_StudyCenter.class);
                    }
                }
                break;
            case R.id.a2_findID:
                moveActivity(A4_FindId.class);
                break;
            case R.id.a2_findPASS:
                moveActivity(A4_FindPw.class);
                break;
            case R.id.a2_joinBt:
                moveActivity(A3_JoinTerms.class);
                break;
            case R.id.btn_quick:
                startActivity(new Intent(A2_Login.this, A5_QuickAdvice.class));      //A5_QuickAdvice.class로 이동
                break;
            case R.id.btn_call:
                startActivity(new Intent(Intent.ACTION_DIAL, Uri.parse("tel:1544-8463")));      //전화 걸기
                break;
        }
    }

    /**
     * 입력필드의 값을 확인한다.
     */
    private boolean isEmpty() {
        id = a2_inputID.getText().toString().trim();
        pw = a2_inputPASS.getText().toString().trim();

        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        if (id.equals("")) {
            ab.setMessage("학번을 입력해주세요");
            a2_inputID.setText("");
            a2_inputID.requestFocus();
        }
        else if (pw.equals("")) {
            ab.setMessage("비밀번호를 입력해주세요");
            a2_inputPASS.setText("");
            a2_inputPASS.requestFocus();
        }
        else {
//            for (int i=0; i<pw.length(); i++) {
//                if (!Character.isLetterOrDigit(pw.charAt(i))) {
                    //특수문자가 있는 경우

//                    Log.i(TAG, "특수문자가 있다 ===> " + pw.charAt(i));
//                }
//                else {
//                    //특수문자가 없는 경우
//                    Log.i(TAG, "특수문자가 없다");
//                }
//            }

            return true;
        }

        ab.setPositiveButton("확인", null);
        ab.show();

        return false;
    }

    /**
     * 로그인 네트워크 통신
     */
    private void doLogin() {
        loading = new ShowLoading(this, "로그인 중입니다.");
        loading.start();

        /*순서
            1.로그인이 되어있는 중인지 확인
            2.로그인 중이라면 로그아웃 할 것인지 물어본 후,
            3.로그인
        */
        String ipAddress = getConnectIpAddress();
        Log.i(TAG, ipAddress);

        String encodingPw;
        try {
            encodingPw = URLEncoder.encode(pw, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            encodingPw = pw;
        }

        String url = "http://cb.egreen.co.kr/mobile_proc/login/new/login_proc_m2.asp";
        ContentValues cValue = new ContentValues();
        cValue.put("userId", id);
        cValue.put("userPw", encodingPw);
        cValue.put("userIp", getConnectIpAddress());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
            cValue.put("uMobileKey", Build.SERIAL);
        }
        else {
            cValue.put("uMobileKey", serial);
        }
        cValue.put("uMobileBrand", Build.BRAND + "::" + Build.MODEL);

        DoLoginNetTask doLoginNT = new DoLoginNetTask(url, cValue);
        doLoginNT.execute();
    }

    /**
     * 학생정보를 조회하기 위한 AsyncTask
     */
    private class DoLoginNetTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public DoLoginNetTask(String url, ContentValues values) {
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

            if (loading != null) {
                loading.stop();
            }

            if (result.equals("FAIL")) {
                //네트워크 통신 오류
            } else {
                //로그인 학번 조회 결과를 처리한다.
                loginResult_proc(result);
            }
        }
    }

    /**
     * 로그인 조회 결과를 처리한다.
     * 인증서 유예자 처리.
     */
    private void loginResult_proc(String result) {
        String[] loginData = result.split(",");
        if (loginData[0].equals("Err")) {
            //오류 처리~
            error_proc(loginData[1]);
        }
        else if (loginData[0].equals("server_check")) {
            //혹시나... 서버에 문제가 있을때..
            showAlert(AF_ASK_LOGOUT, "죄송합니다.\n서버점검으로 인해 앱 수강이 어렵습니다.\n홈페이지를 참고해주세요");
        }
        else {
            //OK, 성공 로직~
            userName = loginData[1];     //학습자 이름
            boolean isNeedCertyLogin = Boolean.parseBoolean(loginData[2]);      //인증서 로그인 필요유무
            String postponementTerm = "";

            try {
                postponementTerm = loginData[3];     //인증서 유예 기간
            } catch (Exception e) {
                e.printStackTrace();
            }

            _isNeedCertyLogin = isNeedCertyLogin;
            if (isNeedCertyLogin) {
                //공인인증서 유예자라면,
                AlertDialog.Builder ab = new AlertDialog.Builder(this);
                ab.setTitle("공인인증서 유예 로그인");
                ab.setMessage(userName + "님은 공인인증서 유예자 입니다.\n유예기간은 " + postponementTerm + "까지 입니다.\n범용공인인증서를 준비해주세요.");
                ab.setPositiveButton("알겠습니다", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //필독 안내 사항Dialog를 띄운다.
                        SharedPreferences sharedNotify = getSharedPreferences("NOTIFY", MODE_PRIVATE);
                        if (sharedNotify.getBoolean("CANCEL_CHECK", false)) {
                            success_proc();
                        }
                        else {
                            //notify dialog 를 띄워 공지를 보여준다.
                            //'다시보지않기' 를 선택하지 않는 동안 계속 실행된다.
                            show_notify();
                        }
                    }
                });
                ab.show();

            } else {
                success_proc();
            }
        }
    }

    /**
     * 최종 로그인 완료, 학번/비번 저장하기 선택에 따른 학번/비번 저장,
     * A6_StudyCenter 로 이동한다.
     */
    private void success_proc() {
        loading = new ShowLoading(this, "로그인 중입니다");
        loading.start();

        String url = "http://cb.egreen.co.kr/mobile_proc/login/new/login_insert_m2.asp";
        ContentValues cValues = new ContentValues();
        cValues.put("userId", id);
        cValues.put("uMobileBrand", Build.BRAND + "::" + Build.MODEL);

        LoginSuccessNetTask loginSuccessNT = new LoginSuccessNetTask(url, cValues);
        loginSuccessNT.execute();
    }

    /**
     * 로그인 기록을 남기기 위한 AsyncTask
     */
    private class LoginSuccessNetTask extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public LoginSuccessNetTask(String url, ContentValues values) {
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
                String [] arrResult = new String[0];

                if (result != "") {
                    arrResult = result.split(",");
                    Log.i(TAG, "arrResult =>" + arrResult[0] + ", arrResult[1] =>" + arrResult[1]);

                    if (result.equals("Err")) {
                        AlertDialog.Builder ab = new AlertDialog.Builder(A2_Login.this);
                        ab.setMessage("죄송합니다.\n로그인 중 예상치 않은 오류가 발생했습니다\n본 원으로 오류메세지를 알려주세요!\n==오류 내용==\n");
                        ab.setPositiveButton("확인", null);
                        ab.show();
                    }
                    else {
                        loginNumber = arrResult[1];
                        SharedPreferences.Editor editor = savedID_PW.edit();

                        editor.putString("UID", id);
                        editor.putBoolean("isNeedCerty", _isNeedCertyLogin);
                        editor.putBoolean("certyState", false);

                        if (chk_savedLoginInfo.isChecked()) {
                            Log.i(TAG, "학번: " + id);
                            Log.i(TAG, "비번: " + pw);
                            editor.putString("UPW", pw);                //비밀번호 저장
                        } else {
                            a2_inputPASS.setText("");
                            editor.putString("UPW", "");
                        }
                        editor.commit();

                        moveActivity(A6_StudyCenter.class);
                    }
                }
                else {
                    Log.i(TAG, "전달 받은 값 없음");
                }
            }
        }
    }

    /**
     * Activity 를 이동한다.
     */
    private void moveActivity(Class activity) {
        StudyInfo si = new StudyInfo();
        si.setUserId(id);
        si.setUserName(userName);
        si.setLoginNumber(loginNumber);

        Intent intent = new Intent(this, activity);
        intent.putExtra("studyInfo", si);
        startActivity(intent);
//        finish();
    }

    /**
     * 로그인 오류 처리
     */
    private void error_proc(String errCode) {
        if (errCode.equals(AF_NOID) || errCode.equals(AF_NO_MATCH_PW)) {         //학번 불일치
            showAlert(AF_NOID, "학번 또는 비밀번호가 다릅니다.\n다시한번 확인해주세요.");
        }
        else if (errCode.equals(AF_NO_MATCH_MKEY)) {
//            RegDevice regDevice = new RegDevice(this, R.layout.reg_device_dig);
//            regDevice.callResultDialog();
//            regDevice.setCsDialogListener(new RegDevice.CsDialogListener() {
//                @Override
//                public void onPositiveClicked(String s) {
//                    Log.i(TAG, "단말기 변경 하겠슴까?: " + s);
//
//                    if (s.equals("OK")) {
//                        changeMobileLogin();
//                    }
//                }
//            });
        }
        else if (errCode.equals(AF_STOP_USER)) {
            showAlert(AF_STOP_USER, "일시정지된 회원은 로그인할 수 없습니다.\n본원에 문의주세요!");
        }
        else if (errCode.equals(AF_NO_DATA)) {
            showAlert(AF_NO_DATA, "없는 학번입니다.\n회원가입을 해주세요.");
        }
        else {
            showAlert("", "예상치 못한 오류가 발생했습니다.\n본 원으로 오류메세지를 알려주세요!\n==오류 내용==\n" + errCode);
        }
    }

//    /**
//     * 모바일 기기 변경 통신을 한다.
//     */
//    private void changeMobileLogin() {
//        if (isEmpty()) {
//            String url = "http://cb.egreen.co.kr/mobile_proc/findUserInfo/new/reDevice_m2.asp";
//            ContentValues cValues = new ContentValues();
//
//            cValues.put("userId", id);      //새로 접속한 모바일 기기에 아이디가 저장되어 있을 수 없다.
//
//            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
//                cValues.put("userMKey", Build.SERIAL);
//            } else {
//                cValues.put("userMKey", serial);
//            }
//            cValues.put("userMDevice", Build.BRAND + "::" + Build.MODEL);
//
//            ChangeMobileNetTask changeMobileNT = new ChangeMobileNetTask(url, cValues);
//            changeMobileNT.execute();
//        }
//    }
//
//    /**
//     * 기기 변경을 남기기 위한 AsyncTask
//     */
//    private class ChangeMobileNetTask extends AsyncTask<Void, Void, String> {
//        private String url;
//        private ContentValues values;
//
//        public ChangeMobileNetTask(String url, ContentValues values) {
//            this.url = url;
//            this.values = values;
//        }
//
//        @Override
//        protected String doInBackground(Void... voids) {
//            String result;
//
//            NetworkConnection nc = new NetworkConnection();
//            result = nc.request(url, values);
//
//            return result;
//        }
//
//        @Override
//        protected void onPostExecute(String s) {
//            super.onPostExecute(s);
//            String result = s;
//
//            if (result.equals("FAIL")) {
//                //네트워크 통신 오류
//            }
//            else {
//                AlertDialog.Builder ab = new AlertDialog.Builder(A2_Login.this);
//
//                if (result.equals("OK")) {
//                    ab.setMessage("변경되었습니다\n로그인 하시겠어요?");
//                    ab.setPositiveButton("예", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            doLogin();
//                        }
//                    });
//                    ab.setNegativeButton("아니오", null);
//                    ab.show();
//                }
//                else {
//                    ab.setTitle("기기 변경 오류");
//                    ab.setMessage("변경에 실패했습니다\n본 원으로 문의해주세요\n오류:" + result);
//                    ab.setPositiveButton("확인", null);
//                    ab.show();
//                }
//            }
//        }
//    }

    /**
     * Notify 띄우기
     */
    public void show_notify() {
        final Notify notify = new Notify(A2_Login.this, R.layout.notify_dig);
        notify.callResultDialog();
        notify.setCsDialogListener(new Notify.CsDialogListener() {
            @Override
            public void onPositiveClicked(String s) {
                Log.i(TAG, s);

                if (s.equals("CANCEL")) {
                    //'다시보지않기'를 선택하면 상태를 저장해둔다.
                    SharedPreferences notify = getSharedPreferences("NOTIFY", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = notify.edit();

                    editor.putBoolean("CANCEL_CHECK", true);
                    editor.commit();
                }

                success_proc();
            }
        });
    }

    /**
     * 상황에 맞는 Alert를 보여준다.
     */
    private void showAlert(final String flag, String msg) {
        AlertDialog.Builder ab = new AlertDialog.Builder(A2_Login.this);
        ab.setCancelable(false);
        ab.setMessage(msg);
        ab.setPositiveButton("확인", null);

        ab.show();
    }

    /**
     * 접속중인 IP 가져오기
     */
    public static String getConnectIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();

                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();

                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onBackPressed() {
//        startActivity(new Intent(this, A1_Main.class));
        finish();
    }

    @Override
    protected void onPause() {
        Log.v(TAG, "onPause()");

        if (loading != null) {
            loading.stop();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "onStop()");
    }
}
