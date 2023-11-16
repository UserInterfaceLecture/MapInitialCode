package com.example.secondproduction;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private GoogleMap mMap;
    private Marker currentMarker = null;
    private static final String TAG = "googlemap";
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int UPDATE_INTERVAL_MS = 1000; // 1초
    private static final int FASTEST_UPDATE_INTERVAL_MS = 500; // 0.5초
    // OnRequestPermissionsResultCallback에서 수신된 결과에서 ActivityCompat.OnRequestPermissionsResultCallback를 사용한 퍼미션 요청을 구별하기 위함
    private static final int PERMISSION_REQUEST_CODE = 100;
    boolean needRequest = false;
    // 앱을 실행하기 위해 필요한 퍼미션 정의
    String[] REQUIRED_PERMISSION = {Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION };
    Location mCurrentLocation;
    LatLng currentPosition;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest; // 주의
    private Location location;
    private View mLayout; // snackbar 사용
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // locationRequest = Google Play Service의 위치 서비스 이용해
        // device의 현재 위치를 요청
        locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL_MS)
                .setFastestInterval(FASTEST_UPDATE_INTERVAL_MS);
        // priority우선순위(PRIORITY_HIGH_ACCURACY=가장 정확한 위치를 요청)
        // interval업데이트간격(위치 업데이트 간격을 1초마다)
        // fastestInterval최고 업데이트간격(가장 빠른 업데이트 간격을 0.5초마다)
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        // addLocationRequest = locationRequest 객체를 추가
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // Location.getFusedLocationProviderClient = FuedLocationProviderClient 객체를 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        // findFragmentById = R.id.map ID를 갖는 Fragment를 XML에서 찾음
        // getSupprotFragmentManager = 현재 Activity의 FragmentManager를 가져옴
        // SupportMapFragment = 이 객체는 지도를 표시하기 위한 Fragment
        mapFragment.getMapAsync(this);
        // OnMapReady Callback 호출
    }
    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: 들어옴 ");
        mMap = googleMap;
        // 지도의 초기위치 이동
        setDefaultLocation();
        // 런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 확인합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
        if(hasFineLocationPermission == PackageManager.PERMISSION_GRANTED && hasCoarseLocationPermission == PackageManager.PERMISSION_GRANTED){
            // 2. 이미 퍼미션을 가지고 있다면
            startLocationUpdates(); // 3. 위치 업데이트 실행
        }else{
            // 2. 퍼미션 요청을 허용한 적 없다면 퍼미션 요청
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, REQUIRED_PERMISSION[0])){
                // 요청 진행하기 전에 퍼미션이 왜필요한지 설명
                Snackbar.make(mLayout, "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Snackbar.LENGTH_INDEFINITE).setAction("확인", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // 사용자에게 퍼미션 요청, 요청 결과는 onRequestPermisionResult에서 수신
                        ActivityCompat.requestPermissions(MapActivity.this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
                    }
                }).show();
            }else{
                // 사용자가 퍼미션 거부를 한적이 없는 경우 퍼미션 요청을 바로 함
                // 요청 결과는 onRequestPermissionResult에서 수신된다.
                ActivityCompat.requestPermissions(this, REQUIRED_PERMISSION, PERMISSION_REQUEST_CODE);
            }
        }
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        // 지도의 UI설정을 가져와 현재 위치 버튼을 활성화 => 사용자 위치를 중심으로 보여주는 버튼 나타남
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Log.d(TAG, "onMapClick: ");
            }
        });
        // 지도를 클릭했을 때 호출되는 callback
    }
    // FusedLocationProviderClient에서 위치 업데이트를 수신할 때 호출되는 callback
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            // 부모 클래스의 동작 유지, 자식 클래스에서 추가적인 로직 수행
            List<Location> locationList = locationResult.getLocations();
            // 객체에서 위치 정보 리스트 가져옴
            if(locationList.size() > 0){
                // 위치 정보가 비어있지 않으면 가장 최근의 위치 정보
                location = locationList.get(locationList.size() -1);
                currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
                String markerTitle = getCurrentAddress(currentPosition);
                String markerSnippet = "위도 :" + String.valueOf(location.getLatitude()) + "경도 :" +
                        String.valueOf(location.getLongitude());
                // 현재 위치에 마커 생성하고 이동
                setCurrentLocation(location, markerTitle, markerSnippet);
                mCurrentLocation = location;
            }
        }
    };
    private String getCurrentAddress(LatLng currentPosition) {
        // 주어진 LatLng 위치를 이용하여 해당 위치의 주소를 얻음
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        // Geocoder 생성, Locale.getDefault => 현재 기기의 기본 Locale을 사용
        List<Address> addresses;
        try{
            addresses = geocoder.getFromLocation(
                    // ReverseGeoCoding = 위도,경도 => 주소
                    currentPosition.latitude,
                    currentPosition.longitude,
                    1);
            // getFromLocation = 주어진 위도와 경도에 해당하는 주소 정보
            // maxResult = 반환할 주소의 최대 개수
        }catch (IOException ioException){
            // Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return  "지오코더 서비스 사용 불가";
        }catch (IllegalArgumentException illegalArgumentException){
            // Toast.makeText(this,"잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";
        }
        if(addresses == null || addresses.size() == 0){
            Toast.makeText(this,"주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";
        }else{
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
            // 문자열로 전환하여 return, marker의 title로 쓰임
        }
    }
    //현재 위치에 마커 생성하고 이동
    private void setCurrentLocation(Location location, String markerTitle, String markerSnippet) {
        if(currentMarker != null)
        {
            currentMarker.remove();
        }
        LatLng currentLatLng =  new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(currentLatLng);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        // 마커 생성하고, 속성 설정하는 부분
        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(currentLatLng);
        mMap.moveCamera(cameraUpdate);
        // 업데이트 된 위치로 카메라 이동
    }
    // 위치 업데이트를 시작하는 함수(위치 업데이트를 받을 수 있도록 설정하는 준비단계)
    private void startLocationUpdates() {
        if(!checkLocationServicesStatus()){
            // 만약에 위치 서비스가 비활성화 되어 있다면... => 위치 서비스 활성화 dialog를 표시
            showDiologForLocationServiceSetting();
        }else{
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
            int hasCoarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);
            // PackageManager.PERMISSION_GRANTED와 비교하여 권한이 부여되었는지 확인
            if(hasFineLocationPermission != PackageManager.PERMISSION_GRANTED|| hasCoarseLocationPermission != PackageManager.PERMISSION_GRANTED ){
                Log.d(TAG, "startLocationUpdates: 퍼미션 없음");
                return;
            }
            // 권한 부여되었다면 FusedLocationProviderClient에게 위치 업데이트를 요청
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            /*requestLocationUpdates(LocationRequest request, LocationCallback callback, Looper looper)
Requests location updates with the given request and results delivered to the given callback on the specified Looper.*/
            if(checkPermission()){
                mMap.setMyLocationEnabled(true);
                // 현재 위치를 활성화
            }
        }
    }
    @Override
    protected void onStart() {
        // 사용자가 Activity로 돌아올 때 위치 정보 업데이트가 필요할 수 있음
        super.onStart();
        Log.d(TAG, "onStart: ");
        if(checkPermission()){
            mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
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
    // 위치 서비스가 비활성화되어 있을 때 dialog표시하는 함수
    private void showDiologForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다. 위치설정을 수정하시겠습니까?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        builder.create().show();
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
        String markerTitle = "위치 정보 가져올 수 없음";
        String markerSnippet = "위치 퍼미션과 GPS 활성 여부를 확인하세요";
        if(currentMarker != null){
            currentMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(DEFAULT_LOCATION);
        markerOptions.title(markerTitle);
        markerOptions.snippet(markerSnippet);
        markerOptions.draggable(true);
        currentMarker = mMap.addMarker(markerOptions);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION, 15);
        mMap.moveCamera(cameraUpdate);
    }
}