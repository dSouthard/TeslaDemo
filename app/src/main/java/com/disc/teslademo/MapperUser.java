package com.disc.teslademo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@DynamoDBTable(tableName = Constants.UserTableName)
public class MapperUser {

    private String userName;
    private String userId;

    // User Connections
//    private String friend;
//    private String playedGame;
//    private int likes;

    private Set<String> friends;           // stores IDs of all users added as friends
    private Set<String> pendingFriends;    // stores IDs of all users pending as friends
    private Set<String> playedGames;       // stores IDs of all games played
    private Set<String> likedGames;        // stores IDs of liked games
//    private String userPicId;               // name of stored user ID pic

    public MapperUser() {  // Constructor
        userName = null;
        userId = null;
        friends =  Collections.synchronizedSet(new HashSet());
        pendingFriends = Collections.synchronizedSet(new HashSet());
        playedGames = Collections.synchronizedSet(new HashSet());
        likedGames = Collections.synchronizedSet(new HashSet());
    }


    public void setUser(MapperUser temp) {
        userName = temp.getUserName();
        userId = temp.getUserId();
        friends = temp.getFriends();
        pendingFriends = temp.getPendingFriends();
        playedGames = temp.getPlayedGames();
        likedGames = temp.getLikedGames();
    }

    @DynamoDBHashKey(attributeName = "UserID")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String newuserId) {
        userId = newuserId;
    }

    @DynamoDBAttribute(attributeName = "UserName")
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

//    @DynamoDBAttribute(attributeName = "Friend")
//    public String getFriend() {return friend;}
//    public void setFriend(String newValue) {friend = newValue;}
//
//    @DynamoDBAttribute(attributeName = "PlayedGame")
//    public String getPlayedGame() {return playedGame;}
//    public void setPlayedGame(String newValue) {playedGame = newValue;}
//
//    @DynamoDBAttribute(attributeName = "Likes")
//    public int getLikes() {return likes;}
//    public void setLikes(int newValue) {likes = newValue;}


//    @DynamoDBAttribute(attributeName = "UserPicID")
//    public String getUserPicId() {
//        return userPicId;
//    }
//
//    public void setUserPicId(String userPicId) {
//        this.userPicId = userPicId;
//    }
//
    @DynamoDBAttribute(attributeName = "LikedGames")
    public Set<String> getLikedGames() {
        return likedGames;
    }

    public boolean setLikedGames(Set<String> newlikedGames) {
        if (!likedGames.isEmpty()) {
            likedGames.clear();
        }
        return likedGames.addAll(newlikedGames);
    }

    public boolean addLikedGame(String newLikedGame) {
        return likedGames.add(newLikedGame);
    }

    public boolean removeLikedGame(String removeName) {
        if (likedGames.isEmpty()) return false;
        return likedGames.remove(removeName);
    }

    @DynamoDBAttribute(attributeName = "PlayedGames")
    public Set<String> getPlayedGames() {
        return playedGames;
    }

    public boolean setPlayedGames(Set<String> newplayedGames) {
        if (!playedGames.isEmpty()) {
            playedGames.clear();
        }
        return playedGames.addAll(newplayedGames);
    }

    public boolean addPlayedGame(String newPlayedGame) {
        return playedGames.add(newPlayedGame);
    }

    public boolean removePlayedGame(String removeName){
        if(playedGames.isEmpty()) return false;
        return playedGames.remove(removeName);
    }

    @DynamoDBAttribute(attributeName = "PendingFriends")
    public Set<String> getPendingFriends() {
        return pendingFriends;
    }

    public boolean setPendingFriends(Set<String> newPendingFriends) {
        if (!pendingFriends.isEmpty()) {
            pendingFriends.clear();
        }
        return pendingFriends.addAll(newPendingFriends);
    }

    public boolean addPendingFriend(String newpendingFriends) {
        return pendingFriends.add(newpendingFriends);
    }

    public boolean removePendingFriend(String removeName){
        if(pendingFriends.isEmpty()) return false;
        return pendingFriends.remove(removeName);
    }

    @DynamoDBAttribute(attributeName = "Friends")
    public Set<String> getFriends() {
        return friends;
    }

    public boolean setFriends(Set<String> newFriends) {
        if (!friends.isEmpty()) {
            friends.clear();
        }
        return friends.addAll(newFriends);
    }

    public boolean addFriend(String newFriends) {
        return friends.add(newFriends);
    }

    public boolean removeFriend(String removeName){
        if(friends.isEmpty()) return false;
        return friends.remove(removeName);
    }
}