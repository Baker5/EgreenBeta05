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

public class UpdateNotify {
    private Context context;
    private CsDialogListener csDialogListener;
    Dialog dig;
    int layout;

    public UpdateNotify(Context context, int layout) {
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
                "<h2>업데이트 알림</h2>" +
                "<h5>아래와 같이 업데이트 되었습니다.</h5>" +
                "<h6>1.비정상 로그아웃시 재로그인까지 20분의 딜레이가 사라졌습니다. 이제 언제든지 PC 또는 모바일에서 로그인할 수 있습니다.</h6>" +
                "<h6>2.공인인증서 로그인을 나의 강의실에서 과목 터치시 할 수 있도록 변경되었습니다.</h6>" +
                "<h6>3.나의 강의실이 학습 및 참여활동 현황을 한 눈에 볼 수 있도록 변경되었습니다.</h6>" +
                "<h6>4.비밃번호에 특수문자가 있을시 로그인되지 않던 문제를 수정하였습니다.</h6>" +
                "<h6>5.기타 오류를 수정하였습니다.</h6>";

        TextView contents = dig.findViewById(R.id.contents);
        contents.setText(Html.fromHtml(contexts_txt));
        contents.setMovementMethod(LinkMovementMethod.getInstance());

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
