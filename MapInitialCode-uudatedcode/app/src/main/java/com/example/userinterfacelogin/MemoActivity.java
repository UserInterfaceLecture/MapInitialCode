package com.example.userinterfacelogin;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.graphics.Bitmap;
import android.net.Uri;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import java.io.ByteArrayOutputStream;
import androidx.annotation.NonNull;
import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


import com.example.userinterfacelogin.databinding.ActivityGoogleSignInBinding;
import com.example.userinterfacelogin.databinding.ActivityMemoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MemoActivity extends AppCompatActivity {
    private Memo memoMaking;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMemoBinding binding =ActivityMemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Date currentDate = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(currentDate);
        binding.textUp.setText(formattedDate);
        Bundle extras = getIntent().getExtras();
        binding.cancelButtonOnEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            Intent GetIntoLogin = new Intent(this,GoogleSignInActivity.class);
            GetIntoLogin.putExtra("source","GetIntoLogin");
            startActivity(GetIntoLogin);
        }
        memoMaking = new Memo();
        memoMaking.setAuthorUid(currentUser.getUid());
        Log.d("S",currentUser.getUid());
        if (extras != null) {
            memoMaking.setLatitude(extras.getDouble("Latitude", 37.494705526855));
            memoMaking.setLongitude(extras.getDouble("Longitude", 126.95994559383));
            Log.d("SHL",Double.toString(memoMaking.getLatitude())+" "+Double.toString(memoMaking.getLongitude()));
        }
        else {
            memoMaking.setLatitude(37.494705526855);
            memoMaking.setLongitude(126.95994559383);
        }
        memoMaking.setMapGrid(memoMaking.mapGridCalculate());

        memoMaking.setCategory1(0);
        memoMaking.setCategory2(0);
        memoMaking.setCategory3(0);

        memoMaking.setLikeCount(0);
        binding.publishButtonOnEditor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap image = binding.canvasView.getCanvasBitmap();
                memoMaking.setTimestamp(Timestamp.now());
                memoMaking.setTextContent(binding.textEditor.getText().toString());
                uploadImage(image,currentUser.getUid());
                finish();
            }
        });
        //노트 에디터
    }
    public void uploadImage(Bitmap bitmap, String userId) {
        // Firebase Storage 인스턴스 가져오기
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        // 현재 시간을 기반으로 파일명 생성
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "JPEG_" + timeStamp + "_";

        // 사용자 UID와 고유한 파일명을 기반으로 한 경로에서 스토리지 참조 생성
        StorageReference userImageRef = storageRef.child("images/" + userId + "/" + fileName + ".jpg");

        // 비트맵을 바이트 배열로 변환
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // 바이트 배열을 Firebase Storage에 업로드
        UploadTask uploadTask = userImageRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // 핸들링 코드 이후 작성
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // 파일의 다운로드 가능한 URI를 얻음
                userImageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        memoMaking.setImageUrl(uri);

                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        CollectionReference userCollectionRef = db.collection("UID").document(currentUser.getUid()).collection("memo");
                        String mapGridCollectionName = String.valueOf(memoMaking.getMapGrid());
                        CollectionReference mapGridCollectionRef = db.collection("MapGrid").document(mapGridCollectionName).collection("memo");

                        userCollectionRef.add(memoMaking)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Memo 정보가 성공적으로 추가됨
                                        Log.d("MemoActivity", "Memo added with ID: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        // Memo 정보 추가 실패
                                        Log.w("MemoActivity", "Error adding memo", e);
                                    }
                                });

                        mapGridCollectionRef.add(memoMaking)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        // Memo 정보가 성공적으로 추가됨
                                        Log.d("MemoActivity", "Memo added with MapGrid: " + documentReference.getId());
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        // Memo 정보 추가 실패
                                        Log.w("MemoActivity", "Error adding memo", e);
                                    }
                                });



                    }
                });
            }
        });
    }
}