package com.example.hw1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ProgressBar;

import com.scichart.charting.model.dataSeries.IOhlcDataSeries;
import com.scichart.charting.model.dataSeries.OhlcDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.FastCandlestickRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.extensions.builders.SciChartBuilder;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import okio.BufferedSource;

public class CoinFragment extends Fragment {

    private final ExecutorService threadPoolExecutor;
    public CryptoCurrency cryptoCurrency;
    public SciChartSurface surface;
    public ProgressBar progressBar;
    Button btnLastWeek, btnLastMonth;
    public AtomicBoolean isLoading = new AtomicBoolean(false);
    public AtomicBoolean isEnded = new AtomicBoolean(false);
    public ArrayList<OHLC> ohlcList = new ArrayList<>();

    IOhlcDataSeries<Date, Double> dataSeries = new OhlcDataSeries<>(Date.class, Double.class);

    boolean isWeek  = true;


    public CoinFragment(CryptoCurrency cryptoCurrency, ExecutorService threadPoolExecutor) {
        this.cryptoCurrency = cryptoCurrency;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin, container, false);
        surface = view.findViewById(R.id.surface);
        btnLastMonth = view.findViewById(R.id.btn_last_month);
        btnLastWeek = view.findViewById(R.id.btn_last_week);

        progressBar = view.findViewById(R.id.progressBar2);

        btnLastWeek.setOnClickListener(v -> {
            btnLastWeek.setBackgroundColor(getResources().getColor(R.color.carbon_green_600));
            btnLastMonth.setBackgroundColor(getResources().getColor(R.color.white));
            isWeek = true;
            dataSeries.clear();
            getOhlc(isWeek);
        });

        btnLastMonth.setOnClickListener(v -> {
            btnLastMonth.setBackgroundColor(getResources().getColor(R.color.carbon_green_600));
            btnLastWeek.setBackgroundColor(getResources().getColor(R.color.white));
            isWeek = false;
            dataSeries.clear();
            getOhlc(isWeek);
        });

        initSciChart();




        getOhlc(isWeek);

        return view;


    }

    private void initSciChart() {
        SciChartBuilder.init(requireContext());
        SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        int size = ohlcList.size();

        final IAxis xAxis = sciChartBuilder.newCategoryDateAxis().withVisibleRange(size - 30, size).withGrowBy(0, 0.1).build();
        final IAxis yAxis = sciChartBuilder.newNumericAxis().withGrowBy(0d, 0.1d).withAutoRangeMode(AutoRange.Always).build();


        final FastCandlestickRenderableSeries rSeries = sciChartBuilder.newCandlestickSeries()
                .withStrokeUp(0xFF00AA00)
                .withFillUpColor(0x8800AA00)
                .withStrokeDown(0xFFFF0000)
                .withFillDownColor(0x88FF0000)
                .withDataSeries(dataSeries)
                .build();

        surface.zoomExtentsX();
        surface.zoomExtentsY();

//        yAxis.setAutoRange(AutoRange.Never);
//        xAxis.setAutoRange(AutoRange.Never);

        UpdateSuspender.using(surface, () -> {
            Collections.addAll(surface.getXAxes(), xAxis);
            Collections.addAll(surface.getYAxes(), yAxis);
            Collections.addAll(surface.getRenderableSeries(), rSeries);
            Collections.addAll(surface.getChartModifiers(), sciChartBuilder.newModifierGroupWithDefaultModifiers().build());

            sciChartBuilder.newAnimator(rSeries).withWaveTransformation().withInterpolator(new DecelerateInterpolator()).withDuration(3000).withStartDelay(350).start();
        });

    }

    private void appendPoints() {
        for (int i = 0; i < ohlcList.size(); i++) {
            String[] strings = ohlcList.get(i).getTime_period_start().split("T");
            SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd");
            Date date2 = null;
            try {
                date2 = formatter2.parse(strings[0]);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            dataSeries.append(date2, ohlcList.get(i).getPrice_open(), ohlcList.get(i).getPrice_high(), ohlcList.get(i).getPrice_low(), ohlcList.get(i).getPrice_close());
        }
    }

    private void getOhlc(boolean isWeek) {
        long monthSeconds = 30 * 24 * 60 * 60;
        long weekSeconds = 7 * 24 * 60 * 60;
        this.isLoading.set(true);
        progressBar.setVisibility(View.VISIBLE);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        if (isWeek) {
            date.setTime(System.currentTimeMillis() - weekSeconds * 1000);

        } else {
            date.setTime(System.currentTimeMillis() - monthSeconds * 1000);
        }


        String url = "https://rest.coinapi.io/v1/ohlcv/BITSTAMP_SPOT_BTC_USD/history?period_id=" +
                "1MIN" + "&time_start=" +
                formatter.format(date.getTime()).replace(" ", "T") +
                "&symbol_id=" + cryptoCurrency.getSymbol();
        threadPoolExecutor.execute(new OhlcApiHandler(url) {
            @Override
            void requestCallback(BufferedSource response) throws IOException {
                final Moshi moshi = new Moshi.Builder().build();
                final JsonAdapter<OHLC[]> cryptoResponseJsonAdapter = moshi.adapter(OHLC[].class);
                OHLC[] jsonResponse = cryptoResponseJsonAdapter.fromJson(response);

                assert jsonResponse != null;

                isLoading.set(false);

                UIHandler uiHandler = new UIHandler() {
                    @Override
                    void callback() {
                        OHLC[] ohlcs = jsonResponse;
                        ohlcList.clear();
                        ohlcList.addAll(Arrays.asList(ohlcs));
                        appendPoints();
                        progressBar.setVisibility(View.GONE);
                        saveOhlcsCurrenciesToCache();
                    }
                };
                threadPoolExecutor.execute(uiHandler);
            }
        });
    }

    private void saveOhlcsCurrenciesToCache() {
        OhlcCacheTread cacheTread = new OhlcCacheTread(getContext(), ohlcList) {
            @Override
            void readFromFileCallback(OHLC[] ohlcs) {
            }
        };
        threadPoolExecutor.execute(cacheTread);
    }
}