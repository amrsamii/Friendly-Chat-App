package com.google.firebase.udacity.friendlychat;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class StatusActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextInputLayout mStatus;
    private Button saveButton;

    //Firebase
    private DatabaseReference  mDatabase;
    private FirebaseUser mUser;


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        //Firebase
        mUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUID = mUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);

        mToolbar = (Toolbar)findViewById(R.id.status_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mStatus = (TextInputLayout)findViewById(R.id.status_editText);
        saveButton = (Button)findViewById(R.id.save_changes_btn);

        String status = getIntent().getStringExtra("status-value");

        mStatus.getEditText().setText(status);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status = mStatus.getEditText().getText().toString();

                mDatabase.child("status").setValue(status).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(StatusActivity.this,"Status Updated Successfully!",Toast.LENGTH_SHORT).show();
                        }
                        else
                            Toast.makeText(StatusActivity.this,"Error in Updating Status!",Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
