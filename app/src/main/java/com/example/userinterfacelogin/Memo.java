package com.example.userinterfacelogin;

import android.net.Uri;

import com.google.firebase.Timestamp;

import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

class Categories {
    private static final Map<Integer, String> ID_TO_NAME_MAP = new HashMap<>();

    public static Map<Integer, String> getIdToNameMap() {
        return ID_TO_NAME_MAP;
    }

    private static final Map<String, Integer> NAME_TO_ID_MAP = new HashMap<>();

    static {
        ID_TO_NAME_MAP.put(0, "없음");
        ID_TO_NAME_MAP.put(1, "여행");
        ID_TO_NAME_MAP.put(2, "음악");
        ID_TO_NAME_MAP.put(3, "축제");
        ID_TO_NAME_MAP.put(4, "공연");
        ID_TO_NAME_MAP.put(5, "미식");
        ID_TO_NAME_MAP.put(6, "---");

        // 카테고리 추가 가능

        for (Map.Entry<Integer, String> entry : ID_TO_NAME_MAP.entrySet()) {
            NAME_TO_ID_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    private Categories() {
    }

    public static String getNameById(int id) {
        return ID_TO_NAME_MAP.get(id);
    }

    public static Integer getIdByName(String name) {
        return NAME_TO_ID_MAP.get(name);
    }
}

public class Memo implements Serializable {
    private double latitude;
    private double longitude;
    private long mapGrid;
    private String authorUid;
    private String textContent;
    private Uri imageUrl; // Firebase Storage에 저장해야 함
    private int likeCount;
    private String category1;
    private String category2;
    private String category3;
    private Timestamp timestamp; // Firestore에 저장할 타임스탬프 필드
    private String firestorePath; // Firestore에 저장될 경로를 저장할 필드
    private String subPath;

    public void setSubPath(String subPath) {
        this.subPath = subPath;
    }

    public String getSubPath() {
        return subPath;
    }

    public String getFirestorePath() {
        return firestorePath;
    }
    public void setFirestorePath(String firestorePath) {
        this.firestorePath = firestorePath;
    }

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

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getCategory3() {
        return category3;
    }

    public void setCategory3(String category3) {
        this.category3 = category3;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

}
