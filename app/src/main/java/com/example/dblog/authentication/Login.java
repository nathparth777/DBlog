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

import com.example.dblog.Forget_pass;
import com.example.dblog.Postview;
import com.example.dblog.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Login extends AppCompatActivity {
    EditText Email, Password;
    Button Login;
    TextView Signup, Forget;
    ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Email = findViewById(R.id.email);
        Password = findViewById(R.id.password);
        Login = findViewById(R.id.login);
        Signup = findViewById(R.id.signup);
        Forget = findViewById(R.id.foget);

        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null){
            Intent i = new Intent(Login.this, Postview.class);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
        }

        Forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, Forget_pass.class));
            }
        });

        progressDialog = new ProgressDialog(Login.this);

        mAuth = FirebaseAuth.getInstance();

        Signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this,Signup.class));
            }
        });

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );

                String mEmail = Email.getText().toString();
                String mPassword = Password.getText().toString();

                if (!TextUtils.isEmpty(mEmail) && !TextUtils.isEmpty(mPassword)){
                    mAuth.signInWithEmailAndPassword(mEmail,mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()){
                                progressDialog.dismiss();
                                startActivity(new Intent(Login.this, Postview.class));
                                finish();
                            }else{
                                progressDialog.dismiss();
                                Toast.makeText(Login.this,"Error: "+ task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    progressDialog.dismiss();
                    if (TextUtils.isEmpty(mEmail)){
                        Email.setError("Email-ID is required!");
                        Email.setHint("Please enter Email-ID");
                    }
                    if (TextUtils.isEmpty(mPassword)) {
                        Password.setError("Password is required!");
                        Password.setHint("Please enter Password");
                    }
                }
            }
        });
    }

}