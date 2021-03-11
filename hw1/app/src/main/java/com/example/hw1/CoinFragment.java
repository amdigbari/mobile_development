package com.example.hw1;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;

import com.scichart.charting.model.dataSeries.IOhlcDataSeries;
import com.scichart.charting.model.dataSeries.OhlcDataSeries;
import com.scichart.charting.visuals.SciChartSurface;
import com.scichart.charting.visuals.axes.AutoRange;
import com.scichart.charting.visuals.axes.IAxis;
import com.scichart.charting.visuals.renderableSeries.FastCandlestickRenderableSeries;
import com.scichart.core.framework.UpdateSuspender;
import com.scichart.extensions.builders.SciChartBuilder;

import java.util.Collections;
import java.util.Date;

public class CoinFragment extends Fragment {

    CryptoCurrency cryptoCurrency;
    SciChartSurface surface;

    public CoinFragment(CryptoCurrency cryptoCurrency) {
        this.cryptoCurrency = cryptoCurrency;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_coin, container, false);
        surface = view.findViewById(R.id.surface);

        initSciChart();


        return view;


    }

    private void initSciChart() {
        SciChartBuilder.init(requireContext());
        SciChartBuilder sciChartBuilder = SciChartBuilder.instance();

        int size = 10;

        final IAxis xAxis = sciChartBuilder.newCategoryDateAxis().withVisibleRange(size - 30, size).withGrowBy(0, 0.1).build();
        final IAxis yAxis = sciChartBuilder.newNumericAxis().withGrowBy(0d, 0.1d).withAutoRangeMode(AutoRange.Always).build();

        IOhlcDataSeries<Date, Double> dataSeries = new OhlcDataSeries<>(Date.class, Double.class);
//        dataSeries.append(priceSeries.getDateData(), priceSeries.getOpenData(), priceSeries.getHighData(), priceSeries.getLowData(), priceSeries.getCloseData());

        final FastCandlestickRenderableSeries rSeries = sciChartBuilder.newCandlestickSeries()
                .withStrokeUp(0xFF00AA00)
                .withFillUpColor(0x8800AA00)
                .withStrokeDown(0xFFFF0000)
                .withFillDownColor(0x88FF0000)
                .withDataSeries(dataSeries)
                .build();

        UpdateSuspender.using(surface, new Runnable() {
            @Override
            public void run() {
                Collections.addAll(surface.getXAxes(), xAxis);
                Collections.addAll(surface.getYAxes(), yAxis);
                Collections.addAll(surface.getRenderableSeries(), rSeries);
                Collections.addAll(surface.getChartModifiers(), sciChartBuilder.newModifierGroupWithDefaultModifiers().build());

                sciChartBuilder.newAnimator(rSeries).withWaveTransformation().withInterpolator(new DecelerateInterpolator()).withDuration(3000).withStartDelay(350).start();
            }
        });

    }
}