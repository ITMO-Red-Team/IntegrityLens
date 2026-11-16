import java.io.*;
import java.sql.SQLOutput;
import java.util.*;

public class airconditioner {
    static class FastReader {
        BufferedReader br;
        StringTokenizer st;

        public FastReader() throws java.io.IOException {
            br = new BufferedReader(
                    new InputStreamReader(System.in));
        }

        String next() {
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt() {
            return Integer.parseInt(next());
        }

        long nextLong() {
            return Long.parseLong(next());
        }

        double nextDouble() {
            return Double.parseDouble(next());
        }

        String nextLine() {
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
    }

    public static class compare implements Comparator<int[]> {
        public int compare(int[] o1, int[] o2) {
            return o1[0] - o2[0];
        }
    }

    public static void main(String[] args) throws IOException {
        FastReader input = new FastReader();
        int testcases = input.nextInt();
        for (int t = 0; t < testcases; t++) {
            int length = input.nextInt();
            int conditioners = input.nextInt();
            int[] array = new int[length];
            int[][] conpositions = new int[conditioners][2]; //0 = position, 1 = value
            for (int i = 0; i < length; i++) {
                array[i] = 2000000069;
            }
            for (int i = 0; i < conditioners; i++) {
                conpositions[i][0] = input.nextInt() - 1;
            }
            for (int i = 0; i < conditioners; i++) {
                conpositions[i][1] = input.nextInt();
                array[conpositions[i][0]] = conpositions[i][1];
            }
            Arrays.sort(conpositions, new compare());

            int position = conditioners - 2;
            int counter = conpositions[conditioners - 1][1];
            for (int i = conpositions[conditioners - 1][0]; i >= 0; i--) { //i = arrayposition
                array[i] = counter;
                if (position >= 0 && i == conpositions[position][0]) {
                    if (conpositions[position][1] < counter) {
                        counter = conpositions[position][1];
                        array[i] = counter;
                    }
                    position--;
                }
                counter++;
            }

            position = 1;
            counter = conpositions[0][1];
            for (int i = conpositions[0][0]; i < length; i++) {
                if (counter < array[i]) {
                    array[i] = counter;
                }
                if (position < conditioners && i == conpositions[position][0]) {
                    if (conpositions[position][1] < counter) {
                        counter = conpositions[position][1];
                        if (counter < array[i]) {
                            array[i] = counter;
                        }
                    }
                    position++;
                }
                counter++;
            }
            for (int i = 0; i < length; i++) {
                System.out.print(array[i] + " ");
            }
            System.out.println();
        }
    }
}