package com.egreen.egreenbeta05.Data;

public class A7_JuchaData2 {
    String sCid;
    int jucha, chasi;
    String totalRatio;

    public A7_JuchaData2(String sCid, int jucha, int chasi, String totalRatio) {
        this.sCid = sCid;
        this.jucha = jucha;
        this.chasi = chasi;
        this.totalRatio = totalRatio;
    }

    public String getsCid() {
        return sCid;
    }
    public int getJucha() {
        return jucha;
    }
    public void setJucha(int jucha) {
        this.jucha = jucha;
    }
    public String getTotalRatio() {
        return totalRatio;
    }
}
