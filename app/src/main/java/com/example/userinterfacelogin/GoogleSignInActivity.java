package com.example.userinterfacelogin;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.userinterfacelogin.databinding.ActivityGoogleSignInBinding;
import com.example.userinterfacelogin.databinding.ActivityGoogleSignOutBinding;
import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.BeginSignInResult;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tomergoldst.tooltips.ToolTipsManager;

public class GoogleSignInActivity extends AppCompatActivity  {
    private static final String TAG = "UILab";
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private ActivityResultLauncher<IntentSenderRequest> oneTapUILauncher;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityGoogleSignInBinding binding = ActivityGoogleSignInBinding.inflate(getLayoutInflater());
        setSupportActionBar(binding.LoginToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 지정된 툴바가 Activity의 Action Bar로 설정, 뒤로가기 버튼 추가됨
        setContentView(binding.getRoot());
        configOneTapSignUpOrSignInClient();
        initFirebaseAuth();
        binding.checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // 체크되었을 때: 비밀번호 보이기
                    binding.pwEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                } else {
                    // 체크 해제되었을 때: 비밀번호 감추기
                    binding.pwEdit.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                }
            }
        });
        binding.signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        binding.registerbtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String email = binding.idEdit.getText().toString();
                String password = binding.pwEdit.getText().toString();
                registerIn(email,password);
            }
        });
        binding.loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.idEdit.getText().toString();
                String password = binding.pwEdit.getText().toString();
                logIn(email,password);
            }
        });
    }
    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "이미 로그인 했음");
            updateUI(currentUser); // Go to MainActivity
        } else {
            Log.d(TAG, "아직 로그인 안했음");
        }
    }
    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
    private void registerIn(String email,String password){
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            // 이메일 또는 비밀번호 중 하나라도 입력되지 않은 경우
            String errorMessage = "Email and password are required!";
            showToast(getApplicationContext(), errorMessage);
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Log.d(TAG, "createUserWithEmail:failure");
                            String errorMessage = task.getException().getMessage();
                            if (errorMessage != null) {
                                showToast(getApplicationContext(), errorMessage);
                            } else {
                                // 기타 에러 메시지가 없는 경우 기본 메시지 출력
                                showToast(getApplicationContext(), "Check your Email and Password!");
                            }
                            updateUI(null);
                        }
                    }
                });
    }
    private void logIn(String email,String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            //Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Log.d(TAG, "signInWithEmail:failure");
                            String errorMessage = task.getException().getMessage();
                            if (errorMessage != null) {
                                showToast(getApplicationContext(), errorMessage);
                            } else {
                                // 기타 에러 메시지가 없는 경우 기본 메시지 출력
                                showToast(getApplicationContext(), "Check your Email and Password!");
                            }
                            updateUI(null);
                        }
                    }
                });
    }
    private void configOneTapSignUpOrSignInClient() {
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        // Your server's client ID, not your Android client ID.
                        .setServerClientId(getString(R.string.default_web_client_id))
                        //.setFilterByAuthorizedAccounts(true) // Only show accounts previously used to sign in.
                        .setFilterByAuthorizedAccounts(false) // Show all accounts on the device.
                        .build())
                .build();
// 다른 액티비티가 결과를 돌려줬을 때 동작할 콜백함수와 함꼐 옴
        // onActivityResult는 이 계정을 Firebase에 등록 요청함
        oneTapUILauncher = registerForActivityResult(new ActivityResultContracts.StartIntentSenderForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(result.getData());
                    // result.getData() = Intent추출
                    String idToken = credential.getGoogleIdToken();
                    if (idToken != null) {
                        // Got an ID token from Google. Use it to authenticate
                        // with Firebase.
                        AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        // ID 토큰을 AuthCredential로 타입변환
                        mAuth.signInWithCredential(firebaseCredential)
                                // signInwithCredential 회원등록요청, 로그인 요청
                                .addOnCompleteListener(GoogleSignInActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            // Sign in success, update UI with the signed-in user's information
                                            Log.d(TAG, "signInWithCredential:success");
                                            FirebaseUser user = mAuth.getCurrentUser();
                                            updateUI(user);
                                        } else {
                                            // If sign in fails, display a message to the user.
                                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                                            updateUI(null);
                                        }
                                    }
                                });
                    }
                } catch (ApiException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
    private void initFirebaseAuth() {
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }
    // https://developers.google.com/identity/one-tap/android/get-saved-credentials
    private void signIn() {
        // Google Login
        // oneTapClient가 Google과 통신함(broker역할)
        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        IntentSender intentSender = beginSignInResult.getPendingIntent().getIntentSender();
                        IntentSenderRequest intentSenderRequest = new IntentSenderRequest.Builder(intentSender).build();
                        oneTapUILauncher.launch(intentSenderRequest);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, e.getLocalizedMessage());
                    }
                });
    }
    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
            startActivity(intent);
//            String source = getIntent().getStringExtra("source");
//            if("GetIntoLogin".equals(source)){
//                Intent intent = new Intent(getApplicationContext(), MemoActivity.class);
//                startActivity(intent);
//            }
//            Intent intent = new Intent(this, GoogleSignOutActivity.class);
//            intent.putExtra("USER_PROFILE", "Your email: " + user.getEmail() + "\n" + "Your uid: " + user.getUid());
//            // USER_PROFILE이 key값
//            startActivity(intent);
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                String source = getIntent().getStringExtra("source");
                if ("GetIntoLogin".equals(source)||"loginAgain".equals(source)) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                }
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
        String source = getIntent().getStringExtra("source");
        if ("GetIntoLogin".equals(source)) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }
}