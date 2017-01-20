package sun.jvm.hotspot.tools;

import java.io.PrintStream;

import sun.jvm.hotspot.gc_implementation.parallelScavenge.PSYoungGen;
import sun.jvm.hotspot.gc_implementation.parallelScavenge.ParallelScavengeHeap;
import sun.jvm.hotspot.gc_interface.CollectedHeap;
import sun.jvm.hotspot.memory.DefNewGeneration;
import sun.jvm.hotspot.memory.GenCollectedHeap;
import sun.jvm.hotspot.oops.ObjectHeap;
import sun.jvm.hotspot.oops.Oop;
import sun.jvm.hotspot.oops.TBObjectHistogram;
import sun.jvm.hotspot.oops.TBSurvivorObjectHistogram;
import sun.jvm.hotspot.runtime.VM;

/**
 * A sample tool which uses the Serviceability Agent's APIs to obtain an object
 * histogram from a remote or crashed VM.
 */
public class TBObjectHistogramTool extends Tool {

    public void run() {
        run(System.out, System.err);
    }

    public void run(PrintStream out, PrintStream err) {
        // Ready to go with the database...
        ObjectHeap heap = VM.getVM().getObjectHeap();
        TBObjectHistogram histogram = new TBObjectHistogram();
        err.println("Iterating over heap. This may take a while...");
        long startTime = System.currentTimeMillis();
        heap.iterate(histogram);
        long endTime = System.currentTimeMillis();
        histogram.printOn(out);
        float secs = (float) (endTime - startTime) / 1000.0f;
        err.println("Heap traversal took " + secs + " seconds.");
    }

    public void printOld() {
        PrintStream out = System.out;
        PrintStream err = System.err;
        // Ready to go with the database...
        ObjectHeap heap = VM.getVM().getObjectHeap();
        TBObjectHistogram histogram = new TBObjectHistogram();
        err.println("Iterating over heap. This may take a while...");
        long startTime = System.currentTimeMillis();
        heap.iterate(histogram);
        long endTime = System.currentTimeMillis();
        histogram.printOnOld(out);
        float secs = (float) (endTime - startTime) / 1000.0f;
        err.println("Heap traversal took " + secs + " seconds.");
    }

    public void printPerm() {
        PrintStream out = System.out;
        PrintStream err = System.err;
        // Ready to go with the database...
        ObjectHeap heap = VM.getVM().getObjectHeap();
        TBObjectHistogram histogram = new TBObjectHistogram();
        err.println("Iterating over heap. This may take a while...");
        long startTime = System.currentTimeMillis();
        heap.iterate(histogram);
        long endTime = System.currentTimeMillis();
        histogram.printOnPerm(out);
        float secs = (float) (endTime - startTime) / 1000.0f;
        err.println("Heap traversal took " + secs + " seconds.");
    }
    
    public void printSurvivor() {
        PrintStream out = System.out;
        PrintStream err = System.err;
        // Ready to go with the database...
        ObjectHeap heap = VM.getVM().getObjectHeap();
        TBSurvivorObjectHistogram histogram = new TBSurvivorObjectHistogram();
        err.println("Iterating over heap. This may take a while...");
        long startTime = System.currentTimeMillis();
        // filter not in survivor.from
        heap.iterate(histogram, new ObjectHeap.ObjectFilter() {
            @Override
            public boolean canInclude(Oop obj) {
                CollectedHeap cHeap = VM.getVM().getUniverse().heap();
                if (cHeap instanceof GenCollectedHeap) {
                    GenCollectedHeap genHeap = (GenCollectedHeap) cHeap;
                    DefNewGeneration newGen = (DefNewGeneration) genHeap.getGen(0);
                    return newGen.from().contains(obj.getHandle());
                } else if (cHeap instanceof ParallelScavengeHeap) {
                    ParallelScavengeHeap psh = (ParallelScavengeHeap) cHeap;
                    PSYoungGen youngGen = psh.youngGen();
                    return youngGen.fromSpace().contains(obj.getHandle());
                } else {
                    throw new RuntimeException("unknown heap type : " + cHeap.getClass());
                }
            }
        });
        long endTime = System.currentTimeMillis();
        histogram.printOn(out);
        float secs = (float) (endTime - startTime) / 1000.0f;
        err.println("Heap traversal took " + secs + " seconds.");
    }

    public static void main(String[] args) {
        TBObjectHistogramTool oh = new TBObjectHistogramTool();
        oh.start(args);
        oh.stop();
    }
}
