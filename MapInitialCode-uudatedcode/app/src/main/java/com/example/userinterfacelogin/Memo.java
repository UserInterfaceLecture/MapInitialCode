package com.example.userinterfacelogin;

import android.net.Uri;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.net.URI;

public class Memo implements Serializable {
    private double latitude;
    private double longitude;
    private long mapGrid;
    private String authorUid;
    private String textContent;
    private Uri imageUrl; // Firebase Storage에 저장해야 함
    private int likeCount;
    private int category1;
    private int category2;
    private int category3;
    private Timestamp timestamp; // Firestore에 저장할 타임스탬프 필드

    public Memo() {
        // Firebase를 위한 기본 생성자
    }

    public void likeCountUp(){this.likeCount++;}
    public long mapGridCalculate(){
        long lat = ((long) (this.latitude * 1000)) % 10000;
        long lon = ((long) (this.longitude * 1000)) % 10000;
        this.mapGrid = (lat * 10000) + (lon);
        return this.mapGrid;
    }


    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public long getMapGrid() {
        return mapGrid;
    }

    public void setMapGrid(long mapGrid) {
        this.mapGrid = mapGrid;
    }

    public String getAuthorUid() {
        return authorUid;
    }

    public void setAuthorUid(String authorUid) {
        this.authorUid = authorUid;
    }

    public String getTextContent() {
        return textContent;
    }

    public void setTextContent(String textContent) {
        this.textContent = textContent;
    }

    public Uri getImageUrl() { return imageUrl; }

    public void setImageUrl(Uri imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCategory1() {
        return category1;
    }

    public void setCategory1(int category1) {
        this.category1 = category1;
    }

    public int getCategory2() {
        return category2;
    }

    public void setCategory2(int category2) {
        this.category2 = category2;
    }

    public int getCategory3() {
        return category3;
    }

    public void setCategory3(int category3) {
        this.category3 = category3;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
