package gin.test;

import java.io.*;
import java.lang.management.*;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.pmw.tinylog.Logger;

/**
 * Saves result of a UnitTest run into UnitTestResult.
 * assumes one test case is run through JUnitCore at a time
 * ignored tests and tests with assumption violations are considered successful (following JUnit standard)
 */

public class TestRunListener extends RunListener {

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private final UnitTestResult unitTestResult;

    private long startTime = 0;

    private long startCPUTime = 0;

    private long startMemoryUsage = 0;

    public volatile boolean running;

    private static final long MEGABYTE = 1024L * 1024L;

    private MemoryProfiler memoryProfiler;

    public static long bytesToMegabytes(long bytes) {
        return bytes / MEGABYTE;
    }

    private long getProcessID() {
        // Get name representing the running Java virtual machine.
        // It returns something like 6460@AURORA. Where the value
        // before the @ symbol is the PID.
        String jvmName = ManagementFactory.getRuntimeMXBean().getName();
        // Extract the PID by splitting the string returned by the
        // bean.getName() method.
        long pid = Long.valueOf(jvmName.split("@")[0]);
        return pid;
    }

    public TestRunListener(UnitTestResult unitTestResult) throws IOException {
        this.unitTestResult = unitTestResult;
//        this.memoryProfiler = memoryProfiler;
//        System.out.println(memoryProfiler.getAverage());


        // get memory profiler object here (passed into TestRunListener)

    }

    public void testAssumptionFailure(Failure failure) {
        Logger.debug("Test " + failure.getTestHeader() + " violated an assumption. Skipped.");
        unitTestResult.addFailure(failure);
    }

    public void testFailure(Failure failure) throws Exception {
        Logger.debug("Test " + failure.getTestHeader() + " produced a failure.");
        unitTestResult.addFailure(failure);
    }

    public void testFinished(Description description) throws Exception {
        Logger.debug("Test " + description + " finished.");

        long endTime = System.nanoTime();
        long endCPUTime = threadMXBean.getCurrentThreadCpuTime();
        long endMemoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long averageUsage = ((endMemoryUsage + startMemoryUsage) / 2);

        unitTestResult.setExecutionTime(endTime - startTime);
        unitTestResult.setCPUTime(endCPUTime - startCPUTime);
        unitTestResult.setMemoryUsage(bytesToMegabytes(averageUsage));
//        double profilerUsage = memoryProfiler.getAverage();
//        if (Double.isNaN(profilerUsage)){
//            unitTestResult.setMemoryUsage(averageUsage);
//        } else {
//            unitTestResult.setMemoryUsage((long) profilerUsage);
//        }
//
//        System.out.printf("Average: %f", memoryProfiler.getAverage());
//        System.out.println(Thread.activeCount());
//        memoryProfiler.resetStats();

        // get average from all samples from memory profiler here
    }




    public void testIgnored(Description description) throws Exception {
        Logger.debug("Test " + description + " ignored.");
    }

    public void testRunFinished(Result result) throws Exception {
        if (result.wasSuccessful()) {
            unitTestResult.setPassed(true);
        }
    }

    public void testRunStarted(Description description) throws Exception {
        assert (description.testCount() == 1);
    }

    public void testStarted(Description description) throws Exception {
        Logger.debug("Test " + description + " started.");
        this.startTime = System.nanoTime();
        this.startCPUTime = threadMXBean.getCurrentThreadCpuTime();
        this.startMemoryUsage = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        // Reset Stats Here



    }
}
