package com.example.hw1;

public class OHLC {

    private Float price_open;
    private Float price_close;
    private Float price_high;
    private Float price_low;

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


    static class OHLCListResponse {
        OHLC[] data;
    }

}
