package com.trading.arbitrage.arbitriangle.marketDataRetrieval;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.binance.BinanceExchange;
import org.knowm.xchange.binance.dto.marketdata.BinanceKline;
import org.knowm.xchange.binance.dto.marketdata.BinancePrice;
import org.knowm.xchange.binance.dto.marketdata.BinanceTicker24h;
import org.knowm.xchange.binance.dto.marketdata.KlineInterval;
import org.knowm.xchange.binance.service.BinanceMarketDataService;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.service.marketdata.MarketDataService;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BinanceExchangeHandler implements ExchangeHandler {
    public static String exchangeName = "Binance";
    private static Exchange exchange = ExchangeFactory.INSTANCE.createExchange(BinanceExchange.class.getName());;
    private static BinanceMarketDataService marketDataService = (BinanceMarketDataService) exchange.getMarketDataService();

    public BinanceExchangeHandler() {
    }

    public BigDecimal retrieveCurrentPrice(CurrencyPair currencyPair) throws IOException {
        Ticker ticker = marketDataService.getTicker(currencyPair);
        return  ticker.getLast();
    }

    public List<CurrencyPair> getAvailableCurrencyPairs() {
        List<CurrencyPair> availableCurrencyPairList = exchange.getExchangeSymbols();
        return availableCurrencyPairList;
    }

    public List<BinancePrice> getFullTickerList() throws IOException {
        return marketDataService.tickerAllPrices();
    }

    public List<Currency> getListOfAvailableCurrencies () throws IOException {
        Set<Currency> pairSet = new HashSet<>();
        List<BinancePrice> currencyPairPriceList = getFullTickerList();

        currencyPairPriceList.forEach(currencyPairPrice -> {
            pairSet.add(currencyPairPrice.getCurrencyPair().base);
            pairSet.add(currencyPairPrice.getCurrencyPair().counter);
        });

        return new ArrayList(pairSet);
    }

    public int getNumberOfAvailableCurrencies() throws IOException {
        return getListOfAvailableCurrencies().size();
    }


    public void getRawData() throws IOException{
        List<BinanceTicker24h> tickers = new ArrayList<>();

        System.out.println("-----------------------------------------------------------------");

        BinanceKline kline = marketDataService.lastKline(CurrencyPair.ADA_BNB, KlineInterval.h1);
        List<BinancePrice> tickerList = marketDataService.tickerAllPrices();
        tickerList.forEach(ticker -> System.out.println(ticker));

        //It now seems we have access to all the data we require for any exchange of our choosing
        // So now we need to input that to a graph

        System.out.println("Kline -----------------------------------------------------------------");
        System.out.println(kline);

        System.out.println("Get Currencies--------------------------------------------------------------");

        System.out.println(exchange.getExchangeMetaData().getCurrencies());

        System.out.println("Get Currencies Key set =------------------------------------------------------------------");

        System.out.println(exchange.getExchangeMetaData().getCurrencyPairs().keySet());

    }


}
