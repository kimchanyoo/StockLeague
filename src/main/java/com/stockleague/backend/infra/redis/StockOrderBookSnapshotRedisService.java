package com.stockleague.backend.infra.redis;

import com.stockleague.backend.stock.dto.response.stock.StockOrderBookDto;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundZSetOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StockOrderBookSnapshotRedisService {

    private final StringRedisTemplate redis;

    private static final Duration SNAP_TTL = Duration.ofSeconds(10);

    private static String seqKey(String ticker) {
        return "ob:verseq:" + ticker;
    }

    private static String verKey(String ticker) {
        return "ob:ver:" + ticker;
    }

    private static String askSnapshotKey(String ticker, Long ver) {
        return "ob:snap:" + ticker + ":" + ver + ":ASK";
    }

    private static String bidSnapshotKey(String ticker, Long ver) {
        return "ob:snap:" + ticker + ":" + ver + ":BID";
    }

    private static String askIdxKey(String ticker, Long ver) {
        return "ob:idx:" + ticker + ":" + ver + ":ASK";
    }

    private static String bidIdxKey(String ticker, Long ver) {
        return "ob:idx:" + ticker + ":" + ver + ":BID";
    }

    public long writeSnapshot(StockOrderBookDto dto) {
        String ticker = dto.ticker();

        String seqKey = seqKey(ticker);
        Long ver = redis.opsForValue().increment(seqKey);
        if (ver == null) {
            return 1L;
        }

        String verKey = verKey(ticker);
        String askSnapshotKey = askSnapshotKey(ticker, ver);
        String bidSnapshotKey = bidSnapshotKey(ticker, ver);
        String askIdxKey = askIdxKey(ticker, ver);
        String bidIdxKey = bidIdxKey(ticker, ver);

        long[] askPrices = dto.askPrices();
        long[] askVolumes = dto.askVolumes();
        long[] bidPrices = dto.bidPrices();
        long[] bidVolumes = dto.bidVolumes();

        long ttlSec = SNAP_TTL.getSeconds();

        redis.executePipelined(new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) {

                RedisOperations<String, String> ops = (RedisOperations<String, String>) operations;

                BoundHashOperations<String, String, String> askSnapshot = ops.boundHashOps(askSnapshotKey);
                BoundZSetOperations<String, String> askIdx = ops.boundZSetOps(askIdxKey);

                for (int i = 0; i < askPrices.length; i++) {
                    if (askVolumes[i] <= 0) {
                        continue;
                    }
                    String price = Long.toString(askPrices[i]);
                    String volume = Long.toString(askVolumes[i]);
                    askSnapshot.put(price, volume);
                    askIdx.add(price, (double) askPrices[i]);
                }

                BoundHashOperations<String, String, String> bidSnapshot = ops.boundHashOps(bidSnapshotKey);
                BoundZSetOperations<String, String> bidIdx = ops.boundZSetOps(bidIdxKey);

                for (int i = 0; i < bidPrices.length; i++) {
                    if (bidVolumes[i] <= 0) {
                        continue;
                    }
                    String price = Long.toString(bidPrices[i]);
                    String volume = Long.toString(bidVolumes[i]);
                    bidSnapshot.put(price, volume);
                    bidIdx.add(price, (double) bidPrices[i]);
                }

                ops.expire(askSnapshotKey, ttlSec, TimeUnit.SECONDS);
                ops.expire(askIdxKey, ttlSec, TimeUnit.SECONDS);
                ops.expire(bidSnapshotKey, ttlSec, TimeUnit.SECONDS);
                ops.expire(bidIdxKey, ttlSec, TimeUnit.SECONDS);

                ops.boundValueOps(verKey).set(String.valueOf(ver), ttlSec, TimeUnit.SECONDS);

                return null;
            }
        });
        return ver;
    }
}
