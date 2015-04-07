package com.disc.teslademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.Iterator;
import java.util.Set;


public class UserProfileActivity extends Activity {

    // Used for taking picture intents when changing user profile image
    private static final int IMAGE_PICK = 1;
    private static final int IMAGE_CAPTURE = 2;
    public static MapperUser currentUserCopy;
    private static String TAG = "UserProfileFragment";
    Context context;
    private ImageView userProfilePic;
    private ImageButton friendListButton;
    private Button updateNameButton;
    private TextView userName, lastGamePlayed, recentActivity;
    private Bitmap profileImage;
    private boolean needToSave = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_profile);
        context = this;

        userProfilePic = (ImageView) findViewById(R.id.userProfileImageView);
        if (MainActivity.currentUser != null && MainActivity.currentUser.getUserName() != null) {
            currentUserCopy = MainActivity.currentUser;
            if (MainActivity.currentUser.getUserName().equals("Diana Southard"))
                userProfilePic.setImageResource(R.drawable.max);
            else userProfilePic.setImageResource(R.drawable.squirrel);
        } else {
            userProfilePic.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }

        // Set up buttons
        friendListButton = (ImageButton) findViewById(R.id.friendsListButton);
        updateNameButton = (Button) findViewById(R.id.updateNameButton);

        // Set up textViews
        userName = (TextView) findViewById(R.id.userProfileName);
        userName.setText(MainActivity.currentUser.getUserName());
        lastGamePlayed = (TextView) findViewById(R.id.lastGamePlayed);
        recentActivity = (TextView) findViewById(R.id.recentActivity);

        Set<String> playedGames = null;
        if (currentUserCopy != null)
            playedGames = currentUserCopy.getPlayedGames();
        if (playedGames == null || playedGames.isEmpty()) {
            recentActivity.setText("No saved games for this profile");
            lastGamePlayed.setText("No recent games");
        } else {

            for (Iterator<String> it = playedGames.iterator(); it.hasNext(); ) {

            }
        }

        // Set up button listeners
        // friendsListButton


        // Update Name Button
        updateNameButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "updateNameBtton clicked.");
                if (currentUserCopy == null) currentUserCopy = MainActivity.currentUser;
                LayoutInflater layoutInflater = LayoutInflater.from(context);
                View promptView = layoutInflater.inflate(R.layout.change_name_prompt_dialog, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                // set title
                alertDialogBuilder.setTitle("Update User Name");

                // set prompts.xml to be the layout file of the alertdialog builder
                alertDialogBuilder.setView(promptView);
                final EditText input = (EditText) promptView.findViewById(R.id.userInput);
                input.requestFocus();
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
                inputMethodManager.restartInput(input);

                // set dialog message
                alertDialogBuilder.setMessage("Do you want to change your name?");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //  get user input and set it to result
                        currentUserCopy.setUserName(input.getText().toString());
                        userName.setText(input.getText().toString());
                        input.setText("");  // clear input field
//                        needToSave = true;
                        saveUser();
                        Toast.makeText(context, "Name updated!", Toast.LENGTH_LONG).show();
                    }
                });
                alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close the dialog box and do nothing
                        dialog.cancel();
                    }
                });
                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }

        });
    }

    public void saveUser() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.SAVE_USER);
    }

    private enum DynamoDBManagerType {
        SAVE_USER
    }

    private class DynamoDBManagerTask extends AsyncTask<DynamoDBManagerType, Void, String> {
        @Override
        protected String doInBackground(DynamoDBManagerType... types) {
            Log.d("DoINBackGround", "On doInBackground...");

            AmazonDynamoDBClient clientManager = new AmazonDynamoDBClient(MainActivity.credentials);
            DynamoDBMapper mapper = new DynamoDBMapper(clientManager);

            switch (types[0]) {
                case SAVE_USER:
                    try {
                        Log.d(TAG, "Saving current user");
                        boolean playGames = false, friends = false, likedGames = false, pendingFriend = false;
                        if (currentUserCopy.getPlayedGames().isEmpty()) {
                            playGames = true;
                            currentUserCopy.addPlayedGame("Empty");
                        }
                        if (currentUserCopy.getFriends().isEmpty()) {
                            friends = true;
                            currentUserCopy.addFriend("Empty");
                        }
                        if (currentUserCopy.getLikedGames().isEmpty()) {
                            currentUserCopy.addLikedGame("Empty");
                            likedGames = true;
                        }
                        if (currentUserCopy.getPendingFriends().isEmpty()) {
                            currentUserCopy.addPendingFriend("Empty");
                            pendingFriend = true;
                        }

                        mapper.save(currentUserCopy);

                        if (playGames) currentUserCopy.removePlayedGame("Empty");
                        if (friends) currentUserCopy.removeFriend("Empty");
                        if (likedGames) currentUserCopy.removeLikedGame("Empty");
                        if (pendingFriend) currentUserCopy.removePendingFriend("Empty");

                        Log.d(TAG, "User saved.");

                    } catch (AmazonServiceException ex) {
                        Log.e(TAG, "Error saving current user");
                        Log.d(TAG, "Service exception: " + ex);
                    }
                    break;

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}
