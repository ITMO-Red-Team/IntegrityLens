import java.util.*;
import java.io.*;

public class PhoenixAndTowers {
    public static void main(String [] args) throws IOException{

        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter out = new BufferedWriter(new OutputStreamWriter(System.out));

        int t = Integer.parseInt(in.readLine());
        for (int test = 0; test < t; test++){
            String [] data = in.readLine().split(" ");
            int n = Integer.parseInt(data[0]);
            int m = Integer.parseInt(data[1]);
            int x = Integer.parseInt(data[2]);

            data = in.readLine().split(" ");
            intPair [] blocks = new intPair[n];
            for (int i = 0; i < n; i++){
                blocks[i] = new intPair(i, Integer.parseInt(data[i]));
            }
            Arrays.sort(blocks);
            TreeSet<intPair> towers = new TreeSet<>();
            // intPair [] towers = new intPair[m];
            for (int i = 0; i < m; i++){
                // towers[i] = new intPair(i+1, 0);
                towers.add(new intPair(i+1, 0));
            }
            int [] newIdx = new int[n];

            for (int i = n - 1; i >= 0; i--){
                //Arrays.sort(towers);
                intPair low = towers.pollFirst();
                //towers[0].value += blocks[i].value;
                low.value += blocks[i].value;
                newIdx[blocks[i].idx] = low.idx;
                towers.add(low);
            }

            int min = n*x;
            int max = 0;
            for (intPair tower : towers){
                if (tower.value < min){
                    min = tower.value;
                }
                if (tower.value > max){
                    max = tower.value;
                }
            }
            if (max - min <= x){
                out.write("YES\n");
                for (int i = 0; i < n; i++){
                    if (i != 0){
                        out.write(" " + newIdx[i]);
                    }else {
                        out.write("" + newIdx[i]);
                    }
                }
                out.write("\n");
            } else {
                out.write("NO\n");
            }
        }
        out.close();
    } 

    static class intPair implements Comparable<intPair>{
        int idx;
        int value;
        intPair(int idx, int value){
            this.idx = idx;
            this.value = value;
        }
        @Override
        public int compareTo(PhoenixAndTowers.intPair o) {
            return this.value != o.value? this.value - o.value: this.idx - o.idx;
        }
    }
}
