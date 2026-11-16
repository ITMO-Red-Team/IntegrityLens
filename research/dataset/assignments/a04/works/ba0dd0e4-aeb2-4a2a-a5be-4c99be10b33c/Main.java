import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

public class PhoenixAndTowers { // Template for CF
    public static class ListComparator implements Comparator<List<Integer>> {

        @Override
        public int compare(List<Integer> l1, List<Integer> l2) {
            for (int i = 0; i < l1.size(); ++i) {
                if (l1.get(i).compareTo(l2.get(i)) != 0) {
                    return l1.get(i).compareTo(l2.get(i));
                }
            }
            return 0;
        }
    }

    public static class Pair {
        int first;
        int second;

        public Pair(int first, int second) {
            this.first = first;
            this.second = second;
        }

        public int getFirst() {
            return first;
        }

        public int getSecond() {
            return second;
        }

        public void setFirst(int a) {
            first = a;
        }

        @Override
        public String toString() {
            return first + " " + second;
        }
    }

    public static void main(String[] args) throws IOException {
        // Check for int overflow!!!!
        // Should you use a long to store the sum or smthn?
        BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(System.out);

        int T = Integer.parseInt(f.readLine());
        for (int i = 0; i < T; i++) {
            StringTokenizer st = new StringTokenizer(f.readLine());
            int n = Integer.parseInt(st.nextToken());
            int m = Integer.parseInt(st.nextToken());
            int x = Integer.parseInt(st.nextToken());
            st = new StringTokenizer(f.readLine());
            List<Integer> list = new ArrayList<>();
            for (int j = 0; j < n; j++) {
                int a = Integer.parseInt(st.nextToken());
                list.add(a);
            }
            ArrayList<Integer> copy = new ArrayList<>(list);
            Collections.sort(list);
            TreeSet<List<Integer>> set = new TreeSet<>(new ListComparator());
            for (int j = 1; j <= m; j++) {
                List<Integer> temp = new ArrayList<>();
                temp.add(0);
                temp.add(j);

                set.add(temp);
                // System.out.println(temp);
            }
            // System.out.println(set);
            Map<Integer, LinkedList<Integer>> map = new HashMap<>();
            for (int j = n - 1; j >= 0; j--) {
                if (map.containsKey(list.get(j))) {
                    map.get(list.get(j)).addLast(set.first().get(1));
                } else {
                    map.put(list.get(j), new LinkedList<>());
                    map.get(list.get(j)).addLast(set.first().get(1));
                }
                List<Integer> temp = new ArrayList<>();
                temp.add(set.first().get(0) + list.get(j));
                temp.add(set.pollFirst().get(1));
                set.add(temp);
            }
            // System.out.println(set);

            if (set.last().get(0) - set.first().get(0) > x) {
                out.println("NO");
            } else {
                out.println("YES");
                for (int j = 0; j < n; j++) {
                    out.print(map.get(copy.get(j)).pollFirst() + " ");
                }
                out.println();
            }
        }
        out.close();
    }

}