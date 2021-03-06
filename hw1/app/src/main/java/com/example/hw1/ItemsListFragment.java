package com.example.hw1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okio.BufferedSource;

public class ItemsListFragment extends Fragment {
    public RecyclerView mRecyclerView;
    public AtomicInteger pageNumber = new AtomicInteger(1);
    public AtomicBoolean isLoading = new AtomicBoolean(false);
    public AtomicBoolean isEnded = new AtomicBoolean(false);
    public ItemsListViewAdaptor mItemsListViewAdaptor;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.items_list, container, false);

        this.mRecyclerView = view.findViewById(R.id.items_list);
        this.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mItemsListViewAdaptor = new ItemsListViewAdaptor();
        this.mRecyclerView.setAdapter(this.mItemsListViewAdaptor);

        addButtonClickListener(view);
        initializeData();

        return view;
    }

    private void addButtonClickListener(View view) {

        Button loadMore = view.findViewById(R.id.load_more);
        loadMore.setOnClickListener(v -> {
            if (!isLoading.get()) {
                isLoading.set(true);
                getCryptoCurrencies(pageNumber.get());
            }
        });
    }

//    private void getCryptoCurrenciesFromCache() {
//        CacheTread cacheTread = new CacheTread(this.context) {
//            @Override
//            void readFromFileCallback(CryptoCurrency[] cryptoCurrencies) {
//                for (CryptoCurrency cryptoCurrency : cryptoCurrencies) {
//                    System.out.println("from cache: " + cryptoCurrency.name + '\n');
//                }
//            }
//        };
//        MainActivity.threadPoolExecutor.execute(cacheTread);
//    }

    private void getCryptoCurrencies(int pageNumber) {
        int itemPerRequest = 20;
        final int startItem = (pageNumber - 1) * itemPerRequest + 1;
        final String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?convert=USD&start=" +
                startItem + "&limit=" + (itemPerRequest);
        MainActivity.threadPoolExecutor.execute(new CryptoCurrenciesAPIHandler(url) {
            @Override
            void requestCallback(BufferedSource response) throws IOException {
                getCryptoCurrenciesCallback(response);
            }
        });
    }

    private void getCryptoCurrenciesCallback(BufferedSource response) throws IOException {
        final Moshi moshi = new Moshi.Builder().build();
        final JsonAdapter<CryptoCurrency.CryptoListResponse> cryptoResponseJsonAdapter = moshi.adapter(CryptoCurrency.CryptoListResponse.class);
        CryptoCurrency.CryptoListResponse jsonResponse = cryptoResponseJsonAdapter.fromJson(response);

        assert jsonResponse != null;

        this.isLoading.set(false);
        this.isEnded.set(jsonResponse.data.length == 20);
        this.pageNumber.set(this.pageNumber.get() + 1);

        UIHandler uiHandler = new UIHandler(this, jsonResponse.data);
        MainActivity.threadPoolExecutor.execute(uiHandler);
//                CacheTread cacheTread = new CacheTread(this.context, jsonResponse.data) {
//                    @Override
//                    void readFromFileCallback(CryptoCurrency[] cryptoCurrencies) {}
//                };
//                MainActivity.threadPoolExecutor.execute(cacheTread);
    }

    private void initializeData() {
//        getCryptoCurrenciesFromCache();
        this.isLoading.set(true);
        getCryptoCurrencies(this.pageNumber.get());
    }

    public void setItemsListViewAdaptor(CryptoCurrency[] cryptoCurrencies) {
        this.mItemsListViewAdaptor.addCryptoCurrencies(cryptoCurrencies);
    }
}
