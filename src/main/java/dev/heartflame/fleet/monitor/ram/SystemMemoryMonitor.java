package dev.heartflame.fleet.monitor.ram;

import dev.heartflame.fleet.monitor.LinuxProc;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public enum SystemMemoryMonitor {;

    private static final String OS_OBJECT_NAME = "java.lang:type=OperatingSystem";
    private static final OperatingSystemMXBean BEAN;

    private static final Pattern MEMINFO_PROC_REGEX = Pattern.compile("^(\\w+):\\s*(\\d+) kB$");

    static {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName beanName = ObjectName.getInstance(OS_OBJECT_NAME);
            BEAN = JMX.newMXBeanProxy(server, beanName, OperatingSystemMXBean.class);
        } catch (Exception error) {
            throw new UnsupportedOperationException("OperatingSystemMXBean class is not supported by the system.", error);
        }
    }

    public static long getUsedMemory() {
        return getTotalMemory() - getAvailableMemory();
    }

    public static long getTotalMemory() {
        // Read the "/proc/meminfo" file and loop through each line.
        for (String line : LinuxProc.MEMORY.readFile()) {
            Matcher matcher = MEMINFO_PROC_REGEX.matcher(line); // Attempt to match

            if (matcher.matches()) {
                String label = matcher.group(1);
                long val = Long.parseLong(matcher.group(2)) * 1024; // Turn kilobyte into bytes;

                if (label.equals("MemTotal")) {
                    return val;
                }
            }
        }

        // Fallback to JVM Measure.
        return BEAN.getTotalPhysicalMemorySize();
    }

    public static long getAvailableMemory() {
        boolean present = false;
        long buffers = 0, cached = 0, free = 0, reclaimable = 0;

        for (String line : LinuxProc.MEMORY.readFile()) {
            Matcher matcher = MEMINFO_PROC_REGEX.matcher(line);

            if (matcher.matches()) {
                present = true;

                String label = matcher.group(1);
                long value = Long.parseLong(matcher.group(2)) * 1024; // kilobytes into bytes

                if (label.equals("MemAvailable")) { return value; }

                switch (label) {

                    case "MemFree":
                        free = value;
                        break;
                    case "Buffers":
                        buffers = value;
                        break;
                    case "Cached":
                        cached = value;
                    case "sReclaimable":
                        reclaimable = value;
                        break;

                }
            }
        }

        if (present) {
            return free + buffers + cached + reclaimable;
        }

        return BEAN.getFreePhysicalMemorySize();
    }

    public interface OperatingSystemMXBean {
        long getCommittedVirtualMemorySize();
        long getTotalSwapSpaceSize();
        long getFreeSwapSpaceSize();
        long getFreePhysicalMemorySize();
        long getTotalPhysicalMemorySize();
    }

}
