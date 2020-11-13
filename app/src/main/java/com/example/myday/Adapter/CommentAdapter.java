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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myday.MainActivity;
import com.example.myday.Model.Comment;
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

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder>
{
    private Context mContext;
    private List<Comment> mComment;
    private String postId;


    public CommentAdapter(Context mContext, List<Comment> mComment, String postId)
    {
        this.mContext = mContext;
        this.mComment = mComment;
        this.postId = postId;
    }





    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.comment_item, parent, false);

        return new CommentAdapter.ViewHolder(view);
    }






    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position)
    {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();        // gets the current user from Firebase Database


        final Comment comment = mComment.get(position);


        holder.comment.setText(comment.getComment());

        getUserInfo(holder.imageProfile, holder.username, comment.getPublisher());







        holder.comment.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, MainActivity.class);

                intent.putExtra("publisherId", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });



        holder.imageProfile.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(mContext, MainActivity.class);

                intent.putExtra("publisherId", comment.getPublisher());
                mContext.startActivity(intent);
            }
        });


        holder.itemView.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View v)
            {
                if (comment.getPublisher().equals(firebaseUser.getUid()))
                {
                    AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();


                    alertDialog.setTitle("Do you want to delete?");

                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "No", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            dialog.dismiss();
                        }
                    });

                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            FirebaseDatabase.getInstance().getReference("Comments").child(postId).child(comment.getCommentId()).removeValue()
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

                            dialog.dismiss();
                        }
                    });


                    alertDialog.show();

                }

                return true;
            }
        });
    }








    @Override
    public int getItemCount()
    {
        return mComment.size();
    }









    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public ImageView imageProfile;
        public TextView username;
        public TextView comment;


        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);



            imageProfile = itemView.findViewById(R.id.image_profile);
            username = itemView.findViewById(R.id.username);
            comment = itemView.findViewById(R.id.comment);
        }
    }




    private void getUserInfo(final ImageView imageView, final TextView username, String publisherId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(publisherId);


        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                User user = dataSnapshot.getValue(User.class);

                Glide.with(mContext).load(user.getImageURL()).into(imageView);

                username.setText(user.getUserName());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {
            }
        });

    }
}
