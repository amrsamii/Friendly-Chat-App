package com.google.firebase.udacity.friendlychat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    EditText emailEditText;
    Button resetButton;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mToolbar = (Toolbar)findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Reset Password");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailEditText = (EditText)findViewById(R.id.email_edit_text);
        resetButton = (Button)findViewById(R.id.reset_button);
        progressBar = (ProgressBar)findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        progressBar.setVisibility(View.INVISIBLE);

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailEditText.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    Toast.makeText(ResetPasswordActivity.this, "Enter your email!!!", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                mAuth.sendPasswordResetEmail(email).addOnCompleteListener(ResetPasswordActivity.this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if(task.isSuccessful())
                            Toast.makeText(ResetPasswordActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                        else
                            Toast.makeText(ResetPasswordActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();

                        progressBar.setVisibility(View.GONE);
                    }
                });


            }
        });
    }
}
