package dev.heartflame.fleet.monitor.network;

import dev.heartflame.fleet.monitor.SystemMonitorExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public enum NetworkMonitor {;

    private static final Logger log = LoggerFactory.getLogger("Network Monitor");
    private static final AtomicReference<Map<String, NetworkInterfaceData>> CURRENT_SYSTEM_NETWORK = new AtomicReference<>();

    // ignore virtual adapters, container bridge networks etc.
    // we don't want to monitor these.
    private static final Pattern IGNORED_INTERFACES_REGEX = Pattern.compile("^(veth\\w+)|(br-\\w+)$");

    private static final Map<String, NetworkDataHolder> AVERAGES = new ConcurrentHashMap<>();

    private static int POLL_INTERVAL = 10;
    private static final int AVERAGE_SIZE_SECONDS = (int) TimeUnit.MINUTES.toSeconds(15);
    private static final int DATA_WINDOW = AVERAGE_SIZE_SECONDS / POLL_INTERVAL; // 4

    static {
        // Schedule the network data collection task.
        SystemMonitorExecutor.EXECUTOR.scheduleAtFixedRate(new PolledNetworkingDataCollectionTask(), 1, POLL_INTERVAL, TimeUnit.SECONDS);
    }

    @SuppressWarnings("EmptyMethod")
    public static void enableMonitoring(int pollInterval) {
        // purposefully empty
        POLL_INTERVAL = pollInterval;
        log.info("Initialising Network Monitor.");
    }

    public static Map<String, NetworkInterfaceData> networkTotals() {
        Map<String, NetworkInterfaceData> vals = CURRENT_SYSTEM_NETWORK.get();
        return vals == null ? Collections.emptyMap() : vals;
    }

    public static Map<String, NetworkDataHolder> networkAverages() {
        return Collections.unmodifiableMap(AVERAGES);
    }

    // -------------------- POLLING -------------------- //

    private static final class PolledNetworkingDataCollectionTask implements Runnable {

        private static final BigDecimal POLL_INTERVAL_DECIMAL = BigDecimal.valueOf(POLL_INTERVAL);

        @Override
        public void run() {
            Map<String, NetworkInterfaceData> values = pollAndGetDiff(NetworkInterfaceData::poll, CURRENT_SYSTEM_NETWORK);
            if (values != null) {
                add(AVERAGES, values);
            }
        }

        private static Map<String, NetworkInterfaceData> pollAndGetDiff(Supplier<Map<String, NetworkInterfaceData>> poller, AtomicReference<Map<String, NetworkInterfaceData>> ref) {
            Map<String, NetworkInterfaceData> latest = poller.get();

            Map<String, NetworkInterfaceData> prev = ref.getAndUpdate(previous -> {
                if (previous == null && latest.isEmpty()) {
                    return null;
                } else {
                    return latest;
                }
            });

            if (prev == null) {
                return null;
            }

            return NetworkInterfaceData.diff(latest, prev);
        }

        private static void add(Map<String, NetworkDataHolder> averagesMap, Map<String, NetworkInterfaceData> values) {

            for (String key : values.keySet()) {
                if (!IGNORED_INTERFACES_REGEX.matcher(key).matches()) {
                    averagesMap.computeIfAbsent(key, k -> new NetworkDataHolder(DATA_WINDOW));
                }
            }

            for (Map.Entry<String, NetworkDataHolder> entry : averagesMap.entrySet()) {
                String intName = entry.getKey();
                NetworkDataHolder averages = entry.getValue();

                NetworkInterfaceData info = values.getOrDefault(intName, NetworkInterfaceData.EMPTY);
                averages.accept(info, PolledNetworkingDataCollectionTask::calculateRate);
            }
        }

        private static BigDecimal calculateRate(long value) {
            return BigDecimal.valueOf(value).divide(POLL_INTERVAL_DECIMAL, RoundingMode.HALF_UP);
        }
    }
}
