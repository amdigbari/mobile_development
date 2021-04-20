package com.example.hw2.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "place_table")
public class Place {
    @PrimaryKey(autoGenerate = true)
    private long id;

    private String name;
    private String latitude;
    private String  Longitude;
    private int priority;

    public void setId(long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        Longitude = longitude;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return Longitude;
    }

    public int getPriority() {
        return priority;
    }


    @Override
    public String toString() {
        return "Place{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", latitude='" + latitude + '\'' +
                ", Longitude='" + Longitude + '\'' +
                ", priority=" + priority +
                '}';
    }
}
