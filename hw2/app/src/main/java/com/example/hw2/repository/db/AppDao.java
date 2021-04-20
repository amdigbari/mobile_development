package com.example.hw2.repository.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.hw2.model.Place;

import java.util.List;

@Dao
public interface AppDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Place place);
    @Update
    void update(Place place);
    @Delete
    void delete(Place place);
    @Query("DELETE FROM place_table")
    void deleteAllPlaces();
    @Query("SELECT * FROM place_table ORDER BY priority DESC")
    List<Place> getAllPlaces();
}
