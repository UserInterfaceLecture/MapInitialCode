package com.example.userinterfacelogin;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.DocumentReference;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


import com.example.userinterfacelogin.databinding.ActivityGoogleSignInBinding;
import com.example.userinterfacelogin.databinding.ActivityMemoBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MemoActivity extends AppCompatActivity {
    private Memo memoMaking;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private List<String> selectedCategories = new ArrayList<>(); // 선택된 카테고리를 저장할 리스트
    private String[] selectedCategory = new String[]{"","",""};
    String subPathID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMemoBinding binding =ActivityMemoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toggleEraserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isEraserMode = !binding.canvasView.isEraseMode();
                binding.canvasView.setEraseMode(isEraserMode);
            }
        });

        binding.clearCanvasButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.canvasView.clearCanvas();
            }
        });

        binding.changeStrokeWidthButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 선 두께를 토글로 변경 (예: 5 -> 10 -> 15 -> 5 -> ...)
                if (binding.canvasView.getLineWidth() == 5) {
                    binding.canvasView.setLineWidth(10);
                } else if (binding.canvasView.getLineWidth() == 10) {
                    binding.canvasView.setLineWidth(15);
                } else {
                    binding.canvasView.setLineWidth(5);
                }
            }
        });


            // 버튼 클릭 시 카테고리 다이얼로그 표시
        binding.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog(1, binding.button1);
            }
        });
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog(2, binding.button2);
            }
        });
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCategoryDialog(3, binding.button3);
            }
        });




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
        memoMaking.setFirestorePath("");

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
    private void showCategoryDialog(final int buttonNumber, final Button buttonToUpdate) {
        final List<String> categories = new ArrayList<>(Categories.getIdToNameMap().values());
        for (int i = 0; i < 3; i++) {
            if (i == (buttonNumber - 1)) {
                continue; // 현재 버튼은 제외하고 다음으로 넘깁니다.
            }

            String itemToRemove = selectedCategory[i];
            if (!itemToRemove.equals("없음") && !itemToRemove.isEmpty()) {
                categories.remove(itemToRemove);
            }
        }
        // 이전에 선택한 카테고리를 체크된 상태로 표시
        final boolean[] checkedCategories = new boolean[categories.size()];
        for (int i = 0; i < categories.size(); i++) {
            String category = categories.get(i);
            checkedCategories[i] = selectedCategories.contains(category);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("카테고리 선택")
                .setSingleChoiceItems(categories.toArray(new CharSequence[0]), -1, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String selectedCategoryG = categories.get(which);
                        if (!selectedCategories.contains(selectedCategoryG)) {
                            selectedCategories.clear(); // 이미 선택한 카테고리 초기화
                            selectedCategories.add(selectedCategoryG);
                            selectedCategory[buttonNumber - 1] = selectedCategoryG;
                            checkedCategories[which] = true; // 선택한 카테고리 체크 상태로 변경
                        }
                    }
                })
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인 버튼 클릭 시 버튼 텍스트 업데이트
                        buttonToUpdate.setText(selectedCategory[buttonNumber - 1]);
                        if(buttonNumber == 1){
                            memoMaking.setCategory1(selectedCategory[buttonNumber - 1]);
                        }
                        else if(buttonNumber == 2){
                            memoMaking.setCategory2(selectedCategory[buttonNumber - 1]);
                        }
                        else if(buttonNumber == 3){
                            memoMaking.setCategory3(selectedCategory[buttonNumber - 1]);
                        }
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedCategory[buttonNumber - 1] = "없음";
                        dialog.dismiss();
                    }
                });

        AlertDialog dialog = builder.create();
        dialog.show();
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
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("firestorePath", "UID/" + currentUser.getUid() + "/memo/" + documentReference.getId());
                                        subPathID = "UID/" + currentUser.getUid() + "/memo/" + documentReference.getId();
                                        documentReference.update(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Firestore 문서 업데이트 성공
                                                        Log.d("MemoActivity", "Firestore 문서 업데이트 성공: " );
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        // Firestore 문서 업데이트 실패
                                                        Log.w("MemoActivity", "Firestore 문서 업데이트 실패: ", e);
                                                    }
                                                });

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
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("firestorePath", "MapGrid/" + mapGridCollectionName + "/memo/" + documentReference.getId());
                                        updates.put("subPath", subPathID);
                                        documentReference.update(updates)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // Firestore 문서 업데이트 성공
                                                        Log.d("MemoActivity", "Firestore 문서 업데이트 성공: ");
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        // Firestore 문서 업데이트 실패
                                                        Log.w("MemoActivity", "Firestore 문서 업데이트 실패: ", e);
                                                    }
                                                });
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