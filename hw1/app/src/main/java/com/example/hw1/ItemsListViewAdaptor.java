package com.example.hw1;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;

public class ItemsListViewAdaptor extends RecyclerView.Adapter<ItemsListViewAdaptor.ViewHolder> {
    private final ArrayList<CryptoCurrency> items;

    private final ItemListCallBack callBack;

    public ItemsListViewAdaptor(ArrayList<CryptoCurrency> cryptoCurrencies, ItemListCallBack callBack) {
        this.items = cryptoCurrencies;
        this.callBack = callBack;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemNameText;
        public TextView itemPriceText;
        public TextView itemDayDifference;
        public TextView itemWeekDifference;
        public TextView itemMonthDifference;
        public ImageView itemAvatar;

        public ViewHolder(View itemView) {
            super(itemView);

            itemNameText = itemView.findViewById(R.id.item_name);
            itemPriceText = itemView.findViewById(R.id.item_price);
            itemDayDifference = itemView.findViewById(R.id.item_day_difference);
            itemWeekDifference = itemView.findViewById(R.id.item_week_difference);
            itemMonthDifference = itemView.findViewById(R.id.item_month_difference);
            itemAvatar = itemView.findViewById(R.id.item_avatar);
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
        holder.itemView.setOnClickListener(v -> callBack.onItemClicked(position));
        showImage("https://s2.coinmarketcap.com/static/img/coins/64x64/" + item.id + ".png", holder.itemAvatar);
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

    public interface ItemListCallBack {
        void onItemClicked(int position);
    }

    private void showImage(String url, ImageView imageView) {
        Picasso.get()
                .load(url)
                .into(imageView);
    }
}
