package sun.jvm.hotspot.oops;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import sun.jvm.hotspot.runtime.VM;

// see sun.jvm.hotspot.oops.ObjectHistogramElement
public class TBSurvivorObjectHistogramElem implements Comparable<TBSurvivorObjectHistogramElem> {

    private final Klass klass;
    private long count; // Number of instances of klass
    private long size; // Total size of all these instances

    public TBSurvivorObjectHistogramElem(Klass klass) {
        this.klass = klass;
    }

    public void updateWith(Oop obj) {
        count = count + 1;
        size = size + obj.getObjectSize();
    }

    @Override
    public int compareTo(TBSurvivorObjectHistogramElem o) {
        return Long.compare(o.size, this.size);
    }

    /** Klass for this ObjectHistogramElement */
    public Klass getKlass() {
        return klass;
    }

    /** Number of instances of klass */
    public long getCount() {
        return count;
    }

    /** Total size of all these instances */
    public long getSize() {
        return size;
    }

    private String getInternalName(Klass k) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        getKlass().printValueOn(new PrintStream(bos));
        // '*' is used to denote VM internal klasses.
        return "* " + bos.toString();
    }

    /** Human readable description **/
    public String getDescription() {
        Klass k = getKlass();
        if (k instanceof InstanceKlass) {
            return k.getName().asString().replace('/', '.');
        } else if (k instanceof ArrayKlass) {
            ArrayKlass ak = (ArrayKlass) k;
            if (k instanceof TypeArrayKlass) {
                TypeArrayKlass tak = (TypeArrayKlass) ak;
                return tak.getElementTypeName() + "[]";
            } else if (k instanceof ObjArrayKlass) {
                ObjArrayKlass oak = (ObjArrayKlass) ak;
                // See whether it's a "system objArray"
                if (oak.equals(VM.getVM().getUniverse().systemObjArrayKlassObj())) {
                    return "* System ObjArray";
                }
                Klass bottom = oak.getBottomKlass();
                int dim = (int) oak.getDimension();
                StringBuffer buf = new StringBuffer();
                if (bottom instanceof TypeArrayKlass) {
                    buf.append(((TypeArrayKlass) bottom).getElementTypeName());
                } else if (bottom instanceof InstanceKlass) {
                    buf.append(bottom.getName().asString().replace('/', '.'));
                } else {
                    throw new RuntimeException("should not reach here");
                }
                for (int i = 0; i < dim; i++) {
                    buf.append("[]");
                }
                return buf.toString();
            }
        }
        return getInternalName(k);
    }

    public static void titleOn(PrintStream tty, int age) {
        tty.println("- age " + age + ":");
        tty.println("#num " + "\t" + "#bytes" + "\t" + "  #instances" + "\t" + "Class description");
        tty.println("--------------------------------------------------------------------------");
    }

    public void printOn(PrintStream tty, int num) {
        final String fmt = "%6d: %8.2f %2s %8d total\t%s";
        Object[] sizeArr = SizeUtils.toHumanReadable(size);
        tty.println(String.format(fmt, num, sizeArr[0], sizeArr[1], count, getDescription()));
    }

}
