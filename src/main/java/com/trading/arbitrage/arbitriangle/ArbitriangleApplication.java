package com.trading.arbitrage.arbitriangle;

import com.trading.arbitrage.arbitriangle.arbitrageDetection.BellmanFordAlgorithm;
import com.trading.arbitrage.arbitriangle.arbitrageDetection.DirectedEdge;
import com.trading.arbitrage.arbitriangle.arbitrageDetection.EdgeWeightedDigraph;
import com.trading.arbitrage.arbitriangle.marketDataRetrieval.BinanceExchangeHandler;
import org.knowm.xchange.binance.dto.marketdata.BinancePrice;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// https://github.com/kevin-wayne/algs4/blob/master/src/main/java/edu/princeton/cs/algs4/Arbitrage.java
// https://algs4.cs.princeton.edu/44sp/BellmanFordSP.java.html

@SpringBootApplication
public class ArbitriangleApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ArbitriangleApplication.class, args);
    }

    //A naive method to find opportunities would be to run

    @Override
    public void run(String... args) throws Exception{
        BinanceExchangeHandler binance = new BinanceExchangeHandler();
        String startNodeCurrency = "PAX";
        String exchangeName = "Binance";

        //Now in a loop need to update the edges of the graph - is this any more efficient than just creating the graph again ?
        //This is temp, does not handle Binance updating their list of coins
        boolean shouldKeepRunning = true;
        int numberOfRuns =0;
        while(shouldKeepRunning){
            numberOfRuns++;

            int numberOfAvailaableCurrencies = binance.getNumberOfAvailableCurrencies();
            List<Currency> uniqueCurrencyList = binance.getListOfAvailableCurrencies();
//            uniqueCurrencyList.forEach(System.out::println);

            //Set up the name tracker
            //CurrencyName, NodeNumber
            Map<String,Integer> nameMap = new HashMap<>();
            for(int i=0; i< uniqueCurrencyList.size(); i++){
                nameMap.put(uniqueCurrencyList.get(i).toString(), i);
            }
            //A reverse mapping used to output
            Map<Integer, String> nameMapInversed = nameMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

            //-------------------------------------------------------------------------------------------------------------

            //Need to initialise the graph size
            EdgeWeightedDigraph G = new EdgeWeightedDigraph(numberOfAvailaableCurrencies);

            //Construct the graph: Get the full list ready for input: COntains the weight data too, not just the node names
            List<BinancePrice> fullPriceList = binance.getFullTickerList();

            for(int i=0; i<fullPriceList.size(); i++ ){
                BinancePrice binancePrice = fullPriceList.get(i);
//                System.out.println(binancePrice);

                //break into components and add to the graph : TODO Note the order v or w ??
                Currency curr1 =  binancePrice.getCurrencyPair().base; //Need to get the list of the uniques and
                Currency curr2 = binancePrice.getCurrencyPair().counter;
                Double rate = binancePrice.getPrice().doubleValue();

                //Keep track of the names in map
                DirectedEdge edge = new DirectedEdge(nameMap.get(curr1.toString()), nameMap.get(curr2.toString()),-Math.log(rate));
                G.addEdge(edge);
            }

            //find a negative cycle - Cycle through the 'unique currency list'
            BellmanFordAlgorithm spt = new BellmanFordAlgorithm(G, nameMap.get(startNodeCurrency));
            if(spt.hasNegativeCycle()){
                shouldKeepRunning = false;
                double amountToTrade = 100.0;
                System.out.println("-----------------------------------------------------------------------------");
                for(DirectedEdge e : spt.negativeCycle()){
                    System.out.printf("%10.5f %s ", amountToTrade, nameMapInversed.get(e.from()));
                    amountToTrade *= Math.exp(-e.weight());
                    System.out.printf("%10.5f %s ", amountToTrade, nameMapInversed.get(e.to()));
                }
            } else {
                System.out.println("No arbitrage opportunity for " +  startNodeCurrency + " on " + exchangeName);
                System.out.println("Number of runs: " + numberOfRuns);
            }
        }




    }
}
