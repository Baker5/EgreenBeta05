package com.egreen.egreenbeta05;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

/*
* 2017.1.12
* 작성자 : 조윤성
*
* 회원가입 Activity
* */

public class A3_Join extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = A3_Join.class.getSimpleName();

    /* View 변수 선언 */
    public static EditText a3_inputName, a3_inputBirth, a3_inputPhone2, a3_inputPhone3, a3_inputEmail1, a3_inputEmail2;       //이름, 생일 입력, 핸드폰 번호, 이메일 입력 필드
    private static EditText a3_inputPw, a3_reInputPw;        //비밀번호, 비밀번호 재입력 입력 필드
    Spinner a3_inputPhone1, a3_emailSpinner;

    Button btn_male, btn_female, a3_joinBt;       //회원가입 버튼

    /* Custom Alert View 변수 선언 */
    TextView a3_strNum, a3_name, a3_birth, a3_email, a3_phone; //결과 Alert의 학번, 이름, 생년월일, 이메일, 핸드폰번호
    Button a3_login;    //결과 Alert의 로그인 버튼

    String birthData, emailDomain, selectFirstPhoneNum;

    /* '-' 포함된 휴대폰 번호를 저장 */
    String phoneNumberWithHyphen;

    /* 성별선택 */
    String whatIsYourSex;
    ArrayList<Button> btnSexArr = new ArrayList<>();

    //키보드 컨트롤
    InputMethodManager imm;

    ////////////////////// ----------------------- onCreate -------------------------------///////////////////
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a3_join);

        /* View 연결 */
        a3_inputName    = findViewById(R.id.a3_inputName);       //이름 입력
        a3_inputBirth   = findViewById(R.id.a3_inputBirth);      //생년월일 입력
        a3_inputPw      = findViewById(R.id.a3_inputPw);         //비밀번호 입력
        a3_reInputPw    = findViewById(R.id.a3_reInputPw);       //비밀번호 재입력

        btn_male        = findViewById(R.id.btn_male);
        btn_female      = findViewById(R.id.btn_female);

        a3_inputPhone1  = findViewById(R.id.a3_inputPhone1);      //핸드폰 번호 입력1
        a3_inputPhone2  = findViewById(R.id.a3_inputPhone2);      //핸드폰 번호 입력2
        a3_inputPhone3  = findViewById(R.id.a3_inputPhone3);      //핸드폰 번호 입력3
        a3_inputEmail1  = findViewById(R.id.a3_inputEmail1);      //이메일 입력
        a3_inputEmail2  = findViewById(R.id.a3_inputEmail2);      //이메일 입력

        a3_joinBt       = findViewById(R.id.a3_joinBt);        //회원가입

        a3_emailSpinner = findViewById(R.id.a3_emailSpinner);

        //키보드 올리기/내리기 설정
        imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        /* 성별 초기 설정 */
        btn_male.setOnClickListener(this);
        btn_female.setOnClickListener(this);
        btnSexArr.add(btn_male);
        btnSexArr.add(btn_female);
        whatIsYourSex = "남";
        setSexChoise(whatIsYourSex);

        /* View.setOnClickListener */
        a3_joinBt.setOnClickListener(this);

        /* 생년월일을 입력할 수 있는 DatepickerFragment를 호출한다. */
        a3_inputBirth.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    DatePickerFragment datePickerFragment = new DatePickerFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("CALL_DATEPICKER_BY_THIS", "A3_Join");
                    datePickerFragment.setArguments(bundle);

                    DialogFragment newPicker = datePickerFragment;
                    newPicker.show(getSupportFragmentManager(), "datePicker");
                }

                return false;
            }
        });

        /* 핸드폰 앞자리를 선택할 수 있는 Spinner를 호출한다. */
        final String [] phoneArr = getResources().getStringArray(R.array.phoneArr);
        ArrayAdapter<String> a5_QAPhoneSelectAdapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, phoneArr);
        a3_inputPhone1.setAdapter(a5_QAPhoneSelectAdapter);
        a3_inputPhone1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectFirstPhoneNum = phoneArr[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        });

        /* e메일을 선택할 수 있는 스피너틀 호출한다. */
        final String [] emailArr = getResources().getStringArray(R.array.emailArr);
        ArrayAdapter<String> a4_FindSelectAdapter
                = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, emailArr);
        a3_emailSpinner.setAdapter(a4_FindSelectAdapter);
        a3_emailSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position != 0) {
                    a3_inputEmail2.setText(emailArr[position]);
                } else {
                    a3_inputEmail2.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {  }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_male:
                whatIsYourSex = "남";
                setSexChoise(whatIsYourSex);
                break;
            case R.id.btn_female:
                whatIsYourSex = "여";
                setSexChoise(whatIsYourSex);
                break;
            case R.id.a3_joinBt:
                NetworkStateCheck netCheck = new NetworkStateCheck(this); //네트워크 연결 상태 확인
                if(netCheck.isConnectionNet()) {
                    if (isInputCheck()) {       //각 입력 필드 확인
                        netConnForJoin();
                    }
                }
                break;
        }
    }

    /**
     * 회원가입~~~~~
     */
    private void netConnForJoin() {
        String phone2 = a3_inputPhone2.getText().toString();
        String phone3 = a3_inputPhone3.getText().toString();

        phoneNumberWithHyphen = selectFirstPhoneNum + "-" + phone2 + "-" + phone3;

        if (passwordMatch()) {
            String email1 = a3_inputEmail1.getText().toString();
            String email2 = a3_inputEmail2.getText().toString();
            emailDomain = email1 + "@" + email2;            //이메일 완성

            if (checkEmail(emailDomain)) {
                String name = a3_inputName.getText().toString();
                String birth = birthData;
                String password = a3_inputPw.getText().toString();
                String phoneNumber = phoneNumberWithHyphen;
                String email = emailDomain;
                String permission = "10";

                /* 생년월일 포맷 */
                String year = birth.substring(0, 4);
                String month = birth.substring(4, 6);
                String day = birth.substring(6, 8);

                birth = year + "-" + month + "-" + day;

                ContentValues cValue = new ContentValues();
                cValue.put("strName", name);
                cValue.put("birth", birth);

                String gender;
                if (whatIsYourSex.equals("남")) {
                    gender = "1";
                }
                else {
                    gender = "2";
                }
                cValue.put("sex", gender);
                cValue.put("pass", password);
                cValue.put("phone", phoneNumber);
                cValue.put("email", email);
                cValue.put("permission", permission);

                String url = "http://cb.egreen.co.kr/mobile_proc/join/join_proc_m2.asp";

//                                Log.i(TAG, name +"/" + birth +"/" + gender +"/" + phoneNumber +"/" + email);

                NetworkConnect networkConnect = new NetworkConnect(url, cValue);
                networkConnect.execute();
            }
        }
    }

    /**
     * 비밀번호 입력 상태 확인 - 두번째 입력과 일치하는지(잘 입력했는지 확인한다.)
     */
    private boolean passwordMatch() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        String pw = a3_inputPw.getText().toString();        //비밀번호
        String rPw = a3_reInputPw.getText().toString();     //비밀번호 재입력

        final Boolean isMatch;

        if (!rPw.equals(pw)) {
            isMatch = false;

            ab.setMessage("비밀번호가 일치하지 않습니다.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_reInputPw.setText("");
                    a3_reInputPw.requestFocus();
                    imm.showSoftInput(a3_reInputPw, 0);
                }
            }).show();
        }
        else {
            isMatch = true;
        }

        return isMatch;
    }

    /**
     * 성별 클릭 하이라이트
     */
    private void setSexChoise(String _sex) {
        for (int i=0; i<btnSexArr.size(); i++) {
            if (btnSexArr.get(i).getText().toString().equals(_sex)) {
                btnSexArr.get(i).setTextColor(Color.WHITE);
                btnSexArr.get(i).setBackgroundResource(R.drawable.btn_back_color);
            }
            else {
                btnSexArr.get(i).setTextColor(Color.BLACK);
                btnSexArr.get(i).setBackgroundResource(R.drawable.btn_back_color_white);
            }
        }
    }

    /**
     * 회원가입 입력 필드 체크
     */
    private boolean isInputCheck() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        String name = a3_inputName.getText().toString().trim();        //이름
        String birth = a3_inputBirth.getText().toString().trim();      //생년월일
        String pw = a3_inputPw.getText().toString().trim();        //비밀번호
        String rPw = a3_reInputPw.getText().toString().trim();     //비밀번호 재입력
        String phone2 = a3_inputPhone2.getText().toString().trim();      //핸드폰 번호2
        String phone3 = a3_inputPhone3.getText().toString().trim();      //핸드폰 번호3
        String email1 = a3_inputEmail1.getText().toString().trim();      //이메일
        String email2 = a3_inputEmail2.getText().toString().trim();      //이메일2

        if (name.equals("")) {      /* 이름 필드 체크 */
            ab.setMessage("이름을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputName.setText("");
                    a3_inputName.requestFocus();
                    imm.showSoftInput(a3_inputName, 0);
                }
            });
            ab.show();
        }
        else if (birth.equals("")) {     /* 생년월일 필드 체크 */
            ab.setMessage("생년월일을 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputBirth.setText("");
                    a3_inputBirth.requestFocus();
                }
            });
            ab.show();
        }
        else if (pw.equals("")) {        /* 비밀번호 필드 체크 */
            ab.setMessage("비밀번호를 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputPw.setText("");
                    a3_inputPw.requestFocus();
                    imm.showSoftInput(a3_inputPw, 0);
                }
            }).show();
        }
        else if (rPw.equals("")) {        /* 비밀번호 재입력 필드 체크 */
            ab.setMessage("비밀번호를 한번 더 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_reInputPw.setText("");
                    a3_reInputPw.requestFocus();
                    imm.showSoftInput(a3_reInputPw, 0);
                }
            }).show();
        }
        else if (phone2.equals("")) {     /* 핸드폰번호 필드 체크 */
            ab.setMessage("핸드폰 번호를 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputPhone2.setText("");
                    a3_inputPhone2.requestFocus();
                    imm.showSoftInput(a3_inputPhone2, 0);
                }
            }).show();
        }
        else if (phone3.equals("")) {     /* 핸드폰번호 필드 체크 */
            ab.setMessage("핸드폰 번호를 입력해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputPhone3.setText("");
                    a3_inputPhone3.requestFocus();
                    imm.showSoftInput(a3_inputPhone3, 0);
                }
            }).show();
        }
        else if (email1.equals("")) {      /* 이메일 필드 체크 */
            ab.setMessage("e메일을 확인해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputEmail1.requestFocus();
                    imm.showSoftInput(a3_inputEmail1, 0);
                }
            }).show();
        }
        else if (email2.equals("")) {      /* 이메일 필드 체크 */
            ab.setMessage("e메일을 확인해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputEmail2.requestFocus();
                    imm.showSoftInput(a3_inputEmail2, 0);
                }
            }).show();
        }
        else {
            return true;
        }

        return false;
    }

    /**
     * 이메일 유효성 체크
     */
    private boolean checkEmail(String email) {
        boolean emailRegular = Pattern.matches("^([\\w\\.\\~\\-]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([\\w-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$", email.trim());
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        if (emailRegular == false) {
            ab.setMessage("사용할 수 없는 e메일 입니다.\ne메일을 다시 확인해주세요.");
            ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    a3_inputEmail1.requestFocus();
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

            AlertDialog.Builder ab = new AlertDialog.Builder(A3_Join.this);
            ab.setCancelable(false);

            try {
                String[] result = (s.trim().split("\\<"));

                if (result[0].equals("already account")) {      //이미 등록된 회원이라면
                    ab.setMessage("이미 등록된 회원입니다.");
                    ab.setPositiveButton("확인", null).show();
                }
                else if (result[0].equals("already email")) {
                    ab.setMessage("해당 email로 가입된 이력이 있습니다.");
                    ab.setPositiveButton("확인", null).show();
                }
                else if (result[0].equals("empty")) {
                    ab.setMessage("회원가입중 오류가 발생했습니다\n본 원으로 문의해주세요!");
                    ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).show();
                }
                else {
                    //가입 성공시, 학번 및 입력된 사항들을 보여줌
                    LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View customLayout = inflater.inflate(R.layout.a3_cs_join_result, null);

                    final String strNum = result[0];
                    String name = a3_inputName.getText().toString();
                    String birth = a3_inputBirth.getText().toString();
                    String phone = phoneNumberWithHyphen;
                    String email = emailDomain;

                    a3_strNum = customLayout.findViewById(R.id.a3_strNum);
                    a3_name = customLayout.findViewById(R.id.a3_name);
                    a3_birth = customLayout.findViewById(R.id.a3_birth);
                    a3_phone = customLayout.findViewById(R.id.a3_phone);
                    a3_email = customLayout.findViewById(R.id.a3_email);
                    a3_login = customLayout.findViewById(R.id.a3_login);

                    a3_strNum.setText(strNum);
                    a3_name.setText(name);
                    a3_birth.setText(birth);
                    a3_phone.setText(phone);
                    a3_email.setText(email);

                    ab.setView(customLayout);

                    final AlertDialog dismissAb;
                    dismissAb = ab.create();
                    dismissAb.setCanceledOnTouchOutside(false);

                    dismissAb.show();

                    a3_login.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(A3_Join.this, A2_Login.class);
                            intent.putExtra("ID", strNum);

                            dismissAb.dismiss();

                            startActivity(intent);
                            finish();
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.i(TAG, "회원가입 오류 : " + e.getMessage());
            }
        }
    }
}
