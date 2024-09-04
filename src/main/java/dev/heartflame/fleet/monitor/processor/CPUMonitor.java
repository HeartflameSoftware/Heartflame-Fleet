package dev.heartflame.fleet.monitor.processor;

import dev.heartflame.fleet.data.RollingAvgData;
import dev.heartflame.fleet.monitor.SystemMonitorExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.JMX;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

public enum CPUMonitor {;

    private static final Logger log = LoggerFactory.getLogger("Bot Actions");
    private static final String OS_OBJECT_NAME = "java.lang:type=OperatingSystem";
    private static final OperatingSystemMXBean BEAN;

    private static final RollingAvgData CPU_AVERAGE_10s = new RollingAvgData(10);
    private static final RollingAvgData CPU_AVERAGE_1m = new RollingAvgData(60);
    private static final RollingAvgData CPU_AVERAGE_15m = new RollingAvgData(60 * 15);

    static {
        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer();
            ObjectName beanName = ObjectName.getInstance(OS_OBJECT_NAME);
            BEAN = JMX.newMXBeanProxy(server, beanName, OperatingSystemMXBean.class);
        } catch (Exception error) {
            throw new UnsupportedOperationException("OperatingSystemMXBean class is not supported by the system.", error);
        }

        SystemMonitorExecutor.EXECUTOR.scheduleAtFixedRate(new PolledProcessorDataCollectionTask(), 1, 1, TimeUnit.SECONDS);
    }

    @SuppressWarnings("EmptyMethod")
    public static void enableMonitoring() {
        // purposefully empty
        log.info("Initialising CPU Monitor.");
    }

    public static double processorLoad() {
        return BEAN.getSystemCpuLoad();
    }

    public static double processorLoad10sAvg() {
        return CPU_AVERAGE_10s.mean();
    }

    public static double processorLoad1mAvg() {
        return CPU_AVERAGE_1m.mean();
    }

    public static double processorLoad15mAvg() {
        return CPU_AVERAGE_15m.mean();
    }

    // -------------------- POLLING -------------------- //

    private static final class PolledProcessorDataCollectionTask implements Runnable {
        private final RollingAvgData[] processorAvgs = new RollingAvgData[]{
        CPU_AVERAGE_10s,
        CPU_AVERAGE_1m,
        CPU_AVERAGE_15m
        };

        @Override
        public void run() {
            BigDecimal processorLoad = new BigDecimal(processorLoad());

            if (processorLoad.signum() != -1) {
                for (RollingAvgData avg : this.processorAvgs) {
                    avg.add(processorLoad);
                }
            }
        }
    }

    // -------------------- MISC -------------------- //

    public interface OperatingSystemMXBean {
        double getSystemCpuLoad();
    }

}
