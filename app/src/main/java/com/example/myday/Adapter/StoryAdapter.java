package com.example.myday.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myday.AddStoryActivity;
import com.example.myday.Model.Story;
import com.example.myday.Model.User;
import com.example.myday.R;
import com.example.myday.StoryActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class StoryAdapter extends RecyclerView.Adapter<StoryAdapter.ViewHolder>
{
    private Context mContext;
    private List<Story> mStory;


    public StoryAdapter(Context mContext, List<Story> mStory)
    {
        this.mContext = mContext;
        this.mStory = mStory;

    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        if (viewType == 0)
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.add_story_item, parent, false);

            return new StoryAdapter.ViewHolder(view);
        }
        else
        {
            View view = LayoutInflater.from(mContext).inflate(R.layout.story_item, parent, false);

            return new StoryAdapter.ViewHolder(view);
        }
    }







    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        final Story story = mStory.get(position);

        userInfo(holder, story.getUserId(), position);



        if (holder.getAdapterPosition() != 0)
        {
            seenStory(holder, story.getUserId());
        }


        if (holder.getAdapterPosition() == 0)
        {
            myStory(holder.addStoryText, holder.storyPlus, false);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (holder.getAdapterPosition() == 0)
                {
                    myStory(holder.addStoryText, holder.storyPlus, true);
                }
                else
                {
                    Intent intent = new Intent(mContext, StoryActivity.class);
                    intent.putExtra("userId", story.getUserId());
                    mContext.startActivity(intent);
                }
            }
        });
    }










    @Override
    public int getItemCount()
    {
        return mStory.size();
    }











    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView storyPhoto;
        public ImageView storyPlus;
        public ImageView storyPhotoSeen;
        public TextView storyUsername;
        public TextView addStoryText;



        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            storyPhoto = itemView.findViewById(R.id.story_photo);
            storyPlus = itemView.findViewById(R.id.story_plus);
            storyPhotoSeen = itemView.findViewById(R.id.story_photo_seen);
            storyUsername = itemView.findViewById(R.id.story_username);
            addStoryText = itemView.findViewById(R.id.add_story_text);
        }
    }


    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
        {
            return 0;
        }

        return 1;
    }





    private void userInfo(final ViewHolder viewHolder, String userId, final int position)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);


        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);

                Glide.with(mContext).load(user.getImageURL()).into(viewHolder.storyPhoto);


                if (position != 0)
                {
                    Glide.with(mContext).load(user.getImageURL()).into(viewHolder.storyPhotoSeen);

                    viewHolder.storyUsername.setText(user.getUserName());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }








    private void myStory(final TextView textView, final ImageView imageView, final boolean click)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(FirebaseAuth.getInstance().getCurrentUser().getUid());


        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int count = 0;

                long timeCurrent = System.currentTimeMillis();

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    Story story = snapshot.getValue(Story.class);

                    if (timeCurrent > story.getTimeStart() && timeCurrent < story.getTimeEnd())
                    {
                        count++;
                    }
                }


                if (click)
                {
                    if (count > 0)
                    {
                        final AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();



                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "View Story", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent intent = new Intent(mContext, StoryActivity.class);
                                intent.putExtra("userId", FirebaseAuth.getInstance().getCurrentUser().getUid());
                                mContext.startActivity(intent);
                                alertDialog.dismiss();

                            }
                        });



                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Add Story", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which)
                            {
                                Intent intent = new Intent(mContext, AddStoryActivity.class);

                                mContext.startActivity(intent);

                                dialog.dismiss();
                            }
                        });



                        alertDialog.show();

                    }
                    else
                    {
                        Intent intent = new Intent(mContext, AddStoryActivity.class);

                        mContext.startActivity(intent);
                    }


                }
                else
                {
                    if (count > 0)
                    {
                        textView.setText("My Story");

                        imageView.setVisibility(View.GONE);
                    }
                    else
                    {
                        textView.setText("Add Story");

                        imageView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }








    private void seenStory(final ViewHolder viewHolder, String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(userId);


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                int i = 0;

                for (DataSnapshot snapshot: dataSnapshot.getChildren())
                {
                    if (! snapshot.child("Views").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).exists() &&
                            System.currentTimeMillis() < snapshot.getValue(Story.class).getTimeEnd())
                    {
                        i++;
                    }
                }


                if (i > 0)
                {
                    viewHolder.storyPhoto.setVisibility(View.VISIBLE);
                    viewHolder.storyPhotoSeen.setVisibility(View.GONE);
                }
                else
                {
                    viewHolder.storyPhoto.setVisibility(View.GONE);
                    viewHolder.storyPhotoSeen.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }
}
