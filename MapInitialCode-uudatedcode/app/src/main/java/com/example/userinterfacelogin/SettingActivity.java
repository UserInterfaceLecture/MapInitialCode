package com.example.userinterfacelogin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.userinterfacelogin.databinding.ActivitySettingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivitySettingBinding binding = ActivitySettingBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.editText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("source","wantProfileFromSetting");
                startActivity(intent);
            }
        });
        binding.editText3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("source","wantProfileFromSetting");
                startActivity(intent);
            }
        });
        binding.editText32.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
                Intent intent = new Intent(getApplicationContext(), GoogleSignInActivity.class);
                intent.putExtra("source","loginAgain");
                startActivity(intent);
            }
        });
        binding.editText33.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                withDraw();
            }
        });
    }
    private void withDraw() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            // AlertDialog 빌더 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("진짜로 계정을 삭제하시겠습니까?");
            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 사용자가 '예'를 선택한 경우
                    deleteAccount();
                }
            });
            builder.setNegativeButton("아니오", null);

            // 다이얼로그 표시
            builder.show();
        } else {
            // 로그인된 사용자가 없는 경우
            Toast.makeText(getApplicationContext(), "로그인된 사용자가 없습니다.", Toast.LENGTH_SHORT).show();
            // 여기에 추가적인 로직이 필요한 경우 작성
        }
//        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
//        intent.putExtra("source","withDraw");
//        startActivity(intent);
    }
    private void deleteAccount() {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        // 계정 삭제
        if (user != null) {
            user.delete()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // 사용자 삭제 성공
                                Toast.makeText(getApplicationContext(), "계정이 성공적으로 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                            } else {
                                // 사용자 삭제 실패
                                Toast.makeText(getApplicationContext(), "계정 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

}