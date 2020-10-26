package com.example.dblog;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.text.LineBreaker;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codesgood.views.JustifiedTextView;
import com.squareup.picasso.Picasso;

public class Blog_detail extends AppCompatActivity {

    ImageView imageView;

    JustifiedTextView textView1, textView2;
    ScrollView scrollView;
    Toolbar toolbar;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blog_detail);

        imageView = findViewById(R.id.inuserimage);
        textView1 = findViewById(R.id.intitle);
        textView2 = findViewById(R.id.inblog);
        toolbar = findViewById(R.id.blog_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_white_arrow_back);
        scrollView = findViewById(R.id.scrollview);
        scrollView.setVerticalScrollBarEnabled(false);


        String inTitle = getIntent().getExtras().get("Title").toString();
        String inBlog = getIntent().getExtras().get("Blog").toString();
        String inImage = getIntent().getExtras().get("Image").toString();
        textView1.setText(inTitle);
        textView2.setText(inBlog);
        //Picasso.get().load(inImage).fit().centerCrop().into(imageView);
        Glide.with(Blog_detail.this)
                .load(inImage)
                .fitCenter()
                .into(imageView);



    }
}