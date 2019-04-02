package com.calculate.shoppinglist;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class Store {
    public String name;
    public double latitude;
    public double longitude;
    Location location = new Location("end");
    public List<Position> position = new ArrayList<>();

    public Store(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getPosition() {
        return position;
    }

    public void setPosition(List position) {
        this.position = position;
    }

    public void addItem(Position pos){
        position.add(pos);
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String toString(){
        return name;
    }

    public Location getLocation(){
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

}
