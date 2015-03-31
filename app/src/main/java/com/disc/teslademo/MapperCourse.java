/**
 * Created by diana on 2/10/15.
 */
package com.disc.teslademo;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBAttribute;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBHashKey;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBTable;

import java.util.ArrayList;
import java.util.List;

@DynamoDBTable(tableName = Constants.CourseTableName)
public class MapperCourse {

    private String courseName;
    private String courseId;

    static public final int MAX_NUMBER_OF_HOLES = 18;

    // Baskets and Teepads
//    private double basketLatitude;
//    private double basketLongtidude;
//    private double par;
//    private double tpadLatitude;
//    private double tpadLongitude;

    private List<Double> basketLatitudes;
    private List<Double> basketLongitudes;
    private List<Double> tpadLatitudes;
    private List<Double> tpadLongitudes;
    private List<Double> basketPars;

    // Course Map Lat/Longs
    private double courseWest;


    MapperCourse() {    // Constructor
        courseName = null;
        courseId = null;
        basketLatitudes = new ArrayList<>();
        basketLongitudes = new ArrayList<>();
        tpadLatitudes = new ArrayList<>();
        tpadLongitudes = new ArrayList<>();
        basketPars = new ArrayList<>();
    }

    public void setCourse(MapperCourse temp) {
        courseId = temp.getCourseId();
        courseName = temp.getCourseName();
//        basketLatitude = temp.getBLat();
//        basketLongtidude = temp.getBLong();
//        tpadLatitude = temp.getLat();
//        tpadLongitude = temp.getLong();
//        par = temp.getPar();
    }

    @DynamoDBHashKey(attributeName = "CourseID")
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String newCourseId) {
        courseId = newCourseId;
        return;
    }

    @DynamoDBAttribute(attributeName = "CourseName")
    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String newCourseName) {
        this.courseName = newCourseName;
        return;
    }

//    @DynamoDBAttribute(attributeName = "BasketLatitude")
//    public double getBLat() {return basketLatitude;}
//    public void setBLat(double newValue) {basketLatitude = newValue;}
//
//    @DynamoDBAttribute(attributeName = "BasketLongitude")
//    public double getBLong() {return basketLongtidude;}
//    public void setBLong(double newValue) {basketLongtidude = newValue;}
//
//    @DynamoDBAttribute(attributeName = "Par")
//    public double getPar() {return par;}
//    public void setPar(double newValue) {par = newValue;}
//
//    @DynamoDBAttribute(attributeName = "TPadLatitude")
//    public double getLat() {return tpadLatitude;}
//    public void setLat(double newValue) {tpadLatitude = newValue;}
//
//    @DynamoDBAttribute(attributeName = "TPadLongitude")
//    public double getLong() {return tpadLongitude;}
//    public void setLong(double newValue) {tpadLongitude = newValue;}

    @DynamoDBAttribute(attributeName = "BasketLatitude")
    public List<Double> getBasketLatitudes() {
        return basketLatitudes;
    }

    public double getABasketLatitude(int listPosition){
        return basketLatitudes.get(listPosition);
    }

    public boolean setABasketLatitude(int listPosition, double newLatitude){
        if (basketLatitudes.size() > listPosition) {   // Check that list is long enough
            basketLatitudes.remove(listPosition);
            basketLatitudes.add(listPosition, newLatitude);
            return true;
        }
        else if ((basketLatitudes.size() == listPosition) & (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            basketLatitudes.add(listPosition, newLatitude);
            return true;
        }
        else
            return false;
    }

    public boolean setAllBasketLatitudes(List<Double> newValues) {
        if (!basketLatitudes.isEmpty()) {
            basketLatitudes.clear();   // empty out the existing list
        }
        basketLatitudes.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "BasketLongitude")
    public List<Double> getBasketLongitudes() {

        return basketLongitudes;
    }

    public double getABasketLongitude(int listPosition){
        return basketLongitudes.get(listPosition);
    }

    public boolean setABasketLongitude(int listPosition, double newLongitude){
        if (basketLongitudes.size() > listPosition) {   // Check that list is long enough
            basketLongitudes.remove(listPosition);
            basketLongitudes.add(listPosition, newLongitude);
            return true;
        }
        else if ((basketLongitudes.size() == listPosition) & (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            basketLongitudes.add(listPosition, newLongitude);
            return true;
        }
        else
            return false;
    }

    public boolean setAllBasketLongitudes(List<Double> newValues) {
        if (!basketLongitudes.isEmpty()) {
            basketLongitudes.clear();   // empty out the existing list
        }
        basketLongitudes.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "BasketPar")
    public List<Double> getBasketPars() {

        return basketPars;
    }

    public double getABasketPar(int listPosition){
        return basketPars.get(listPosition);
    }

    public boolean setABasketPar(int listPosition, double newPar){
        if (basketPars.size() > listPosition) {   // Check that list is long enough
            basketPars.remove(listPosition);
            basketPars.add(listPosition, newPar);
            return true;
        }
        else if ((basketPars.size() == listPosition) & (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            basketPars.add(listPosition, newPar);
            return true;
        }
        else
            return false;
    }

    public boolean setAllPars(List<Double> newValues) {
        if (!basketPars.isEmpty()) {
            basketPars.clear();   // empty out the existing list
        }
        basketPars.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "TeepadLatitudes")
    public List<Double> getTpadLatitudes() {

        return tpadLatitudes;
    }

    public double getATpadLatitude(int listPosition){
        return tpadLatitudes.get(listPosition);
    }

    public boolean setATpadLatitude(int listPosition, double newValue){
        if (tpadLatitudes.size() > listPosition) {   // Check that list is long enough
            tpadLatitudes.remove(listPosition);
            tpadLatitudes.add(listPosition, newValue);
            return true;
        }
        else if ((tpadLatitudes.size() == listPosition) & (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            tpadLatitudes.add(listPosition, newValue);
            return true;
        }
        else
            return false;
    }

    public boolean setAllTeePadLatitudes(List<Double> newValues) {
        if (!tpadLatitudes.isEmpty()) {
            tpadLatitudes.clear();   // empty out the existing list
        }
        tpadLatitudes.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "TeepadLongitudes")
    public List<Double> getTpadLongitudes() {

        return tpadLongitudes;
    }

    public double getATpadLongitude(int listPosition){
        return tpadLongitudes.get(listPosition);
    }

    public boolean setATpadLongitude(int listPosition, double newValue){
        if (tpadLongitudes.size() > listPosition) {   // Check that list is long enough
            tpadLongitudes.remove(listPosition);
            tpadLongitudes.add(listPosition, newValue);
            return true;
        }
        else if ((tpadLongitudes.size() == listPosition) & (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            tpadLongitudes.add(listPosition, newValue);
            return true;
        }
        else
            return false;
    }

    public boolean setAllTeePadLongitudes(List<Double> newValues) {
        if (!tpadLongitudes.isEmpty()) {
            tpadLongitudes.clear();   // empty out the existing list
        }
        tpadLongitudes.addAll(newValues);
        return true;
    }
}


