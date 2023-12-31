package com.example.userinterfacelogin;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.userinterfacelogin.databinding.ActivityGoogleSignInBinding;
import com.example.userinterfacelogin.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;
    private static final String TAG = "googlemap";
    private static final int UPDATE_INTERVAL_MS = 100000;
    private static final int FASTEST_UPDATE_INTERVAL_MS = 50000;
    // OnRequestPermissionsResultCallback에서 수신된 결과에서 ActivityCompat.OnRequestPermissionsResultCallback를 사용한 퍼미션 요청을 구별하기 위함
    private static final int PERMISSION_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION };
    Location mCurrentLocation;
    LatLng mCurrentPosition;
    CircleOptions circleOptions;
    private FusedLocationProviderClient mFusedLocationClient;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private LocationRequest locationRequest; // 주의
    private FirebaseFirestore db;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String nickname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        // locationRequest = Google Play Service의 위치 서비스 이용해
        // device의 현재 위치를 요청
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL_MS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        // addLocationRequest = locationRequest 객체를 추가
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        String uid = currentUser.getUid();

        DocumentReference profileDocRef = db.collection("UID").document(uid).collection("profile").document("profile");
        profileDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (documentSnapshot.exists()) {
                            nickname = documentSnapshot.getString("nickname");
                            if (nickname != null) {
                                Log.d("LoginActivity", "사용자 닉네임: " + nickname);
                                binding.mainNickname.setText(nickname);
                            } else {
                                Log.d("LoginActivity", "닉네임이 설정되지 않았습니다.");
                                nickname = "익명의 숭실인";
                                binding.mainNickname.setText(nickname);
                            }
                        } else {
                            // 프로필 문서가 존재하지 않는 경우
                            Log.d("LoginActivity", "프로필 문서가 존재하지 않습니다.");
                            nickname = "Guest";
                            binding.mainNickname.setText(nickname);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        // 프로필 문서 확인 실패
                        Log.w("LoginActivity", "프로필 문서 확인 실패", e);
                        nickname = "Guest";
                        binding.mainNickname.setText(nickname);
                    }
                });

        if(currentUser == null){
            Intent GetIntoLogin = new Intent(this,GoogleSignInActivity.class);
            GetIntoLogin.putExtra("source","GetIntoLogin");
            startActivity(GetIntoLogin);
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // OnMapReady Callback 호출
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationAndStartActivity();
            }
        });
        binding.imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent WantProfile = new Intent(getApplicationContext(), ProfileActivity.class);
                WantProfile.putExtra("source","wantProfileFromMain");
                startActivity(WantProfile);
            }
        });
        binding.tobulletinboardactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { getLocationAndStartActivity2(); }
        });
        binding.compas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.compas) {
                    requestLocation();
                }
            }
        });
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: 들어옴 ");
        mMap = googleMap;
        requestLocation();
    }
    private BitmapDescriptor getMarkerIconFromColorA(int color,int radius) {
        Bitmap bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        paint.setAntiAlias(true);
        canvas.drawCircle(24, 24, radius, paint);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void addColoredMarker(LatLng position, int color,int radius) {
        BitmapDescriptor icon = getMarkerIconFromColorA(color,radius);
        mMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(icon));
    }
    private void setCircle(LatLng position,int color){
        CircleOptions circleOptions = new CircleOptions()
                .center(position)
                .radius(40) // 반경의 크기 (미터 단위)
                .strokeWidth(0)
                .fillColor(color);
        mMap.addCircle(circleOptions);
    }
    private LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() != null) {
                // 위치 정보를 성공적으로 받아왔을 때의 처리
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                mCurrentPosition = new LatLng(latitude, longitude);
                addColoredMarker(mCurrentPosition,
                        ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),24);
                setCircle(mCurrentPosition,
                        ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 19f));
                loadMemosNearby(mCurrentPosition);
            }
        }
    };
    private void requestLocation() {
        if (checkLocationServicesStatus()) {
            if (checkPermission()) {
                // 위치 정보 요청
                Task<Location> locationTask = mFusedLocationClient.getLastLocation();
                locationTask.addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            handleLocationSuccess(location);
                        }
                        else{
                            setDefaultLocation();
                        }
                    }
                });
            } else {
                // 퍼미션 요청
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
            }
        } else {
            // 위치 서비스 비활성화 시 사용자에게 메시지 표시
            showLocationServiceDialog();
        }
    }
    private void handleLocationSuccess(Location location) {
        Log.d("SHL", "성공적으로 수행");
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        mCurrentPosition = new LatLng(latitude, longitude);
        //        if (mMap != null) {
        //            mMap.clear(); // 맵 상의 모든 마커 및 그래픽 제거
        //        }
        addColoredMarker(mCurrentPosition,
                ContextCompat.getColor(MainActivity.this, R.color.colorPrimary),24);
        setCircle(mCurrentPosition,
                ContextCompat.getColor(MainActivity.this, R.color.colorAccent));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrentPosition, 19f));
        loadMemosNearby(mCurrentPosition);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); // Call the superclass method
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, request location again
                requestLocation();
            } else {
                // Permission denied, show a message to the user
                showPermissionDeniedSnackbar();
            }
        }
    }
    private void showPermissionDeniedSnackbar() {
        Snackbar.make(findViewById(android.R.id.content), "위치 권한이 거부되었습니다.", Snackbar.LENGTH_SHORT).show();
    }
    private void showLocationServiceDialog() {
        // 위치 서비스가 비활성화되었을 때 사용자에게 다이얼로그를 표시하는 코드 추가
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("위치 서비스가 비활성화되었습니다. 설정에서 활성화하시겠습니까?");
        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("아니오", null);
        builder.show();
    }
    @Override
    protected void onStart() {
        // 사용자가 Activity로 돌아올 때 위치 정보 업데이트가 필요할 수 있음
        super.onStart();
        Log.d(TAG, "onStart: ");
        if(checkPermission()){
            mFusedLocationClient.requestLocationUpdates(locationRequest,locationCallback, null);
            if(mMap!=null){
                mMap.setMyLocationEnabled(true);
            }
        }
    }
    @Override
    protected void onStop() {
        // 사용자의 화면에 더이상 표시되지 않으면 현재 위치에 대한 실시간 업데이트 필요x
        super.onStop();
        if(mFusedLocationClient != null){
            mFusedLocationClient.removeLocationUpdates(locationCallback);
            // removeLocationUpdates = 위치 업데이트를 수신하지 않도록 설정
        }
    }
    private boolean checkPermission(){
        // 런타임 퍼미션 처리와 유사, 함수 서브루틴
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED|| hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED ){
            Log.d(TAG, "startLocationUpdates: 퍼미션 없음");
            return false;

        }else{
            return true;
        }
    }
    // 현재 장치에서 위치 서비스가 활성화되어 있는지 확인
    private boolean checkLocationServicesStatus() {
        // LocationManager 가져오기
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // isProviderEnabled => GPS_PROVIDER(위치기반 GPS 서비스)또는 NETWORK_PROVIDER(네트워크 기반 위치 서비스) 중 하나라도 활성화 되어 있는지 확인
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }
    // 퍼미션 요청하기 전에 기본 위치로 이동시켜놓는 함수
    private void setDefaultLocation() {
        // 기본 위치
        LatLng DEFAULT_LOCATION = new LatLng(37.56, 126.97);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }
    private void getLocationAndStartActivity() {
        // 위치 퍼미션 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mCurrentLocation = location;
                                //위치정보 담은 인텐트
                                Intent intent = new Intent(MainActivity.this, MemoActivity.class);
                                intent.putExtra("Latitude", mCurrentLocation.getLatitude());
                                intent.putExtra("Longitude", mCurrentLocation.getLongitude());
                                intent.putExtra("source","WantMemo");
                                startActivity(intent);
                            }
                        }
                    });
        }
    }
    private void getLocationAndStartActivity2() {
        // 위치 퍼미션 확인
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            mFusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                mCurrentLocation = location;
                                //위치정보 담은 인텐트
                                Intent intent = new Intent(MainActivity.this, BulletinboardActivity.class);
                                intent.putExtra("Latitude", mCurrentLocation.getLatitude());
                                intent.putExtra("Longitude", mCurrentLocation.getLongitude());
                                intent.putExtra("source","WantBulletinboard");
                                startActivity(intent);
                            }
                        }
                    });
        }
    }
    private void loadMemosNearby(LatLng userLocation) {
        String Uid = currentUser.getUid();
        double latl = userLocation.latitude;
        double lonl = userLocation.longitude;
        Log.d("SHL", "lat and lon = " + latl + " " + lonl);
        long lat = ((long) (latl * 1000)) % 10000;
        long lon = ((long) (lonl * 1000)) % 10000;
        long mapGrid = lat * 10000 + lon;
        Log.d("SHLL", "current mapGrid " + mapGrid);
        Log.d("SHL", Long.toString(mapGrid));
        // mapGrid 9개 가져올 수 있습니다...
        for (long i = mapGrid-10000; i <= mapGrid+10000; i += 10000) {
            for (long j=i-1;j<=i+1;j++){
                Log.d("SHL", "current mapGrid " + i);
                long finalI = j;
                // 50개 이하로 가져옵니다...
                db.collection("MapGrid").document(Long.toString(j)).collection("memo").limit(50)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                Log.d("SHL", finalI + " get into onComplete");
                                LatLng eachPosition;
                                if (task.isSuccessful()) {
                                    if (task.getResult().isEmpty()) {
                                        Log.d("SHL", "No documents found.");
                                    } else {
                                        Log.d("SHL", "Documents found!");
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            Log.d("SHL", document.getId() + " => " + document.getData());
                                            Double mlatitude = document.getDouble("latitude");
                                            Double mlongitude = document.getDouble("longitude");
                                            eachPosition = new LatLng(mlatitude, mlongitude);
                                            addColoredMarker(eachPosition, ContextCompat.getColor(MainActivity.this, R.color.black),18);
                                        }
                                    }
                                } else {
                                    Log.d("SHL", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        }
    }
}
