import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

class Helpers {
    static void printArray(int[] a) {
        for (int x : a) {
            System.out.print(x + " ");
        }
        System.out.println();
    }

    static void printIndexed(int[] a) {
        for (int i = 0; i < a.length; i++) {
            System.out.print(a[i] + " ");
        }
        System.out.println();
    }

    static void printYesNo(boolean condition) {
        if (condition) {
            System.out.println("YES");
        } else {
            System.out.println("NO");
        }
    }

    static void debug(Object... values) {
        System.out.println(Arrays.deepToString(values));
    }

    static void debugArray(int[] a) {
        System.out.print("Array: ");
        System.out.println(Arrays.toString(a));
    }

    static long sum(int[] a) {
        long sum = 0;
        for (int x : a) {
            sum += x;
        }
        return sum;
    }

    static long sumIndexed(int[] a) {
        int n = a.length;
        long sum = 0;
        for (int i = 0; i < n; i++) {
            sum += a[i];
        }
        return sum;
    }

    static int max(int[] a) {
        int max = Integer.MIN_VALUE;
        for (int x : a) {
            max = Math.max(x, max);
        }
        return max;
    }

    static int maxIndexed(int[] a) {
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < a.length; i++) {
            max = Math.max(max, a[i]);
        }
        return max;
    }

    static int max(int a, int b, int c, int d) {
        a = Math.max(a, b);
        c = Math.max(c, d);
        return Math.max(a, c);
    }

    static int min(int[] a) {
        int min = Integer.MAX_VALUE;
        for (int x : a) {
            min = Math.min(x, min);
        }
        return min;
    }

    static Map<Integer, Integer> frequencies(int[] a) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x : a) {
            if (counts.containsKey(x)) {
                counts.put(x, counts.get(x) + 1);
            } else {
                counts.put(x, 1);
            }
        }
        return counts;
    }

    static Map<Integer, Integer> frequenciesMerged(int[] a) {
        Map<Integer, Integer> counts = new HashMap<>();
        for (int x : a) {
            counts.put(x, counts.getOrDefault(x, 0) + 1);
        }
        return counts;
    }

    static void fill(int[][] table, int value) {
        for (int i = 0; i < table.length; i++) {
            Arrays.fill(table[i], value);
        }
    }

    static String reversed(String s) {
        StringBuilder result = new StringBuilder(s);
        result.reverse();
        return result.toString();
    }
}
