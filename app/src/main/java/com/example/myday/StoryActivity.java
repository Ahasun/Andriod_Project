package com.example.myday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.myday.Model.Story;
import com.example.myday.Model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import jp.shts.android.storiesprogressview.StoriesProgressView;

public class StoryActivity extends AppCompatActivity implements StoriesProgressView.StoriesListener
{
    int counter = 0;
    long pressTime = 0L;
    long limit = 500L;


    StoriesProgressView storiesProgressView;
    ImageView image;
    ImageView storyPhoto;
    TextView storyUsername;

    List<String> images;
    List<String> storyIds;
    String userId;




    LinearLayout containerSeen;
    TextView seenNumber;
    ImageView storyDelete;





    private View.OnTouchListener onTouchListener = new View.OnTouchListener()           // swiping up and down on the screen
    {

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                    pressTime = System.currentTimeMillis();
                    storiesProgressView.pause();
                    return false;

                case MotionEvent.ACTION_UP:
                    long now = System.currentTimeMillis();
                    storiesProgressView.resume();
                    return limit < now - pressTime;
            }


            return false;
        }
    };










    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);



        storiesProgressView = findViewById(R.id.stories);
        image = findViewById(R.id.image_story);
        storyPhoto = findViewById(R.id.story_photo);
        storyUsername = findViewById(R.id.story_username);


        userId = getIntent().getStringExtra("userId");


        containerSeen = findViewById(R.id.container_seen);
        seenNumber = findViewById(R.id.seen_number);
        storyDelete = findViewById(R.id.story_delete);



        containerSeen.setVisibility(View.GONE);
        storyDelete.setVisibility(View.GONE);


        if (userId.equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
        {
            containerSeen.setVisibility(View.VISIBLE);
            storyDelete.setVisibility(View.VISIBLE);
        }









        getStories(userId);
        userInfo(userId);





        // reverse - tapping on left side of the screen
        View reverse = findViewById(R.id.reverse);

        reverse.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                storiesProgressView.reverse();

            }
        });

        reverse.setOnTouchListener(onTouchListener);





        // skip - tapping on the right side of the screen
        View skip = findViewById(R.id.skip);

        skip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                storiesProgressView.skip();

            }
        });

        skip.setOnTouchListener(onTouchListener);






        containerSeen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(StoryActivity.this, FollowersActivity.class);
                intent.putExtra("id", userId);
                intent.putExtra("storyId", storyIds.get(counter));
                intent.putExtra("title", "Views");
                startActivity(intent);
            }
        });




        storyDelete.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyIds.get(counter));


                reference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Toast.makeText(StoryActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        });


    }


    @Override
    public void onNext()
    {
        Glide.with(getApplicationContext()).load(images.get(++counter)).into(image);

        addView(storyIds.get(counter));
        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onPrev()
    {
        if ((counter - 1) < 0)
        {
            return;
        }

        Glide.with(getApplicationContext()).load(images.get(--counter)).into(image);

        seenNumber(storyIds.get(counter));
    }

    @Override
    public void onComplete()
    {
        finish();

    }


    @Override
    protected void onDestroy()
    {
        storiesProgressView.destroy();

        super.onDestroy();
    }


    @Override
    protected void onPause()
    {
        storiesProgressView.pause();


        super.onPause();
    }


    @Override
    protected void onResume()
    {
        storiesProgressView.resume();
        super.onResume();
    }


    private void getStories(String userId)
    {
        images = new ArrayList<>();
        storyIds = new ArrayList<>();


        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);

        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                images.clear();
                storyIds.clear();

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Story story = snapshot.getValue(Story.class);

                    long timeCurrent = System.currentTimeMillis();

                    if (timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd())
                    {
                        images.add(story.getImageURL());
                        storyIds.add(story.getStoryId());
                    }
                }


                storiesProgressView.setStoriesCount(images.size());
                storiesProgressView.setStoryDuration(5000L);
                storiesProgressView.setStoriesListener(StoryActivity.this);
                storiesProgressView.startStories(counter);


                Glide.with(getApplicationContext()).load(images.get(counter)).into(image);

                addView(storyIds.get(counter));
                seenNumber(storyIds.get(counter));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }





    private void userInfo(String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);


        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);

                Glide.with(getApplicationContext()).load(user.getImageURL()).into(storyPhoto);

                storyUsername.setText(user.getUserName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }



    private void addView(String storyId)
    {
        FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId).child("Views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).setValue(true);


    }



    private void seenNumber(String storyId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId).child(storyId).child("Views");


        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                seenNumber.setText("" + dataSnapshot.getChildrenCount());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });


    }

}
