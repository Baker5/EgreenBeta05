package com.egreen.egreenbeta05.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.egreen.egreenbeta05.Data.A1_NoticeListData;
import com.egreen.egreenbeta05.R;

import java.util.ArrayList;

/**
 * Created by ys23 on 2015. 10. 22..
 */
public class A1_NoticeAdapter extends BaseAdapter {
    Context context;
    int layout;
    ArrayList<A1_NoticeListData> data = new ArrayList<A1_NoticeListData>();
    LayoutInflater inflater;

    public A1_NoticeAdapter(Context applicationContext, int a1_custum_noticelist, ArrayList<A1_NoticeListData> noticeListData) {
        this.context = applicationContext;
        this.layout = a1_custum_noticelist;
        this.data = noticeListData;
        this.inflater = (LayoutInflater) this.context.getSystemService(applicationContext.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Object getItem(int position) {
        return data.get(position).getNoticeTitle();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(layout, parent, false);
        }

        /* Custom 한 ListView 의 View 연결 */
        TextView a1_noticeImportance = convertView.findViewById(R.id.a1_noticeImportance);
        TextView a1_noticeTitle = convertView.findViewById(R.id.a1_noticeTitle);

        /* 공지사항 제목 View에 웹에서 받아온 data 넣기 */
        a1_noticeTitle.setText(data.get(position).getNoticeTitle());

        /* 웹에서 받아온 flag 값에 따라 '중요', '공지' 를 표시 */
        if (data.get(position).getNoticeimportance() == 1) {
            a1_noticeImportance.setBackgroundResource(R.drawable.important_btn);
            a1_noticeImportance.setText("중요");
        } else {
            a1_noticeImportance.setBackgroundResource(R.drawable.notice_btn);
            a1_noticeImportance.setText("공지");
        }

        return convertView;
    }
}
