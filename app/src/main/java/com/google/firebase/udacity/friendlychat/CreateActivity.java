package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.regex.Pattern;

public class CreateActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    TextInputLayout userEditText , emailEditText , passEditText;
    Button createButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;


    private DatabaseReference mDatabase;

    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^" +
                    "(?=.*[0-9])" +         //at least 1 digit
                    //"(?=.*[a-z])" +         //at least 1 lower case letter
                    //"(?=.*[A-Z])" +         //at least 1 upper case letter
                    "(?=.*[a-zA-Z])" +      //any letter
                    //  "(?=.*[@#$%^&+=])" +    //at least 1 special character
                    "(?=\\S+$)" +           //no white spaces
                    ".{6,}" +               //at least 6 characters  // after comma is upper limit e.g 20 char
                    "$");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create);
        mToolbar = (Toolbar)findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Create Account");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userEditText = (TextInputLayout) findViewById(R.id.user_edit_text);
        emailEditText =(TextInputLayout) findViewById(R.id.email_edit_text);
        passEditText = (TextInputLayout) findViewById(R.id.pass_edit_text);
        createButton = (Button)findViewById(R.id.create_button);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);



        progressBar.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createAccount();
            }
        });


    }




    private void createAccount()
    {
      final String userName = userEditText.getEditText().getText().toString();
      String password = passEditText.getEditText().getText().toString();
      String email = emailEditText.getEditText().getText().toString();

        if(!validateEmail(email)|!validatePassword(password)| !validateUserName(userName))
            return;

      progressBar.setVisibility(View.VISIBLE);
      mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(CreateActivity.this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {

              if(task.isSuccessful())
              {
                  FirebaseUser user = mAuth.getCurrentUser();

                  String user_uid = user.getUid();
                  mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(user_uid);

                  UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                          .setDisplayName(userName)
                          .build();



                  user.updateProfile(profileChangeRequest);
                  HashMap<String,String> userMap = new HashMap<>();
                  userMap.put("name",userName);
                  userMap.put("status","Hi there, i'm using Friendly Chat App.");
                  userMap.put("image","default");
                  userMap.put("thumb_image","default");

                  mDatabase.setValue(userMap);






                  Intent intent = new Intent(CreateActivity.this,MainActivity.class);
                  intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                  startActivity(intent);
                  finish();
              }
              else
              {
                  Toast.makeText(CreateActivity.this,"Enter valid data",Toast.LENGTH_SHORT).show();
                  progressBar.setVisibility(View.INVISIBLE);
              }
          }
      });



    }
    private boolean validateEmail(String email)
    {
        if(TextUtils.isEmpty(email))
        {
            emailEditText.setError("Field can't be empty");
            return false;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEditText.setError("Please enter a valid email address");
            return false;
        }
        else
        {
            emailEditText.setError(null);
            return true;
        }
    }

    private boolean validatePassword(String password)
    {
        if(TextUtils.isEmpty(password))
        {
            passEditText.setError("Field can't be empty");
            return false;
        }
        else if(!PASSWORD_PATTERN.matcher(password).matches())
        {
            passEditText.setError("Password must be at least 6 charachters and contians one digit");
            return false;
        }
        else
        {
            passEditText.setError(null);
            return true;
        }
    }

    private boolean validateUserName(String userName)
    {
        if(TextUtils.isEmpty(userName))
        {
            userEditText.setError("Field can't be empty");
            return false;
        }
        else if (userName.length()>15)
        {
            userEditText.setError("Username too long");
            return false;
        }
        else
        {
            userEditText.setError(null);
            return true;
        }

    }


}
