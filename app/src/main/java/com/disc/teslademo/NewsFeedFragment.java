package com.disc.teslademo;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

public class NewsFeedFragment extends Fragment {

    private static final String TAG = "NewsFeedFragment";
    private static int NEW_GAME = 11, VIEW_DETAILS = 12;
    private ListView wallFeed;
    private ImageView profilePicture;
    private ArrayList<String> wallList;
    private ArrayAdapter<String> listAdapter;
    private TextView userName;

    private MapperPlayedGame gameTemp;
    private MapperUser userTemp;
    private String loadID;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_news_feed,
                container, false);

        gameTemp = new MapperPlayedGame();
        userTemp = new MapperUser();

        userName = (TextView) view.findViewById(R.id.userNameField);
        wallFeed = (ListView) view.findViewById(R.id.wallFeedListView);
        profilePicture = (ImageView) view.findViewById(R.id.newsFeedProfilePic);
        if (MainActivity.currentUser != null) {
            if (MainActivity.currentUser.getUserName().equals("Diana Southard"))
                profilePicture.setImageResource(R.drawable.max);
            else profilePicture.setImageResource(R.drawable.squirrel);
        } else {
            profilePicture.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }


        wallList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(getActivity(), R.layout.friend_name, wallList);
        wallFeed.setAdapter(listAdapter);

        // Set up wall feed
        updateWallFeed();   // get recent activity

        final Button startNewGameBttn = (Button) view.findViewById(R.id.newGameButton);
        startNewGameBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "startNewGameBttn clicked.");
                Intent intent = new Intent(getActivity(), GameManager.class);
                startActivityForResult(intent, NEW_GAME);
            }
        });

        final Button updateWallBttn = (Button) view.findViewById(R.id.updateWallBttn);
        updateWallBttn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "updateWallBttn clicked.");
                if (MainActivity.currentUser == null)
                    ((MainActivity) getActivity()).loadUser();
                updateWallFeed();
            }
        });

        Button userProfile = (Button) view.findViewById(R.id.userProfile);
        userProfile.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "userProfile clicked.");
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            }

        });


        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NEW_GAME) {
            if (resultCode == Activity.RESULT_OK) {
                MainActivity.currentUser.addPlayedGame(data.getStringExtra("GameID"));
                updateWallFeed();
            }
        }
    }

    public void updateWallFeed() {

        if (MainActivity.currentUser != null && MainActivity.currentUser.getUserName() != null) {
            userName.setText(MainActivity.currentUser.getUserName());
            if (MainActivity.currentUser.getUserName().equals("Diana Southard"))
                profilePicture.setImageResource(R.drawable.max);
            else profilePicture.setImageResource(R.drawable.squirrel);
        } else {
            profilePicture.setImageResource(R.drawable.com_facebook_profile_picture_blank_portrait);
        }


        if (!wallList.isEmpty()) wallList.clear();
        if (!listAdapter.isEmpty()) listAdapter.clear();

        boolean recentActivity = false;
        if (MainActivity.currentUser != null && MainActivity.currentUser.getUserName() != null) {
            Iterator<String> gameIterator;

            StringBuilder forWall = new StringBuilder();

            // Add user games
            Set<String> setTemp1 = MainActivity.currentUser.getPlayedGames();
            if (!setTemp1.isEmpty()) {
                for (gameIterator = setTemp1.iterator(); gameIterator.hasNext(); ) {
                    loadID = gameIterator.toString();
                    getGame();
                    for (int i = 0; i < 500; i++) ;
                    // Get Game Summary
                    String newSummary = makeGameSummary();
                    wallList.add(newSummary);
                }
                recentActivity = true;
            }

            // Get friends' games
            Set<String> setTemp2 = MainActivity.currentUser.getFriends();
            if (!setTemp2.isEmpty()) {
                for (gameIterator = setTemp2.iterator(); gameIterator.hasNext(); ) {
                    loadID = gameIterator.toString();
                    getFriend();
                    for (int i = 0; i < 500; i++) ;
                    Set<String> subSet = userTemp.getPlayedGames();
                    if (!subSet.isEmpty()) {
                        for (String aSubSet : subSet) {
                            loadID = aSubSet.toString();
                            getGame();
                            for (int i = 0; i < 500; i++) ;
                            String newSummary = makeGameSummary();
                            wallList.add(newSummary);
                        }
                    }
                }
                recentActivity = true;
            }

            if (!recentActivity) {
                forWall.append("No recent activity.");
                forWall.append(System.getProperty("line.separator"));
                forWall.append(System.getProperty("line.separator"));
            }
            wallList.add(forWall.toString());
        } else {
            wallList.add("Not logged in.");
        }

        listAdapter.notifyDataSetChanged();
    }

    private String makeGameSummary() {
        StringBuilder forWall = new StringBuilder();
        // Get Game Summary
        forWall.append("Played by: " + gameTemp.getPlayedBy() + " on " + gameTemp.getGameDate());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Played at: " + gameTemp.getGameLocation());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Total stroke count: " + gameTemp.getTotalStrokes());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Likes: " + gameTemp.getLikes());
        forWall.append(System.getProperty("line.separator"));
        forWall.append(System.getProperty("line.separator"));
        return forWall.toString();
    }


    private void getGame() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_GAME);
    }

    private void getFriend() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_FRIEND);
    }

    private enum DynamoDBManagerType {
        GET_GAME, GET_FRIEND
    }

    private class DynamoDBManagerTask extends AsyncTask<DynamoDBManagerType, Void, String> {
        @Override
        protected String doInBackground(DynamoDBManagerType... types) {
            Log.d("DoINBackGround", "On doInBackground...");

            AmazonDynamoDBClient clientManager = new AmazonDynamoDBClient(MainActivity.credentials);
            DynamoDBMapper mapper = new DynamoDBMapper(clientManager);

            switch (types[0]) {
                case GET_GAME:
                    try {
                        Log.d(TAG, "Loading Game " + loadID);
                        gameTemp = mapper.load(MapperPlayedGame.class, loadID);
                    } catch (AmazonServiceException ex) {
                        Log.e(TAG, "Error loading game " + loadID);
                        Log.e(TAG, "Error: " + ex);
                    }
                    break;
                case GET_FRIEND:
                    try {
                        Log.d(TAG, "Loading Friend " + loadID);
                        userTemp = mapper.load(MapperUser.class, loadID);
                        removeEmpty(DynamoDBManagerType.GET_FRIEND);
                    } catch (AmazonServiceException ex) {
                        Log.e(TAG, "Error loading game " + loadID);
                        Log.e(TAG, "Error: " + ex);
                    }
                    break;
            }
            return null;
        }

        private void removeEmpty(DynamoDBManagerType type) {
            if (type == DynamoDBManagerType.GET_FRIEND) {
                userTemp.removePendingFriend("Empty");
                userTemp.removeLikedGame("Empty");
                userTemp.removePlayedGame("Empty");
                userTemp.removeFriend("Empty");
            }
            if (type == DynamoDBManagerType.GET_GAME) {

            }
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}