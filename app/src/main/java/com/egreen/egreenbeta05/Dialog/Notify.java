package com.egreen.egreenbeta05.Dialog;

import android.app.Dialog;
import android.content.Context;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.egreen.egreenbeta05.R;

public class Notify {
    private Context context;
    private CsDialogListener csDialogListener;
    Dialog dig;
    int layout;

    public Notify(Context context, int layout) {
        this.context = context;
        this.layout = layout;
    }

    public void callResultDialog() {
        dig = new Dialog(context);
        dig.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dig.setContentView(layout);
        dig.setCancelable(false);
        dig.show();

        String contexts_txt = "" +
                "<h2>알려드립니다!</h2>" +
                "<div>1.본 원은 1인 1기기 접속을 허용하고 있어요!</div>" +
                "<div>2.모바일에서는 학습 진도처리만 가능해요!</div>" +
                "<div>3.<b><font color=#FF0000>3주차 학습</b>부터 <b>범용공인인증서</b>가 필요해요!</div>" +
                "<div>4.팁활동, 오리엔테이션 등은 PC에서 진행해주세요!</div>" +
                "<div>5.가급적 안정적인 접속환경에서 학습해주세요!</div>" +
                "<div>6.접속환경에 따라 장애가 있을 수 있어요!</div>" +
                "<div>7.문제가 있다면 바로 본원으로 문의해주세요!</div>" +
                "<div>8.자세한 내용은 홈페이지를 참고해주세요!</div>" +
                "<div><a href='http://cb.egreen.co.kr'>홈페이지 바로가기</a></div>";

        TextView contents = dig.findViewById(R.id.contents);
        contents.setText(Html.fromHtml(contexts_txt));
        contents.setMovementMethod(LinkMovementMethod.getInstance());

        Button btn_cancel = dig.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csDialogListener.onPositiveClicked("CANCEL");
                dig.dismiss();
            }
        });

        Button btn_ok = dig.findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                csDialogListener.onPositiveClicked("OK");
                dig.dismiss();
            }
        });
    }

    public interface CsDialogListener {
        void onPositiveClicked(String s);
    }

    public void setCsDialogListener(CsDialogListener csDialogListener) {
        this.csDialogListener = csDialogListener;
    }
}
