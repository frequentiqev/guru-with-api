package com.example.guruwithapi.presentation.passwordupdate;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.guruwithapi.R;
import com.example.guruwithapi.preference.PrefManager;
import com.example.guruwithapi.presentation.login.LoginActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import pub.devrel.easypermissions.EasyPermissions;

@RequiresApi(api = Build.VERSION_CODES.N)
public class UpdatePasswordActivity extends AppCompatActivity{
    public static final int WRITE_EXTERNAL_STORAGE_CODE = 100;
    public static final int READ_EXTERNAL_STORAGE_CODE = 200;
    private static final int INTERNET = 300;
    private static final int ACCESS_FINE_LOCATION_CODE = 400;
    private static final int ACCESS_COARSE_LOCATION_CODE = 500;

    private Context mycontext;
    private PrefManager prefManager;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private FirebaseAnalytics mFirebaseAnalytics;
    private FirebaseCrashlytics crashlytics;

    private Button btnUpdatePassword;
    private Toolbar toolbar;
    private ImageView toolbarBackButton, ivEyePassword, ivEyeConfirmPassword, ivLogoguruUpdatePassword;
    private TextView toolbarTitle;
    private EditText etUpdatePassword, etConfirmUpdatePassword;
    private Boolean isEyePassword = true, isEyeConfirmPassword = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        askPermission();
        defineIds();
        initUi();
        initAction();
    }

    private void defineIds(){
        etUpdatePassword = (EditText) findViewById(R.id.etUpdatePassword);
        etConfirmUpdatePassword = (EditText) findViewById(R.id.etUpdateConfirmPassword);
        btnUpdatePassword = (Button) findViewById(R.id.btnUpdatePassword);
        toolbar = (Toolbar) findViewById(R.id.toolbarWithBack);
        toolbarBackButton = (ImageView) findViewById(R.id.ivBackButtonToolbar);
        ivLogoguruUpdatePassword = (ImageView) findViewById(R.id.ivLogoguruUpdatePassword);
        ivEyePassword = (ImageView) findViewById(R.id.ivEyePasswordUpdate);
        ivEyeConfirmPassword = (ImageView) findViewById(R.id.ivEyeConfirmPasswordUpdate);
        toolbarTitle = (TextView) findViewById(R.id.tvTitleToolbar);

        mycontext = this;
        prefManager = new PrefManager(mycontext);
        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() == null) {
            prefManager.removeAllPreference();
            mAuth.signOut();

            Toast.makeText(mycontext, "Waktu login Anda habis. Silakan Login kembali (1)", Toast.LENGTH_LONG).show();
            startActivity(new Intent(mycontext, LoginActivity.class));
            finish();
        } else {
            mUser = mAuth.getCurrentUser();
            mUser.getIdToken(true)
                    .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                        public void onComplete(@NonNull Task<GetTokenResult> task) {
                            if (task.isSuccessful()) {
                                String idToken = task.getResult().getToken();
                                // Send token to your backend via HTTPS
                            } else {
                                prefManager.removeAllPreference();
                                mAuth.signOut();

                                Toast.makeText(mycontext, "Waktu login Anda habis. Silakan Login kembali (2)", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(mycontext, LoginActivity.class));
                                finish();
                            }
                        }
                    });
        }

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(mycontext);
        mFirebaseAnalytics.setUserProperty("userID", prefManager.getUserID());
        mFirebaseAnalytics.setUserProperty("userEmail", prefManager.getEmail());

        crashlytics = FirebaseCrashlytics.getInstance();
        crashlytics.setCustomKey("userID", prefManager.getUserID());
        crashlytics.setCustomKey("userEmail", prefManager.getEmail());
    }

    private void initUi() {
        toolbarTitle.setText(getString(R.string.prompt_password));

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initAction() {
        ivLogoguruUpdatePassword.setOnClickListener(view -> {
            //throw new RuntimeException("Test Crash");
        });

        ivEyePassword.setOnClickListener(view -> {
            isEyePassword = !isEyePassword;

            if (isEyePassword){
                ivEyePassword.setImageResource(R.drawable.ic_eye_disable);
                etUpdatePassword.setTransformationMethod(new PasswordTransformationMethod());
            } else {
                ivEyePassword.setImageResource(R.drawable.ic_eye_enable);
                etUpdatePassword.setTransformationMethod(new HideReturnsTransformationMethod());
            }
        });

        ivEyeConfirmPassword.setOnClickListener(view -> {
            isEyeConfirmPassword = !isEyeConfirmPassword;

            if (isEyeConfirmPassword){
                ivEyeConfirmPassword.setImageResource(R.drawable.ic_eye_disable);
                etConfirmUpdatePassword.setTransformationMethod(new PasswordTransformationMethod());
            } else {
                ivEyeConfirmPassword.setImageResource(R.drawable.ic_eye_enable);
                etConfirmUpdatePassword.setTransformationMethod(new HideReturnsTransformationMethod());
            }
        });

        btnUpdatePassword.setOnClickListener(view -> {
            String password = etUpdatePassword.getText().toString().trim();
            String confirmPassword = etConfirmUpdatePassword.getText().toString().trim();

            if (password == null || password.length() == 0 || password.equals("")) {
                Toast.makeText(this, "Password tidak boleh kosong", Toast.LENGTH_LONG).show();
            } else if (confirmPassword == null || confirmPassword.length() == 0 || confirmPassword.equals("")) {
                Toast.makeText(this, "Konfirmasi Password tidak boleh kosong", Toast.LENGTH_LONG).show();
            } else if (!password.equals(confirmPassword)){
                Toast.makeText(this, "Password dan Konfirmasi Password tidak sama",Toast.LENGTH_LONG).show();
            } else {
                if (mAuth.getCurrentUser() != null ) {
                    mAuth.getCurrentUser().updatePassword(password).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(UpdatePasswordActivity.this,"Password telah diganti", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(this,"Password tidak berhasil diganti karena user tidak ditemukan", Toast.LENGTH_LONG).show();
                }
            }
        });

        toolbarBackButton.setOnClickListener(view -> {
            finish();
        });
    }

    private void askPermission() {
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.app_name),
                    WRITE_EXTERNAL_STORAGE_CODE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.app_name),
                    READ_EXTERNAL_STORAGE_CODE,
                    Manifest.permission.READ_EXTERNAL_STORAGE);
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.INTERNET)) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.app_name),
                    INTERNET,
                    Manifest.permission.INTERNET);
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.app_name),
                    ACCESS_FINE_LOCATION_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!EasyPermissions.hasPermissions(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            EasyPermissions.requestPermissions(
                    this,
                    getString(R.string.app_name),
                    ACCESS_COARSE_LOCATION_CODE,
                    Manifest.permission.ACCESS_COARSE_LOCATION);
        }
    }
}

