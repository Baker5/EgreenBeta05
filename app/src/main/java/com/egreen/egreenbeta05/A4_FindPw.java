package com.egreen.egreenbeta05;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

/*
* 2017.1.12
* 작성자 : 조윤성
*
* 비밀번호 찾기 Activity
* */

public class A4_FindPw extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = A4_FindPw.class.getSimpleName();

    /* View 변수 선언 */
    public static EditText a4_findPwInputName, a4_findPwInputStrNumber, a4_findPwInputBirth, a4_findPwInputEmail1, a4_findPwInputEmail2;     //이름, 학번, 생년월일, 이메일 입력 필드
    Spinner a4_emailSpinner;

    Button a4_findPwBt;     //비밀번호 찾기 버튼

    String birthData;

    //키보드 컨트롤
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a4_find_pw);

        /* View 연결 */
        a4_findPwInputName      = findViewById(R.id.a4_findPwInputName);     //이름 입력
        a4_findPwInputStrNumber = findViewById(R.id.a4_findPwInputStrNumber);        //학번 입력
        a4_findPwInputBirth     = findViewById(R.id.a4_findPwInputBrith);        //생년월일 입력
        a4_findPwInputEmail1    = findViewById(R.id.a4_findPwInputEmail1);        //이메일 입력
        a4_findPwInputEmail2    = findViewById(R.id.a4_findPwInputEmail2);        //도메인 입력
        a4_findPwBt             = findViewById(R.id.a4_findPwBt);      //비밀번호 찾기

        a4_emailSpinner         = findViewById(R.id.a4_emailSpinner);   //e메일 스피너

        /* View.setOnClickListener */
        a4_findPwBt.setOnClickListener(this);

        //키보드 올리기/내리기 설정
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        a4_findPwInputBirth.setInputType(InputType.TYPE_NULL);      //키보드, 커서 안나옴
        a4_findPwInputBirth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    imm.hideSoftInputFromWindow(a4_findPwInputBirth.getWindowToken(), 0);
                    DatePickerFragment datePickerFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("CALL_DATEPICKER_BY_THIS", "A3_FindPw");
                    datePickerFragment.setArguments(bundle);

                    DialogFragment newPicker = datePickerFragment;
                    newPicker.show(getSupportFragmentManager(), "datePicker");
                }

                return false;
            }
        });

        final String [] emailArr = getResources().getStringArray(R.array.emailArr);
        ArrayAdapter<String> a4_FindSelectAdapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, emailArr);
        a4_emailSpinner.setAdapter(a4_FindSelectAdapter);
        a4_emailSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    a4_findPwInputEmail2.setText(emailArr[position]);
                } else {
                    a4_findPwInputEmail2.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        });

        /* 학번찾기 Activity에서 전달된 자료를 UI Set */
        Intent intent = getIntent();
        if (intent.getStringExtra("ID") != null) {
            a4_findPwInputStrNumber.setText(intent.getStringExtra("ID"));
            a4_findPwInputName.setText(intent.getStringExtra("NAME"));
            a4_findPwInputBirth.setText(intent.getStringExtra("BIRTH"));
            birthData = intent.getStringExtra("BIRTH_DATA");
            a4_findPwInputEmail1.setText(intent.getStringExtra("EMAIL1"));
            a4_findPwInputEmail2.setText(intent.getStringExtra("EMAIL2"));
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a4_findPwBt:
                NetworkStateCheck netCheck = new NetworkStateCheck(this); //네트워크 연결 상태 확인
                if (netCheck.isConnectionNet()) {
                    if (isInputCheck()) {       //각 입력 필드 확인
                        netConnForFindPw();
                    }
                }
                break;
        }
    }

    /**
     * 비밀번호 찾기 메소드~~~~~~
     */
    private void netConnForFindPw() {
        String email1 = a4_findPwInputEmail1.getText().toString().trim();
        String email2 = a4_findPwInputEmail2.getText().toString().trim();
        String emailDomain = email1 + "@" + email2;            //이메일 완성

        if (checkEmail(emailDomain)) {
            String id = a4_findPwInputStrNumber.getText().toString().trim();
            String name = a4_findPwInputName.getText().toString().trim();
            String birth = birthData;
            String email = emailDomain;

            /* 생년월일 포맷 */
            String year = birth.substring(0, 4);
            String month = birth.substring(4, 6);
            String day = birth.substring(6, 8);

            birth = year + "-" + month + "-" + day;

            ContentValues cValue = new ContentValues();
            cValue.put("userName", name);
            cValue.put("userId", id);
            cValue.put("userBirth", birth);
            cValue.put("userEmail", email);

            String url = "http://cb.egreen.co.kr/mobile_proc/findUserInfo/find_userPw_m2.asp";

            NetworkConnect networkConnect = new NetworkConnect(url, cValue);
            networkConnect.execute();
        }
    }

    /* 비밀번호찾기 입력 필드 체크 */
    private boolean isInputCheck() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        String name = a4_findPwInputName.getText().toString().trim();
        String strNum = a4_findPwInputStrNumber.getText().toString().trim();
        String birth = a4_findPwInputBirth.getText().toString().trim();
        String email1 = a4_findPwInputEmail1.getText().toString().trim();
        String email2 = a4_findPwInputEmail2.getText().toString().trim();

        if (name.equals("")) {      /* 이름 필드 체크 */
            ab.setMessage("이름을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findPwInputName.requestFocus();
                    imm.showSoftInput(a4_findPwInputName, 0);
                }
            });
            ab.show();
        }
        else if (strNum.equals("")) {     /* 학번 필드 체크 */
            ab.setMessage("학번을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findPwInputStrNumber.requestFocus();
                    imm.showSoftInput(a4_findPwInputStrNumber, 0);
                }
            });
            ab.show();
        }
        else if (birth.equals("")) {     /* 생년월일 필드 체크 */
            ab.setMessage("생년월일을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findPwInputBirth.requestFocus();
                }
            });
            ab.show();
        }
        else if (email1.equals("")) {      /* 이메일 필드 체크 */
            ab.setMessage("e메일을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findPwInputEmail1.requestFocus();
                    imm.showSoftInput(a4_findPwInputEmail1, 0);
                }
            }).show();
        }
        else if (email2.equals("")) {      /* 이메일 필드 체크 */
            ab.setMessage("도메인을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findPwInputEmail2.requestFocus();
                    imm.showSoftInput(a4_findPwInputEmail2, 0);
                }
            }).show();
        }
        else {
            return true;
        }

        return false;
    }

    private boolean checkBirth(String birth) {
        if (birth.length() < 8 || birth.length() > 8) {
            return true;
        } else {
            return false;
        }
    }

    /* 이메일 유효성 체크 */
    private boolean checkEmail(String email) {
        boolean emailRegular = Pattern.matches("^([\\w\\.\\~\\-]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", email.trim());
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        if (emailRegular == false) {
            ab.setMessage("사용할 수 없는 e메일 입니다.\ne메일을 다시 확인해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findPwInputEmail1.requestFocus();
                }
            }).show();

            return false;
        }

        return true;
    }

    private class NetworkConnect extends AsyncTask<Void, Void, String> {
        private String url;
        private ContentValues values;

        public NetworkConnect(String url, ContentValues value) {
            this.url = url;
            this.values = value;
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

            AlertDialog.Builder ab = new AlertDialog.Builder(A4_FindPw.this);
            ab.setCancelable(false);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View customLayout = inflater.inflate(R.layout.a4_cs_find_pw_result_alert, null);

            TextView a4_name = (TextView) customLayout.findViewById(R.id.a4_pw_name);
            TextView a4_init = (TextView) customLayout.findViewById(R.id.a4_pw_init);
            Button a4_login = (Button) customLayout.findViewById(R.id.a4_pw_login);

            Date date = new Date();
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
            String initData = format.format(date);
            a4_init.setText("생년월일(8자리, 예:" + initData + ")");

            String [] result = s.trim().split("\\[");
            if (result[0].equals("Success")) {
                a4_name.setText(a4_findPwInputName.getText().toString() + " 학습자님의 비밀번호가");

                a4_login.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(A4_FindPw.this, A2_Login.class);
                        intent.putExtra("ID", a4_findPwInputStrNumber.getText().toString());

                        startActivity(intent);
                        finish();
                    }
                });

                ab.setView(customLayout);
                ab.show();
            } else {
                ab.setMessage("등록된 회원이 아닙니다.")
                        .setPositiveButton("확인", null)
                        .show();
            }
        }
    }
}
