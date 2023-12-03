package com.example.userinterfacelogin;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.userinterfacelogin.databinding.ActivityGoogleSignOutBinding;
import com.google.firebase.auth.FirebaseAuth;

public class GoogleSignOutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGoogleSignOutBinding binding = ActivityGoogleSignOutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Intent intent = getIntent();
        String userProfile = intent.getStringExtra("USER_PROFILE");
        binding.textview.setText(userProfile);
        binding.loginReady.setText("Login Successful!");
        binding.signOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
                // signOUt하면 signIn페이지로 돌아감
                finish();
            }
        });
        binding.memoInButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finish();
                Intent memoInto = new Intent(getApplicationContext(),MemoActivity.class);
                memoInto.putExtra("source","memoInto");
                startActivity(memoInto);
            }
        });
    }
    // getInstance는 firebasesytem의 인증 instance를 얻을 수 있음
    // 현재 사용자의 인증 상태를 관리하고, 사용자를 인증하거나 로그아웃 하는 등의 작업 수행
    private void signOut() {
        FirebaseAuth.getInstance().signOut();
    }
}