package com.google.firebase.udacity.friendlychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity {

    private static final int PICKER_REQUEST_CODE = 431;
    private Toolbar mToolbar;


    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private ListView mMessageListView;
    private MessageAdapter mMessageAdapter;
    private ImageButton mPhotoPickerButton;
    private EditText mMessageEditText;
    private ImageView mSendButton;

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mMessagesReference;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth mAuth;
    private StorageReference mStorage;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        mToolbar = (Toolbar)findViewById(R.id.main_app_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Chat Room");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mMessagesReference = mFirebaseDatabase.getReference().child("chat_room");
        mStorage = FirebaseStorage.getInstance().getReference().child("chat-room-files");


        mMessageListView = (ListView) findViewById(R.id.messageListView);
        mPhotoPickerButton = (ImageButton) findViewById(R.id.photoPickerButton);
        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mSendButton = (ImageView) findViewById(R.id.sendButton);
        mSendButton.setEnabled(false);
        // Initialize message ListView and its adapter
        List<FriendlyMessage> friendlyMessages = new ArrayList<>();
        mMessageAdapter = new MessageAdapter(this, R.layout.item_message, friendlyMessages);
        mMessageListView.setAdapter(mMessageAdapter);


        mPhotoPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent,"complete my action using"), PICKER_REQUEST_CODE);
            }
        });

        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });



        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT)});

        // Send button sends a message and clears the EditText
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                if(mMessageEditText.getText().toString().length()!=0) {
                    mSendButton.setPressed(true);
                    FriendlyMessage friendlyMessage = new FriendlyMessage(mMessageEditText.getText().toString(), user.getDisplayName(), null);

                    mMessagesReference.push().setValue(friendlyMessage);

                    // Clear input box
                    mMessageEditText.setText("");
                }
                else
                    mSendButton.setPressed(false);
            }
        });

        attachDatabaseReadListener();

    }


    private void attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = new ChildEventListener() {
                @Override
                public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    FriendlyMessage friendlyMessage = dataSnapshot.getValue(FriendlyMessage.class);
                    mMessageAdapter.add(friendlyMessage);
                }

                @Override
                public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                }

                @Override
                public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            };

            mMessagesReference.addChildEventListener(mChildEventListener);
        }
    }

    private void detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesReference.removeEventListener(mChildEventListener);
            mChildEventListener = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        detachDatabaseReadListener();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode==PICKER_REQUEST_CODE && resultCode==RESULT_OK)
        {
            Uri selectedImageUri = data.getData();
        mStorage.child(selectedImageUri.getLastPathSegment()).putFile(selectedImageUri).addOnSuccessListener(this, new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                  @Override
                  public void onSuccess(Uri uri) {

                      FriendlyMessage friendlyMessage = new FriendlyMessage(null,mAuth.getCurrentUser().getDisplayName(),uri.toString());
                      mMessagesReference.push().setValue(friendlyMessage);
                  }
              });


            }
        });
        }
    }
}
