package com.egreen.egreenbeta05;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;

/*
* 2017.1.12
* 작성자 : 조윤성
*
* Main에서 선택한 공지사항을 보여주는 Activity
* */

public class A1_NoticeWebView extends AppCompatActivity {
    private static final String TAG = A1_NoticeWebView.class.getSimpleName();

    /* View 변수 선언 */
    WebView a1_noticeWV;        //공지사항을 뿌려주는 웹뷰

    /* 로딩을 표시하는 Dialog */
    ProgressDialog loading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a1_notice_webview);

        /* 약관을 보여줄때까지 로딩 Dialog 설정 */
        loading = new ProgressDialog(A1_NoticeWebView.this);
        loading.setMessage("공지사항을 불러오는 중입니다.");
        loading.setIndeterminate(true);
        loading.setCancelable(true);

        /* View 연결 */
        a1_noticeWV     = findViewById(R.id.a1_noticeWV);     //공지사항

        /* WebView 설정 */
        a1_noticeWV.getSettings().setSupportZoom(true);
        a1_noticeWV.getSettings().setBuiltInZoomControls(true);
        a1_noticeWV.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        /* A3_Join 에서 전달받은 각 URL을 WebView에 뿌려줌 */
        Intent intent = getIntent();
        if (intent.getStringExtra("NOTICE") != null) {
            Log.i(TAG, "NOTICE: " + intent.getStringExtra("NOTICE"));

            a1_noticeWV.setWebViewClient(new WebBrowserClient());
            a1_noticeWV.loadUrl("http://cb.egreen.co.kr/mobile_proc/cs_notice_view.asp?cCid=" + intent.getStringExtra("NOTICE"));
        }
    }

    public class WebBrowserClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            loading.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            loading.dismiss();
        }
    }
}
