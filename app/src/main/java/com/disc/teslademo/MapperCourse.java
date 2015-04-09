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

    static public final int MAX_NUMBER_OF_HOLES = 18;
    private String courseName;
    private String courseId;
    private List<Double> basketLatitudes;
    private List<Double> basketLongitudes;
    private List<Double> tpadLatitudes;
    private List<Double> tpadLongitudes;
    private List<Double> basketPars;

    // Course Map Lat/Longs
    private double courseWest;


//    MapperCourse() {    // Constructor
//        courseName = null;
//        courseId = null;
//        basketLatitudes = new ArrayList<>();
//        basketLongitudes = new ArrayList<>();
//        tpadLatitudes = new ArrayList<>();
//        tpadLongitudes = new ArrayList<>();
//        basketPars = new ArrayList<>();
//    }

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

    @DynamoDBAttribute(attributeName = "BasketLatitude")
    public List<Double> getBasketLatitudes() {
        return basketLatitudes;
    }

    public boolean setBasketLatitudes(List<Double> newValues) {
        if (basketLatitudes == null) {
            basketLatitudes = new ArrayList<>();
        }
        if (!basketLatitudes.isEmpty()) {
            basketLatitudes.clear();   // empty out the existing list
        }
        basketLatitudes.addAll(newValues);
        return true;
    }

    public double getABasketLatitude(int listPosition) {
        if (basketLatitudes == null) {
            basketLatitudes = new ArrayList<>();
        }
        if (basketLatitudes.size() > listPosition)
            return basketLatitudes.get(listPosition);
        else
            return 0;
    }

    public boolean setABasketLatitude(int listPosition, double newLatitude) {
        if (basketLatitudes == null) {
            basketLatitudes = new ArrayList<>();
        }
        if (basketLatitudes.isEmpty() && listPosition == 0) {
            basketLatitudes.add(newLatitude);
            return true;
        } else {
            if (basketLatitudes.size() > listPosition) {   // Check that list is long enough
                basketLatitudes.remove(listPosition);
                basketLatitudes.add(listPosition, newLatitude);
                return true;
            } else if ((basketLatitudes.size() == listPosition) && (listPosition < MAX_NUMBER_OF_HOLES)) {  // Add at end of list
                basketLatitudes.add(listPosition, newLatitude);
                return true;
            } else
                return false;
        }
    }

    @DynamoDBAttribute(attributeName = "BasketLongitude")
    public List<Double> getBasketLongitudes() {
        if (basketLongitudes == null) {
            basketLongitudes = new ArrayList<>();
        }
        return basketLongitudes;
    }

    public double getABasketLongitude(int listPosition) {
        if (basketLongitudes == null) {
            basketLongitudes = new ArrayList<>();
        }
        if (basketLongitudes.size() > listPosition)
            return basketLongitudes.get(listPosition);
        else
            return 0;
    }

    public boolean setABasketLongitude(int listPosition, double newLongitude) {
        if (basketLongitudes == null) {
            basketLongitudes = new ArrayList<>();
        }
        if (basketLongitudes.size() > listPosition) {   // Check that list is long enough
            basketLongitudes.remove(listPosition);
            basketLongitudes.add(listPosition, newLongitude);
            return true;
        } else if ((basketLongitudes.size() == listPosition) && (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            basketLongitudes.add(listPosition, newLongitude);
            return true;
        } else
            return false;
    }

    public boolean setBasketLongitudes(List<Double> newValues) {
        if (basketLongitudes == null) {
            basketLongitudes = new ArrayList<>();
        }
        if (!basketLongitudes.isEmpty()) {
            basketLongitudes.clear();   // empty out the existing list
        }
        basketLongitudes.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "BasketPar")
    public List<Double> getBasketPars() {
        if (basketPars == null) {
            basketPars = new ArrayList<>();
        }
        return basketPars;
    }

    public double getABasketPar(int listPosition) {
        if (basketPars == null) {
            basketPars = new ArrayList<>();
        }
        if (basketPars.size() > listPosition)
            return basketPars.get(listPosition);
        else
            return 0;
    }

    public boolean setABasketPar(int listPosition, double newPar) {
        if (basketLongitudes == null) {
            basketLongitudes = new ArrayList<>();
        }
        if (basketPars.size() > listPosition) {   // Check that list is long enough
            basketPars.remove(listPosition);
            basketPars.add(listPosition, newPar);
            return true;
        } else if ((basketPars.size() == listPosition) && (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            basketPars.add(listPosition, newPar);
            return true;
        } else
            return false;
    }

    public boolean setBasketPars(List<Double> newValues) {
        if (basketPars == null) {
            basketPars = new ArrayList<>();
        }
        if (!basketPars.isEmpty()) {
            basketPars.clear();   // empty out the existing list
        }
        basketPars.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "TeepadLatitudes")
    public List<Double> getTpadLatitudes() {
        if (tpadLatitudes == null) {
            tpadLatitudes = new ArrayList<>();
        }
        return tpadLatitudes;
    }

    public double getATpadLatitude(int listPosition) {
        if (tpadLatitudes == null) {
            tpadLatitudes = new ArrayList<>();
        }
        if (tpadLatitudes.size() > listPosition)
            return tpadLatitudes.get(listPosition);
        else
            return 0;
    }

    public boolean setATpadLatitude(int listPosition, double newValue) {
        if (tpadLatitudes == null) {
            tpadLatitudes = new ArrayList<>();
        }
        if (tpadLatitudes.size() > listPosition) {   // Check that list is long enough
            tpadLatitudes.remove(listPosition);
            tpadLatitudes.add(listPosition, newValue);
            return true;
        } else if ((tpadLatitudes.size() == listPosition) && (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            tpadLatitudes.add(listPosition, newValue);
            return true;
        } else
            return false;
    }

    public boolean setTpadLatitudes(List<Double> newValues) {
        if (tpadLatitudes == null) {
            tpadLatitudes = new ArrayList<>();
        }
        if (!tpadLatitudes.isEmpty()) {
            tpadLatitudes.clear();   // empty out the existing list
        }
        tpadLatitudes.addAll(newValues);
        return true;
    }

    @DynamoDBAttribute(attributeName = "TeepadLongitudes")
    public List<Double> getTpadLongitudes() {
        if (tpadLongitudes == null) {
            tpadLongitudes = new ArrayList<>();
        }
        return tpadLongitudes;
    }

    public double getATpadLongitude(int listPosition) {
        if (tpadLongitudes == null) {
            tpadLongitudes = new ArrayList<>();
        }
        if (tpadLongitudes.size() > listPosition)
            return tpadLongitudes.get(listPosition);
        else
            return 0;
    }

    public boolean setATpadLongitude(int listPosition, double newValue) {
        if (tpadLongitudes == null) {
            tpadLongitudes = new ArrayList<>();
        }
        if (tpadLongitudes.size() > listPosition) {   // Check that list is long enough
            tpadLongitudes.remove(listPosition);
            tpadLongitudes.add(listPosition, newValue);
            return true;
        } else if ((tpadLongitudes.size() == listPosition) & (MAX_NUMBER_OF_HOLES > listPosition)) {  // Don't add outside 18 hole limit
            tpadLongitudes.add(listPosition, newValue);
            return true;
        } else
            return false;
    }

    public boolean setTpadLongitudes(List<Double> newValues) {
        if (tpadLongitudes == null) {
            tpadLongitudes = new ArrayList<>();
        }
        if (!tpadLongitudes.isEmpty()) {
            tpadLongitudes.clear();   // empty out the existing list
        }
        tpadLongitudes.addAll(newValues);
        return true;
    }
}