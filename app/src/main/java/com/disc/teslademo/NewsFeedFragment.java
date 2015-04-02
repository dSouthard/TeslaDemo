package com.disc.teslademo;

import android.app.AlertDialog;
 import android.content.DialogInterface;
 import android.content.Intent;
 import android.os.Bundle;
 import android.support.v4.app.Fragment;
 import android.util.Log;
 import android.view.LayoutInflater;
 import android.view.View;
 import android.view.ViewGroup;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.TextView;
 import android.widget.Toast;

public class NewsFeedFragment extends Fragment {

     private static final String TAG = "NewsFeedFragment";
     private TextView wallFeed, userName;
     private Button likePostBttn = null;

     @Override
     public View onCreateView(LayoutInflater inflater,
                              ViewGroup container, Bundle savedInstanceState) {
         super.onCreateView(inflater, container, savedInstanceState);
         View view = inflater.inflate(R.layout.fragment_news_feed,
                 container, false);

         wallFeed = (TextView) view.findViewById(R.id.wallFeed);
         userName = (TextView) view.findViewById(R.id.currentUserNameTxt);
         if (MainActivity.currentUser != null && MainActivity.currentUser.getUserName() != null) {
             userName.setText(MainActivity.currentUser.getUserName());
             updateWallFeed();
         }
 //        else {
 //            ((MainActivity)getActivity()).loadUser();
 //            userName.setText(MainActivity.currentUser.getUserName());
 //        }

         final Button startNewGameBttn = (Button) view.findViewById(R.id.newGameButton);
         startNewGameBttn.setOnClickListener(new View.OnClickListener() {

             public void onClick(View v) {
                 Log.i(TAG, "startNewGameBttn clicked.");
 //                Intent intent = new Intent(getActivity(), GameManager.class);
                 Intent intent = new Intent(getActivity(), GameManager.class);
                 startActivity(intent);
 //                ((MainActivity)getActivity()).saveUser();   // Save user once you return from new game
             }
         });

         final Button updateWallBttn = (Button) view.findViewById(R.id.updateWallBttn);
         updateWallBttn.setOnClickListener(new View.OnClickListener() {

             public void onClick(View v) {
                 Log.i(TAG, "updateWallBttn clicked.");
                 ((MainActivity) getActivity()).loadUser();
                 updateWallFeed();
             }
         });

         final Button findFriendsBttn = (Button) view.findViewById(R.id.findFriendsBttn);
         if (MainActivity.currentUser != null && MainActivity.currentUser.getUserName() == null)
             ((MainActivity) getActivity()).loadUser();
         findFriendsBttn.setOnClickListener(new View.OnClickListener() {

             public void onClick(View v) {
                 Log.i(TAG, "findFriendsBttn clicked.");
                 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                 // set title
                 alertDialogBuilder.setTitle("Add a Friend");

                 // set dialog message
                 alertDialogBuilder.setMessage("Do you want to add "
                         + " as a friend?");
                 alertDialogBuilder.setCancelable(false);
                 alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
 //                    MainActivity.currentUser.addFriend(MainActivity.user2.getUserId());
                         ((MainActivity) getActivity()).loadUser();
                         updateWallFeed();
                     }
                 });
                 alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();    // do nothing
                     }
                 });
                 // create alert dialog
                 AlertDialog alertDialog = alertDialogBuilder.create();

                 // show it
                 alertDialog.show();
             }

         });

         likePostBttn = (Button) view.findViewById(R.id.likePostBttn);
         if (MainActivity.currentUser == null || MainActivity.currentUser.getFriends() == null)
             likePostBttn.setVisibility(View.INVISIBLE);
         likePostBttn.setOnClickListener(new View.OnClickListener() {

             public void onClick(View v) {
                 Log.i(TAG, "likePostBttn clicked.");
                 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                 // set title
                 alertDialogBuilder.setTitle("Like Post");

                 // set dialog message
                 alertDialogBuilder.setMessage("Do you want to like this post?");
                 alertDialogBuilder.setCancelable(false);
                 alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
 //                        int currentLikes = MainActivity.user2.getLikes();
 //                        MainActivity.currentUser.addLikedGame();
 //                        ((MainActivity)getActivity()).updateUser2();
                         ((MainActivity) getActivity()).loadUser();
                         updateWallFeed();
                     }
                 });
                 alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         dialog.cancel();    // do nothing
                     }
                 });
                 // create alert dialog
                 AlertDialog alertDialog = alertDialogBuilder.create();

                 // show it
                 alertDialog.show();
             }

         });

         Button updateNameBttn = (Button) view.findViewById(R.id.updateNameBttn);
         updateNameBttn.setVisibility(View.VISIBLE);
         updateNameBttn.setOnClickListener(new View.OnClickListener() {

             public void onClick(View v) {
                 Log.i(TAG, "updateNameBttn clicked.");
                 LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
                 View promptView = layoutInflater.inflate(R.layout.change_name_prompt_dialog, null);
                 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                 // set title
                 alertDialogBuilder.setTitle("Update User Name");

                 // set prompts.xml to be the layout file of the alertdialog builder
                 alertDialogBuilder.setView(promptView);
                 final EditText input = (EditText) promptView.findViewById(R.id.userInput);

                 // set dialog message
                 alertDialogBuilder.setMessage("Do you want to change your name?");
                 alertDialogBuilder.setCancelable(false);
                 alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         //  get user input and set it to result
                         MainActivity.currentUser.setUserName(input.getText().toString());
                         ((MainActivity) getActivity()).saveUser();   // save to server
 //                        ((MainActivity)getActivity()).updatecurrentUser();
                         userName.setText(input.getText().toString());
                         input.setText("");  // clear input field
                         Toast.makeText(getActivity(), "Name updated!", Toast.LENGTH_LONG).show();
                         updateWallFeed();
                     }
                 });
                 alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         // if this button is clicked, just close
                         // the dialog box and do nothing
                         dialog.cancel();
                     }
                 });
                 // create alert dialog
                 AlertDialog alertDialog = alertDialogBuilder.create();

                 // show it
                 alertDialog.show();
             }

         });


         Button userProfile = (Button) view.findViewById(R.id.userProfile);
         userProfile.setOnClickListener(new View.OnClickListener() {

             public void onClick(View v) {
                 Log.i(TAG, "userProfile clicked.");
                 AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                 // set title
                 alertDialogBuilder.setTitle("User Profile");

                 // set dialog message
                 if (MainActivity.currentUser.getPlayedGames() != null) ;
 //                    alertDialogBuilder.setMessage(MainActivity.currentUser.getPlayedGames());
                 else
                     alertDialogBuilder.setMessage("No saved games.");
                 alertDialogBuilder.setCancelable(false);
                 alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                         //  get user input and set it to result
                         dialog.dismiss();
                     }
                 });

                 // create alert dialog
                 AlertDialog alertDialog = alertDialogBuilder.create();

                 // show it
                 alertDialog.show();
             }

         });


         return view;
     }

     public void updateWallFeed() {
         String wallFeedString = "USER ACTIVITY:";
 //        String wallFeedString = "";
         StringBuilder sb = new StringBuilder(wallFeedString);
         if (MainActivity.currentUser.getPlayedGames() == null) {
             // No friends
             sb.append("No user activity to display");
         } else {
             sb.append(System.getProperty("line.separator"));
             sb.append(System.getProperty("line.separator"));
             sb.append("You: ");
             sb.append(System.getProperty("line.separator"));
             for (int i = 0; i < MainActivity.currentUser.getPlayedGames().size(); i++) {
                 sb.append(MainActivity.currentUser.getPlayedGames());
             }
         }

         if (MainActivity.currentUser.getFriends().isEmpty()) {
             // No friends
             sb.append("No friend activity to display");
         } else {
             sb.append("FRIEND ACTIVITY:");
 //            sb.append(System.getProperty("line.separator"));
 //            sb.append(System.getProperty("line.separator"));
 //            sb.append(MainActivity.user2.getUserName());
 //            sb.append(System.getProperty("line.separator"));
 //            sb.append(MainActivity.user2.getPlayedGame());
 //            likePostBttn.setText("Likes:" + MainActivity.user2.getLikes());
             likePostBttn.setVisibility(View.VISIBLE);
         }
         wallFeed.setText(sb.toString());
     }
 }