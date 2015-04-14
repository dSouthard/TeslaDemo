package com.disc.teslademo;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;


public class UserProfileActivity extends Activity {

    // Used for taking picture intents when changing user profile image
    public static MapperUser currentUserCopy;
    private static String TAG = "UserProfileFragment";
    Context context;
    private ImageView userProfilePic;
    private ImageButton friendListButton;
    private Button updateNameButton;
    private TextView userName, lastGamePlayed;
    private ListView profileWallFeed;
    private ArrayAdapter<String> listAdapter;
    private ArrayList<String> wallList;

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

        // Set up activity feed
        wallList = new ArrayList<>();
        for (int i = 0; i < NewsFeedFragment.listOfUserOnlyGames.size(); i++) {
            wallList.add(makeGameSummary(NewsFeedFragment.listOfUserOnlyGames.get(i)));
        }
        profileWallFeed = (ListView) findViewById(R.id.profileWallFeed);
        listAdapter = new ArrayAdapter<>(this, R.layout.friend_name, wallList);
        profileWallFeed.setAdapter(listAdapter);
        listAdapter.notifyDataSetChanged();

        profileWallFeed.setClickable(true);
        profileWallFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                String o = (String) profileWallFeed.getItemAtPosition(position);
//                Toast.makeText(getParent(), o, Toast.LENGTH_SHORT).show();

                NewsFeedFragment.mapThisGame = NewsFeedFragment.listOfUserOnlyGames.get(position);
                Intent intent = new Intent(getBaseContext(), GameMapDisplay.class);

                startActivity(intent);
            }
        });

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

        // Update Name Button
        Button returnToNewsfeedBttn = (Button) findViewById(R.id.profileReturnBttn);
        returnToNewsfeedBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "returnToNewsfeedBttn clicked.");
                finish();
            }

        });
    }

    private String makeGameSummary(MapperPlayedGame game) {
        StringBuilder forWall = new StringBuilder();
        // Get Game Summary
        forWall.append("Game Played On " + game.getGameDate());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("At: " + game.getGameLocation());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Total stroke count: " + game.getTotalStrokes());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Likes: " + game.getLikes());
        forWall.append(System.getProperty("line.separator"));
        forWall.append(System.getProperty("line.separator"));
        return forWall.toString();
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
