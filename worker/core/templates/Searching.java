import java.util.List;

class Searching {
    static int binarySearch(int[] a, int target) {
        int low = 0;
        int high = a.length - 1;
        while (low <= high) {
            int mid = low + (high - low) / 2;
            if (a[mid] == target) {
                return mid;
            } else if (a[mid] < target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    static int binarySearchRecursive(int[] a, int low, int high, int target) {
        if (low > high) {
            return -1;
        }
        int mid = (low + high) / 2;
        if (a[mid] == target) {
            return mid;
        }
        if (a[mid] > target) {
            return binarySearchRecursive(a, low, mid - 1, target);
        }
        return binarySearchRecursive(a, mid + 1, high, target);
    }

    static int lowerBound(int[] a, int target) {
        int low = 0;
        int high = a.length;
        while (low < high) {
            int mid = (low + high) / 2;
            if (a[mid] < target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    static int upperBound(int[] a, int target) {
        int low = 0;
        int high = a.length;
        while (low < high) {
            int mid = (low + high) / 2;
            if (a[mid] <= target) {
                low = mid + 1;
            } else {
                high = mid;
            }
        }
        return low;
    }

    static int lowerBoundOpen(int[] a, int target) {
        int l = -1;
        int r = a.length;
        while (l + 1 < r) {
            int m = (l + r) >>> 1;
            if (a[m] >= target) {
                r = m;
            } else {
                l = m;
            }
        }
        return r;
    }

    static int upperBoundList(List<Integer> list, int target) {
        int l = -1;
        int r = list.size();
        while (r - l > 1) {
            int m = (l + r) / 2;
            if (list.get(m) > target) {
                r = m;
            } else {
                l = m;
            }
        }
        return r;
    }

    static int lastNotGreater(List<Integer> list, int n, int target) {
        int low = 0;
        int high = n - 1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (list.get(mid) <= target) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return high;
    }

    static int lowerBoundRecursive(int[] a, int low, int high, int key) {
        if (low > high) {
            return low;
        }
        int mid = low + (high - low) / 2;
        if (key <= a[mid]) {
            return lowerBoundRecursive(a, low, mid - 1, key);
        }
        return lowerBoundRecursive(a, mid + 1, high, key);
    }

    static int firstOccurrence(int[] a, int target) {
        int low = 0;
        int high = a.length - 1;
        int answer = -1;
        while (low <= high) {
            int mid = (low + high) / 2;
            if (a[mid] >= target) {
                if (a[mid] == target) {
                    answer = mid;
                }
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return answer;
    }

    static long searchOnAnswer(long low, long high) {
        long answer = high;
        while (low <= high) {
            long mid = low + (high - low) / 2;
            if (check(mid)) {
                answer = mid;
                high = mid - 1;
            } else {
                low = mid + 1;
            }
        }
        return answer;
    }

    static boolean check(long value) {
        return value >= 0;
    }

    static double ternarySearch(double low, double high) {
        for (int i = 0; i < 100; i++) {
            double m1 = low + (high - low) / 3;
            double m2 = high - (high - low) / 3;
            if (value(m1) < value(m2)) {
                high = m2;
            } else {
                low = m1;
            }
        }
        return low;
    }

    static double value(double x) {
        return x * x;
    }

    static int linearSearch(int[] a, int target) {
        for (int i = 0; i < a.length; i++) {
            if (a[i] == target) {
                return i;
            }
        }
        return -1;
    }
}
