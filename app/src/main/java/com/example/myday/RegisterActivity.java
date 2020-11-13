package com.example.myday;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity
{
    EditText userName;
    EditText fullName;
    EditText email;
    EditText password;
    Button register;
    TextView textLogin;

    FirebaseAuth auth;
    DatabaseReference reference;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        // connect variables with UI nodes
        userName = findViewById(R.id.username);
        fullName = findViewById(R.id.fullName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        register = findViewById(R.id.register);
        textLogin = findViewById(R.id.textLogin);


        auth = FirebaseAuth.getInstance();


        textLogin.setOnClickListener(new View.OnClickListener()     // When user clicks on the "Already have an Account? login" TextView
        {
            @Override
            public void onClick(View view)
            {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });



        register.setOnClickListener(new View.OnClickListener()      // When user clicks on the "register" button after filling out the info.
        {
            @Override
            public void onClick(View v)
            {
                progressDialog = new ProgressDialog(RegisterActivity.this);
                progressDialog.setMessage("Please Wait...");
                progressDialog.show();

                String usernameAsString = userName.getText().toString();
                String fullNameAsString = fullName.getText().toString();
                String emailAsString = email.getText().toString();
                String passwordAsString = password.getText().toString();


                if (TextUtils.isEmpty(usernameAsString) || TextUtils.isEmpty(fullNameAsString) || TextUtils.isEmpty(emailAsString) || TextUtils.isEmpty(passwordAsString))
                {
                    Toast.makeText(RegisterActivity.this, "All fields are required!", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if (passwordAsString.length() < 6)
                {
                    Toast.makeText(RegisterActivity.this, "Password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    register(usernameAsString, fullNameAsString, emailAsString, passwordAsString);
                }
            }
        });
    }





    private void register(final String username, final String fullName, String email, String password)
    {

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            // registration of user is successful
                            FirebaseUser firebaseUser = auth.getCurrentUser();
                            String userId = firebaseUser.getUid();
                            reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);

                            HashMap<String, Object> hashMap = new HashMap<>();
                            hashMap.put("id", userId);
                            hashMap.put("username", username.toLowerCase());
                            hashMap.put("fullName", fullName);
                            hashMap.put("bio", "");
                            hashMap.put("imageURL", "https://firebasestorage.googleapis.com/v0/b/makeotrip.appspot.com/o/default-profile-picture1.jpg?alt=media&token=93f96da6-fbd2-4752-affd-331091560e34");


                            // add the newly created user account to Firebase database
                            reference.setValue(hashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<Void> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        progressDialog.dismiss();

                                        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);

                                    }
                                }
                            });
                        }
                        else
                        {
                            progressDialog.dismiss();

                            Toast.makeText(RegisterActivity.this, "You can't register with this email or password", Toast.LENGTH_SHORT).show();

                        }
                    }
                });
    }
}
