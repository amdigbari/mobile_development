package com.example.hw1;


public class CryptoCurrency {
    private String name;
    private Integer id;
    private String symbol;
    private Quote quote;

    public CryptoCurrency(Integer id, String name, String symbol, Quote quote) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.quote = quote;
    }

    public static class Quote {
        private USD USD;

        public Quote(USD usd) {
            this.USD = usd;
        }

        public Quote.USD getUSD() {
            return USD;
        }

        public void setUSD(Quote.USD USD) {
            this.USD = USD;
        }

        public static class USD {
            private Float price;
            private Float percent_change_1h;
            private Float percent_change_24h;
            private Float percent_change_7d;

            public USD(Float price, Float percent_change_1h, Float percent_change_24h, Float percent_change_7d) {
                this.price = price;
                this.percent_change_1h = percent_change_1h;
                this.percent_change_24h = percent_change_24h;
                this.percent_change_7d = percent_change_7d;
            }

            public Float getPrice() {
                return price;
            }

            public void setPrice(Float price) {
                this.price = price;
            }

            public Float getPercent_change_1h() {
                return percent_change_1h;
            }

            public void setPercent_change_1h(Float percent_change_30d) {
                this.percent_change_1h = percent_change_1h;
            }

            public Float getPercent_change_24h() {
                return percent_change_24h;
            }

            public void setPercent_change_24h(Float percent_change_24h) {
                this.percent_change_24h = percent_change_24h;
            }

            public Float getPercent_change_7d() {
                return percent_change_7d;
            }

            public void setPercent_change_7d(Float percent_change_7d) {
                this.percent_change_7d = percent_change_7d;
            }
        }
    }

    static class CryptoListResponse {
        CryptoCurrency[] data;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Quote getQuote() {
        return quote;
    }

    public void setQuote(Quote quote) {
        this.quote = quote;
    }
}
