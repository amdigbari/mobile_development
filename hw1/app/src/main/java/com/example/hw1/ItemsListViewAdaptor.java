package com.example.hw1;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ItemsListViewAdaptor extends RecyclerView.Adapter<ItemsListViewAdaptor.ViewHolder> {
    private final ArrayList<CryptoCurrency> items = new ArrayList<CryptoCurrency>();


    public ItemsListViewAdaptor() {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView itemNameText;
        public TextView itemIdText;

        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            itemNameText = itemView.findViewById(R.id.item_name);
            itemIdText = itemView.findViewById(R.id.item_id);
        }
    }

    @NotNull
    @Override
    public ItemsListViewAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View contactView = inflater.inflate(R.layout.item_card, parent, false);

        // Return a new holder instance
        return new ViewHolder(contactView);
    }

    // Involves populating data into the item through holder
    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ItemsListViewAdaptor.ViewHolder holder, int position) {
        // Get the data model based on position
        CryptoCurrency item = this.items.get(position);

        // Set item views based on your views and data model
        TextView itemNameView = holder.itemNameText;
        itemNameView.setText(item.name);

        TextView itemIdView = holder.itemIdText;
        itemIdView.setText(item.id.toString());
    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return this.items.size();
    }

    public void addCryptoCurrencies(CryptoCurrency[] cryptoCurrencies) {
        this.items.addAll(Arrays.asList(cryptoCurrencies));

        this.notifyDataSetChanged();

        System.out.println("Hey there 4: " + this.items.size());
    }
}
