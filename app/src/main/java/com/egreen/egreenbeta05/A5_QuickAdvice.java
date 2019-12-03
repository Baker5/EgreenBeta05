package com.egreen.egreenbeta05;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

/*
* 2017.1.12
* 작성자 : 조윤성
*
* 하단의 '빠른상담' Activity
* */

public class A5_QuickAdvice extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = A5_QuickAdvice.class.getSimpleName();

    /* View 변수 선언 */
    EditText a5_QAInputName, a5_QAInputPhone2, a5_QAInputPhone3, a5_QAInputAdvice;     //이름, 핸드폰번호, 문의 사항 입력 필드
    Spinner a5_QAInputPhone1, a5_QAClassSelect, a5_QATimeSelect;       //희망과정 선택
    Button a5_QABt;     //문의하기 버튼

    /* 선택한 희망과정을 담는 변수 */
    String selectClass = "", selectTime = "", selectFirstPhoneNum = "";

    /* '-' 포함된 휴대폰 번호를 저장 */
    String phoneNumberWithHyphen;

    ////////////////////// ----------------------- onCreate -------------------------------///////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a5_quick_advice);

        /* View 연결 */
        a5_QAInputName      = findViewById(R.id.a5_QAInputName);
        a5_QAInputAdvice    = findViewById(R.id.a5_QAInputAdvice);
        a5_QAInputPhone2    = findViewById(R.id.a5_QAInputPhone2);
        a5_QAInputPhone3    = findViewById(R.id.a5_QAInputPhone3);

        a5_QAClassSelect    = findViewById(R.id.a5_QAClassSelect);
        a5_QATimeSelect     = findViewById(R.id.a5_QATimeSelect);
        a5_QAInputPhone1    = findViewById(R.id.a5_QAInputPhone1);

        a5_QABt     = findViewById(R.id.a5_QABt);

        /* View.setOnClickListener */
        a5_QABt.setOnClickListener(this);

        /* 희망과정선택 Spinner 설정 */
        final String [] classArr = getResources().getStringArray(R.array.classArr);
        ArrayAdapter<String> a5_QAClassSelectAdapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, classArr);
        a5_QAClassSelect.setAdapter(a5_QAClassSelectAdapter);
        a5_QAClassSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectClass = classArr[position];
                } else {
                    selectClass = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        /* 상담희망시간 선택 Spinner 설정 */
        final String [] timeArr = getResources().getStringArray(R.array.timeArr);
        ArrayAdapter<String> a5_QATimeSelectAdapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, timeArr);
        a5_QATimeSelect.setAdapter(a5_QATimeSelectAdapter);
        a5_QATimeSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    selectTime = timeArr[position];
                } else {
                    selectTime = "";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        final String [] phoneArr = getResources().getStringArray(R.array.phoneArr);
        ArrayAdapter<String> a5_QAPhoneSelectAdapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, phoneArr);
        a5_QAInputPhone1.setAdapter(a5_QAPhoneSelectAdapter);
        a5_QAInputPhone1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectFirstPhoneNum = phoneArr[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        });

        Intent intent = getIntent();
        if (intent != null) {
            a5_QAInputName.setText(intent.getStringExtra("USER_NAME"));
        }

        /* Test Setup */
//        a5_QAInputName.setText("조윤성");
//        a5_QAInputPhone.setText("01052876741");
//        a5_QAInputAdvice.setText("모바일 테스트");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a5_QABt:
                NetworkStateCheck netCheck = new NetworkStateCheck(this); //네트워크 연결 상태 확인
                if (netCheck.isConnectionNet()) {
                    if (inputCheck()) {
                        netConnForQuickAdvice();
                    }
                }
                break;
        }
    }

    /**
     * 간단상담 남기기 메소드~~~~~
     */
    private void netConnForQuickAdvice() {
        String phone2 = a5_QAInputPhone2.getText().toString().trim();
        String phone3 = a5_QAInputPhone3.getText().toString().trim();

        phoneNumberWithHyphen = selectFirstPhoneNum + "-" + phone2 + "-" + phone3;

        String name = a5_QAInputName.getText().toString().trim();
        String phoneNumber = phoneNumberWithHyphen;
        String wantClass = selectClass;
        String wantTime = selectTime;
        String question = a5_QAInputAdvice.getText().toString();

        ContentValues cValue = new ContentValues();
        cValue.put("uname", name);
        cValue.put("uHakwi", wantClass);
        cValue.put("uphone", phoneNumber);
        cValue.put("utime", wantTime);
        cValue.put("uInquiry", question);

        String url = "http://cb.egreen.co.kr/mobile_proc/quickCounsel_m2.asp";

        NetworkConnect networkConnect = new NetworkConnect(url, cValue);
        networkConnect.execute();
    }

    /* 학번찾기 입력 필드 체크 */
    private boolean inputCheck() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        String name = a5_QAInputName.getText().toString().trim();
        String phone2 = a5_QAInputPhone2.getText().toString().trim();
        String phone3 = a5_QAInputPhone3.getText().toString().trim();
        String phone = "";
        String advice = a5_QAInputAdvice.getText().toString().trim();

        final InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);     //키보드 올리기

        if (name.equals("")) {      /* 이름 필드 체크 */
            ab.setMessage("이름을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a5_QAInputName.requestFocus();
                    imm.showSoftInput(a5_QAInputName, 0);
                }
            });
            ab.show();
        }
        else if (phone2.equals("")) {     /* 생년월일 필드 체크 */
            ab.setMessage("전화번호를 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a5_QAInputPhone2.requestFocus();
                    imm.showSoftInput(a5_QAInputPhone2, 0);
                }
            });
            ab.show();
        }
        else if (phone3.equals("")) {     /* 생년월일 필드 체크 */
            ab.setMessage("전화번호를 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a5_QAInputPhone3.requestFocus();
                    imm.showSoftInput(a5_QAInputPhone3, 0);
                }
            });
            ab.show();
        }
        else if (selectClass.equals("")) {
            ab.setMessage("상담희망과정을 선택해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            }).show();
        }
        else if (selectTime.equals("")) {
            ab.setMessage("상담희망시간을 선택해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) { }
            }).show();
        }
        else if (advice.equals("")) {      /* 이메일 필드 체크 */
            ab.setMessage("입력된 내용이 없으면 기본 메세지로 전송됩니다.\n\n[기본메세지]\n\"상담 부탁드립니다. 감사합니다.\"\n\n전송 하시겠습니까?");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a5_QAInputAdvice.setText("상담 부탁드립니다.\n감사합니다.");
                }
            }).show();
        }
        else {
//            Log.i(TAG, "name: " + name + ", birth: " + birth + ", email: " + email);

            return true;
        }
        return false;
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

            String [] result = s.split("\\[");
            AlertDialog.Builder ab = new AlertDialog.Builder(A5_QuickAdvice.this);

            if (result[0].equals("success")) {
                ab.setMessage("등록되었습니다.\n상담희망시간에 연락드리겠습니다.\n감사합니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(A5_QuickAdvice.this, A1_Main.class));
                                finish();
                            }
                        }).show();
            } else {
                ab.setMessage("오류가 발생하여 등록에 실패했습니다.\n잠시 후 다시 시도해주세요.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(A5_QuickAdvice.this, A1_Main.class));
                                finish();
                            }
                        }).show();
            }
        }
    }
}