package com.egreen.egreenbeta05.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.egreen.egreenbeta05.Data.A6_MyClassListData;
import com.egreen.egreenbeta05.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

public class A6_MyClassListAdapter extends RecyclerView.Adapter {
    private static final String TAG = A6_MyClassListAdapter.class.getSimpleName();

    Context context;
    ArrayList<A6_MyClassListData> data;
    private MyClassListClickListener mListener;

    public A6_MyClassListAdapter(Context context, ArrayList<A6_MyClassListData> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.a6_study_center_c, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final ViewHolder v = (ViewHolder) holder;

        /* 색 변경 */
        final boolean isEnable = data.get(position).isEnable();
        if (isEnable) {
            //모바일에서 수강 가능한 과목이라면,
            v.topLayout.setBackgroundResource(R.drawable.border_black);
            v.classTitle.setTextColor(ContextCompat.getColor(context, R.color.black));
            v.studyDate.setTextColor(ContextCompat.getColor(context, R.color.black));
            v.attendRatio.setTextColor(ContextCompat.getColor(context, R.color.black));
            v.ratio_txt.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else {
            v.topLayout.setBackgroundResource(R.drawable.border_gray);
            v.classTitle.setTextColor(ContextCompat.getColor(context, R.color.gray));
            v.studyDate.setTextColor(ContextCompat.getColor(context, R.color.gray));
            v.attendRatio.setTextColor(ContextCompat.getColor(context, R.color.gray));
            v.ratio_txt.setTextColor(ContextCompat.getColor(context, R.color.gray));
        }

        v.classTitle.setText(data.get(position).getClassTitle());
        //밑줄긋기
//        v.classTitle.setPaintFlags(v.classTitle.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        String studyDate_txt;
        if (isEnable) {
            if (data.get(position).getStudyDate().equals("0")) {
                studyDate_txt = "(출석기간아님)";
                v.studyDate.setText(studyDate_txt);
                v.studyDate.setTextColor(ContextCompat.getColor(context, R.color.gray));
            } else {
                studyDate_txt = "(" + data.get(position).getStudyDate() + " 학습중)";
                v.studyDate.setText(studyDate_txt);
                v.studyDate.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
        }

        //진도율
        v.attendRatio.setText(data.get(position).getAttendRatio() + "%");

        if (mListener != null) {
            v.topLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String classId = data.get(position).getClassId();
                    String classTitle = data.get(position).getClassTitle();
                    String directoryName = data.get(position).getDirectoryName();
                    String isReadOrientation = data.get(position).getIsReadOrientation();
                    String isSurvey = data.get(position).getIsSurvey();
                    String isThereHighSchoolName = data.get(position).getIsThereHighSchoolName();
                    String isDongPost = data.get(position).isDongPost();
                    String myGoal = data.get(position).getMyGoal();
                    String myNote = data.get(position).getMyNote();
                    String studyDate = data.get(position).getStudyDate();
                    String selfInt = data.get(position).getSelfInt();
                    String discussion = data.get(position).getDiscussion();
//                    Log.i(TAG, "Clicked" + classId);

                    mListener.onItemClicked(isEnable, classId, classTitle, directoryName, isReadOrientation,
                            isSurvey, isThereHighSchoolName, isDongPost, myGoal, myNote, studyDate, selfInt, discussion);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
//        Log.i(TAG, "데이터가 얼마나 있니??? " + data.size());
        return data.size();
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout topLayout;
        TextView classTitle;
        TextView studyDate;
        TextView attendRatio;
        TextView ratio_txt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            topLayout = itemView.findViewById(R.id.topLayout);
            classTitle = itemView.findViewById(R.id.classTitle);
            studyDate = itemView.findViewById(R.id.studyDate);
            attendRatio = itemView.findViewById(R.id.attendRatio);
            ratio_txt = itemView.findViewById(R.id.ratio_txt);
        }
    }

    public interface MyClassListClickListener {
        void onItemClicked(boolean isEnable, String classId, String classTitle, String strUrl, String isReadOrientation,
                           String isSurvey, String isThereHighSchoolName, String isDongPost, String myGoal, String myNote,
                           String studyDate, String selfInt, String discussion);
    }

    public void setOnClickListener(MyClassListClickListener listener) {
        this.mListener = listener;
    }
}
