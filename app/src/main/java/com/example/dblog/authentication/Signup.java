package com.example.dblog.authentication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dblog.Postview;
import com.example.dblog.R;
import com.example.dblog.UserPicName;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signup extends AppCompatActivity {

    EditText Email, Password, Repass;
    Button Signup;
    TextView Login;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Repass = findViewById(R.id.repassword);
        Signup = findViewById(R.id.signup);
        Login = findViewById(R.id.login);
        mAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(Signup.this);

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Signup.this,Login.class));
            }
        });

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                String mEmail = Email.getText().toString();
                String mPassword = Password.getText().toString();
                String mRepass = Repass.getText().toString();

                if (!TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mPassword) && !TextUtils.isEmpty(mRepass)){
                    if (mPassword.equals(mRepass)){
                        mAuth.createUserWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    startActivity(new Intent(Signup.this, UserPicName.class));
                                    finish();
                                }else {
                                    progressDialog.dismiss();
                                    Toast.makeText(Signup.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
                        progressDialog.dismiss();
                        Toast.makeText(Signup.this,"Password not match",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    progressDialog.dismiss();
                    if (TextUtils.isEmpty(mEmail)) {
                        Email.setError("Email-ID is required!");
                        Email.setHint("Please enter Email-ID");
                    }
                    if (TextUtils.isEmpty(mPassword)) {
                        Password.setError("Password is required!");
                        Password.setHint("Please enter Password");
                    }
                    if (TextUtils.isEmpty(mRepass)) {
                        Repass.setError("Password is required!");
                        Repass.setHint("Please enter Re-Password");
                    }
                }
            }
        });
    }
}