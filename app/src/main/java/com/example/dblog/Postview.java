package com.example.dblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dblog.authentication.Login;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.Filter;
import com.google.firestore.v1.StructuredQuery;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Postview extends AppCompatActivity  {

    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;

    CircleImageView circleImageView;
    TextView Name, emptyview;
    String name, image;
    //int flag;

    private FirebaseAuth mAuth;
    private String Current_userID;

    private RecyclerView recyclerView;
    private FirebaseFirestore firebaseFirestore;
    private FirestoreRecyclerAdapter adapter;

    boolean doubleBackToExitPressedOnce = false;



    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_postview);

        mAuth = FirebaseAuth.getInstance();

        Current_userID = mAuth.getUid();
        recyclerView = findViewById(R.id.recyclerview);


        firebaseFirestore = FirebaseFirestore.getInstance();

        circleImageView = (CircleImageView) findViewById(R.id.uimage);
        Name = (TextView)findViewById(R.id.uname);

        setupToolbar();
        setupDrawer();
        setupNav();


        DocumentReference docIdRef = firebaseFirestore.collection("Users").document(Current_userID);
        docIdRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getString("Name");
                        image = document.getString("UserPic");

                    } else {
                        startActivity(new Intent(Postview.this, UserPicName.class));
                        finish();
                        Toast.makeText(Postview.this, "Please fill this Info", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });



        Query query = firebaseFirestore.collection("AllPost").orderBy("Date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<Nodel> options = new FirestoreRecyclerOptions.Builder<Nodel>()
                .setQuery(query,Nodel.class)
                .build();

        adapter = new FirestoreRecyclerAdapter<Nodel, ViewHolder>(options){
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_list,parent,false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull final Nodel model) {
                /*DocumentSnapshot snapshotid = getSnapshots().getSnapshot(holder.getAdapterPosition());
                String documentid = snapshotid.getId();

                DocumentReference docRef = firebaseFirestore.collection("Users").document(Current_userID).collection("Activity_Like").document(documentid);
                docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                holder.mlike.setImageResource(R.drawable.ic_cam);
                                flag = 1;
                                Global global = (Global)getApplicationContext();
                                global.setData(flag);
                            }
                        }
                    }
                });*/

                holder.mtitle.setText(model.getTitle());
                holder.mname.setText("Blog by "+model.getName());
                Glide.with(Postview.this).load(model.getImage()).fitCenter().skipMemoryCache(false).into(holder.mimage);
                //Picasso.get().load(model.getImage()).into(holder.mimage);

                /*holder.mlike.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                        final String docid = snapshot.getId();

                        firebaseFirestore.collection("AllPost").document(docid).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()){
                                    String likeval = task.getResult().getString("Like");
                                    int like = Integer.parseInt(likeval);

                                    Global global = (Global)getApplicationContext();
                                    int flag = global.getData();

                                    if (flag==1){
                                        holder.mlike.setImageResource(R.drawable.ic_camera);
                                        like = like-1;
                                        likeval = Integer.toString(like);

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("Like", likeval);
                                        firebaseFirestore.collection("AllPost").document(docid).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                firebaseFirestore.collection("Users").document(Current_userID).collection("Activity_Like").document(docid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Toast.makeText(Postview.this, "Disliked", Toast.LENGTH_SHORT).show();
                                                        Global global = (Global)getApplicationContext();
                                                        int flag=0;
                                                        global.setData(flag);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Postview.this, "Failed to Dislike"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Postview.this, "Failed to Dislike"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }else {
                                        holder.mlike.setImageResource(R.drawable.ic_cam);
                                        like = like+1;
                                        likeval = Integer.toString(like);

                                        Map<String, Object> data = new HashMap<>();
                                        data.put("Like", likeval);
                                        firebaseFirestore.collection("AllPost").document(docid).update(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Map<String, Object> data1 = new HashMap<>();
                                                data1.put("Like", "You like "+model.getName()+ " Blog.");
                                                firebaseFirestore.collection("Users").document(Current_userID).collection("Activity_Like").document(docid).set(data1).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {

                                                        Toast.makeText(Postview.this, "Liked", Toast.LENGTH_SHORT).show();
                                                        Global global = (Global)getApplicationContext();
                                                        int flag=1;
                                                        global.setData(flag);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(Postview.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {

                                                Toast.makeText(Postview.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }


                                }else {
                                    Toast.makeText(Postview.this, ""+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(Postview.this, "Failed"+e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });*/

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = getItem(position).getTitle();
                        String blog = getItem(position).getBlog();
                        String image = getItem(position).getImage();
                        Intent i = new Intent(Postview.this,Blog_detail.class);
                        i.putExtra("Image",image);
                        i.putExtra("Title",title);
                        i.putExtra("Blog",blog);
                        startActivity(i);
                    }
                });

            }
        };

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    private class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView mimage;
        //private ImageView mlike;
        private TextView mtitle;
        private TextView mname;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mimage = itemView.findViewById(R.id.image);
            mtitle = itemView.findViewById(R.id.text);
            mname = itemView.findViewById(R.id.createby);
            //mlike = itemView.findViewById(R.id.like);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();

    }
    @Override
    protected void onStart() {
        super.onStart();
        if (Current_userID==null){
            startActivity(new Intent(Postview.this, Login.class));
            finish();
        }
        adapter.startListening();

    }
    private void setupDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.app_name,R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_white_menu);
    }

    private void setupToolbar() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void setupNav() {
        navigationView = findViewById(R.id.navigation);

        DocumentReference docRef = firebaseFirestore.collection("Users").document(Current_userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        name = document.getString("Name");
                        image = document.getString("UserPic");

                        View nav_view = navigationView.getHeaderView(0);
                        Name = (TextView)nav_view.findViewById(R.id.uname);
                        circleImageView = (CircleImageView) nav_view.findViewById(R.id.uimage);


                        if (image==null){
                            Name.setText(name);
                        }else {
                            Name.setText(name);
                            Glide.with(Postview.this).load(image).circleCrop().skipMemoryCache(false).into(circleImageView);
                        }


                        //Picasso.get().load(image).into(circleImageView);


                        nav_view.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent profile = new Intent(Postview.this,Profile.class);
                                profile.putExtra("Name",name);
                                profile.putExtra("Image",image);
                                startActivity(profile);
                            }
                        });


                    }
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.nav_home:
                        drawerLayout.closeDrawer(GravityCompat.START);
                        break;
                    case R.id.nav_addblog:
                        Intent pd=new Intent(Postview.this,StoreData.class);
                        startActivity(pd);
                        break;

                    case R.id.nav_help:
                        Intent au=new Intent(Postview.this,Help.class);
                        startActivity(au);

                        break;
                    case R.id.nav_setting:
                        Intent intent=new Intent(Postview.this,Setting.class);
                        startActivity(intent);

                        break;
                    case R.id.nav_signout:
                        Toast.makeText(Postview.this, "Successfully SignOut", Toast.LENGTH_SHORT).show();
                        mAuth.signOut();
                        startActivity(new Intent(Postview.this,Login.class));
                        finish();
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        drawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (doubleBackToExitPressedOnce){
            super.onBackPressed();
            return;
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}

