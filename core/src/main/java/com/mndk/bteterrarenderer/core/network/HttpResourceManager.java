package com.mndk.bteterrarenderer.core.network;

import com.mndk.bteterrarenderer.util.image.ImageUtil;
import com.mndk.bteterrarenderer.dep.terraplusplus.http.Http;
import com.mndk.bteterrarenderer.util.Loggers;
import io.netty.buffer.ByteBuf;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import javax.annotation.Nullable;
import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class HttpResourceManager {

    // match -> replace with "$1://$2/"
    private final Pattern PROTOCOL_HOST_PORT = Pattern.compile("^(https?)://(([^:/]+)(?::(\\d+))?)/.*$");
    private final Map<String, Integer> MCR_ENTRIES = new ConcurrentHashMap<>();

    public CompletableFuture<BufferedImage> downloadAsImage(String url, @Nullable Integer maxConcurrentRequests) {
        return download(url, maxConcurrentRequests).thenApplyAsync(buf -> {
            try { return ImageUtil.bufferToImage(buf); }
            catch (Exception e) { throw new RuntimeException(e); }
        });
    }

    @SneakyThrows
    public CompletableFuture<ByteBuf> download(String url, @Nullable Integer maxConcurrentRequests) {
        if (maxConcurrentRequests != null) {
            Matcher matcher = PROTOCOL_HOST_PORT.matcher(url);
            if (matcher.matches()) {
                String host = matcher.group(1) + "://" + matcher.group(2) + "/";
                // 1. If the host is not in the map, add it
                // 2. If the host is in the map, replace the value if it is different
                // Update maximum concurrent requests to the host if the above
                // conditions are met
                Integer previous = MCR_ENTRIES.put(host, maxConcurrentRequests);
                if (previous == null || !previous.equals(maxConcurrentRequests)) {
                    Http.setMaximumConcurrentRequestsTo(host, maxConcurrentRequests);
                    Loggers.get(HttpResourceManager.class).info("Updated max concurrent requests to {}: {}", host, maxConcurrentRequests);
                }
            }
        }
        return Http.get(url);
    }
}
