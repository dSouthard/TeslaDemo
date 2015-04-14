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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class NewsFeedFragment extends Fragment {

    private static final String TAG = "NewsFeedFragment";
    public static List<MapperPlayedGame> listOfUserOnlyGames;
    public static MapperPlayedGame mapThisGame;
    private static int NEWGAME = 11;
    private ListView wallFeed;
    private ImageView profilePicture;
    private ArrayList<String> wallList;
    private List<MapperPlayedGame> listOfGames;
    private ArrayAdapter<String> listAdapter;
    private TextView userName;
    private MapperPlayedGame gameTemp;
    private MapperUser userTemp;
    private String loadID;
//    private ExpandableListAdapter newsfeedlistAdapter;
//    private ExpandableListView expListView;
//    private List<String> listDataHeader;
//    private HashMap<String, List<String>> listDataChild;

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
        listOfGames = new ArrayList<>();
        listOfUserOnlyGames = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(getActivity(), R.layout.friend_name, wallList);
        wallFeed.setAdapter(listAdapter);

        // Set up wall feed
        updateWallFeed();   // get recent activity

        wallFeed.setClickable(true);
        wallFeed.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
//                String o = (String) wallFeed.getItemAtPosition(position);
//                Toast.makeText(getActivity(), o, Toast.LENGTH_SHORT).show();

                mapThisGame = listOfGames.get(position);
                Intent intent = new Intent(getActivity(), GameMapDisplay.class);
                ;
                startActivity(intent);
            }
        });

        View.OnClickListener newGameClickListner = new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "startNewGameBttn clicked.");
                Intent intent = new Intent(getActivity(), GameManager.class);
                startActivityForResult(intent, NEWGAME);
            }
        };

        final Button startNewGameBttn = (Button) view.findViewById(R.id.newGameButton);
        startNewGameBttn.setOnClickListener(newGameClickListner);

        ImageView startGameImage = (ImageView) view.findViewById(R.id.startNewGameImage);
        startGameImage.setClickable(true);
        startGameImage.setOnClickListener(newGameClickListner);

        Button userProfile = (Button) view.findViewById(R.id.userProfile);
        userProfile.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "userProfile clicked.");
                Intent intent = new Intent(getActivity(), UserProfileActivity.class);
                startActivity(intent);
            }
        });

        Button updateWallBtt = (Button) view.findViewById(R.id.updateWallBttn);
        updateWallBtt.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Log.i(TAG, "updateWallBtt clicked.");
                updateWallFeed();
            }

        });


        Log.d(TAG, "Return View");
        return view;
    }

    /*
    * Preparing the list data
    */
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
        if (!listOfGames.isEmpty()) listOfGames.clear();
        if (!listOfUserOnlyGames.isEmpty()) listOfUserOnlyGames.clear();

        boolean recentActivity = false; // Keep track in case there is nothing to dispay
        // Adding child data

        if (MainActivity.currentUser != null && MainActivity.currentUser.getUserName() != null) {
            Log.d(TAG, "Starting Wall Feed Update");
            Iterator<String> gameIterator;

            // Add user games
            Set<String> setTemp1 = MainActivity.currentUser.getPlayedGames();
            if (!setTemp1.isEmpty()) {
                Log.d(TAG, "Adding user game info in Wall Feed Update");
                for (gameIterator = setTemp1.iterator(); gameIterator.hasNext(); ) {
                    loadID = gameIterator.next();
                    getGame();
                    while (gameTemp == null || gameTemp.getPlayedBy() == null || !gameTemp.getgameId().equals(loadID)) {
                        // Wait for game info to load
                        // TODO: Add progress bar
                    }
                    listOfGames.add(gameTemp);  // add game to list
                    listOfUserOnlyGames.add(gameTemp);
                }
                recentActivity = true;
            } // All user's games have been added

            // Get friends' games
            Set<String> setTemp2 = MainActivity.currentUser.getFriends();
            if (!setTemp2.isEmpty()) {
                Log.d(TAG, "Adding friend game info in Wall Feed Update");
                for (gameIterator = setTemp2.iterator(); gameIterator.hasNext(); ) {
                    loadID = gameIterator.next();
                    getFriend();
                    while (userTemp.getUserName() == null || !userTemp.getUserId().equals(loadID)) {
                        // wait for user data to load
                        // TODO: Add progress bar
                    }
                    Set<String> subSet = userTemp.getPlayedGames();
                    if (!subSet.isEmpty()) {
                        for (String aSubSet : subSet) {
                            loadID = aSubSet;
                            getGame();
                            while (gameTemp.getPlayedBy() == null || !gameTemp.getgameId().equals(loadID)) {
                                // wait for game to load
                                // TODO: Add progress bar
                            }
//                            String newSummary = makeGameSummary();
//                            wallList.add(newSummary);
                            listOfGames.add(gameTemp);  // add game to list
                        }
                    }
                }
                recentActivity = true;
            }   // All user friend games have been added

            // Check if there is any activity to display
            if (!recentActivity) {  // No activity
                wallList.add("No recent activity." + System.getProperty("line.separator") + System.getProperty("line.separator"));
            } else {  // Activity to display
                listOfGames = sortByDate(listOfGames);  // Sort games by date
                for (int i = 0; i < listOfGames.size(); i++) {
                    wallList.add(makeGameSummary(listOfGames.get(i)));    // Add summary
                }

                if (!listOfUserOnlyGames.isEmpty()) {   // Separate list for just user games, used for user progile
                    listOfUserOnlyGames = sortByDate(listOfUserOnlyGames);
                }

            }
        } else {
            wallList.add("Not logged in.");   // Not logged in yet
        }

        listAdapter.notifyDataSetChanged();
        Log.d(TAG, "Finished updating list info");
//

    }


    private List<MapperPlayedGame> sortByDate(List<MapperPlayedGame> games) {
        DateFormat format = new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy", Locale.ENGLISH);
        for (int i = 0; i < games.size(); i++) {
            for (int j = 1; j < games.size() - i; j++) {
                Date oldDate, newDate;
                try {
                    newDate = format.parse(games.get(j - 1).getGameDate());
                    oldDate = format.parse(games.get(j).getGameDate());
                    if (oldDate.after(newDate)) {
                        Collections.swap(games, j, j - 1);    // Swap the order
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        return games;
    }

    private String makeGameSummary(MapperPlayedGame game) {
        StringBuilder forWall = new StringBuilder();
        // Get Game Summary
        forWall.append("Played by: ");
        if (game.getPlayedBy().equals(MainActivity.currentUser.getUserName())) {
            forWall.append("You");
        } else
            forWall.append(game.getPlayedBy());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("On ").append(game.getGameDate());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Played at: ").append(game.getGameLocation());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Total stroke count: ").append(game.getTotalStrokes());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Total course par: ").append(game.getTotalPars());
        forWall.append(System.getProperty("line.separator"));
        forWall.append("Likes: ").append(game.getLikes());
        forWall.append(System.getProperty("line.separator"));
        forWall.append(System.getProperty("line.separator"));
        return forWall.toString();
    }

//    @Override
//    public void onMapReady(GoogleMap map) {
//        map.setMyLocationEnabled(false);
//        gameMap.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
//        gameMap.getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(startMap, 17f));
//
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == NEWGAME && resultCode == Activity.RESULT_OK) {
            Log.d(TAG, "New game was saved, reload wallfeed");
            ((MainActivity) getActivity()).loadUser();   // Reload user
//            try {
//                wait(500);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
            updateWallFeed();
        }
    }//onActivityResult

    private void getGame() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_GAME);
    }

    private void getFriend() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_FRIEND);
    }

    private enum DynamoDBManagerType {
        GET_GAME, GET_FRIEND, GET_USER
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
//            if (type == DynamoDBManagerType.GET_GAME) {
//
//            }
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}