package dev.heartflame.fleet.monitor.network;

import com.google.common.collect.ImmutableMap;
import dev.heartflame.fleet.monitor.LinuxProc;
import lombok.Getter;
import lombok.NonNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

@Getter
public final class NetworkInterfaceData {

    public static final NetworkInterfaceData EMPTY = new NetworkInterfaceData("", 0, 0, 0, 0, 0, 0);

    private final String name;
    private final long rxBytes;
    private final long rxPackets;
    private final long rxErrors;
    private final long txBytes;
    private final long txPackets;
    private final long txErrors;

    public NetworkInterfaceData(String name, long rxBytes, long rxPackets, long rxErrors, long txBytes, long txPackets, long txErrors) {
        this.name = name;
        this.rxBytes = rxBytes;
        this.rxPackets = rxPackets;
        this.rxErrors = rxErrors;
        this.txBytes = txBytes;
        this.txPackets = txPackets;
        this.txErrors = txErrors;
    }

    public long getBytes(Direction direction) {
        switch (direction) {
            case RECEIVE:
                return this.rxBytes;
            case TRANSMIT:
                return this.txBytes;
            default:
                throw new AssertionError();
        }
    }

    public long getPackets(Direction direction) {
        switch (direction) {
            case RECEIVE:
                return this.rxPackets;
            case TRANSMIT:
                return this.txPackets;
            default:
                throw new AssertionError();
        }
    }

    /**
     *
     * @return {@link Map<String, NetworkInterfaceData>} Map of the system network statistics (for each interface).
     */
    public static @NonNull Map<String, NetworkInterfaceData> poll() {
        try {
            List<String> output = LinuxProc.NETWORK.readFile();
            return parseData(output);
        } catch (Exception error) {
            error.printStackTrace();
            return Collections.emptyMap();
        }
    }

    private static final Pattern NETWORK_INTERFACE_PATTERN = Pattern.compile("^\\s*(\\w+):([\\d\\s]+)$");

    static @NonNull Map<String, NetworkInterfaceData> parseData(List<String> output) {
        if (output.size() < 3) {
            // The first two lines of the linux netdata (output variable) doesn't contain any useful data,
            // so if there are less than 3 lines, we return an empty map as there is no data to provide.
            return Collections.emptyMap();
        }

        // We get the first line (aka. the header) and split it into the three categories: Interface, Receive and Transmit
        String header = output.get(1);
        String[] categories = header.split("\\|"); // Split them into their three respective categories
        if (categories.length != 3) {
            // Unknown format. Most linux distros should be fine though.
            return Collections.emptyMap();
        }

        List<String> rxFields = Arrays.asList(categories[1].trim().split("\\s+"));
        List<String> txFields = Arrays.asList(categories[2].trim().split("\\s+"));

        int rxFieldsLength = rxFields.size();
        int txFieldsLength = txFields.size();

        int fieldRxBytes = rxFields.indexOf("bytes");
        int fieldRxPackets = rxFields.indexOf("packets");
        int fieldRxErrors = rxFields.indexOf("errs");

        int fieldTxBytes = rxFieldsLength + txFields.indexOf("bytes");
        int fieldTxPackets = rxFieldsLength + txFields.indexOf("packets");
        int fieldTxErrors = rxFieldsLength + txFields.indexOf("errs");

        int expectedFieldsLength = rxFieldsLength + txFieldsLength;

        if (IntStream.of(fieldRxBytes, fieldRxPackets, fieldRxErrors, fieldTxBytes, fieldTxPackets, fieldTxErrors).anyMatch(i -> i == -1)) {
            // We are missing required fields therefore we return an empty map.
            return Collections.emptyMap();
        }

        ImmutableMap.Builder<String, NetworkInterfaceData> builder = ImmutableMap.builder();

        for (String line : output.subList(2, output.size())) {
            Matcher matcher = NETWORK_INTERFACE_PATTERN.matcher(line);
            if (matcher.matches()) {
                String intName = matcher.group(1); // Get the name of the interface.
                String[] stringValues = matcher.group(2).trim().split("\\s+");

                if (stringValues.length != expectedFieldsLength) {
                    continue;
                }

                long[] values = Arrays.stream(stringValues).mapToLong(Long::parseLong).toArray();
                builder.put(intName, new NetworkInterfaceData(
                        intName,
                        values[fieldRxBytes],
                        values[fieldRxPackets],
                        values[fieldRxErrors],
                        values[fieldTxBytes],
                        values[fieldTxPackets],
                        values[fieldTxErrors]
                ));
            }
        }

        return builder.build();
    }

    /**
     * In order to calculate the rate we need to calculate the difference between the current and previous readings.
     *
     * @param current The current polled values. (latest).
     * @param previous The previously polled values
     * @return The difference between the polled values {@link Map<String, NetworkInterfaceData>}
     */
    public static @NonNull Map<String, NetworkInterfaceData> diff(Map<String, NetworkInterfaceData> current, Map<String, NetworkInterfaceData> previous) {
        if (previous == null || previous.isEmpty()) {
            return current;
        }

        ImmutableMap.Builder<String, NetworkInterfaceData> builder = ImmutableMap.builder();
        for (NetworkInterfaceData networkInterface : current.values()) {
            String name = networkInterface.getName(); // Get the name of the interface
            builder.put(name, networkInterface.sub(previous.getOrDefault(name, EMPTY)));
        }
        return builder.build();
    }


    public NetworkInterfaceData sub(NetworkInterfaceData other) {
        if (other == EMPTY || other.isZero()) {
            return this;
        }

        return new NetworkInterfaceData(
                this.name,
                this.rxBytes - other.rxBytes,
                this.rxPackets - other.rxPackets,
                this.rxErrors - other.rxErrors,
                this.txBytes - other.txBytes,
                this.txPackets - other.txPackets,
                this.txErrors - other.txErrors
        );
    }

    /**
     * Check if there is no network data for this network interface.
     *
     * @return True if there is no network data. False if there has been recorded data.
     */
    public boolean isZero() {
        return this.rxBytes == 0 && this.rxPackets == 0 && this.rxErrors == 0 &&
                this.txBytes == 0 && this.txPackets == 0 && this.txErrors == 0;
    }

    public enum Direction{

        RECEIVE("rx"),
        TRANSMIT("tx");

        private final String direction;

        Direction(String direction) {
            this.direction = direction;
        }

        public String getDirection() {
            return this.direction;
        }
    }
}
