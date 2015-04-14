package com.disc.teslademo;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.facebook.AppEventsLogger;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends FragmentActivity
        implements LoginFragment.OnFragmentInteractionListener {

    public static final int LOGIN = 0, NEWSFEED = 1, SETTINGS = 2;
    /* TODO:
        - create option to like friends' posts
        - create option to leave message on friends' posts???????
        - create user profile fragment with:
            - link to list of current friends
                - opens up new list of friends
                - shows list of other users not designated as friends, w/ option to add
                - indicates which friends are pending friend acceptance

    */
    private static final String TAG = "Tesla Demo Main Activity";
    private static final int FRAGMENT_COUNT = SETTINGS + 1;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    public static MapperUser currentUser;
    public static CognitoCachingCredentialsProvider credentials;
    // ArrayLists for server pulls
    public static ArrayList<MapperUser> userSearchResults;
    public static ArrayList<MapperPlayedGame> playedGamesSearchResults;
    public static Bitmap bitmap;
    public static URL image_path;
    public static Bitmap myBitmap;
    public static DialogFragment resultDialog;
    static String currentUserName;
    private static NewsFeedFragment newsFeedFragment;
    // ArrayAdapters to keep track of sorted games
    private ArrayAdapter<String> userGames;
    private ArrayAdapter<String> friendGames;
    private ArrayAdapter<String> otherUserGames;
    private MenuItem settings;  // use this to trigger the UserSettingsFragment display
    // User Profile Variables
    private String currentUserId;
    private boolean isResumed = false, loggedIn = false;
    private UiLifecycleHelper uiHelper;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        credentials = new CognitoCachingCredentialsProvider(
                this,
                Constants.AWS_ACCOUNT_ID,
                Constants.IDENTITY_POOL_ID,
                Constants.UNAUTH_ROLE_ARN,
                null,
                Regions.US_EAST_1);

        setContentView(R.layout.main);

        // Initialize array adapters for game sorting
        userGames = new ArrayAdapter<>(this, R.layout.friend_name);
        friendGames = new ArrayAdapter<>(this, R.layout.friend_name);
        otherUserGames = new ArrayAdapter<>(this, R.layout.friend_name);

        bitmap = null;
        currentUser = new MapperUser();
        loadUser();
//        getGames();
//        sortGames();

        // Set up fragments
        FragmentManager fm = getSupportFragmentManager();
        LoginFragment loginFragment = (LoginFragment) fm.findFragmentById(R.id.loginFragment);
        newsFeedFragment = (NewsFeedFragment) fm.findFragmentById(R.id.newsfeedFragment);

        newsFeedFragment.updateWallFeed();

        fragments[LOGIN] = loginFragment;
        fragments[NEWSFEED] = newsFeedFragment;
        fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : fragments) {   // Hide all fragments initially
            transaction.hide(fragment);
        }
        transaction.commit();

    }

    public void loadUser() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_USER);
    }

    public void getGames() {
        new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_GAMES);
    }

    @Override
    public void onResume() {

        Session session = Session.getActiveSession();
        if (session != null &&
                (session.isOpened() || session.isClosed())) {
            onSessionStateChange(session, session.getState());
        }
        uiHelper.onResume();

        isResumed = true;

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
        isResumed = false;

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    // handle the case where fragments are newly instantiated and the authenticated
    // versus nonauthenticated UI needs to be properly set
    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();

        if (session != null && session.isOpened()) {
            // if the session is already open, try to show the NEWSFEED fragment
            showFragment(NEWSFEED, false);
        } else {
            // otherwise present the splash screen and ask the user to login, unless the user explicitly skipped.
            showFragment(LOGIN, false);
        }
    }

//    private void sortGames() {
//
//        // Clear out previous text
//        userGames.clear();
//        friendGames.clear();
//
//        // Iterate through masterList and sort all users into correct arraylist
////        if (playedGamesSearchResults != null) {
////            for (MapperPlayedGame temp : playedGamesSearchResults) {
////                // Check if game was played by current user
////                // Current game was played by the user
////                if (currentUser.getPlayedGames().contains(temp.getgameId()))
////                    userGames.add(temp.getGameDate() + '\n'
////                            + temp.getGameLocation() + '\n'
////                            + temp.getTotalStrokes() + '\n');
////
////                // Check if game was played by friend
////                Set friendsTemp = currentUser.getFriends();
////                for (Iterator<MapperUser> friendTempIt = (Iterator<MapperUser>)friendsTemp.iterator(); friendTempIt.hasNext(); ) {
////                    if (friendTempIt.next().getPlayedGames().contains(temp.getgameId()))
////                        friendGames.add(friendTempIt.next().getUserName() + " Played a game at: "
////                                + temp.getGameLocation() + " on "
////                                + temp.getGameDate() + '\n'
////                                + " Total Strokes: " + temp.getTotalStrokes() + '\n');
////                }
////            }
////        }
//    }

    public void showSettingsFragment() {
        showFragment(SETTINGS, true);
    }

    private void getBitmapFromURL(URL url) {
        if (url != null) {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                myBitmap = BitmapFactory.decodeStream(input);
            } catch (IOException e) {
                // Log exception
            }
        }
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed Called");
        if (fragments[SETTINGS].isVisible()) {
            showFragment(NEWSFEED, false);
        }
    }

    private void onSessionStateChange(Session session, SessionState state) {
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            // check for the OPENED state instead of session.isOpened() since for the
            // OPENED_TOKEN_UPDATED state, the NEWSFEED fragment should already be showing.
            if (state.equals(SessionState.OPENED)) {
                Request.newMeRequest(session, new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (user != null) {
//                            Display the parsed user info
                            loggedIn = true;
                            currentUserId = user.getId();
                            currentUserName = user.getFirstName() + " " + user.getLastName();
                            try {
                                image_path = new URL("http://graph.facebook.com/" + currentUserId + "/picture?type=small");
                                new DynamoDBManagerTask().execute(DynamoDBManagerType.GET_BITMAP);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Log.d(TAG, "Response : " + response);
                            Log.d(TAG, "UserID : " + user.getId());
                            Log.d(TAG, "User Name : " + user.getFirstName() + " " + user.getLastName());
                            loadUser(); // Set up user
                            while (currentUser.getUserName() == null) {
                                // TODO: Add progress bar
                            }
                            newsFeedFragment.updateWallFeed();
                        }
                    }

                }).executeAsync();

                // Show the authenticated fragment
                showFragment(NEWSFEED, false);
            } else if (state.isClosed()) {
                loggedIn = false;
                showFragment(LOGIN, false);
                Toast.makeText(MainActivity.this, "Logged Off", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // only add the menu when the selection fragment is showing
        if (fragments[NEWSFEED].isVisible()) {
            if (menu.size() == 0) {
                settings = menu.add(R.string.logOut);
//                gamePlay = menu.add(R.string.newGame);
            }
            return true;
        } else {
            menu.clear();
            settings = null;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(settings)) {
            showSettingsFragment();
            return true;
        }
        return false;
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }

    private enum DynamoDBManagerType {
        GET_USER, SAVE_USER, GET_GAMES, GET_BITMAP
    }

    private class DynamoDBManagerTask extends AsyncTask<DynamoDBManagerType, Void, String> {
        @Override
        protected String doInBackground(DynamoDBManagerType... types) {
            Log.d("DoINBackGround", "On doInBackground...");

            AmazonDynamoDBClient clientManager = new AmazonDynamoDBClient(credentials);
            DynamoDBMapper mapper = new DynamoDBMapper(clientManager);

            if (loggedIn) {
                switch (types[0]) {
                    case GET_USER:
                        try {
                            Log.d(TAG, "Loading all saved users");
                            // Retrieve all users from saved User Table, returned in undetermined order
                            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                            PaginatedScanList scanResult = mapper.scan(MapperUser.class, scanExpression);
                            userSearchResults = new ArrayList<>();
                            userSearchResults.addAll(scanResult);        // Change result to ArrayList
                            Log.d(TAG, "Retrieved all saved users");

                            for (int i = 0; i < userSearchResults.size(); i++) {
                                if (userSearchResults.get(i).getUserId().equals(currentUserId)) {
                                    Log.d(TAG, "Logged in, user " + currentUserName + " exists, loading......");
                                    //MapperUser temp = mapper.load(MapperUser.class, userSearchResults.get(i).getUserId());
                                    currentUser.setUser(userSearchResults.get(i));

                                    currentUser.removePlayedGame("Empty");
                                    currentUser.removeFriend("Empty");
                                    currentUser.removeLikedGame("Empty");
                                    currentUser.removePendingFriend("Empty");

                                    Log.d(TAG, currentUser.getUserName() + "  is loaded");
                                }
                            }

                            getBitmapFromURL(image_path);

                            // Check if user was loaded
                            if (currentUser.getUserName() == null) {
                                Log.d(TAG, "User did not previously exist, adding current user to database....");
                                currentUser = new MapperUser();
                                currentUser.setUserId(currentUserId);
                                currentUser.setUserName(currentUserName);

                                try {
                                    Log.d(TAG, "Saving current user");

                                    boolean playGames = false, friends = false, likedGames = false, pendingFriend = false;
                                    if (currentUser.getPlayedGames().isEmpty()) {
                                        playGames = true;
                                        currentUser.addPlayedGame("Empty");
                                    }
                                    if (currentUser.getFriends().isEmpty()) {
                                        friends = true;
                                        currentUser.addFriend("Empty");
                                    }
                                    if (currentUser.getLikedGames().isEmpty()) {
                                        currentUser.addLikedGame("Empty");
                                        likedGames = true;
                                    }
                                    if (currentUser.getPendingFriends().isEmpty()) {
                                        currentUser.addPendingFriend("Empty");
                                        pendingFriend = true;
                                    }

                                    mapper.save(currentUser);

                                    if (playGames) currentUser.removePlayedGame("Empty");
                                    if (friends) currentUser.removeFriend("Empty");
                                    if (likedGames) currentUser.removeLikedGame("Empty");
                                    if (pendingFriend) currentUser.removePendingFriend("Empty");

                                } catch (AmazonServiceException ex) {
                                    Log.e(TAG, "Error saving current user");
                                }
                            }

                        } catch (AmazonServiceException ex) {
                            Log.e(TAG, "Error loading users");
                        }
                        break;
                    case SAVE_USER:
                        try {
                            Log.d(TAG, "Saving current user");
                            boolean playGames = false, friends = false, likedGames = false, pendingFriend = false;
                            if (currentUser.getPlayedGames().isEmpty()) {
                                playGames = true;
                                currentUser.addPlayedGame("Empty");
                            }
                            if (currentUser.getFriends().isEmpty()) {
                                friends = true;
                                currentUser.addFriend("Empty");
                            }
                            if (currentUser.getLikedGames().isEmpty()) {
                                currentUser.addLikedGame("Empty");
                                likedGames = true;
                            }
                            if (currentUser.getPendingFriends().isEmpty()) {
                                currentUser.addPendingFriend("Empty");
                                pendingFriend = true;
                            }

                            mapper.save(currentUser);

                            if (playGames) currentUser.removePlayedGame("Empty");
                            if (friends) currentUser.removeFriend("Empty");
                            if (likedGames) currentUser.removeLikedGame("Empty");
                            if (pendingFriend) currentUser.removePendingFriend("Empty");

                            Log.d(TAG, "User saved.");

                        } catch (AmazonServiceException ex) {
                            Log.e(TAG, "Error saving current user");
                            Log.d(TAG, "Service exception: " + ex);
                        }
                        break;
                    case GET_GAMES:
                        try {
                            Log.d(TAG, "Loading all saved games");
                            // Retrieve all games from saved User Table, returned in undetermined order
                            DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
                            PaginatedScanList scanResult = (PaginatedScanList) mapper.scan(MapperUser.class, scanExpression);
                            playedGamesSearchResults = new ArrayList<>();
                            playedGamesSearchResults.addAll(scanResult);        // Change result to ArrayList
                            Log.d(TAG, "Retrieved all saved users");
                        } catch (AmazonServiceException ex) {
                            Log.e(TAG, "Error loading games");
                        }
                        break;
                    case GET_BITMAP:
                        break;
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
        }
    }
}