package com.calculate.shoppinglist;

public class Position {
    static int number = 0;
    public int ID;
    public String name;
    public double pieces;

    public Position(String name, double pieces) {
        number++;
        this.ID = number;
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
