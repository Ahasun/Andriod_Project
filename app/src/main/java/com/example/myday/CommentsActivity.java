package com.example.myday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myday.Adapter.CommentAdapter;
import com.example.myday.Model.Comment;
import com.example.myday.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CommentsActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private List<Comment> commentList;

    EditText addComment;
    ImageView imageProfile;
    TextView post;
    String postId;
    String publisherId;

    FirebaseUser firebaseUser;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        Toolbar toolbar = findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });



        Intent intent = getIntent();

        postId = intent.getStringExtra("postId");
        publisherId = intent.getStringExtra("publisherId");




        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        commentList = new ArrayList<>();

        commentAdapter = new CommentAdapter(this, commentList, postId);

        recyclerView.setAdapter(commentAdapter);








        addComment = findViewById(R.id.add_comment);
        imageProfile = findViewById(R.id.image_profile);
        post = findViewById(R.id.post);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();






        // When user clicks on POST after typing a comment
        post.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (addComment.getText().toString().equals(""))
                {
                    Toast.makeText(CommentsActivity.this, "You can't send an empty comment", Toast.LENGTH_SHORT);
                }
                else
                {
                    addComment();
                }

            }
        });


        getImage();
        readComments();
    }






    private void addComment()                   // adds comment to firebase database
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);
            // from database,  go to or create "Comments" section if not already created.
            // create entry for postId for the photo that was commented on.


        String commentId = reference.push().getKey();


        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("comment", addComment.getText().toString());        // get text from addComment EditText field
        hashMap.put("publisher", firebaseUser.getUid());                // get the current user id for the account who made the comment
        hashMap.put("commentId", commentId);





        reference.child(commentId).setValue(hashMap);                             // push info into database for that postId.  postId -> hashMap value
        addComment.setText("");
        addNotification();
    }



    public void addNotification()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(publisherId);


        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("text", "commented: " + addComment.getText().toString());
        hashMap.put("postId", postId);
        hashMap.put("isPost", true);




        reference.push().setValue(hashMap);
        // updates the database with values inside the hashMap
    }






    private void getImage()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(firebaseUser.getUid());
            // from database, gets the user Id for the current user.


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                // DataSnapShot instance contains data from a Firebase Database location.  Anytime you read Database data, you receive the data as a DataSnapShot.
                // They are efficiently generated immutable copies of the data at a Firebase Database location.
                // They can't be modified and will never change.
                // To modify data at a location, use DatabaseReference reference with setValue(Object) etc...


                User user = dataSnapshot.getValue(User.class);
                    // User - from Model Package


                Glide.with(getApplicationContext()).load(user.getImageURL()).into(imageProfile);
                    // retrieves user profile image and adds it into image_profile section for xml file.

            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }




    private void readComments()
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Comments").child(postId);


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                commentList.clear();

                for (DataSnapshot snapshot : dataSnapshot.getChildren())
                {
                    Comment comment = snapshot.getValue(Comment.class);
                        // retrieves the values from Database and converts it into Comment class since the variables for the class and the keys in database match
                        //      variables for Comment: comment and publisher
                        //      keys in database:    comment and publisher

                    commentList.add(comment);

                }


                commentAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }
}
