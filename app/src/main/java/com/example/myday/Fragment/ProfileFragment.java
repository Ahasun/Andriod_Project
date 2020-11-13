package com.example.myday.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.myday.Adapter.MyPhotoAdapter;
import com.example.myday.EditProfileActivity;
import com.example.myday.FollowersActivity;
import com.example.myday.Model.Post;
import com.example.myday.Model.User;
import com.example.myday.OptionsActivity;
import com.example.myday.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


public class ProfileFragment extends Fragment
{

    ImageView imageProfile;
    ImageView options;
    TextView posts;
    TextView followers;
    TextView following;
    TextView fullName;
    TextView bio;
    TextView username;
    Button editProfile;
    ImageButton myPhotos;
    ImageButton savedPhotos;
    FirebaseUser firebaseUser;
    String profileId;
    RecyclerView recyclerView;
    MyPhotoAdapter myPhotoAdapter;
    List<Post> postList;


    List<String> mySaves;
    RecyclerView recyclerViewSaves;
    MyPhotoAdapter myPhotoAdapterSaves;
    List<Post> postListSaves;






    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();




        // Shared Preference object points to a file containing key value pairs and provides simple methods to read and write them.
        // can be though of as a dictionary or key/value pair.


        SharedPreferences prefs = getContext().getSharedPreferences("PREFS", Context.MODE_PRIVATE);

        profileId = prefs.getString("profileid", "none");





        imageProfile = view.findViewById(R.id.image_profile);
        options = view.findViewById(R.id.options);
        posts = view.findViewById(R.id.posts);
        followers = view.findViewById(R.id.followers);
        following = view.findViewById(R.id.following);
        fullName = view.findViewById(R.id.fullName);
        bio = view.findViewById(R.id.bio);
        username = view.findViewById(R.id.username);
        editProfile = view.findViewById(R.id.edit_profile);
        myPhotos = view.findViewById(R.id.my_photos);
        savedPhotos = view.findViewById(R.id.saved_photos);


        // ************* for regular posts *****************************
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(getContext(), 3);       // sets 3 photos for width
        recyclerView.setLayoutManager(linearLayoutManager);
        postList = new ArrayList<>();
        myPhotoAdapter = new MyPhotoAdapter(getContext(), postList);
        recyclerView.setAdapter(myPhotoAdapter);



        // ************* for saved posts ********************************
        recyclerViewSaves = view.findViewById(R.id.recycler_view_save);
        recyclerViewSaves.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManagerSaves = new GridLayoutManager(getContext(), 3);       // sets 3 photos for width
        recyclerViewSaves.setLayoutManager(linearLayoutManagerSaves);
        postListSaves = new ArrayList<>();
        myPhotoAdapterSaves = new MyPhotoAdapter(getContext(), postListSaves);
        recyclerViewSaves.setAdapter(myPhotoAdapterSaves);




        recyclerView.setVisibility(View.VISIBLE);       // make recycler view for regular posts visible
        recyclerViewSaves.setVisibility(View.GONE);     // hide recycler view for saved posts




        userInfo();
        getFollowers();
        getNumberOfPosts();
        getMyPhotos();
        getMySaves();




        if (profileId.equals(firebaseUser.getUid()))
        {
            editProfile.setText("Edit Profile");
        }
        else
        {
            checkFollow();
            savedPhotos.setVisibility(View.GONE);
        }




        // for when the user clicks " Edit Profile" or "Follow" or "Following"  on the profile screen
        editProfile.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                String button = editProfile.getText().toString();


                if (button.equals("Edit Profile"))
                {
                    // go to edit profile
                    startActivity(new Intent(getContext(), EditProfileActivity.class));
                }
                else if (button.equals("Follow"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following").child(profileId).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("Followers").child(firebaseUser.getUid()).setValue(true);

                    addNotification();
                }
                else if (button.equals("Following"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following").child(profileId).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("Followers").child(firebaseUser.getUid()).removeValue();

                }

            }
        });



        // when user clicks on the options icon
        options.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getContext(), OptionsActivity.class);

                startActivity(intent);
            }
        });





        // when user clicks on myPhotos icon, show the user's uploaded photos
        myPhotos.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recyclerView.setVisibility(View.VISIBLE);       // make recycler view for regular posts visible
                recyclerViewSaves.setVisibility(View.GONE);     // hide recycler view for saved posts
            }
        });


        // when user clicks on savedPhotos icon, show the posts the user has saved
        savedPhotos.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                recyclerView.setVisibility(View.GONE);              // hide recycler view for regular posts
                recyclerViewSaves.setVisibility(View.VISIBLE);     // make recycler view visible for saved posts


            }
        });




        // when followers icon is clicked, display all users who are following that user
        followers.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getContext(), FollowersActivity.class);

                intent.putExtra("id", profileId);
                intent.putExtra("title", "Followers");
                startActivity(intent);
            }
        });



        // when following icon is clicked, display all accounts that user is following
        following.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(getContext(), FollowersActivity.class);

                intent.putExtra("id", profileId);
                intent.putExtra("title", "Following");
                startActivity(intent);
            }
        });


        return view;
    }




    public void addNotification()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(profileId);


        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postId", "");
        hashMap.put("isPost", false);




        reference.push().setValue(hashMap);
        // updates the database with values inside the hashMap
    }






    private void userInfo()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(profileId);

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (getContext() == null)
                {
                    return;
                }

                User user = dataSnapshot.getValue(User.class);

                Glide.with(getContext()).load(user.getImageURL()).into(imageProfile);


                username.setText(user.getUserName());
                fullName.setText(user.getFullName());
                bio.setText(user.getBio());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }




    private void checkFollow()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following");


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(profileId).exists())
                {
                    editProfile.setText("Following");
                }
                else
                {
                    editProfile.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }






    private void getFollowers()
    {
        // get the number of followers the current user has
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("Followers");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                followers.setText("" + dataSnapshot.getChildrenCount());
                    // set the number of followers the user has onto the screen
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });



        // get the number users the current user is following
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference().child("Follow").child(profileId).child("Following");

        reference1.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                following.setText("" + dataSnapshot.getChildrenCount());
                    // sets the number of users the current user is following
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }





    private void getNumberOfPosts()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int i = 0;

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Post post = snapshot.getValue(Post.class);

                    if (post.getPublisher().equals(profileId))
                    {
                        i++;
                    }
                }

                posts.setText("" + i);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }








    private void getMyPhotos()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                postList.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Post post = snapshot.getValue(Post.class);

                    if (post.getPublisher().equals(profileId))
                    {
                        postList.add(post);
                    }
                }


                Collections.reverse(postList);

                myPhotoAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }







    private void getMySaves()
    {
        mySaves = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Saves").child(firebaseUser.getUid());


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    mySaves.add(snapshot.getKey());
                }

                readSaves();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void readSaves()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                postListSaves.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Post post = snapshot.getValue(Post.class);

                    for (String id: mySaves)
                    {
                        if (post.getPostId().equals(id))
                        {
                            postListSaves.add(post);
                        }
                    }
                }

                myPhotoAdapterSaves.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
