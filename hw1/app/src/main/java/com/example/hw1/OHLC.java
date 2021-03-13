package com.example.hw1;

public class OHLC {

    private Float price_open;
    private Float price_close;
    private Float price_high;
    private Float price_low;
    private String time_period_start;
    private String time_period_end;
    private String time_open;
    private String time_close;
    private String volume_traded;
    private String trades_count;

    public OHLC(Float price_open, Float price_close, Float price_high, Float price_low) {
        this.price_open = price_open;
        this.price_close = price_close;
        this.price_high = price_high;
        this.price_low = price_low;
    }

    public OHLC() {
    }

    public double getPrice_open() {
        return price_open;
    }

    public void setPrice_open(Float price_open) {
        this.price_open = price_open;
    }

    public double getPrice_close() {
        return price_close;
    }

    public void setPrice_close(Float price_close) {
        this.price_close = price_close;
    }

    public double getPrice_high() {
        return price_high;
    }

    public void setPrice_high(Float price_high) {
        this.price_high = price_high;
    }

    public double getPrice_low() {
        return price_low;
    }

    public void setPrice_low(Float price_low) {
        this.price_low = price_low;
    }

    public String getTime_period_start() {
        return time_period_start;
    }

    public void setTime_period_start(String time_period_start) {
        this.time_period_start = time_period_start;
    }

    public String getTime_period_end() {
        return time_period_end;
    }

    public void setTime_period_end(String time_period_end) {
        this.time_period_end = time_period_end;
    }

    public String getTime_open() {
        return time_open;
    }

    public void setTime_open(String time_open) {
        this.time_open = time_open;
    }

    public String getTime_close() {
        return time_close;
    }

    public void setTime_close(String time_close) {
        this.time_close = time_close;
    }

    public String getVolume_traded() {
        return volume_traded;
    }

    public void setVolume_traded(String volume_traded) {
        this.volume_traded = volume_traded;
    }

    public String getTrades_count() {
        return trades_count;
    }

    public void setTrades_count(String trades_count) {
        this.trades_count = trades_count;
    }

    static class OHLCListResponse {
        OHLC[] data;
    }

}
