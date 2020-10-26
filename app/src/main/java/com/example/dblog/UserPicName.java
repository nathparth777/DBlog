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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
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

public class UserPicName extends AppCompatActivity {

    Toolbar toolbar;
    TextInputLayout Name;
    CircleImageView Userpic;
    Button Next;
    TextView Addpic;
    ProgressDialog progressDialog;
    String userid;
    private Bitmap compressed;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference, storeRef;
    private FirebaseStorage storage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pic_name);

        toolbar = findViewById(R.id.pic_name_toolbar);
        setSupportActionBar(toolbar);

        Name = (TextInputLayout) findViewById(R.id.ed1);
        Userpic = (CircleImageView) findViewById(R.id.upic);
        Next = findViewById(R.id.next);
        Addpic = (TextView) findViewById(R.id.add_image);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(UserPicName.this);


        Userpic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                    if (ContextCompat.checkSelfPermission(UserPicName.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


                        Toast.makeText(UserPicName.this, "Permission Denied", Toast.LENGTH_LONG).show();


                        ActivityCompat.requestPermissions(UserPicName.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        choseImage();
                    }
                } else {
                    choseImage();
                }
            }
        });

        if (compressed!=null){
            Addpic.setVisibility(View.INVISIBLE);
        }

        Next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.progress_dialog);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                final String name = Name.getEditText().getText().toString();

                if(!TextUtils.isEmpty(name)){
                    if (compressed==null){
                        Map<String, Object> userval = new HashMap<>();
                        userval.put("Name", name);
                        userval.put("UserPic", null);
                        //uploading hashmap in firebase firestore
                        firebaseFirestore.collection("Users").document(userid).set(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    progressDialog.dismiss();
                                    Toast.makeText(UserPicName.this,"Welcome "+name,Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(UserPicName.this,Postview.class));
                                    finish();
                                }else {
                                    progressDialog.dismiss();
                                    Toast.makeText(UserPicName.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }else{
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
                                    userval.put("Name", name);
                                    userval.put("UserPic", downloadUri.toString());

                                    //uploading hashmap in firebase firestore
                                    firebaseFirestore.collection("Users").document(userid).set(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                progressDialog.dismiss();
                                                Toast.makeText(UserPicName.this,"Welcome "+name,Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(UserPicName.this,Postview.class));
                                                finish();
                                            }else {
                                                progressDialog.dismiss();
                                                Toast.makeText(UserPicName.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });

                                } else {
                                    // Handle failures
                                    // ...
                                    Toast.makeText(UserPicName.this,"failled",Toast.LENGTH_SHORT).show();

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
                                userval.put("Name", name);
                                userval.put("UserPic", downloadUri.toString());

                                //uploading hashmap in firebase firestore
                                firebaseFirestore.collection("Users").document(userid).set(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            progressDialog.dismiss();
                                            Toast.makeText(UserPicName.this,"Welcome "+name,Toast.LENGTH_SHORT).show();
                                            startActivity(new Intent(UserPicName.this,Postview.class));
                                            finish();
                                        }else {
                                            progressDialog.dismiss();
                                            Toast.makeText(UserPicName.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                // Handle failures
                                // ...
                                Toast.makeText(UserPicName.this,"failled",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });*/


                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(UserPicName.this,"Please Enter your Name",Toast.LENGTH_SHORT).show();
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
                Userpic.setImageBitmap(compressed);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }

}