package com.egreen.egreenbeta05;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

/**
 * Created by JDirectory on 2018. 10. 31..
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    String activityName;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();          //오늘 날짜로 디폴트값을 설정하기 위해 캘린더 객체 선언
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);          //MONTH : 0 ~ 11
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);         //this는 리스너를 가르키는 이 프레그먼트 클래스 자신을 가르킴
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        Calendar ca = Calendar.getInstance();
        ca.set(year, monthOfYear, dayOfMonth);

        SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
        String data = format.format(ca.getTime());

        String formatStr = year + "년 " + (monthOfYear+1) + "월 " + dayOfMonth + "일";

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            activityName = bundle.getString("CALL_DATEPICKER_BY_THIS", "NOT");
        }

        Log.i("PICKER", "by Activity: " + activityName);
        if (activityName.equals("A3_FindId")) {
            A4_FindId a4_findId = (A4_FindId) getActivity();
            a4_findId.a4_findIdInputBirth.setText(formatStr);     //유저가 선택한 날짜로 텍스트 변경
            a4_findId.birthData = data;        //서버로 전송될 포맷
        }
        else if (activityName.equals("A3_FindPw")){
            A4_FindPw a4_findPw = (A4_FindPw) getActivity();
            a4_findPw.a4_findPwInputBirth.setText(formatStr);     //유저가 선택한 날짜로 텍스트 변경
            a4_findPw.birthData = data;        //서버로 전송될 포맷
        }
        else if (activityName.equals("A3_Join")) {
            A3_Join a3_join = (A3_Join) getActivity();
            a3_join.a3_inputBirth.setText(formatStr);     //유저가 선택한 날짜로 텍스트 변경
            a3_join.birthData = data;        //서버로 전송될 포맷
        }
        else if (activityName.equals("StudyAgreement")) {
            StudyAgreement studyAgreement = (StudyAgreement) getActivity();
            studyAgreement.et_graduationDate.setText("졸업 " + year + "-" + (monthOfYear+1) + "-" + dayOfMonth);
        }
        else {
            Log.i("DATE_PICKER", "오류");
        }

        Log.i("DATE_PICKER", activityName + ": " + data);
    }
}
