package com.example.hw1;


public class CryptoCurrency {
    public String name;
    public Integer id;
    public String symbol;
    public Quote quote;

    public CryptoCurrency(Integer id, String name, String symbol, Quote quote) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.quote = quote;
    }

    public static class Quote {
        public USD USD;

        public Quote(USD usd) {
            this.USD = usd;
        }

        public static class USD {
            public Float price;
            public Float percent_change_1h;
            public Float percent_change_24h;
            public Float percent_change_7d;

            public USD(Float price, Float percent_change_1h, Float percent_change_24h, Float percent_change_7d) {
                this.price = price;
                this.percent_change_1h = percent_change_1h;
                this.percent_change_24h = percent_change_24h;
                this.percent_change_7d = percent_change_7d;
            }
        }
    }
}
