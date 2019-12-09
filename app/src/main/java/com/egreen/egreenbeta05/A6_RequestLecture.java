package com.egreen.egreenbeta05;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.net.URISyntaxException;

import androidx.appcompat.app.AppCompatActivity;

public class A6_RequestLecture extends AppCompatActivity {

    private static final String TAG = A6_RequestLecture.class.getSimpleName();
    private static final String DOMAIN = "http://cb.egreen.co.kr/mobile_proc/lecture/";

    private static final int DIALOG_PROGRESS_WEBVIEW = 0;
    private static final int DIALOG_PROGRESS_MESSAGE = 1;
    private static final int DIALOG_ISP = 2;

    /* View 변수 선언 */
    WebView a6_lecutreList;

    /* 상태 저장 */
    String id;
    SharedPreferences.Editor editor;

    /* WebView가 전부 로딩될때까지 보여줄 Dialog */
    ProgressDialog loadingDialog;
    AlertDialog alertIsp;

    /* 이니페이에서 뒤로가기 버튼을 위한 변수 */
    String iniUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.a6_request_lecture);

        /* View 연결 */
        a6_lecutreList  = findViewById(R.id.a6_lectureList);

        /* 웹뷰가 모두 로딩될때까지 보여줄 Dialog */
        loadingDialog = new ProgressDialog(this);
        loadingDialog.setCancelable(false);
        loadingDialog.setMessage("잠시만 기다려주세요.");
        loadingDialog.setIndeterminate(true);
        loadingDialog.setCancelable(false);
        loadingDialog.setCanceledOnTouchOutside(false);

        /* WebView 설정 */
        a6_lecutreList.setWebViewClient(new WebBrowserClient());
        a6_lecutreList.setWebChromeClient(new webViewCromeClient());
        a6_lecutreList.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        a6_lecutreList.clearCache(true);

        a6_lecutreList.getSettings().setJavaScriptEnabled(true);
        a6_lecutreList.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

        a6_lecutreList.addJavascriptInterface(new JSCall(), "jscall");

        a6_lecutreList.getSettings().setSupportMultipleWindows(true);
        a6_lecutreList.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);

        // 저장된 학번, 이름을 가져온다.
        id = getUserId();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            a6_lecutreList.getSettings().setMixedContentMode(a6_lecutreList.getSettings().MIXED_CONTENT_ALWAYS_ALLOW);
//            cookieManager.setAcceptThirdPartyCookies(a6_lecutreList, true);
        }
//        CookieSyncManager.getInstance().sync();

        String urlData = "cUserId=" + id;
        Log.i(TAG, "urlData: " + urlData);
        a6_lecutreList.loadUrl(DOMAIN + "lecture_list_m.asp?" + urlData);

        showGuide();
    }

    public void showGuide() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);

        String guide = "" +
                "모바일에서는 개강일을 선택하여 신청할 수 없습니다." +
                "\n다른 개강일의 수강신청을 원하면 PC로 신청해주세요." +
                "\n\n수강신청현황은 홈페이지에서 확인할 수 있습니다." +
                "\n\n30만원 이상 신용카드결제시 '공인인증서' 인증이 필요합니다." +
                "\n\n신청 취소는 본원으로 문의주세요.";

        ab.setTitle("안내사항")
                .setMessage(guide)
                .setPositiveButton("확인", null)
                .show();
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

            Log.i(TAG, "수신된 메시지: " + s);

            if (s.equals("Success")) {
                //로그아웃
                editor.putInt("LOGINSUCCESS", 0);               //로그인상태 1(로그인 상태), 0(로그아웃 상태)
                editor.putBoolean("LOGOUT", true);
                editor.putInt("WHENMOVEACTIVITY", 5);
                editor.commit();

                startActivity(new Intent(A6_RequestLecture.this, A2_Login.class));
                finish();
            }
        }
    }

    private class WebBrowserClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.i(TAG, "실행중인 url: " + url);

            if (!url.startsWith("http://") && !url.startsWith("https://") && !url.startsWith("javascript:")) {
                Intent intent;

                try {
//                    Log.d("<INIPAYMOBILE", "intent url : " + url);
                    intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);

//                    Log.d("<INIPAYMOBILE", "intent getDataString : " + intent.getDataString());
//                    Log.d("<INIPAYMOBILE", "intent getPackage : " + intent.getPackage());
                } catch (URISyntaxException e) {
                    Log.e("<INIPAYMOBILE", "URI syntax error : " + url + " : " + e.getMessage());

                    return false;
                }

                Uri uri = Uri.parse(intent.getDataString());
                intent = new Intent(Intent.ACTION_VIEW, uri);

                try {
                    startActivity(intent);
                    /*
                        가맹점의 사정에 따라 현재 화면을 종료하지 않아도 됨
                        삼성카드 기타 안심클릭에서는 종료되면 안되기 때문에,
                        조건을 걸어 종료하도록 하겠음
                     */

                    if (url.startsWith("ispmobile://")) {
                        finish();
                    }
                } catch (ActivityNotFoundException e) {
//                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, ActivityNotFoundException Input >> " + url);
//                    Log.e("INIPAYMOBILE", "INIPAYMOBILE, uri.getScheme() " + intent.getDataString());

                    if (url.startsWith("ispmobile://")) {
                        //ISP
                        view.loadData("<html><body></body></html>", "text/html", "euc-kr");
                        onAlertShow(DIALOG_ISP);

                        return false;
                    } else if (url.startsWith("intent://")) {
                        //INTENT:// 인입될시 예외처리

                        try {
                            Intent excepIntent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                            String packageNm = excepIntent.getPackage();

//                            Log.d("<INIPAYMOBILE>", "excepIntent getPackage: " + packageNm);
                            excepIntent = new Intent(Intent.ACTION_VIEW);
                            /*
                                가맹점별로 원하는 방식을 사용하면 됨
                                market URL
                                market://search?q="+packageNm => packageNm 을 검색어로 마켓, 검색 페이지 이동
                                market://search?q=pname:"+packageNm => packageNm 을 패키지로 갖는 앱 검색, 페이지 이동
                                market://details?id="+packageNm => packageNm 에 해당하는 앱 상세 페이지로 이동
                             */
                            excepIntent.setData(Uri.parse("market://search?q=" + packageNm));
                            startActivity(excepIntent);

                        } catch (URISyntaxException ex) {
                            Log.e("<INIPAYMOBILE>", "INTENT:// 인입될시 예외 처리 오류 : " + ex);
                        }
                    }
                }        } else {
                view.loadUrl(url);

                return false;
            }

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
//            Log.i(TAG, "시작 URL: " + url);
            loadingDialog.show();

            iniUrl = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
//            Log.i(TAG, "끝 URL: " + url);

            loadingDialog.dismiss();
        }
    }

    private class webViewCromeClient extends WebChromeClient {
        //단순 alert을 띄울때는 onJsAlert ('확인' 버튼만 존재하는 단순 팝업)
        @Override
        public boolean onJsAlert(WebView view, String url, final String message, final JsResult result) {
//            Log.i(TAG, "Alert: " + message);

            new AlertDialog.Builder(A6_RequestLecture.this)
                    .setCancelable(false)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();

            return true;
        }

        //'확인', '취소' 등 두 개 이상의 버튼이 있는 팝업을 띄울때는 onJsConfirm 사용
        //'confirm'은 확인, 'cancel'은 취소
        @Override
        public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
//            Log.i(TAG, "Alert: " + message);

            new AlertDialog.Builder(A6_RequestLecture.this)
                    .setMessage(message)
                    .setPositiveButton(android.R.string.ok,
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.confirm();
                                }
                            })
                    .setNegativeButton(android.R.string.no,
                            new AlertDialog.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    result.cancel();
                                }
                            })
                    .setCancelable(false)
                    .create()
                    .show();

            return true;
        }

        /* 실명인증 팝업 허용 */
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            WebView newWV = new WebView(A6_RequestLecture.this);
            WebSettings webSettings = newWV.getSettings();
            webSettings.setJavaScriptEnabled(true);

            final Dialog dialog = new Dialog(A6_RequestLecture.this);
            dialog.setContentView(newWV);
            dialog.setCanceledOnTouchOutside(false);
            dialog.show();

            newWV.setWebChromeClient(new WebChromeClient() {
                @Override
                public void onCloseWindow(WebView window) {
//                    super.onCloseWindow(window);
                    dialog.dismiss();
                }
            });

            ((WebView.WebViewTransport)resultMsg.obj).setWebView(newWV);
            resultMsg.sendToTarget();

//            return super.onCreateWindow(view, isDialog, isUserGesture, resultMsg);
            return true;
        }
    }

    /* 샘플 url을 가져오는 인터페이스 */
    private class JSCall {
        @JavascriptInterface
        public void jsCall_Android(final String msg) {
            Handler handler = new Handler();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "JS 메세지: " + msg);
                    String[] splitMsg = msg.split("#");
                    AlertDialog.Builder ab = new AlertDialog.Builder(A6_RequestLecture.this);
//                    ab.setCancelable(false);
                    Boolean isBack = false;

                    if (splitMsg[0].equals("finish")) {
                        ab.setMessage("수강신청이 완료되었습니다. 감사합니다.");
                        isBack = true;
                    }
                    else if (splitMsg[0].equals("error")) {
                        ab.setMessage("결제도중 오류가 발생했습니다. 다시 시도해주세요" + msg);
                        isBack = true;
                    }
                    else if (splitMsg[0].equals("reTry")) {
                        Log.i(TAG, "" + msg);
                        ab.setMessage(splitMsg[1] + "님은 현재 " + splitMsg[2] + "개 과목이 신청되어있으며,\n최대 8과목까지 신청가능합니다.");
                    }
                    else if (splitMsg[0].equals("ov")) {
                        startActivity(new Intent(A6_RequestLecture.this, A2_Login.class));
                        finish();
                    }
                    else {        //중복과목 message 를 출력
                        ab.setMessage(splitMsg[0]);
                        isBack = true;
                    }

                    if (isBack) {
                        ab.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setResult(RESULT_OK);
                                finish();
                            }
                        });
                        ab.show();
                    }
                    else {
                        ab.setPositiveButton("확인", null);
                        ab.show();
                    }
                }
            });
        }
    }

    protected Dialog onAlertShow(int id) {
        switch (id) {
            case DIALOG_PROGRESS_WEBVIEW:
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage("로딩중입니다.\n잠시만 기다려주세요.");
                dialog.setIndeterminate(true);
                dialog.setCancelable(false);

                return dialog;

            case DIALOG_PROGRESS_MESSAGE:
                break;

            case DIALOG_ISP:
                alertIsp = new AlertDialog.Builder(A6_RequestLecture.this)
                        .setCancelable(false)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("알림")
                        .setMessage("모바일 ISP 어플리케이션이 설치되어 있지 않습니다.\n설치를 눌러 진행해주십시오.\n취소를 누르면 결제가 취소됩니다.")
                        .setPositiveButton("설치", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String ispUrl = "http://mobile.vpay.co.kr/jsp/MISP/andown.jsp";
                                a6_lecutreList.loadUrl(ispUrl);
                            }
                        })
                        .setNegativeButton("취쇠", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(A6_RequestLecture.this, "결제를 취소하였습니다", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }).create();

                return alertIsp;
        }

        return null;
    }

    private void logout_proc() {
        ContentValues cValue = new ContentValues();
        cValue.put("userid", id);

        String url = "http://cb.egreen.co.kr/mobile_proc/login/logout_proc.asp";

        NetworkConnect networkConnect = new NetworkConnect(url, cValue);
        networkConnect.execute();
    }

    @Override
    public void onBackPressed() {
        //수강신청 메뉴가 있을때 활성화
//        super.onBackPressed();
        if (iniUrl.contains("lecture_list_m.asp") || iniUrl.contains("lecture_list_m2.asp")) {
            setResult(RESULT_OK);
            finish();
        }
        else if (iniUrl.contains("inicis.com")) {
            quitRequestLecture();
        }
        else if (iniUrl.contains("certifyComplete_m.asp") || iniUrl.contains("certifyComplete_m2.asp")) {
            quitRequestLecture();
        }
        else if (iniUrl.contains("card_Complete.asp") || iniUrl.contains("card_Complete2.asp")) {
            quitRequestLecture();
        }
        else if (iniUrl.contains("vBank_Complete.asp") || iniUrl.contains("vBank_Complete2.asp")) {
            quitRequestLecture();
        }
        else {
            a6_lecutreList.goBack();
        }

        //수강신청 메뉴가 없을때 활성화
//        exit_proc();
    }

    /**
     * 회원 ID를 가져온다.
     */
    private String getUserId() {
        /*
         * 저장되어 있는 회원 ID를 가져온다.
         */
        SharedPreferences savedID = getSharedPreferences("LOGIN_INFO", MODE_PRIVATE);
        String id = "";
        try {
            id = savedID.getString("UID", "");
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (id.equals("")) {
            Log.i(TAG, "회원 ID 없다.");
        }
        else {
//            Log.i(TAG, id);
        }

        return id;
    }

    private void quitRequestLecture() {
        AlertDialog.Builder ab = new AlertDialog.Builder(this);
        ab.setCancelable(false);
        ab.setMessage("수강신청을 종료합니다")
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setResult(RESULT_OK);
                        finish();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart()");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart()");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause()");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop()");
    }
}
