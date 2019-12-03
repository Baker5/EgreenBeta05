package com.egreen.egreenbeta05;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.CheckBox;

public class A3_JoinTerms extends AppCompatActivity implements View.OnClickListener {

    //약관 체크버튼
    CheckBox chk1, chk2, chk3;
    ShowLoading loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a3_join_terms);

        chk1 = findViewById(R.id.check1);
        chk2 = findViewById(R.id.check2);
        chk3 = findViewById(R.id.check3);

        loading = new ShowLoading(this, "잠시만 기다려주세요");

        WebView terms1_txt = findViewById(R.id.terms1_txt);
        terms1_txt.setWebViewClient(new WebBrowserClient());
        terms1_txt.loadUrl("file:///android_asset/html/click-wrap.html");

        WebView terms2_txt = findViewById(R.id.terms2_txt);
        terms2_txt.loadUrl("file:///android_asset/html/personal-information.html");

        WebView terms3_txt = findViewById(R.id.terms3_txt);
        terms3_txt.loadUrl("file:///android_asset/html/trust.html");

        Button prevStep = findViewById(R.id.prevStep);
        Button nextStep = findViewById(R.id.nextStep);

        prevStep.setOnClickListener(this);
        nextStep.setOnClickListener(this);
    }

    private class WebBrowserClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            //컨텐츠 시작,
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
            //컨텐츠 종료
            loading.stop();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.prevStep:
                finish();
                break;
            case R.id.nextStep:
                if (chk1.isChecked() && chk2.isChecked() && chk3.isChecked()) {
                    startActivity(new Intent(this, A3_Join.class));
                    finish();
                }
                else {
                    //모두 동의를 안했을때 알럿을 띄운다.
                    showAlert();
                }
                break;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (loading != null) {
            loading.stop();
        }
    }

    private void showAlert() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setMessage("모두 동의해주셔야 회원가입이 가능합니다");
        ab.setPositiveButton("확인", null);
        ab.show();
    }
}