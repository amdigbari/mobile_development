package com.example.hw1;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okio.BufferedSource;

public class ItemsListFragment extends Fragment {
    private ExecutorService threadPoolExecutor;
    public RecyclerView mRecyclerView;
    public AtomicInteger pageNumber = new AtomicInteger(1);
    public AtomicBoolean isLoading = new AtomicBoolean(false);
    public AtomicBoolean isEnded = new AtomicBoolean(false);
    public ItemsListViewAdaptor mItemsListViewAdaptor;
    private Handler mHandler = new Handler();
    private final ArrayList<CryptoCurrency> apiCryptoCurrencies = new ArrayList<>();
    private final ArrayList<CryptoCurrency> cacheCryptoCurrencies = new ArrayList<>();
    private final ArrayList<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private ProgressBar progressBar;
    private Button btnLoadMore;

    public ItemsListFragment(ExecutorService threadPoolExecutor) {
        this.threadPoolExecutor = threadPoolExecutor;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.items_list, container, false);

        progressBar = view.findViewById(R.id.progressBar);
        btnLoadMore = view.findViewById(R.id.btn_load_more);
        this.mRecyclerView = view.findViewById(R.id.items_list);
        this.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mItemsListViewAdaptor = new ItemsListViewAdaptor(cryptoCurrencies, position -> {
            Toast.makeText(getContext(), position + " ", Toast.LENGTH_SHORT).show();
            if (!isLoading.get()) {
                isLoading.set(true);
                getCryptoCurrencies(this.pageNumber.get());
            }
        });
        this.mRecyclerView.setAdapter(this.mItemsListViewAdaptor);

        btnLoadMore.setOnClickListener(v -> {
            getCryptoCurrencies(this.pageNumber.get());
        });

        initializeData();

        return view;
    }

    private void getCryptoCurrenciesFromCache() {
        CacheTread cacheTread = new CacheTread(this.getContext()) {
            @Override
            void readFromFileCallback(CryptoCurrency[] cryptoCurrencies) {
                cacheCryptoCurrencies.addAll(Arrays.asList(cryptoCurrencies));
                mergeCryptoCurrencies();
            }
        };
        threadPoolExecutor.execute(cacheTread);
    }

    private void mergeCryptoCurrencies() {
        Map<String, CryptoCurrency> cryptoCurrencyMap = new LinkedHashMap<>();
        for (CryptoCurrency cryptoCurrency : apiCryptoCurrencies) {
            cryptoCurrencyMap.put(cryptoCurrency.symbol, cryptoCurrency);
        }
        Map<String, CryptoCurrency> cacheCryptoCurrencyMap = new LinkedHashMap<>();
        for (CryptoCurrency cryptoCurrency : cacheCryptoCurrencies) {
            cacheCryptoCurrencyMap.put(cryptoCurrency.symbol, cryptoCurrency);
        }
        cryptoCurrencyMap.putAll(cacheCryptoCurrencyMap);

        cryptoCurrencies.clear();
        cryptoCurrencies.addAll(cryptoCurrencyMap.values());
        this.mItemsListViewAdaptor.notifyDataSetChanged();
    }

    private void getCryptoCurrencies(int pageNumber) {
        int itemPerRequest = 5;
        progressBar.setVisibility(View.VISIBLE);
        final int startItem = (pageNumber - 1) * itemPerRequest + 1;
        final String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?convert=USD&start=" +
                startItem + "&limit=" + (itemPerRequest);
        threadPoolExecutor.execute(new CryptoCurrenciesAPIHandler(url) {
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
        threadPoolExecutor.execute(uiHandler);

        saveCryptoCurrenciesToCache();
        mHandler.post(() -> progressBar.setVisibility(View.GONE));
    }

    private void initializeData() {
        if (Utils.isNetworkConnected(getContext())) {
            getCryptoCurrencies(this.pageNumber.get());
            this.isLoading.set(true);
        } else {
            getCryptoCurrenciesFromCache();
        }
    }

    public void setItemsListViewAdaptor(CryptoCurrency[] cryptoCurrencies) {
        this.apiCryptoCurrencies.addAll(Arrays.asList(cryptoCurrencies));
        mergeCryptoCurrencies();
    }

    private void saveCryptoCurrenciesToCache() {
        CacheTread cacheTread = new CacheTread(this.getContext(), this.cryptoCurrencies) {
            @Override
            void readFromFileCallback(CryptoCurrency[] cryptoCurrencies) {
            }
        };
        threadPoolExecutor.execute(cacheTread);
    }

    @Override
    public void onDestroy() {
        threadPoolExecutor.shutdown();
        super.onDestroy();
    }

}
