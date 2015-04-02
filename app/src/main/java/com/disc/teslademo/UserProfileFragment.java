package com.disc.teslademo;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Iterator;
import java.util.Set;


public class UserProfileFragment extends Fragment {

    private ImageButton friendListButton;
    private Button updateNameButton, updateUserPictureButton;
    private OnFragmentInteractionListener mListener;
    private TextView userName, lastGamePlayed, recentActivity;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Set up buttons
        friendListButton = (ImageButton) view.findViewById(R.id.friendsListButton);
        updateNameButton = (Button) view.findViewById(R.id.updateNameBttn);
        updateUserPictureButton = (Button) view.findViewById(R.id.updateUserPictureButtn);

        // Set up textViews
        userName = (TextView) view.findViewById(R.id.userProfileName);
        lastGamePlayed = (TextView) view.findViewById(R.id.lastGamePlayed);
        recentActivity = (TextView) view.findViewById(R.id.recentActivity);

        Set<String> playedGames = MainActivity.currentUser.getPlayedGames();
        if (playedGames.isEmpty()) {
            recentActivity.setText("No saved games for this profile");
            lastGamePlayed.setText("No recent games");
        } else {

            for (Iterator<String> it = playedGames.iterator(); it.hasNext(); ) {

            }
        }

        return view;
    }

    private void onFriendsTextPressed() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }

}
