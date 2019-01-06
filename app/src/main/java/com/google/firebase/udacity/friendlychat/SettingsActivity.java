package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private static final int PICKER_REQUEST_CODE = 431;
    private TextView nameTextView, statusTextView;
    private CircleImageView circleImageView;
    private Button imageButton, statusButton;

    private FirebaseUser mCurrentUser;
    private DatabaseReference mDatabase;
    private StorageReference mStorage;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameTextView = (TextView) findViewById(R.id.name_tv);
        statusTextView = (TextView) findViewById(R.id.status_tv);
        circleImageView = (CircleImageView)findViewById(R.id.settings_image);
        imageButton = (Button) findViewById(R.id.change_image_btn);
        statusButton = (Button) findViewById(R.id.change_status_btn);

        mStorage = FirebaseStorage.getInstance().getReference().child("profile-images");


        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUID = mCurrentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);


        //retrieve data
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();


                nameTextView.setText(name);
                statusTextView.setText(status);
                Glide.with(circleImageView.getContext())
                        .load(image)
                        .into(circleImageView);
            }

            //for error
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        statusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String status = statusTextView.getText().toString();
                Intent intent = new Intent(SettingsActivity.this, StatusActivity.class);
                intent.putExtra("status-value", status);
                startActivity(intent);
            }
        });


        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "complete my action using"), PICKER_REQUEST_CODE);

               /* CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);*/
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICKER_REQUEST_CODE && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {


                Uri resultUri = result.getUri();

                String userUID = mCurrentUser.getUid();
                StorageReference filePath = mStorage.child(userUID + ".jpg");

                filePath.putFile(resultUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot task) {


                        task.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                mDatabase.child("image").setValue(uri.toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(SettingsActivity.this, "Saved!!", Toast.LENGTH_LONG).show();
                                        } else
                                            Toast.makeText(SettingsActivity.this, "Error!!", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        });


                    }

                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
