package com.stockleague.backend.infra.redis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AtomicOrderbookMatcher {

    private final StringRedisTemplate redis;
    private final ObjectMapper mapper = new ObjectMapper();

    private static final String LUA_BUY = """
    local ticker = ARGV[1]
    local limit  = tonumber(ARGV[2])
    local need   = tonumber(ARGV[3])
    if not need or need <= 0 then
      return '{"version":null,"filled":0,"matches":[]}'
    end

    local verKey = "ob:ver:"..ticker
    local ver = redis.call("GET", verKey)
    if not ver then
      return '{"version":null,"filled":0,"matches":[]}'
    end

    local snap = "ob:snap:"..ticker..":"..ver..":ASK"
    local idx  = "ob:idx:" ..ticker..":"..ver..":ASK"
    local cons = "ob:cons:"..ticker..":"..ver..":ASK"

    local ttlms = redis.call("PTTL", snap)
    if ttlms and ttlms > 0 then
        redis.call("PEXPIRE", cons, ttlms)
        redis.call("PEXPIRE", snap, ttlms)
        redis.call("PEXPIRE", idx,  ttlms)
    end

    local prices = redis.call("ZRANGEBYSCORE", idx, "-inf", limit)
    local filled = 0.0
    local matches = {}

    for i=1,#prices do
      if need <= 0 then break end
      local p = prices[i]
      local snapVolume = tonumber(redis.call("HGET", snap, p)) or 0.0
      if snapVolume > 0 then
        local used    = tonumber(redis.call("HGET", cons, p)) or 0.0
        local avail   = snapVolume - used
        if avail > 0 then
          local take = need
          if avail < need then take = avail end
          if take > 0 then
            redis.call("HINCRBYFLOAT", cons, p, take)
            table.insert(matches, {price=p, volume=take})
            filled = filled + take
            need   = need - take
          end
        end
      end
    end

    local result = { version = ver, filled = filled, matches = matches }
    return cjson.encode(result)
    """;

    private static final String LUA_SELL = """
    local ticker = ARGV[1]
    local limit  = tonumber(ARGV[2])
    local need   = tonumber(ARGV[3])
    if not need or need <= 0 then
      return '{"version":null,"filled":0,"matches":[]}'
    end

    local verKey = "ob:ver:"..ticker
    local ver = redis.call("GET", verKey)
    if not ver then
      return '{"version":null,"filled":0,"matches":[]}'
    end

    local snap = "ob:snap:"..ticker..":"..ver..":BID"
    local idx  = "ob:idx:" ..ticker..":"..ver..":BID"
    local cons = "ob:cons:"..ticker..":"..ver..":BID"

    local ttlms = redis.call("PTTL", snap)
    if ttlms and ttlms > 0 then
        redis.call("PEXPIRE", cons, ttlms)
        redis.call("PEXPIRE", snap, ttlms)
        redis.call("PEXPIRE", idx,  ttlms)
    end

    local prices = redis.call("ZREVRANGEBYSCORE", idx, "+inf", limit)
    local filled = 0.0
    local matches = {}

    for i=1,#prices do
      if need <= 0 then break end
      local p = prices[i]
      local snapVolume = tonumber(redis.call("HGET", snap, p)) or 0.0
      if snapVolume > 0 then
        local used    = tonumber(redis.call("HGET", cons, p)) or 0.0
        local avail   = snapVolume - used
        if avail > 0 then
          local take = need
          if avail < need then take = avail end
          if take > 0 then
            redis.call("HINCRBYFLOAT", cons, p, take)
            table.insert(matches, {price=p, volume=take})
            filled = filled + take
            need   = need - take
          end
        end
      end
    end

    local result = { version = ver, filled = filled, matches = matches }
    return cjson.encode(result)
    """;

    private final DefaultRedisScript<String> BUY_SCRIPT = new DefaultRedisScript<>(LUA_BUY, String.class);
    private final DefaultRedisScript<String> SELL_SCRIPT = new DefaultRedisScript<>(LUA_SELL, String.class);

    public MatchResult matchBuy(String ticker, long limitPrice, BigDecimal needVolume) {
        try {
            String json = redis.execute(BUY_SCRIPT, List.of(), ticker, String.valueOf(limitPrice), needVolume.toPlainString());
            return parse(json);
        } catch (DataAccessException e) {
            return MatchResult.empty();
        }
    }

    public MatchResult matchSell(String ticker, long limitPrice, BigDecimal needVolume) {
        try {
            String json = redis.execute(SELL_SCRIPT, List.of(), ticker, String.valueOf(limitPrice), needVolume.toPlainString());
            return parse(json);
        } catch (DataAccessException e) {
            return MatchResult.empty();
        }
    }

    private MatchResult parse(String json) {
        if (json == null) return MatchResult.empty();
        try {
            return mapper.readValue(json, MatchResult.class);
        } catch (Exception e) {
            return MatchResult.empty();
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MatchResult {
        private String version;                 // 사용된 스냅샷 버전 (문자열)
        private BigDecimal filled;              // 총 체결 수량
        private List<Fill> matches;             // 가격별 체결 분해

        public boolean hasFill() {
            return filled != null && filled.compareTo(BigDecimal.ZERO) > 0;
        }
        public static MatchResult empty() {
            MatchResult r = new MatchResult();
            r.version = null; r.filled = BigDecimal.ZERO; r.matches = new ArrayList<>();
            return r;
        }
    }

    @Getter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fill {
        private String price;
        private BigDecimal volume;
        public BigDecimal priceAsBigDecimal() { return new BigDecimal(price); }
    }
}
