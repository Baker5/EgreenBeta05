package com.egreen.egreenbeta05;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

/*
* 2017.1.12
* 작성자 : 조윤성
*
* 학번 찾기 Activity
* */

public class A4_FindId extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = A4_FindId.class.getSimpleName();

    /* View 변수 선언 */
    public EditText a4_findIdInputName, a4_findIdInputBirth, a4_findIdInputEmail1, a4_findIdInputEmail2;        //이름, 생일, 이메일 입력 필드
    Spinner a4_emailSpinner;

    Button a4_findIdIdBt;        //학번 찾기 버튼

    String birthData;

    //키보드 컨트롤
    InputMethodManager imm;

    ////////////////////// ----------------------- onCreate -------------------------------///////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a4_find_id);
        /* View 연결 */
        a4_findIdInputName = findViewById(R.id.a4_findIdInputName);     //이름 입력
        a4_findIdInputBirth = findViewById(R.id.a4_findIdInputBirth);    //생년월일 입력
        a4_findIdInputEmail1 = findViewById(R.id.a4_findIdInputEmail1);    //이메일 입력
        a4_findIdInputEmail2 = findViewById(R.id.a4_findIdInputEmail2);

        a4_findIdIdBt = findViewById(R.id.a4_findIdBt);      //학번 찾기

        a4_emailSpinner = findViewById(R.id.a4_emailSpinner);

        /* View.setOnClickListener */
        a4_findIdIdBt.setOnClickListener(this);

        //키보드 올리기/내리기 설정
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        a4_findIdInputBirth.setInputType(InputType.TYPE_NULL);      //키보드, 커서 안나옴
        a4_findIdInputBirth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                    imm.hideSoftInputFromWindow(a4_findIdInputBirth.getWindowToken(), 0);
                    DatePickerFragment datePickerFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("CALL_DATEPICKER_BY_THIS", "A3_FindId");
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
                    a4_findIdInputEmail2.setText(emailArr[position]);
                } else {
                    a4_findIdInputEmail2.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        });

        /* Test Setup */
//        a4_findIdInputName.setText("조윤성");
//        a4_findIdInputBirth.setText("1987년 2월 3일");
//        birthData = "19870203";
//        a4_findIdInputEmail.setText("ys.hazard@gmail.com");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.a4_findIdBt:
                NetworkStateCheck netCheck = new NetworkStateCheck(this); //네트워크 연결 상태 확인
                if (netCheck.isConnectionNet()) {
                    if (isInputCheck()) {       //각 입력 필드 확인
                        netConnForFindId();
                    }
                }
                break;
        }
    }

    /**
     * 학번 찾기 메소드!
     */
    private void netConnForFindId() {
        String email1 = a4_findIdInputEmail1.getText().toString().trim();
        String email2 = a4_findIdInputEmail2.getText().toString().trim();
        String emailDomain = email1 + "@" + email2;            //이메일 완성

        if (checkEmail(emailDomain)) {
            String name = a4_findIdInputName.getText().toString().trim();
            String birth = birthData;
            String email = emailDomain;

            String year = birth.substring(0, 4);
            String month = birth.substring(4, 6);
            String day = birth.substring(6, 8);

            birth = year + "-" + month + "-" + day;

            ContentValues cValue = new ContentValues();
            cValue.put("userName", name);
            cValue.put("userBirth", birth);
            cValue.put("userEmail", email);

            String url = "http://cb.egreen.co.kr/mobile_proc/findUserInfo/find_userId_m2.asp";

            NetworkConnect networkConnect = new NetworkConnect(url, cValue);
            networkConnect.execute();
        }
    }

    /* 학번찾기 입력 필드 체크 */
    private boolean isInputCheck() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        String name = a4_findIdInputName.getText().toString().trim();      //이름
        String birth = a4_findIdInputBirth.getText().toString().trim();        //생년월일
        String email1 = a4_findIdInputEmail1.getText().toString().trim();        //사용자 이메일
        String email2 = a4_findIdInputEmail2.getText().toString().trim();        //이메일 도메인

        if (name.equals("")) {      /* 이름 필드 체크 */
            ab.setMessage("이름을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findIdInputName.requestFocus();
                    a4_findIdInputName.setText("");
                    imm.showSoftInput(a4_findIdInputName, 0);
                }
            });
            ab.show();
        }
        else if (birth.equals("")) {     /* 생년월일 필드 체크 */
            ab.setMessage("생년월일을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findIdInputBirth.requestFocus();
                }
            });
            ab.show();
        }
        else if (email1.equals("")) {      /* 이메일 필드 체크 */
            ab.setMessage("e메일을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findIdInputEmail1.requestFocus();
                    a4_findIdInputEmail1.setText("");
                    imm.showSoftInput(a4_findIdInputEmail1, 0);
                }
            }).show();
        }
        else if (email2.equals("")) {      /* 이메일 도메인 체크 */
            ab.setMessage("도메인을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a4_findIdInputEmail2.requestFocus();
                    imm.showSoftInput(a4_findIdInputEmail2, 0);
                }
            }).show();
        }
        else {
            return true;
        }

        return false;
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
                    a4_findIdInputEmail1.requestFocus();
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

            AlertDialog.Builder ab = new AlertDialog.Builder(A4_FindId.this);
            ab.setCancelable(false);

            String[] result = s.trim().split("\\<");
            if (!result[0].equals("[]")) {

                //Custom Alert를 띄우기 위해 Custom된 layout을 불러옴
                LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View customLayout = inflater.inflate(R.layout.a4_cs_find_id_result_alert, null);

                String id = "";
                String name = "";

                TextView a4_id = (TextView) customLayout.findViewById(R.id.a4_id_strNum);
                TextView a4_name = (TextView) customLayout.findViewById(R.id.a4_id_name);
                Button a4_login = (Button) customLayout.findViewById(R.id.a4_id_login);
                Button a4_findPw = (Button) customLayout.findViewById(R.id.a4_id_findPw);

                JSONObject jsObj = new JSONObject();

                try {
                    JSONArray jsArr = new JSONArray(s);

                    for (int i = 0; i < jsArr.length(); i++) {
                        jsObj = jsArr.getJSONObject(i);

                        id = jsObj.getString("cId").trim();
                        name = jsObj.getString("strName");
                    }

                    a4_id.setText(id);
                    a4_name.setText(name);

                    final String tName = name;
                    final String tId = id;
                    final String tBirth = a4_findIdInputBirth.getText().toString();
                    final String tBirthData = birthData;
                    final String tEmail1 = a4_findIdInputEmail1.getText().toString();
                    final String tEmail2 = a4_findIdInputEmail2.getText().toString();

                    ab.setView(customLayout);
                    final AlertDialog dismissAb;
                    dismissAb = ab.create();
                    dismissAb.show();

                    /* 학번을 찾고 '로그인하기' 버튼을 눌렀을때, A2_Login Activity로 이동. ID 전달 */
                    a4_login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(A4_FindId.this, A2_Login.class);
                            intent.putExtra("ID", tId);

                            dismissAb.dismiss();

                            startActivity(intent);
                            finish();
                        }
                    });

                    /* 학번을 찾고 '비밀번호 찾기' 버튼을 눌렀을때, A4_FindPw Activity로 이동. ID 전달 */
                    a4_findPw.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(A4_FindId.this, A4_FindPw.class);
                            intent.putExtra("NAME", tName);
                            intent.putExtra("ID", tId);
                            intent.putExtra("BIRTH", tBirth);
                            intent.putExtra("BIRTH_DATA", tBirthData);
                            intent.putExtra("EMAIL1", tEmail1);
                            intent.putExtra("EMAIL2", tEmail2);

                            dismissAb.dismiss();

                            startActivity(intent);
                            finish();
                        }
                    });
                } catch (JSONException e) {
                    Log.i(TAG, "JSONArray Exc: " + e.getMessage());
                }
            } else {
                ab.setMessage("등록된 회원이 아닙니다.")
                        .setPositiveButton("확인", null)
                        .show();
            }
        }
    }
}
