package com.example.hw1;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ItemsListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private final ExecutorService threadPoolExecutor;
    public RecyclerView mRecyclerView;
    public AtomicInteger pageNumber = new AtomicInteger(1);
    public AtomicBoolean isLoading = new AtomicBoolean(false);
    public AtomicBoolean isEnded = new AtomicBoolean(false);
    public ItemsListViewAdaptor mItemsListViewAdaptor;
    private final ArrayList<CryptoCurrency> apiCryptoCurrencies = new ArrayList<>();
    private final ArrayList<CryptoCurrency> cacheCryptoCurrencies = new ArrayList<>();
    private final ArrayList<CryptoCurrency> cryptoCurrencies = new ArrayList<>();
    private SwipeRefreshLayout swipeRefreshLayout;

    public ItemsListFragment(ExecutorService threadPoolExecutor) {
        super();
        this.threadPoolExecutor = threadPoolExecutor;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.items_list, container, false);

        Button btnLoadMore = view.findViewById(R.id.btn_load_more);
        this.mRecyclerView = view.findViewById(R.id.items_list);
        this.mRecyclerView.setHasFixedSize(true);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(view.getContext());
        this.mRecyclerView.setLayoutManager(mLayoutManager);
        this.mItemsListViewAdaptor = new ItemsListViewAdaptor(cryptoCurrencies, position -> {
            Toast.makeText(getContext(), position + " ", Toast.LENGTH_SHORT).show();

            getCryptoCurrencies(this.pageNumber.get(), false);

        });
        this.mRecyclerView.setAdapter(this.mItemsListViewAdaptor);

        this.setRefreshListener(view);

        btnLoadMore.setOnClickListener(v -> getCryptoCurrencies(this.pageNumber.get(), false));

        initializeData();

        return view;
    }

    private void setRefreshListener(View view) {
        this.swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        this.swipeRefreshLayout.setOnRefreshListener(this);
    }

    private void getCryptoCurrenciesFromCache() {
        threadPoolExecutor.execute(new CacheTread(this.getContext()) {
            @Override
            void readFromFileCallback(CryptoCurrency[] cryptoCurrencies) {
                cacheCryptoCurrencies.addAll(Arrays.asList(cryptoCurrencies));
                mergeCryptoCurrencies();
            }
        });
    }

    private void mergeCryptoCurrencies() {
        Map<String, CryptoCurrency> cryptoCurrencyMap = new LinkedHashMap<>();
        if (apiCryptoCurrencies.size() > 0) {
            cryptoCurrencies.clear();
            for (CryptoCurrency cryptoCurrency : apiCryptoCurrencies) {
                cryptoCurrencyMap.put(cryptoCurrency.symbol, cryptoCurrency);
            }
        } else if (cacheCryptoCurrencies.size() > 0) {
            cryptoCurrencies.clear();
            Map<String, CryptoCurrency> cacheCryptoCurrencyMap = new LinkedHashMap<>();
            for (CryptoCurrency cryptoCurrency : cacheCryptoCurrencies) {
                cacheCryptoCurrencyMap.put(cryptoCurrency.symbol, cryptoCurrency);
            }
            cryptoCurrencyMap.putAll(cacheCryptoCurrencyMap);
        }


        cryptoCurrencies.clear();
        cryptoCurrencies.addAll(cryptoCurrencyMap.values());
        this.mItemsListViewAdaptor.notifyDataSetChanged();
    }

    private void getCryptoCurrencies(int page, boolean clear) {
        if (!this.isLoading.get()) {
            int itemPerRequest = 10;
            this.isLoading.set(true);
            final int startItem = (page - 1) * itemPerRequest + 1;
            final String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest?convert=USD&start=" +
                    startItem + "&limit=" + (itemPerRequest);
            swipeRefreshLayout.setRefreshing(true);
            threadPoolExecutor.execute(new CryptoCurrenciesAPIHandler(url) {
                @Override
                void requestCallback(String response) {
                    if (clear) {
                        apiCryptoCurrencies.clear();
                    }
                    getCryptoCurrenciesCallback(response);
                    pageNumber.set(page + 1);
                }

                @Override
                void requestCatchCallback() {
                    getCryptoCurrenciesCatchCallback();
                }
            });
        }
    }

    private void getCryptoCurrenciesCallback(String response) {
        try {
            final JSONArray data = new JSONObject(response).getJSONArray("data");
            Gson gson = new GsonBuilder().create();
            CryptoCurrency[] responseData = gson.fromJson(data.toString(), CryptoCurrency[].class);

            this.isLoading.set(false);
            this.isEnded.set(responseData.length == 20);

            threadPoolExecutor.execute(new UIHandler() {
                @Override
                void callback() {
                    setItemsListViewAdaptor(responseData);
                    saveCryptoCurrenciesToCache();
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getCryptoCurrenciesCatchCallback() {
        this.isLoading.set(false);
        this.swipeRefreshLayout.setRefreshing(false);
    }

    private void initializeData() {
        getCryptoCurrenciesFromCache();
        if (Utils.isNetworkConnected(Objects.requireNonNull(getContext()))) {
            this.swipeRefreshLayout.post(() -> this.getCryptoCurrencies(1, true));
        }
    }

    public void setItemsListViewAdaptor(CryptoCurrency[] cryptoCurrencies) {
        this.apiCryptoCurrencies.addAll(Arrays.asList(cryptoCurrencies));
        mergeCryptoCurrencies();
    }

    private void saveCryptoCurrenciesToCache() {
        threadPoolExecutor.execute(new CacheTread(this.getContext(), this.cryptoCurrencies) {
            @Override
            void readFromFileCallback(CryptoCurrency[] cryptoCurrencies) {
            }
        });
    }

    @Override
    public void onDestroy() {
        threadPoolExecutor.shutdown();
        super.onDestroy();
    }

    @Override
    public void onRefresh() {
        getCryptoCurrencies(1, true);
    }
}
