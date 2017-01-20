package sun.jvm.hotspot.oops;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * just for survivor objects
 */
public class TBSurvivorObjectHistogram implements HeapVisitor {

    private static final int MAX_THRESHOLD = 16;

    private final List<Map<Klass, TBSurvivorObjectHistogramElem>> ageArr;

    public TBSurvivorObjectHistogram() {
        ageArr = new ArrayList<Map<Klass, TBSurvivorObjectHistogramElem>>(MAX_THRESHOLD);
        for (int i = 0; i < MAX_THRESHOLD; i++) {
            ageArr.add(new HashMap<Klass, TBSurvivorObjectHistogramElem>());
        }
    }

    @Override
    public void prologue(long usedSize) {
    }

    @Override
    public void epilogue() {
    }

    @Override
    public boolean doObj(Oop obj) {
        int age = obj.getMark().age();
        Klass klass = obj.getKlass();
        Map<Klass, TBSurvivorObjectHistogramElem> map = ageArr.get(age);
        if (!map.containsKey(klass)) {
            map.put(klass, new TBSurvivorObjectHistogramElem(klass));
        }
        map.get(klass).updateWith(obj);
        return false;
    }

    /**
     * Call this after the iteration is complete to obtain the
     * ObjectHistogramElements in descending order of total heap size consumed
     * in the form of a {@code List<List<TBSurvivorObjectHistogramElem>> }.
     */
    private List<List<TBSurvivorObjectHistogramElem>> getElements() {
        List<List<TBSurvivorObjectHistogramElem>> list = new ArrayList<>(ageArr.size());
        for (Map<Klass, TBSurvivorObjectHistogramElem> map : ageArr) {
            List<TBSurvivorObjectHistogramElem> subList = new ArrayList<>(map.values().size());
            subList.addAll(map.values());
            Collections.sort(subList);
            list.add(subList);
        }
        return list;
    }

    public void printOn(PrintStream tty) {
        tty.println("Survivor object Histogram:");
        tty.println();
        List<List<TBSurvivorObjectHistogramElem>> list = getElements();

        int surCount = 0;
        long surSize = 0;
        for (int i = 0; i < list.size(); i++) {
            List<TBSurvivorObjectHistogramElem> ageList = list.get(i);
            // No print if empty
            if (ageList.isEmpty()) {
                continue;
            }
            TBSurvivorObjectHistogramElem.titleOn(tty, i);
            int num = 0;
            int totalCount = 0;
            long totalSize = 0;
            for (TBSurvivorObjectHistogramElem elem : ageList) {
                num++;
                totalCount += elem.getCount();
                totalSize += elem.getSize();
                elem.printOn(tty, num);
            }
            tty.println(String.format("Age-%d-summary: %12d bytes, %10d total", i, totalSize, totalCount));
            tty.println();
            surCount += totalCount;
            surSize += totalSize;
        }
        tty.println(String.format("Survivor-summary: %12d bytes, %10d total", surSize, surCount));
    }

}
