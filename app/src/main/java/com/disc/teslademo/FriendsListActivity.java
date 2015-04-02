/*
 * Copyright (C) 2009 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.disc.teslademo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * This Activity appears as a dialog. It lists any paired devices and
 * devices detected in the area after discovery. When a device is chosen
 * by the user, the MAC address of the device is sent back to the parent
 * Activity in the result Intent.
 */


public class FriendsListActivity extends Activity {
    // Debugging
    private static final String TAG = "Friends List Activity";
    private static final boolean D = true;
    // Return Intent extra
    public static String EXTRA_FRIEND_NAME = "friend_id";
    private static String noFriends = "You have no friends.";
    private static String noOtherUsers = "No other users found.";
    private static String noPendingRequests = "You have no pending friend requests.";
    // The on-click listener for adding friends in the ListViews
    private OnItemClickListener listClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {

            String info = ((TextView) v).getText().toString();

            if ((!info.equals(noPendingRequests)) && !info.equals(noFriends) && !info.equals(noOtherUsers)) {
                // Create the result Intent and include the new friend ID

                Intent intent = new Intent();
                intent.putExtra(EXTRA_FRIEND_NAME, info);

                // Set result and finish this Activity
                setResult(Activity.RESULT_OK, intent);
            } else {
                setResult(Activity.RESULT_CANCELED);
            }

            finish();
        }
    };

    // Member fields
    private ArrayAdapter<String> currentFriends;
    private ArrayAdapter<String> friendRequests;
    private ArrayAdapter<String> otherUsers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
//        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.friends_list);

        // Set result CANCELED incase the user backs out
        setResult(Activity.RESULT_CANCELED);

        // Initialize array adapters. One for current friends, one for pending friend
        // requests, and one for all other users
        currentFriends = new ArrayAdapter<String>(this, R.layout.friend_name);
        friendRequests = new ArrayAdapter<String>(this, R.layout.friend_name);
        otherUsers = new ArrayAdapter<String>(this, R.layout.friend_name);

        // Find and set up the ListView for current friends
        ListView currentFriendsView = (ListView) findViewById(R.id.friends_list);
        currentFriendsView.setAdapter(currentFriends);
        currentFriendsView.setOnItemClickListener(listClickListener);

        // Find and set up the ListView for pending friend requests
        ListView friendRequestsView = (ListView) findViewById(R.id.new_requests);
        friendRequestsView.setAdapter(friendRequests);
        friendRequestsView.setOnItemClickListener(listClickListener);

        // Find and set up the ListView for other users
        ListView otherUsersView = (ListView) findViewById(R.id.new_users);
        otherUsersView.setAdapter(otherUsers);
        otherUsersView.setOnItemClickListener(listClickListener);

        // Get a set of all current users
        ArrayList<MapperUser> masterList = MainActivity.userSearchResults;
        Set<String> currentFriendSet = MainActivity.currentUser.getFriends();
        Set<String> pendingFriendSet = MainActivity.currentUser.getPendingFriends();

        // Iterate through masterList and sort all users into correct arraylist
        for (Iterator<MapperUser> it = masterList.iterator(); it.hasNext(); ) {
            MapperUser temp = it.next();
            if (currentFriendSet.contains(temp.getUserId())) {
                // User is a current friend
                currentFriends.add(temp.getUserName() + '\n' + temp.getUserId());
            }
            if (pendingFriendSet.contains(temp.getUserId())) {
                // User is a pending friend
                friendRequests.add(temp.getUserName() + '\n');
            } else {
                // User is another users
                otherUsers.add(temp.getUserName() + '\n');
            }
        }

        // Check if arrays are empty
        if (currentFriends.isEmpty()) {
            currentFriends.add(noFriends);
        }

        if (friendRequests.isEmpty()) {
            friendRequests.add(noPendingRequests);
        }

        if (otherUsers.isEmpty()) {
            otherUsers.add(noOtherUsers);
        }
    }
}
