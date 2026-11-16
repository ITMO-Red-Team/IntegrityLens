import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;

class Sorting {
    static void bubbleSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (a[j] > a[j + 1]) {
                    int temp = a[j];
                    a[j] = a[j + 1];
                    a[j + 1] = temp;
                }
            }
        }
    }

    static void selectionSort(int[] a) {
        int n = a.length;
        for (int i = 0; i < n - 1; i++) {
            int min = i;
            for (int j = i + 1; j < n; j++) {
                if (a[j] < a[min]) {
                    min = j;
                }
            }
            int temp = a[min];
            a[min] = a[i];
            a[i] = temp;
        }
    }

    static void insertionSort(int[] a) {
        for (int i = 1; i < a.length; i++) {
            int key = a[i];
            int j = i - 1;
            while (j >= 0 && a[j] > key) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = key;
        }
    }

    static void mergeSort(int[] a, int left, int right) {
        if (left < right) {
            int mid = left + (right - left) / 2;
            mergeSort(a, left, mid);
            mergeSort(a, mid + 1, right);
            merge(a, left, mid, right);
        }
    }

    static void merge(int[] a, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        int[] l = new int[n1];
        int[] r = new int[n2];
        for (int i = 0; i < n1; i++) {
            l[i] = a[left + i];
        }
        for (int j = 0; j < n2; j++) {
            r[j] = a[mid + 1 + j];
        }
        int i = 0;
        int j = 0;
        int k = left;
        while (i < n1 && j < n2) {
            if (l[i] <= r[j]) {
                a[k] = l[i];
                i++;
            } else {
                a[k] = r[j];
                j++;
            }
            k++;
        }
        while (i < n1) {
            a[k] = l[i];
            i++;
            k++;
        }
        while (j < n2) {
            a[k] = r[j];
            j++;
            k++;
        }
    }

    static void mergeWithBuffer(int[] a, int[] buffer, int left, int mid, int right) {
        int i = left;
        int j = mid;
        int k = left;
        while (i < mid && j < right) {
            buffer[k++] = a[i] <= a[j] ? a[i++] : a[j++];
        }
        while (i < mid) {
            buffer[k++] = a[i++];
        }
        while (j < right) {
            buffer[k++] = a[j++];
        }
        for (k = left; k < right; k++) {
            a[k] = buffer[k];
        }
    }

    static void quickSort(int[] a, int low, int high) {
        if (low < high) {
            int p = partition(a, low, high);
            quickSort(a, low, p - 1);
            quickSort(a, p + 1, high);
        }
    }

    static int partition(int[] a, int low, int high) {
        int pivot = a[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (a[j] < pivot) {
                i++;
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
            }
        }
        int temp = a[i + 1];
        a[i + 1] = a[high];
        a[high] = temp;
        return i + 1;
    }

    static void quickSortHoare(int[] a, int left, int right) {
        int i = left;
        int j = right;
        int pivot = a[(left + right) / 2];
        while (i <= j) {
            while (a[i] < pivot) {
                i++;
            }
            while (a[j] > pivot) {
                j--;
            }
            if (i <= j) {
                int temp = a[i];
                a[i] = a[j];
                a[j] = temp;
                i++;
                j--;
            }
        }
        if (left < j) {
            quickSortHoare(a, left, j);
        }
        if (i < right) {
            quickSortHoare(a, i, right);
        }
    }

    static void heapSort(int[] a) {
        int n = a.length;
        for (int i = n / 2 - 1; i >= 0; i--) {
            heapify(a, n, i);
        }
        for (int i = n - 1; i > 0; i--) {
            int temp = a[0];
            a[0] = a[i];
            a[i] = temp;
            heapify(a, i, 0);
        }
    }

    static void heapify(int[] a, int n, int i) {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;
        if (l < n && a[l] > a[largest]) {
            largest = l;
        }
        if (r < n && a[r] > a[largest]) {
            largest = r;
        }
        if (largest != i) {
            int swap = a[i];
            a[i] = a[largest];
            a[largest] = swap;
            heapify(a, n, largest);
        }
    }

    static void countingSort(int[] a, int max) {
        int[] count = new int[max + 1];
        for (int x : a) {
            count[x]++;
        }
        int k = 0;
        for (int i = 0; i <= max; i++) {
            while (count[i] > 0) {
                a[k++] = i;
                count[i]--;
            }
        }
    }

    static void sortViaList(int[] a) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int x : a) {
            list.add(x);
        }
        Collections.sort(list);
        for (int i = 0; i < a.length; i++) {
            a[i] = list.get(i);
        }
    }

    static void sortShuffled(int[] a) {
        Random random = new Random();
        for (int i = 0; i < a.length; i++) {
            int r = random.nextInt(a.length);
            int temp = a[i];
            a[i] = a[r];
            a[r] = temp;
        }
        Arrays.sort(a);
    }

    static void mergeRanges(int[] a, int l1, int r1, int l2, int r2) {
        int[] temp = new int[r2 - l1 + 1];
        int first = l1;
        int second = l2;
        for (int i = 0; i < temp.length; i++) {
            if (first > r1) {
                temp[i] = a[second];
                second++;
                continue;
            }
            if (second > r2) {
                temp[i] = a[first];
                first++;
                continue;
            }
            if (a[first] < a[second]) {
                temp[i] = a[first];
                first++;
            } else {
                temp[i] = a[second];
                second++;
            }
        }
        for (int i = 0; i < temp.length; i++) {
            a[l1 + i] = temp[i];
        }
    }

    static void mergeSortRanges(int[] a, int l, int r) {
        if (l == r) {
            return;
        }
        int mid = (l + r) / 2;
        mergeSortRanges(a, l, mid);
        mergeSortRanges(a, mid + 1, r);
        mergeRanges(a, l, mid, mid + 1, r);
    }

    static void shuffle(int[] a) {
        Random random = new Random();
        for (int i = a.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
        }
    }

    static void swap(int[] a, int i, int j) {
        int temp = a[i];
        a[i] = a[j];
        a[j] = temp;
    }

    static void reverseHalf(int[] a) {
        int n = a.length;
        for (int i = 0; i < n / 2; i++) {
            int t = a[i];
            a[i] = a[n - i - 1];
            a[n - i - 1] = t;
        }
    }

    static void reverseViaCopy(int[] a) {
        int n = a.length;
        int[] copy = new int[n];
        for (int i = 0; i < n; i++) {
            copy[i] = a[n - i - 1];
        }
        for (int i = 0; i < n; i++) {
            a[i] = copy[i];
        }
    }

    static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1]) {
                return false;
            }
        }
        return true;
    }

    static void reverse(int[] a) {
        int i = 0;
        int j = a.length - 1;
        while (i < j) {
            int temp = a[i];
            a[i] = a[j];
            a[j] = temp;
            i++;
            j--;
        }
    }
}
