package com.example.hw1;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ItemsListViewAdaptor extends RecyclerView.Adapter<ItemsListViewAdaptor.ViewHolder> {
    private final ArrayList<CryptoCurrency> items = new ArrayList<CryptoCurrency>();


    public ItemsListViewAdaptor() {

    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemNameText;
        public TextView itemPriceText;
        public TextView itemDayDifference;
        public TextView itemWeekDifference;
        public TextView itemMonthDifference;

        public ViewHolder(View itemView) {
            super(itemView);

            itemNameText = itemView.findViewById(R.id.item_name);
            itemPriceText = itemView.findViewById(R.id.item_price);
            itemDayDifference = itemView.findViewById(R.id.item_day_difference);
            itemWeekDifference = itemView.findViewById(R.id.item_week_difference);
            itemMonthDifference = itemView.findViewById(R.id.item_month_difference);
        }
    }

    @NotNull
    @Override
    public ItemsListViewAdaptor.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        View contactView = inflater.inflate(R.layout.item_card, parent, false);

        return new ViewHolder(contactView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ItemsListViewAdaptor.ViewHolder holder, int position) {
        CryptoCurrency item = this.items.get(position);

        holder.itemNameText.setText(item.symbol + " | " + item.name);
        holder.itemPriceText.setText(item.quote.USD.price.toString() + "$");
        showItemPercent(holder.itemDayDifference, item.quote.USD.percent_change_24h, "1d");
        showItemPercent(holder.itemWeekDifference, item.quote.USD.percent_change_7d, "7d");
        showItemPercent(holder.itemMonthDifference, item.quote.USD.percent_change_30d, "30d");
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    @SuppressLint("SetTextI18n")
    private void showItemPercent(TextView view, Float value, String prefix) {
        DecimalFormat df = new DecimalFormat("0.00; 0.00");
        view.setText(prefix + ": " + df.format(value) + "%");
        if (value > 0f) {
            view.setTextColor(Color.GREEN);
        } else if (value < 0f) {
            view.setTextColor(Color.RED);
        }
    }

    public void addCryptoCurrencies(CryptoCurrency[] cryptoCurrencies) {
        this.items.addAll(Arrays.asList(cryptoCurrencies));

        this.notifyDataSetChanged();
    }
}
