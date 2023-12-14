package com.example.userinterfacelogin;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.example.userinterfacelogin.databinding.ActivityBulletinboardBinding;
import com.example.userinterfacelogin.databinding.NearnoteitemBinding;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class BulletinboardActivity extends AppCompatActivity {
    Memo forCalculate = new Memo();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityBulletinboardBinding binding = ActivityBulletinboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        double currentlat = 0;
        double currentlon = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            currentlat = extras.getDouble("Latitude");
            currentlon = extras.getDouble("Longitude");

            // Rest of your code here
        } else {
            // Handle the case where extras are null, log an error, show a message, etc.
            Log.e("BulletinboardActivity", "No extras found");

        }
        forCalculate.setLatitude(currentlat);
        forCalculate.setLongitude(currentlon);

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
        binding.settingbutton.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent2 = new Intent(getApplicationContext(), SettingActivity.class);
                startActivity(intent2);
            }
        }));

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        long MapgridAttribute = forCalculate.mapGridCalculate();
        String []nearMapGrids = new String[9];
        int n = 0;
        for(int i = -1; i < 2; i++){
            for (int j = -1; j<2; j++){
                nearMapGrids[n] = Long.toString(MapgridAttribute + (i * 10000) + j);
                n++;
            }
        }
        List<Memo> memoList = new ArrayList<>();
        List<Memo> memoList2 = new ArrayList<>();
        YourAdapter adapter = new YourAdapter(memoList, this);
        YourAdapter adapter2 = new YourAdapter(memoList2, this);
        for(int i = 0; i < 9; i++) {
            db.collection("MapGrid").document(nearMapGrids[i]).collection("memo")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot memoDocument : task.getResult()) {
                                // "imageUrl" 필드를 Uri로 가져오기
                                String imageUrlString = memoDocument.getString("imageUrl");
                                Uri imageUrl = Uri.parse(imageUrlString);
                                Memo memo = new Memo();
                                memo.setImageUrl(imageUrl);
                                memoList.add(memo);
                                Log.d("aaa", "imageurl ", task.getException());
                            }
                            adapter.notifyDataSetChanged();
                        }
                        else {Log.d("BulletinboardActivity", "Error getting memo documents: ", task.getException());}
                    });
        }
        // ViewPager에 어댑터 설정
        CountDownLatch countDownLatch = new CountDownLatch(9); // 9번의 비동기 요청이 모두 완료될 때까지 대기

        for (int i = 0; i < 9; i++) {
            db.collection("MapGrid").document(nearMapGrids[i]).collection("memo")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot memoDocument : task.getResult()) {
                                Memo memo = memoDocument.toObject(Memo.class);
                                memoList2.add(memo);
                            }
                        } else {
                            Log.d("BulletinboardActivity", "Error getting memo documents: ", task.getException());
                        }
                        countDownLatch.countDown(); // 비동기 요청이 완료되었음을 알림
                    });
        }

        try {
            countDownLatch.await(); // 모든 비동기 요청이 완료될 때까지 대기
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

// 모든 메모가 추가된 memoList를 원하는 기준으로 정렬
        Collections.sort(memoList, new Comparator<Memo>() {
            @Override
            public int compare(Memo o1, Memo o2) {
                // 원하는 정렬 기준으로 비교
                return Integer.compare(o2.getLikeCount(), o1.getLikeCount());
            }
        });

// 여기서 adapter 등을 사용하여 UI 업데이트
        adapter2.notifyDataSetChanged();
        binding.ViewPager.setAdapter(adapter);
        binding.ViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);

    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        NearnoteitemBinding imageBinding;

        public ImageViewHolder(NearnoteitemBinding binding) {
            super(binding.getRoot());
            imageBinding = binding;
        }

        public void bind(Memo memo) {
            // 이미지 로드 및 표시
            Glide.with(itemView.getContext())
                    .load(memo.getImageUrl())
                    .into(imageBinding.getRoot());
        }
    }

    public class YourAdapter extends RecyclerView.Adapter<ImageViewHolder> {

        private List<Memo> memos;
        private Context context;

        public YourAdapter(List<Memo> memos, Context context) {
            this.memos = memos;
            this.context = context;
        }

        @Override
        public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            NearnoteitemBinding binding = NearnoteitemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ImageViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(ImageViewHolder holder, int position) {
            // 데이터를 ViewHolder에 바인딩
            Memo memo = memos.get(position);
            holder.bind(memo);
        }

        @Override
        public int getItemCount() {
            return memos.size();
        }
    }
}
