package com.egreen.egreenbeta05.Data;

/**
 * Created by ys23 on 2015. 10. 22..
 */
public class A1_NoticeListData {
    int noticeimportance;
    String noticeCid;
    String noticeTitle;

    public A1_NoticeListData(int noticeimportance, String noticeCid, String noticeTitle) {
        this.noticeimportance = noticeimportance;
        this.noticeCid = noticeCid;
        this.noticeTitle = noticeTitle;
    }

    public int getNoticeimportance() {
        return noticeimportance;
    }
    public String getNoticeCid() {
        return noticeCid;
    }
    public String getNoticeTitle() {
        return noticeTitle;
    }
}
