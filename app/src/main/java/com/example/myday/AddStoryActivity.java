package com.example.myday;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.theartofdev.edmodo.cropper.CropImage;

import java.util.HashMap;

public class AddStoryActivity extends AppCompatActivity
{
    private Uri mImageUri;
    String myUrl = "";
    private StorageTask storageTask;
    StorageReference storageReference;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_story);



        storageReference = FirebaseStorage.getInstance().getReference("Story");

        CropImage.activity()
                .setAspectRatio(9, 16)
                .start(AddStoryActivity.this);
    }





    private String getFileExtension(Uri uri)
    {
        ContentResolver contentResolver = getContentResolver();

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();

        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));
    }






    private void publishStory()
    {
        final ProgressDialog progressDialog = new ProgressDialog(this);

        progressDialog.setMessage("Posting");
        progressDialog.show();



        if (mImageUri != null)
        {
            final StorageReference imageReference = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(mImageUri));

            storageTask = imageReference.putFile(mImageUri);


            storageTask.continueWithTask(new Continuation()
            {
                @Override
                public Task<Uri> then(@NonNull Task task) throws Exception
                {
                   if (!task.isSuccessful())
                   {
                       throw task.getException();
                   }

                   return imageReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if (task.isSuccessful())
                    {
                        Uri downloadUri = task.getResult();

                        myUrl = downloadUri.toString();

                        String myId = FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Story").child(myId);

                        String storyId = reference.push().getKey();


                        long timeEnd = System.currentTimeMillis() + 86400000;       // 86400000 1 day in milliseconds


                        HashMap<String, Object> hashMap = new HashMap<>();

                        hashMap.put("imageURL", myUrl);
                        hashMap.put("timeStart", ServerValue.TIMESTAMP);
                        hashMap.put("timeEnd", timeEnd);
                        hashMap.put("storyId", storyId);
                        hashMap.put("userId", myId);


                        reference.child(storyId).setValue(hashMap);

                        progressDialog.dismiss();


                        finish();
                    }
                    else
                    {
                        Toast.makeText(AddStoryActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }

                }
            });
        }
        else
        {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
        }
    }







    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);



        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            mImageUri = result.getUri();

            publishStory();
        }
        else
        {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();

            startActivity(new Intent(AddStoryActivity.this, MainActivity.class));

            finish();
        }
    }
}
