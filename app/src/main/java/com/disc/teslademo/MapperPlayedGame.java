package com.disc.teslademo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.List;

/**
* Created by diana on 2/17/15.
*/

@DynamoDBTable(tableName = Constants.UserTableName)
public class MapperPlayedGame {

    // Hash and range
    private String gameId;
    private String gameDate;

    // Game attributes
    private String playedBy;
    private String gameLocation;
    private int totalStrokes;
    private List<Integer> holeStrokes;
    private List<Double> holeDistances;
    private int likes;

    @DynamoDBHashKey(attributeName = "GameID")
    public String getgameId() {
        return gameId;
    }

    public void setgameId(String newgameId) {
        gameId = newgameId;
    }

    @DynamoDBAttribute(attributeName = "GameDate")
    public String getGameDate() {
        return gameDate;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    @DynamoDBAttribute(attributeName = "PlayedBy")
    public String getPlayedBy() {
        return playedBy;
    }

    public void setPlayedBy(String playedBy) {
        this.playedBy = playedBy;
    }

    @DynamoDBAttribute(attributeName = "HoleStrokes")
    public List<Integer> getHoleStrokes() {
        return holeStrokes;
    }

    public void setHoleStrokes(List<Integer> newholeStrokes) {
        if (!holeStrokes.isEmpty()) {
            holeStrokes.clear();
        }
        holeStrokes.addAll(newholeStrokes);
    }

    public boolean addHoleStroke(int position, int newHoleStroke) {
        if (holeStrokes.size() >= position) {
            holeStrokes.add(position, newHoleStroke);
            return true;
        }
        else
            return false;
    }

    public void changeHoleStroke(int holeStrokePosition, int newHoleStroke) {
        if (holeStrokes.isEmpty() || holeStrokes.size() < holeStrokePosition) return;
        holeStrokes.remove(holeStrokePosition);
        holeStrokes.add(holeStrokePosition, newHoleStroke);
    }

    @DynamoDBAttribute(attributeName = "GameLocation")
    public String getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(String newgameLocation) {
        gameLocation = newgameLocation;
    }

//    @DynamoDBAttribute(attributeName = "HoleDistances")
//    public List<Double> getHoleDistances() {
//        return holeDistances;
//    }
//
//    public void setHoleDistances(List<Double> newholeDistances) {
//        if (!holeDistances.isEmpty()) {
//            holeDistances.clear();
//        }
//        holeDistances.addAll(newholeDistances);
//    }

//    public void addHoleDistance(Double newholeDistances) {
//        holeDistances.add(newholeDistances);
//    }
//
//    public void replaceHoleDistance(int holeDistancePostion, Double replaceValue){
//        if(holeDistances.isEmpty() || holeDistances.size() < holeDistancePostion) return;
//        holeDistances.remove(holeDistancePostion);
//        holeDistances.add(holeDistancePostion, replaceValue);
//    }

    @DynamoDBAttribute(attributeName = "Likes")
    public int getLikes() {
        return likes;
    }

    public void setLikes(int newLikes) {
        likes = newLikes;
    }

    public void addLike() {
        likes++;
    }

    public void removeLike() {
        likes--;
    }

    @DynamoDBAttribute(attributeName = "TotalStrokes")
    public int getTotalStrokes() {
        return totalStrokes;
    }

    public void setTotalStrokes(int newStrokes) {
        totalStrokes = newStrokes;
    }

}