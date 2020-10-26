package com.example.dblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_edit extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView circleImageView;
    TextView  Save;
    ImageView ChoosePic;
    EditText Name;
    private Bitmap compressed;
    String userid;
    private FirebaseAuth mAuth;
    ProgressDialog progressDialog;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference, storeRef;
    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        toolbar = findViewById(R.id.etoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_cancel);

        circleImageView = findViewById(R.id.edit_pic);
        ChoosePic = findViewById(R.id.choosepic);
        Name = findViewById(R.id.edit_name);
        Save = findViewById(R.id.save);
        progressDialog = new ProgressDialog(Profile_edit.this);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        String eName = getIntent().getExtras().getString("Name");
        final String eImage = getIntent().getExtras().getString("Image");
        if (eImage==null){
            Name.setText(eName);
        }else{
            Name.setText(eName);
            Glide.with(Profile_edit.this).load(eImage).centerCrop().into(circleImageView);
        }

        ChoosePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                    if (ContextCompat.checkSelfPermission(Profile_edit.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


                        Toast.makeText(Profile_edit.this, "Permission Denied", Toast.LENGTH_LONG).show();


                        ActivityCompat.requestPermissions(Profile_edit.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        choseImage();
                    }
                } else {
                    choseImage();
                }
            }
        });

        Save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                final String update_name = Name.getText().toString();


                if(!TextUtils.isEmpty(update_name)){
                    if (compressed==null){
                        Map<String, Object> userval = new HashMap<>();
                        userval.put("Name", update_name);

                        //uploading hashmap in firebase firestore
                        firebaseFirestore.collection("Users").document(userid).update(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(Profile_edit.this,"Edit Successful ",Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(Profile_edit.this,Profile.class));
                                    finish();
                                }else {
                                    progressDialog.dismiss();
                                    Toast.makeText(Profile_edit.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else {
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


                        compressed.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);


                        byte[] thumbData = byteArrayOutputStream.toByteArray();

                        storage = FirebaseStorage.getInstance();
                        storeRef = storage.getReference();
                        storageReference = storeRef.child("user_profile").child(userid+"/"+userid);

                        UploadTask image_path = storageReference.putBytes(thumbData);
                        Task<Uri> urlTask = image_path.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }

                                // Continue with the task to get the download URL
                                return storageReference.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    final Uri downloadUri = task.getResult();

                                    Map<String, Object> userval = new HashMap<>();
                                    userval.put("Name", update_name);
                                    userval.put("UserPic", downloadUri.toString());

                                    //uploading hashmap in firebase firestore
                                    firebaseFirestore.collection("Users").document(userid).update(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Toast.makeText(Profile_edit.this,"Edit Successful ",Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(Profile_edit.this,Profile.class));
                                                finish();
                                            }else {
                                                progressDialog.dismiss();
                                                Toast.makeText(Profile_edit.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    // Handle failures
                                    // ...
                                    Toast.makeText(Profile_edit.this,"Failled",Toast.LENGTH_SHORT).show();

                                }
                            }
                        });
                    }

                    /*ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


                    compressed.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);


                    byte[] thumbData = byteArrayOutputStream.toByteArray();

                    storage = FirebaseStorage.getInstance();
                    storeRef = storage.getReference();
                    storageReference = storeRef.child("user_profile").child(userid+"/"+userid);

                    UploadTask image_path = storageReference.putBytes(thumbData);
                    Task<Uri> urlTask = image_path.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return storageReference.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                final Uri downloadUri = task.getResult();

                                Map<String, Object> userval = new HashMap<>();
                                userval.put("Name", update_name);
                                userval.put("UserPic", downloadUri.toString());

                                //uploading hashmap in firebase firestore
                                firebaseFirestore.collection("Users").document(userid).update(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(Profile_edit.this,"Edit Successful ",Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(Profile_edit.this,Profile.class));
                                            finish();
                                        }else {
                                            progressDialog.dismiss();
                                            Toast.makeText(Profile_edit.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                // Handle failures
                                // ...
                                Toast.makeText(Profile_edit.this,"Failled",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });*/

                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(Profile_edit.this,"Please Enter your Name",Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void choseImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent,"pick an image"),1);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode== RESULT_OK) {

            try {
                InputStream inputStream = getContentResolver().openInputStream(data.getData());
                compressed = BitmapFactory.decodeStream(inputStream);
                Glide.with(Profile_edit.this).load(compressed).centerCrop().into(circleImageView);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }
}