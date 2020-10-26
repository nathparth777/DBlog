package com.example.dblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    Toolbar toolbar;
    CircleImageView circleImageView;
    TextView PName;
    TextView emptyview;


    private FirebaseAuth mAuth;
    private String Current_userID;
    private RecyclerView recyclerView;
    private FirestoreRecyclerAdapter adapter1;
    private FirebaseFirestore firebaseFirestore;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        toolbar = findViewById(R.id.ptoolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_white_arrow_back);

        emptyview = findViewById(R.id.empty);
        circleImageView = findViewById(R.id.pimage);
        PName = findViewById(R.id.pname);
        mAuth = FirebaseAuth.getInstance();
        Current_userID = mAuth.getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.recyclerview);

        firebaseFirestore.collection("Users").document(Current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    String name = task.getResult().getString("Name");
                    String image = task.getResult().getString("UserPic");

                    if (image==null){
                        PName.setText(name);
                    }else {
                        PName.setText(name);
                        Glide.with(Profile.this).load(image).circleCrop().skipMemoryCache(false).into(circleImageView);
                    }
                }else{
                    Toast.makeText(Profile.this, "Error getting Document", Toast.LENGTH_SHORT).show();
                }
            }
        });

        firebaseFirestore.collection("Users").document(Current_userID).collection("Post").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult().isEmpty()){
                    emptyview.setVisibility(View.VISIBLE);
                    recyclerView.setVisibility(View.GONE);
                }else {
                    Query query1 = firebaseFirestore.collection("Users").document(Current_userID).collection("Post").orderBy("Date", Query.Direction.DESCENDING);

                    FirestoreRecyclerOptions<postModel> option = new FirestoreRecyclerOptions.Builder<postModel>()
                            .setQuery(query1,postModel.class)
                            .build();


                    adapter1 = new FirestoreRecyclerAdapter<postModel, ViewHolder>(option) {
                        @NonNull
                        @Override
                        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.propost_list,parent,false);
                            return new ViewHolder(view);
                        }

                        @Override
                        protected void onBindViewHolder(@NonNull final ViewHolder holder, final int position, @NonNull postModel model) {
                            holder.mtitle.setText(model.getTitle());
                            Glide.with(Profile.this).load(model.getPost()).fitCenter().skipMemoryCache(false).into(holder.mimage);

                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    String title = getItem(position).getTitle();
                                    String blog = getItem(position).getBlog();
                                    String image = getItem(position).getPost();
                                    Intent i = new Intent(Profile.this,ProBlog_detail.class);
                                    i.putExtra("Image",image);
                                    i.putExtra("Title",title);
                                    i.putExtra("Blog",blog);
                                    startActivity(i);
                                }
                            });
                            holder.mdelete.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    DocumentSnapshot snapshot = getSnapshots().getSnapshot(holder.getAdapterPosition());
                                    final String docid = snapshot.getId();

                                    firebaseFirestore.collection("AllPost").document(docid).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            firebaseFirestore.collection("Users").document(Current_userID).collection("Post").document(docid)
                                                    .delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Toast.makeText(Profile.this, "Blog deleted successfully", Toast.LENGTH_SHORT).show();

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(Profile.this, "Blog delete failed", Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(Profile.this, "Blog delete failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                }
                            });

                        }
                    };


                    recyclerView.setHasFixedSize(true);
                    recyclerView.setLayoutManager(new LinearLayoutManager(Profile.this));
                    recyclerView.setAdapter(adapter1);

                    DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(Profile.this,DividerItemDecoration.VERTICAL);
                    dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
                    recyclerView.addItemDecoration(dividerItemDecoration);
                }
            }
        });


        /*Query query1 = firebaseFirestore.collection("Users").document(Current_userID).collection("Post").orderBy("Date", Query.Direction.DESCENDING);

        FirestoreRecyclerOptions<postModel> option = new FirestoreRecyclerOptions.Builder<postModel>()
                .setQuery(query1,postModel.class)
                .build();


        adapter1 = new FirestoreRecyclerAdapter<postModel, ViewHolder>(option) {
            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.propost_list,parent,false);
                return new ViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ViewHolder holder, final int position, @NonNull postModel model) {
                holder.mtitle.setText(model.getTitle());
                Glide.with(Profile.this).load(model.getPost()).centerCrop().into(holder.mimage);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String title = getItem(position).getTitle();
                        String blog = getItem(position).getBlog();
                        String image = getItem(position).getPost();
                        Intent i = new Intent(Profile.this,Blog_detail.class);
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
        recyclerView.setAdapter(adapter1);*/
    }

    private class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView mimage, mdelete;
        private TextView mtitle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            mimage = itemView.findViewById(R.id.profilepost);
            mtitle = itemView.findViewById(R.id.profilename);
            mdelete = itemView.findViewById(R.id.delete);

        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseFirestore.collection("Users").document(Current_userID).collection("Post").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.getResult().isEmpty()){
                    adapter1.stopListening();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseFirestore.collection("Users").document(Current_userID).collection("Post").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (!task.getResult().isEmpty()){
                    adapter1.startListening();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater =getMenuInflater();
        inflater.inflate(R.menu.profile_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_edit){
            firebaseFirestore.collection("Users").document(Current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){
                        String name = task.getResult().getString("Name");
                        String image = task.getResult().getString("UserPic");

                        Intent editprofie = new Intent(Profile.this,Profile_edit.class);
                        editprofie.putExtra("Name",name);
                        editprofie.putExtra("Image",image);
                        startActivity(editprofie);
                    }else{
                        Toast.makeText(Profile.this, "Error getting Document", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            return true;
        }
        return false;
    }
}