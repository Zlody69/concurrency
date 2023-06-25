package course.concurrency.m2_async.cf.min_price;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        ExecutorService executors = Executors.newCachedThreadPool();
        List<CompletableFuture<Double>> completableFutureList = shopIds.stream()
                .map(aLong -> CompletableFuture
                        .supplyAsync(() ->
                                priceRetriever.getPrice(itemId, aLong), executors))
                .collect(Collectors.toList());


        return completableFutureList.parallelStream()
                .map(doubleCompletableFuture -> doubleCompletableFuture
                        .thenApplyAsync(aDouble -> aDouble, executors)
                        .orTimeout(2500, TimeUnit.MILLISECONDS)
                        .exceptionally(throwable -> Double.NaN)
                        .join())
                .min(Double::compareTo)
                .orElse(Double.NaN);

    }
}
