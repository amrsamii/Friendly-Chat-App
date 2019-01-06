package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    TextInputLayout passEditText;
    TextInputLayout emailEditText;
    Button loginButton;
    FirebaseAuth mAuth;
    ProgressBar progressBar;
    TextView forgetTextView;

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
        setContentView(R.layout.activity_sign);
        mToolbar = (Toolbar)findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        passEditText = (TextInputLayout) findViewById(R.id.pass_edit_text);
        emailEditText = (TextInputLayout) findViewById(R.id.email_edit_text);
        loginButton = (Button)findViewById(R.id.login_button);
        forgetTextView = (TextView)findViewById(R.id.forget_textview);

        progressBar = (ProgressBar)findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);
        mAuth = FirebaseAuth.getInstance();

        progressBar.getProgressDrawable().setColorFilter(
                Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);


        forgetTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this,ResetPasswordActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            login();
            }
        });
    }

  /*  @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user!=null) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }
    }*/

    private void login()
    {
        String email = emailEditText.getEditText().getText().toString();
        String pass = passEditText.getEditText().getText().toString();

        // the reason we type one | not || because if one of the calls is true , compiler will not look at the other calls because if one of them is true so the whole condition is true
        // but we want to call the 3 methods so we put only one |
        if(!validateEmail(email)|!validatePassword(pass))
            return;



        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {

                    progressBar.setVisibility(View.INVISIBLE);
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    // we put this line because even this activity is finished , the start activity stills in the stack so if we press back button , we can go back to it
                    // so we create here new task , and clear all previous tasks
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }

                else
                    {
                    Toast.makeText(LoginActivity.this, "Wrong Email or Password", Toast.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        finish();
    }
}
