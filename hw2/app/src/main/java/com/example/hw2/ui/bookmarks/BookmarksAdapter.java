package com.example.hw2.ui.bookmarks;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.hw2.R;
import com.example.hw2.model.Place;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class BookmarksAdapter extends RecyclerView.Adapter<BookmarksAdapter.ViewHolder> {
    private final ArrayList<Place> places;

    private final ItemListCallBack showCallBack;
    private final ItemListCallBack removeCallBack;

    public BookmarksAdapter(ArrayList<Place> places, ItemListCallBack showCallBack, ItemListCallBack removeCallBack) {
        super();
        this.places = places;
        this.showCallBack = showCallBack;
        this.removeCallBack = removeCallBack;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placeName;
        public TextView placeGeocode;

        public ViewHolder(View itemView) {
            super(itemView);

            placeName = itemView.findViewById(R.id.bookmark_name);
            placeGeocode = itemView.findViewById(R.id.bookmark_geocode);
        }
    }

    @NotNull
    @Override
    public BookmarksAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.bookmark_card, parent, false);

        return new ViewHolder(contactView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(BookmarksAdapter.ViewHolder holder, int position) {
        Place item = this.places.get(position);

        holder.placeName.setText(item.getName());
        holder.placeGeocode.setText(item.getLatitude() + ", " + item.getLongitude());
        holder.itemView.findViewById(R.id.show_bookmark).setOnClickListener(v -> showCallBack.onItemClicked(position));
        holder.itemView.findViewById(R.id.remove_bookmark).setOnClickListener(v -> removeCallBack.onItemClicked(position));
    }

    @Override
    public int getItemCount() {
        return this.places.size();
    }

    public interface ItemListCallBack {
        void onItemClicked(int position);
    }

    public Place getItem(int position) {
        return places.get(position);
    }
}
