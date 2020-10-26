package com.example.dblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dblog.authentication.Login;
import com.google.android.gms.tasks.OnCanceledListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Update_pass extends AppCompatActivity {

    Toolbar toolbar;
    TextView Save;
    EditText New_pass, Confirm_pass, Old_pass;
    FirebaseUser user;
    ProgressDialog progressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_pass);

        toolbar = findViewById(R.id.stoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);

        Save = findViewById(R.id.ssave);
        New_pass = findViewById(R.id.new_pass);
        Confirm_pass = findViewById(R.id.confirm_pass);
        Old_pass = findViewById(R.id.old_pass);
        user = FirebaseAuth.getInstance().getCurrentUser();
        progressDialog = new ProgressDialog(Update_pass.this);

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );

                String old_pass = Old_pass.getText().toString();
                final String new_Pass = New_pass.getText().toString();
                String confirm_Pass = Confirm_pass.getText().toString();
                if (!TextUtils.isEmpty(old_pass) && !TextUtils.isEmpty(new_Pass) && !TextUtils.isEmpty(confirm_Pass)){
                    if (new_Pass.equals(confirm_Pass)){

                        AuthCredential authCredential = EmailAuthProvider.getCredential(user.getEmail(),old_pass);
                        user.reauthenticate(authCredential).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                user.updatePassword(new_Pass)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    progressDialog.dismiss();
                                                    Toast.makeText(Update_pass.this, "User Password Updated", Toast.LENGTH_SHORT).show();
                                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                                    mAuth.signOut();
                                                    startActivity(new Intent(Update_pass.this, Login.class));
                                                    finishAffinity();
                                                    finish();
                                                }
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            progressDialog.dismiss();
                                            Toast.makeText(Update_pass.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(Update_pass.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });


                    }else {
                        progressDialog.dismiss();
                        Toast.makeText(Update_pass.this, "Password not match", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    progressDialog.dismiss();
                    Toast.makeText(Update_pass.this, "Please fill all Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}