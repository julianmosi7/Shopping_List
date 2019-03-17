package com.calculate.shoppinglist;

public class Position {
    public int ID;
    public String name;
    public double pieces;

    public Position(String name, double pieces) {
        this.ID = ID++;
        this.name = name;
        this.pieces = pieces;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return pieces;
    }

    public void setPrice(double price) {
        this.pieces = price;
    }

    public String toString(){
        return ID + " " + name + " " + pieces;
    }
}
