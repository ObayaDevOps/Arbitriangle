package com.trading.arbitrage.arbitriangle.marketDataRetrieval;

import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

public interface ExchangeHandler {
    BigDecimal retrieveCurrentPrice(CurrencyPair currencyPair) throws IOException;

    List<CurrencyPair> getAvailableCurrencyPairs();
}
