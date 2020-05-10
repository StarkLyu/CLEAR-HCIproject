package com.example.clear;

public class PositionInfo {
    String positionID;
    String positionName;
    double latitude;
    double longitude;
    String city;

    public PositionInfo(String positionID, String positionName, double latitude, double longitude, String city){
        this.positionID=positionID;
        this.positionName=positionName;
        this.latitude=latitude;
        this.longitude=longitude;
        this.city=city;
    }

    String getPositionID(){
        return this.positionID;
    }

    void setPositionID(String positionID){
        this.positionID=positionID;
    }

    String getPositionName(){
        return this.positionName;
    }

    void setPositionName(String positionName){
        this.positionName=positionName;
    }

    double getLatitude(){
        return this.latitude;
    }

    void setLatitude(double latitude){
        this.latitude=latitude;
    }

    double getLongitude(){
        return this.longitude;
    }

    void setLongitude(double longitude){
        this.longitude=longitude;
    }

    String getCity(){
        return this.city;
    }

    void setCity(String city){
        this.city=city;
    }
}
