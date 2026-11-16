//Created by Aminul on 7/23/2021.

import java.io.*;
import java.util.*;

import static java.lang.Math.*;

public class C {
    public static void main(String[] args) throws Exception {
        InputReader in = new InputReader(System.in);
        PrintWriter pw = new PrintWriter(System.out);
        char[] keys = {'a', 'b', 'c', 'd', 'e'};
        int test = in.nextInt();
        for (int t = 1; t <= test; t++) {
            int n = in.nextInt();
            String[] words = new String[n];
            for (int i = 0; i < n; i++) {
                words[i] = in.next();
            }

            int res = 0;
            for (char c : keys) {
                res = max(res, getMaxCount(n, words, c));
            }
            pw.println(res);
        }

        pw.close();
    }

    static int getMaxCount(int n, String[] words, char c) {
        Integer[] arr = new Integer[n];
        for (int i = 0; i < n; i++) {
            arr[i] = 2 * getCount(words[i], c) - words[i].length();
        }
        Arrays.sort(arr, Comparator.reverseOrder());
        int sum = 0, count = 0;
        for (int i : arr) {
            sum += i;
            if (sum <= 0) {
                break;
            }
            count++;
        }
        return count;
    }

    static int getCount(String s, char c) {
        int count = 0;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == c) {
                count++;
            }
        }
        return count;
    }

    static void debug(Object... obj) {
        System.err.println(Arrays.deepToString(obj));
    }

    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream));
            tokenizer = null;
        }

        public String next() throws Exception {
            while (tokenizer == null || !tokenizer.hasMoreTokens())
                tokenizer = new StringTokenizer(reader.readLine());
            return tokenizer.nextToken();
        }

        public int nextInt() throws Exception {
            return Integer.parseInt(next());
        }
    }
}