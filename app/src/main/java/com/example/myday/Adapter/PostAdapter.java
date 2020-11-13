package com.example.myday.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.myday.CommentsActivity;
import com.example.myday.FollowersActivity;
import com.example.myday.Fragment.PostDetailFragment;
import com.example.myday.Fragment.ProfileFragment;
import com.example.myday.Model.Post;
import com.example.myday.Model.User;
import com.example.myday.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import java.util.HashMap;
import java.util.List;

public class PostAdapter  extends RecyclerView.Adapter<PostAdapter.ViewHolder>
{
    public Context mContext;
    public List<Post> mPost;
    private FirebaseUser firebaseUser;



    public PostAdapter(Context mContext, List<Post> mPost)
    {
        this.mContext = mContext;
        this.mPost = mPost;
    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, parent, false);

        return new PostAdapter.ViewHolder(view);


        // View - represents the basic building block for user interface components.
        //      a view occupies a rectangular area on the screen and is responsible for drawing and event handling.
        //      View is the basic class for widgets, which are used to create interactive UI components(buttons, text fields, etc)

        // The ViewGroup subclass is the base class for layouts, which are invisible containers that hold other Views (or other ViewGroups) and define their layout properties


        // LayoutInflater - instantiates a layout XML file into its corresponding View Objects.


        // inflate - inflate a new view hierarchy from the specified XML resource.
        //      When you write an XML layout, it will be inflated by the Android OS which basically means that it will be rendered by creating view object in memory.
        //      Inflating is the process of adding a view(.xml) to activity on Runtime.

    }





    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final Post post = mPost.get(position);


        Glide.with(mContext).load(post.getPostImage()).apply(new RequestOptions().placeholder(R.drawable.placeholder)).into(holder.postImage); //change




        // show if description is not null. Else show description of post.
        if (post.getDescription().equals(""))
        {
            holder.description.setVisibility(View.GONE);//change
        }
        else
        {
            holder.description.setVisibility(View.VISIBLE);//change

            holder.description.setText(post.getDescription());//change
        }




        publisherInfo(holder.imageProfile, holder.username, holder.publisher, post.getPublisher());//change



        isLiked(post.getPostId(), holder.like);
        numberOfLikes(holder.likes, post.getPostId());
        getComments(post.getPostId(), holder.comments);
        isSaved(post.getPostId(), holder.save);









        // ********************* when user clicks on profile picture *********************
        holder.imageProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });



        // ********************* when user clicks on a username *********************
        holder.username.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });


        // ********************* when user clicks on the publisher of a comment *********************
        holder.publisher.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
            }
        });


        // ********************* when user clicks on the post image *********************
        holder.postImage.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();

                editor.putString("postid", post.getPostId());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new PostDetailFragment()).commit();
            }
        });




        // ********************* when user clicks on save post *********************
        holder.save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (holder.save.getTag().equals("save"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid()).child(post.getPostId()).setValue(true);
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid()).child(post.getPostId()).removeValue();
                }
            }
        });


        // ********************* when user clicks the like button for a post *********************
        holder.like.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                if (holder.like.getTag().equals("like"))
                {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).setValue(true);

                    addNotification(post.getPublisher(), post.getPostId());
                }
                else
                {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostId()).child(firebaseUser.getUid()).removeValue();
                }

            }
        });





        // ********************* when user clicks on comment button *********************
        holder.comment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, CommentsActivity.class);

                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());        // data transfer between activites. Key value pair

                mContext.startActivity(intent);



                //  android application components can connect to other android applications.
                //  this connection is based on a task description represented by an Intent object.

                // Intent - is an abstract description of an operation to be performed.
                //      an intent provides a facility for performing late runtime binding between the code in different applications.
                //      its most significant use us in the launching of activities, where it can be thought of as the glue between activities.
                //      it is basically a passive data structure holding an abstract description of an action to be performed.


                // Intents are asynchronous messages which allow application components to request functionality from other android components.
                //      intents allow you to interact with components from the same applications as well as with components contributed by other applications.
                //      startActivity() - you can define that the intent should be used to start an activity.


                // explicit intent
                //      explicitly define the component which should be called by the android system.
                //      intent.putExtra()

            }
        });



        // ********************* user clicks on view all comments *********************
        holder.comments.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, CommentsActivity.class);

                intent.putExtra("postId", post.getPostId());
                intent.putExtra("publisherId", post.getPublisher());

                mContext.startActivity(intent);
            }
        });



        // when user clicks on likes, display all users who liked the post
        holder.likes.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, FollowersActivity.class);

                intent.putExtra("id", post.getPostId());
                intent.putExtra("title", "Likes");
                mContext.startActivity(intent);

            }
        });



        // when user clicks on the 3 dots "more" menu button
        holder.more.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PopupMenu popupMenu = new PopupMenu(mContext, v);



                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                       switch (item.getItemId())
                       {
                           case R.id.edit:
                               editPost(post.getPostId());
                               return true;

                           case R.id.delete:
                               FirebaseDatabase.getInstance().getReference("Posts").child(post.getPostId()).removeValue()
                                       .addOnCompleteListener(new OnCompleteListener<Void>()
                                       {
                                           @Override
                                           public void onComplete(@NonNull Task<Void> task)
                                           {
                                               if (task.isSuccessful())
                                               {
                                                   Toast.makeText(mContext, "Deleted!", Toast.LENGTH_SHORT).show();
                                               }
                                           }
                               });
                               return true;


                           case R.id.report:
                               Toast.makeText(mContext, "Report Post", Toast.LENGTH_SHORT).show();
                               return true;


                           default:
                               return false;
                       }
                    }
                });



                popupMenu.inflate(R.menu.post_menu);

                if (! post.getPublisher().equals(firebaseUser.getUid()))
                {
                    popupMenu.getMenu().findItem(R.id.edit).setVisible(false);
                    popupMenu.getMenu().findItem(R.id.delete).setVisible(false);
                }


                popupMenu.show();
            }
        });
    }



    @Override
    public int getItemCount()
    {
        return mPost.size();
    }







    // inner class
    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView imageProfile;
        public ImageView postImage;
        public ImageView like;
        public ImageView comment;
        public ImageView save;
        public ImageView more;

        public TextView username;
        public TextView likes;
        public TextView publisher;
        public TextView description;
        public TextView comments;





        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);


            imageProfile = itemView.findViewById(R.id.image_profile);
            postImage = itemView.findViewById(R.id.post_image);
            like = itemView.findViewById(R.id.like);
            comment = itemView.findViewById(R.id.comment);
            save = itemView.findViewById(R.id.save);
            more = itemView.findViewById(R.id.more);
            username = itemView.findViewById(R.id.username);
            likes = itemView.findViewById(R.id.likes);
            publisher = itemView.findViewById(R.id.publisher);
            description = itemView.findViewById(R.id.description);
            comments = itemView.findViewById(R.id.comments);
        }
    }






    public void addNotification(String userId, String postId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);


        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postId", postId);
        hashMap.put("isPost", true);




        reference.push().setValue(hashMap);
            // updates the database with values inside the hashMap
    }














    private void getComments(String postId, final TextView comments)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postId);

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                comments.setText("View All " + dataSnapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }











    private void isLiked(String postId, final ImageView imageView)
    {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postId);


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(firebaseUser.getUid()).exists())
                {
                    imageView.setImageResource(R.drawable.ic_liked);

                    imageView.setTag("liked");

                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_like);

                    imageView.setTag("like");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }





    private void numberOfLikes(final TextView likes, String postId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postId);


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                likes.setText(dataSnapshot.getChildrenCount() + " likes");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }












    private void publisherInfo(final ImageView imageProfile, final TextView username, final TextView publisher, String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);

                Glide.with(mContext).load(user.getImageURL()).into(imageProfile);

                username.setText(user.getUserName());

                publisher.setText(user.getUserName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }




    private void isSaved(final String postId, final ImageView imageView)
    {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Saves").child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(postId).exists())
                {
                    imageView.setImageResource(R.drawable.ic_save_black);
                    imageView.setTag("saved");
                }
                else
                {
                    imageView.setImageResource(R.drawable.ic_save);
                    imageView.setTag("save");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }




    private void editPost(final String postId)
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        alertDialog.setTitle("Edit Post");


        final EditText editText = new EditText(mContext);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        editText.setLayoutParams(layoutParams);
        alertDialog.setView(editText);


        getText(postId, editText);


        alertDialog.setPositiveButton("Edit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                HashMap<String, Object> hashMap = new HashMap<>();

                hashMap.put("description", editText.getText().toString());


                FirebaseDatabase.getInstance().getReference("Posts").child(postId).updateChildren(hashMap);
            }
        });


        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });

        alertDialog.show();

    }




    private void getText(String postId, final EditText editText)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Posts").child(postId);



        reference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                editText.setText(dataSnapshot.getValue(Post.class).getDescription());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });
    }
}
