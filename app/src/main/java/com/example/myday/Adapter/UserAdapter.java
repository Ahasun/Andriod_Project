package com.example.myday.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myday.Fragment.ProfileFragment;
import com.example.myday.MainActivity;
import com.example.myday.Model.User;
import com.example.myday.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;





public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder>
{
    private Context mContext;
    private List<User> mUsers;
    private boolean isFragment;     // necessary since you can't jump from an activity to a fragment


    private FirebaseUser firebaseUser;



    public UserAdapter(Context mContext, List<User> mUsers, boolean isFragment)
    {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isFragment = isFragment;
    }




    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position)
    {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final User user = mUsers.get(position);

        holder.btn_follow.setVisibility(View.VISIBLE);          // sets follow button visible
        holder.username.setText(user.getUserName());            // sets username
        holder.fullName.setText(user.getFullName());            // sets full name


        Glide.with(mContext).load(user.getImageURL()).into(holder.image_profile);
            // Glide is a fast and efficient open source media management and image loading framework for android that
            //      wraps decoding, memory and disk caching, and resource pooling into a simple and easy to use interface.
            // Primary focus is on making scrolling any kind of a list of images as smooth and fast as possible.




        isFollowing(user.getId(), holder.btn_follow);
            // Displays button next to other user's username.
            // if following, button displays "following". Else, it displays "follow"



        if (user.getId().equals(firebaseUser.getUid()))
        {
            holder.btn_follow.setVisibility(View.GONE);
                // if its not the current user's profile, hide the "edit profile" button
        }



        // For when button navigation bar is clicked
        holder.itemView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isFragment)
                {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", user.getId());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ProfileFragment()).commit();
                }
                else
                {
                    Intent intent = new Intent(mContext, MainActivity.class);

                    intent.putExtra("publisherid", user.getId());

                    mContext.startActivity(intent);
                }
            }
        });




        // For when follow / following button is clicked
        holder.btn_follow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (holder.btn_follow.getText().toString().equals("Follow"))        // if follow button is clicked
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following").child(user.getId()).setValue(true);

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("Followers").child(firebaseUser.getUid()).setValue(true);

                    addNotification(user.getId());
                }

                else    // if following button is clicked
                {
                    FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following").child(user.getId()).removeValue();

                    FirebaseDatabase.getInstance().getReference().child("Follow").child(user.getId()).child("Followers").child(firebaseUser.getUid()).removeValue();                }
            }
        });
    }







    public void addNotification(String userId)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(userId);

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userId", firebaseUser.getUid());
        hashMap.put("text", "started following you");
        hashMap.put("postId", "");
        hashMap.put("isPost", false);


        reference.push().setValue(hashMap);
        // updates the database with values inside the hashMap
    }






    @Override
    public int getItemCount()
    {
        return mUsers.size();
    }





    public class ViewHolder extends RecyclerView.ViewHolder
    {
        public TextView username;
        public TextView fullName;
        public CircleImageView image_profile;
        public Button btn_follow;


        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            fullName = itemView.findViewById(R.id.fullName);
            image_profile = itemView.findViewById(R.id.image_profile);
            btn_follow = itemView.findViewById(R.id.btn_follow);
        }
    }





    // Displays button next to other user's username.  If following, button displays "following". Else, it displays "follow"
    private void isFollowing(final String userId, final Button button)
    {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid()).child("Following");

        reference.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.child(userId).exists())
                {
                    button.setText("Following");
                }
                else
                {
                    button.setText("Follow");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
}
