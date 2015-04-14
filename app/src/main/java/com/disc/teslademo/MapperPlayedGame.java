package com.disc.teslademo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by diana on 2/17/15.
 */

@DynamoDBTable(tableName = Constants.PlayedGameTableName)
public class MapperPlayedGame {

    // Hash and range
    private String gameId;
    private String gameDate;

    // Game attributes
    private String playedBy;
    private String gameLocation;
    private int totalStrokes;
    private List<Integer> holeStrokes;
    private List<Double> plotPoints;
    private int likes;
    private int totalHoles;
    private int totalPars;
    private String totalGameTime;

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
        if (holeStrokes == null) {
            holeStrokes = new ArrayList<>();
        }
        return holeStrokes;
    }

    public void setHoleStrokes(List<Integer> newholeStrokes) {
        if (holeStrokes == null) {
            holeStrokes = new ArrayList<>();
        }
        if (!holeStrokes.isEmpty()) {
            holeStrokes.clear();
        }
        holeStrokes.addAll(newholeStrokes);
    }

    public boolean addHoleStroke(int position, int newHoleStroke) {
        if (holeStrokes == null) {
            holeStrokes = new ArrayList<>();
        }
        if (holeStrokes.size() >= position) {
            holeStrokes.add(position, newHoleStroke);
            return true;
        } else
            return false;
    }

    public void setHoleStroke(int holeStrokePosition, int newHoleStroke) {
        if (holeStrokes == null) {
            holeStrokes = new ArrayList<>();
        }
        if (holeStrokes.isEmpty() || holeStrokes.size() < holeStrokePosition) return;
        holeStrokes.remove(holeStrokePosition);
        holeStrokes.add(holeStrokePosition, newHoleStroke);
    }

    public int getHoleStroke(int holeStrokePosition) {
        if (holeStrokes == null) {
            holeStrokes = new ArrayList<>();
        }
        if (holeStrokes.isEmpty() || holeStrokes.size() < holeStrokePosition) return 0;
        return holeStrokes.get(holeStrokePosition);

    }

    @DynamoDBAttribute(attributeName = "GameLocation")
    public String getGameLocation() {
        return gameLocation;
    }

    public void setGameLocation(String newgameLocation) {
        gameLocation = newgameLocation;
    }

    @DynamoDBAttribute(attributeName = "PlotPoints")
    public List<Double> getPlotPoints() {
        if (plotPoints == null)
            plotPoints = new ArrayList<>();
        return plotPoints;
    }

    public void setPlotPoints(List<Double> newplotPoints) {
        if (plotPoints == null)
            plotPoints = new ArrayList<>();
        if (!plotPoints.isEmpty()) {
            plotPoints.clear();
        }
        plotPoints.addAll(newplotPoints);
    }

    public void addPlotPoint(Double newplotPoints) {
        if (plotPoints == null)
            plotPoints = new ArrayList<>();
        plotPoints.add(newplotPoints);
    }

    public void replacePlotPoint(int holeDistancePostion, Double replaceValue) {
        if (plotPoints == null)
            plotPoints = new ArrayList<>();
        if (plotPoints.isEmpty() || plotPoints.size() < holeDistancePostion) return;
        plotPoints.remove(holeDistancePostion);
        plotPoints.add(holeDistancePostion, replaceValue);
    }

    public Double getPlotPoint(int plotPoint) {
        return plotPoints.get(plotPoint);
    }

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

    @DynamoDBAttribute(attributeName = "TotalHoles")
    public int getTotalHoles() {
        return totalHoles;
    }

    public void setTotalHoles(int newHoles) {
        totalHoles = newHoles;
    }

    @DynamoDBAttribute(attributeName = "TotalGameTime")
    public String getTotalGameTime() {
        return totalGameTime;
    }

    public void setTotalGameTime(String newTime) {
        totalGameTime = newTime;
    }

    @DynamoDBAttribute(attributeName = "TotalPars")
    public int getTotalPars() {
        return totalPars;
    }

    public void setTotalPars(int newPars) {
        totalPars = newPars;
    }

}