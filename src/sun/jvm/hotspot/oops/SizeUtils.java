package sun.jvm.hotspot.oops;

/**
 * simple tool for size only
 */
final class SizeUtils {

    private static final float k = 1024;
    private static final float m = k * 1024;
    private static final float g = m * 1024;

    private SizeUtils() {
        // avoid create
    }

    /**
     * Convert size to human readable format. Not format here, just return
     * adjusted size and unit (eg. [18.235, "KB"]
     * 
     * @param size
     *            original size in B
     * @return [size by float, unit by String]
     */
    static Object[] toHumanReadable(float size) {
        Object[] arr = new Object[2];
        final float humanSize;
        String unit;
        if (size < k) {
            unit = "B";
            humanSize = size;
        } else if (size < m) {
            unit = "KB";
            humanSize = size / k;
        } else if (size < g) {
            unit = "MB";
            humanSize = size / m;
        } else {
            unit = "GB";
            humanSize = size / g;
        }
        arr[0] = humanSize;
        arr[1] = unit;
        return arr;
    }

}
