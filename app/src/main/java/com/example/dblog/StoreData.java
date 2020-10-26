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
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
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
import java.util.Locale;
import java.util.Map;

public class StoreData extends AppCompatActivity {

    Toolbar toolbar;
    Button mSave;
    ImageView mUserimage;
    EditText editText, editText2;
    String userid;
    String username;
    String Current_userID;
    String value;
    ProgressDialog progressDialog;
    int val;
    private Bitmap compressed;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private StorageReference storageReference, storeRef, storageReference1;

    private FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_data);

        toolbar = findViewById(R.id.store_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_white_arrow_back);

        mSave = findViewById(R.id.save);
        editText = findViewById(R.id.ed1);
        editText2 = findViewById(R.id.ed2);
        mUserimage = findViewById(R.id.userimage);
        mAuth = FirebaseAuth.getInstance();
        userid = mAuth.getCurrentUser().getUid();
        Current_userID = mAuth.getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        storageReference1 = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(StoreData.this);

        firebaseFirestore.collection("Users").document(Current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    if (task.getResult().exists()){
                        if (task.getResult().get("IMGValue")!=null){
                            value = task.getResult().getString("IMGValue");
                            val = Integer.parseInt(value);
                        }
                        else{
                            val =1;
                        }
                    }

                }
            }
        });



        mUserimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {


                    if (ContextCompat.checkSelfPermission(StoreData.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {


                        Toast.makeText(StoreData.this, "Permission Denied", Toast.LENGTH_LONG).show();


                        ActivityCompat.requestPermissions(StoreData.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        choseImage();
                    }
                } else {
                    choseImage();
                }
            }
        });


        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressDialog.show();
                progressDialog.setContentView(R.layout.uploading_prograss);
                progressDialog.getWindow().setBackgroundDrawableResource(
                        android.R.color.transparent
                );
                final String title = editText.getText().toString();
                final String blog = editText2.getText().toString();

                if(compressed!=null && !TextUtils.isEmpty(title) && !TextUtils.isEmpty(blog)){

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();


                    compressed.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);


                    byte[] thumbData = byteArrayOutputStream.toByteArray();

                    //UploadTask image_path = storageReference.child("user_image").child(userid + ".jpg").putBytes(thumbData);


                    storage = FirebaseStorage.getInstance();
                    storeRef = storage.getReference();
                    storageReference = storeRef.child("user_image").child(userid+"/"+userid+val);
                    storageReference1 = storeRef.child("All_images/"+userid+val);
                    val=val+1;
                    value=Integer.toString(val);

                    UploadTask image_path = storageReference.putBytes(thumbData);

                    image_path.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                        }
                    });
                    UploadTask image_path1 = storageReference1.putBytes(thumbData);
                    Task<Uri> urlTask = image_path1.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                        @Override
                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                            if (!task.isSuccessful()) {
                                throw task.getException();
                            }

                            // Continue with the task to get the download URL
                            return storageReference1.getDownloadUrl();
                        }
                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                        @Override
                        public void onComplete(@NonNull Task<Uri> task) {
                            if (task.isSuccessful()) {
                                final Uri downloadUri = task.getResult();

                                //Timestamp for Date and Time
                                Calendar cal = Calendar.getInstance();
                                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
                                final String date = simpleDateFormat.format(cal.getTime());




                                final Map<String, Object> userval = new HashMap<>();
                                userval.put("IMGValue", value);

                                //uploading hashmap in firebase firestore
                                firebaseFirestore.collection("Users").document(userid).update(userval).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()){
                                            //progressDialog.dismiss();
                                            //Toast.makeText(StoreData.this,"User Data saved Successfully",Toast.LENGTH_SHORT).show();
                                            //startActivity(new Intent(StoreData.this,MainActivity.class));


                                            firebaseFirestore.collection("Users").document(Current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.getResult().exists()){
                                                        username = task.getResult().getString("Name");

                                                        Map<String, Object> allimage = new HashMap<>();
                                                        allimage.put("Image", downloadUri.toString());
                                                        allimage.put("Date", date);
                                                        allimage.put("Title", title);
                                                        allimage.put("Blog", blog);
                                                        allimage.put("Name", username);
                                                        //allimage.put("Like", 0);

                                                        firebaseFirestore.collection("AllPost").document(userid+val).set(allimage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    //progressDialog.dismiss();
                                                                    //Toast.makeText(StoreData.this,"User Data saved Successfully",Toast.LENGTH_SHORT).show();
                                                                    //startActivity(new Intent(StoreData.this,MainActivity.class));


                                                                    Map<String, Object> userData = new HashMap<>();
                                                                    userData.put("Title", title);
                                                                    userData.put("Date", date);
                                                                    userData.put("Post", downloadUri.toString());
                                                                    userData.put("Blog", blog);



                                                                    firebaseFirestore.collection("Users").document(userid).collection("Post").document(userid+val).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()){
                                                                                progressDialog.dismiss();
                                                                                Toast.makeText(StoreData.this,"User Data saved Successfully",Toast.LENGTH_SHORT).show();
                                                                                startActivity(new Intent(StoreData.this,Postview.class));
                                                                                finish();
                                                                            }else {
                                                                                progressDialog.dismiss();
                                                                                Toast.makeText(StoreData.this,"Firestore Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });

                                                                }else {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(StoreData.this,"Firestore Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    }
                                                }
                                            });

                                            /*Map<String, Object> allimage = new HashMap<>();
                                            allimage.put("Image", downloadUri.toString());
                                            allimage.put("Date", date);
                                            allimage.put("Title", title);
                                            allimage.put("Blog", blog);

                                            firebaseFirestore.collection("AllPost").document(userid+val).set(allimage).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()){
                                                        //progressDialog.dismiss();
                                                        //Toast.makeText(StoreData.this,"User Data saved Successfully",Toast.LENGTH_SHORT).show();
                                                        //startActivity(new Intent(StoreData.this,MainActivity.class));


                                                        Map<String, Object> userData = new HashMap<>();
                                                        userData.put("Title", title);
                                                        userData.put("Date", date);
                                                        userData.put("Post", downloadUri.toString());
                                                        userData.put("Blog", blog);


                                                        firebaseFirestore.collection("Users").document(userid).collection("Post").document("Post"+val).set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()){
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(StoreData.this,"User Data saved Successfully",Toast.LENGTH_SHORT).show();
                                                                    startActivity(new Intent(StoreData.this,Postview.class));
                                                                    finish();
                                                                }else {
                                                                    progressDialog.dismiss();
                                                                    Toast.makeText(StoreData.this,"Firestore Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                                }
                                                            }
                                                        });

                                                    }else {
                                                        progressDialog.dismiss();
                                                        Toast.makeText(StoreData.this,"Firestore Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });*/


                                        }else {
                                            progressDialog.dismiss();
                                            Toast.makeText(StoreData.this,"Firestore Error: "+task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                                //For All Users



                                //uploading hashmap in firebase firestore




                            } else {
                                // Handle failures
                                // ...
                                Toast.makeText(StoreData.this,"failed",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }
                else{
                    progressDialog.dismiss();
                    Toast.makeText(StoreData.this,"Please fill all fields",Toast.LENGTH_SHORT).show();
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
                //mUserimage.setImageBitmap(compressed);
                Glide.with(StoreData.this).load(compressed).centerCrop().into(mUserimage);
            }catch (FileNotFoundException e){
                e.printStackTrace();
            }
        }
    }
}