package com.example.hw1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import okio.BufferedSource;

public class CoinFragment extends Fragment {

    private final ExecutorService threadPoolExecutor;
    public CryptoCurrency cryptoCurrency;
    public SciChartSurface surface;
    public ProgressBar progressBar;
    public AtomicBoolean isLoading = new AtomicBoolean(false);
    public AtomicBoolean isEnded = new AtomicBoolean(false);
    public List<OHLC> ohlcList = new ArrayList<>();


    public CoinFragment(CryptoCurrency cryptoCurrency, ExecutorService threadPoolExecutor) {
        this.cryptoCurrency = cryptoCurrency;
        this.threadPoolExecutor = threadPoolExecutor;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin, container, false);
        surface = view.findViewById(R.id.surface);

        progressBar = view.findViewById(R.id.progressBar2);

        getOhlc();

        return view;


    }

    private void initSciChart() {
        SciChartBuilder.init(requireContext());
        SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        int size = ohlcList.size();

        final IAxis xAxis = sciChartBuilder.newCategoryDateAxis().withVisibleRange(size - 30, size).withGrowBy(0, 0.1).build();
        final IAxis yAxis = sciChartBuilder.newNumericAxis().withGrowBy(0d, 0.1d).withAutoRangeMode(AutoRange.Always).build();

        IOhlcDataSeries<Integer, Double> dataSeries = new OhlcDataSeries<>(Integer.class, Double.class);
        for (int i = 0; i < ohlcList.size(); i++) {

            dataSeries.append(i/*priceSeries.getDateData()*/, ohlcList.get(i).getPrice_open(), ohlcList.get(i).getPrice_high(), ohlcList.get(i).getPrice_low(), ohlcList.get(i).getPrice_close());
        }

        final FastCandlestickRenderableSeries rSeries = sciChartBuilder.newCandlestickSeries()
                .withStrokeUp(0xFF00AA00)
                .withFillUpColor(0x8800AA00)
                .withStrokeDown(0xFFFF0000)
                .withFillDownColor(0x88FF0000)
                .withDataSeries(dataSeries)
                .build();

        UpdateSuspender.using(surface, () -> {
            Collections.addAll(surface.getXAxes(), xAxis);
            Collections.addAll(surface.getYAxes(), yAxis);
            Collections.addAll(surface.getRenderableSeries(), rSeries);
            Collections.addAll(surface.getChartModifiers(), sciChartBuilder.newModifierGroupWithDefaultModifiers().build());

            sciChartBuilder.newAnimator(rSeries).withWaveTransformation().withInterpolator(new DecelerateInterpolator()).withDuration(3000).withStartDelay(350).start();
        });

    }

    private void getOhlc() {
        this.isLoading.set(true);
        progressBar.setVisibility(View.VISIBLE);
        String lastWeek = "https://rest.coinapi.io/v1/ohlcv/BITSTAMP_SPOT_BTC_USD/history?period_id=" + "1MIN" + "&time_start=" + "2016-01-01T00:00:00";
        String lastMonth = "https://rest.coinapi.io/v1/ohlcv/BITSTAMP_SPOT_BTC_USD/history?period_id=" + "1MIN" + "&time_start=" + "2016-01-01T00:00:00";
        threadPoolExecutor.execute(new OhlcApiHandler(lastMonth) {
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
                        ohlcList.addAll(Arrays.asList(ohlcs));
                        initSciChart();
                        progressBar.setVisibility(View.GONE);
//                        saveCryptoCurrenciesToCache();
                    }
                };
                threadPoolExecutor.execute(uiHandler);
            }
        });
    }


    private List<OHLC> getOhlcData(boolean isLastWeek) {
        List<OHLC> list = new ArrayList();




        return list;

    }


}