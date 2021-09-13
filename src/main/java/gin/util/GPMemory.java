package gin.util;

import java.io.*;
import java.util.List;

import gin.Patch;
import gin.test.UnitTest;
import gin.test.UnitTestResultSet;

public class GPMemory extends GPSimple {

//    MemoryProfiler2 memoryProfiler2;
//
//    public MemoryProfiler2 getMemoryProfiler(){
//        return memoryProfiler2;
//    }

    public static void main(String[] args) throws IOException, InterruptedException {
        GPMemory sampler = new GPMemory(args);
        sampler.sampleMethods();
    }

    public GPMemory(String[] args) throws IOException, InterruptedException {
        super(args);

//        Thread main_thread = Thread.currentThread();
//        MemoryProfiler memoryProfiler = new MemoryProfiler(main_thread);
//        Thread memoryProfilerThread = new Thread(memoryProfiler);
//        memoryProfilerThread.start();
    }

    /* ===== Implementing Abstract Methods ===== */


    protected UnitTestResultSet initFitness(String className, List<UnitTest> tests, Patch origPatch) {
        UnitTestResultSet results = testPatch(className, tests, origPatch);
        return results;
    }

    protected double fitness(UnitTestResultSet results) {

        double fitness = Double.MAX_VALUE;

        // IF COMPILED AND TEST SUITE SUCCESS THEN
        // return weighted fitness for memory usage
        if (results.getCleanCompile() && results.allTestsSuccessful()) {
            System.out.println((results.totalMemoryUsage() / 1000000));
            return (double) (results.totalMemoryUsage() / 1000000);
        }
        // else return default max value
        return fitness;
    }

    protected boolean fitnessThreshold(UnitTestResultSet results, double orig) {

        return results.allTestsSuccessful();
    }

    protected double compareFitness(double newFitness, double oldFitness) {

        return oldFitness - newFitness;
    }
}

