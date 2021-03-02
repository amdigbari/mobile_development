package com.example.hw1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    public ExecutorService threadPoolExecutor =
            new ThreadPoolExecutor(5, 10, 5000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    public static RecyclerView itemsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeUI();
        initializeData();

    }

    private void getCryptoCurrencies(int pageNumber) {
        int itemPerRequest = 20;
        final int startItem = (pageNumber - 1) * itemPerRequest + 1;
        final String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?convert=USD&start=" +
                startItem + "&limit=" + (startItem + itemPerRequest - 1);
        this.threadPoolExecutor.execute(new OkHttpHandler(url));
    }

    private void initializeData() {
        getCryptoCurrencies(1);
    }

    private void initializeUI() {
        MainActivity.itemsListView = (RecyclerView) findViewById(R.id.items_list);
        itemsListView.setLayoutManager(new LinearLayoutManager(this));
        MainActivity.itemsListView.setAdapter(new ItemsListViewAdaptor(Collections.emptyList()));
    }

    public static void setItemsListViewAdaptor(CryptoCurrency[] cryptoCurrencies) {
        MainActivity.itemsListView.setAdapter(new ItemsListViewAdaptor(Arrays.asList(cryptoCurrencies)));
    }
}