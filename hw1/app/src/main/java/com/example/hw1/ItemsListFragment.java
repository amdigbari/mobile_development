package com.example.hw1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemsListFragment extends Fragment {
    public static RecyclerView mRecyclerView;
    public static AtomicInteger pageNumber = new AtomicInteger(1);
    public static AtomicBoolean isLoading = new AtomicBoolean(false);
    public static AtomicBoolean isEnded = new AtomicBoolean(false);
    public static ItemsListViewAdaptor mItemsListViewAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.items_list, container, false);

        ItemsListFragment.mRecyclerView = view.findViewById(R.id.items_list);
        ItemsListFragment.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        ItemsListFragment.mRecyclerView.setLayoutManager(mLayoutManager);
        mItemsListViewAdaptor = new ItemsListViewAdaptor();
        ItemsListFragment.mRecyclerView.setAdapter(mItemsListViewAdaptor);

        addButtonClickListener(view);
        initializeData();

        return view;
    }

    private void addButtonClickListener(View view) {

        Button loadMore = view.findViewById(R.id.load_more);
        loadMore.setOnClickListener(v -> {
            System.out.println("Hey there 2: " + isLoading.get() + " " + pageNumber.get());
            if (!isLoading.get()) {
                isLoading.set(true);
                getCryptoCurrencies(pageNumber.get());
            }
        });
    }

    private void getCryptoCurrencies(int pageNumber) {
        int itemPerRequest = 20;
        final int startItem = (pageNumber - 1) * itemPerRequest + 1;
        final String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?convert=USD&start=" +
                startItem + "&limit=" + (itemPerRequest);
        MainActivity.threadPoolExecutor.execute(new CryptoCurrenciesAPIHandler(url));
    }

    private void initializeData() {
        isLoading.set(true);
        getCryptoCurrencies(pageNumber.get());
    }

    public static void setItemsListViewAdaptor(CryptoCurrency[] cryptoCurrencies) {
        mItemsListViewAdaptor.addCryptoCurrencies(cryptoCurrencies);
    }


}
