package gin.test;

import java.lang.management.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


// After inspecting, /proc/ not available on mac os x environ. Should make mem. profiler cross platform compatible

public class MemoryProfiler implements Runnable {

    /* ======= INSTANTIATE VARS ======= */

    private Thread parentThread;
    private Thread t;
    private final List<Double> samples;
    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();


    public MemoryProfiler(Thread parentThreadGiven){
        parentThread = parentThreadGiven;
        samples = Collections.synchronizedList(new ArrayList<>());
    }

    /* ======== Profiling Functions ======= */

    public synchronized double getAverage(){
        synchronized (samples){

            double sum = 0.0;
            for (double sample : samples){
                sum += sample;
            }
//            return ((samples.stream().mapToDouble(d -> d).average().orElse(0.0)) * 1000);
            return (sum/samples.size());
        }
    }

    public boolean checkProcess(){
        if (!parentThread.isAlive()){
            return false;
        }
        return true;
    }

    public void resetStats() throws InterruptedException {
        synchronized (samples){
            samples.clear();
//            Thread.sleep(10);
        }
    }

    @Override
    public void run() {
        System.out.println("Memory Profiler Called");

        while(checkProcess()){
            System.out.println(memoryMXBean.getHeapMemoryUsage().getUsed());
            samples.add((double) memoryMXBean.getHeapMemoryUsage().getUsed());
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}












