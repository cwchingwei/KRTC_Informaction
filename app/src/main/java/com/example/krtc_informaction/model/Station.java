package com.example.krtc_informaction.model;

public class Station {
    private int seq;
    private String 車站編號;
    private String 車站中文名稱;
    private String 車站英文名稱;
    private double 車站緯度;
    private double 車站經度;

    // Getter 和 Setter
    public int getSeq() {
        return seq;
    }

    public void setSeq(int seq) {
        this.seq = seq;
    }

    public String getStationId() {
        return 車站編號;
    }

    public void setStationId(String 車站編號) {
        this.車站編號 = 車站編號;
    }

    public String getStationNameZh() {
        return 車站中文名稱;
    }

    public void setStationNameZh(String 車站中文名稱) {
        this.車站中文名稱 = 車站中文名稱;
    }

    public String getStationNameEn() {
        return 車站英文名稱;
    }

    public void setStationNameEn(String 車站英文名稱) {
        this.車站英文名稱 = 車站英文名稱;
    }

    public double getLatitude() {
        return 車站緯度;
    }

    public void setLatitude(double 車站緯度) {
        this.車站緯度 = 車站緯度;
    }

    public double getLongitude() {
        return 車站經度;
    }

    public void setLongitude(double 車站經度) {
        this.車站經度 = 車站經度;
    }

    @Override
    public String toString() {
        return "車站編號: " + 車站編號 +
                ", 車站名稱(中): " + 車站中文名稱 +
                ", 車站名稱(英): " + 車站英文名稱 +
                ", 緯度: " + 車站緯度 +
                ", 經度: " + 車站經度;
    }
}