package com.example.userinterfacelogin;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.userinterfacelogin.databinding.ActivityBulletinboardBinding;
import com.example.userinterfacelogin.databinding.NearnoteitemBinding;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class BulletinboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBulletinboardBinding binding = ActivityBulletinboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        binding.homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
            }
        });
        binding.settingbutton.setOnClickListener((new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
                Intent intent2 = new Intent(getApplicationContext(),SettingActivity.class);
                startActivity(intent2);
            }
        }));

        List<String> yourImageList = new ArrayList<>();

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        YourAdapter yourAdapter = new YourAdapter(yourImageList, this);

        CollectionReference imagesRef = db.collection("MapGrid");
        // Firestore 컬렉션 이름

        imagesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    // Firestore 문서에서 이미지 URL 가져오기
                    String imageUrl = document.getString("imageUrl");
                    // RecyclerView에 데이터 추가
                    yourImageList.add(imageUrl);
                }

                // RecyclerView 갱신
                yourAdapter.notifyDataSetChanged();
            } else {
                // 처리 중 오류가 발생한 경우
                Log.d("aaa", "Error getting documents: ", task.getException());
            }
        });
        LinearLayoutManager linearlayoutmanager = new LinearLayoutManager(this);
        linearlayoutmanager.setOrientation(LinearLayoutManager.HORIZONTAL);
        binding.recyclerView.setLayoutManager(linearlayoutmanager);
        binding.recyclerView.setAdapter(yourAdapter);
        }

        public class ImageViewHolder extends RecyclerView.ViewHolder {
            NearnoteitemBinding imageBinding;

            public ImageViewHolder(NearnoteitemBinding binding) {
                super(binding.getRoot());
                imageBinding = binding;
            }
        }
        public class YourAdapter extends RecyclerView.Adapter<ImageViewHolder> {

            private List<String> imageUrls;
            private Context context;

            public YourAdapter(List<String> imageUrls, Context context) {
                this.imageUrls = imageUrls;
                this.context = context;
            }

            @Override
            public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                NearnoteitemBinding binding = NearnoteitemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);     //우리가 붙이는 건 아니다. 보통 가져다주면 알아서 붙인다.
                return new ImageViewHolder(binding);
            }

            @Override
            public void onBindViewHolder(ImageViewHolder holder, int position) {
            // Glide를 사용하여 이미지 로드 및 표시
                Glide.with(context)
                    .load(imageUrls.get(position))
                    .into(holder.imageBinding.getRoot());
            }

            @Override
            public int getItemCount() {
                return imageUrls.size();
            }
    }
}