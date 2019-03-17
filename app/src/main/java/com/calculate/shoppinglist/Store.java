package com.calculate.shoppinglist;

import java.util.List;

public class Store {
    public String name;
    public List<Position> position;

    public Store(String name, List position) {
        this.name = name;
        this.position = position;
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

    public String toString(){
        return name;
    }
}
