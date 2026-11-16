import java.util.*;
import java.lang.*;
import java.io.*;

// Created by @thesupremeone on 02/05/21
public class C {
    static class Tower implements Comparable<Tower>{
        int index;
        int height = 0;
        LinkedList<Integer> blocks;
        public Tower(int index) {
            this.index = index;
            blocks = new LinkedList<>();
        }
        void addBlock(int block){
            blocks.add(block);
            height += block;
        }
        @Override
        public int compareTo(Tower tower) {
            if(height!=tower.height)
                return Integer.compare(height, tower.height);
            return Integer.compare(index, tower.index);
        }
    }

    void solve() throws IOException {
        int ts = getInt();
        for (int t = 1; t <= ts; t++){
            int n = getInt();
            int m = getInt();
            int x = getInt();
            Integer[] h = new Integer[n];
            Integer[] org = new Integer[n];
            for (int i = 0; i < n; i++){
                h[i] = getInt();
                org[i] = h[i];
            }
            if(m==1){
                println("YES");
                for (int i = 0; i < n; i++) {
                    print("1 ");
                }
                println("");
                continue;
            }
            TreeSet<Tower> towers = new TreeSet<>();
            for (int i = 0; i < m; i++){
                Tower tower = new Tower(i+1);
                towers.add(tower);
            }
            Arrays.sort(h, Comparator.reverseOrder());
            for (int i = 0; i < n; i++) {
                Tower tower = towers.pollFirst();
                if(tower!=null){
                    tower.addBlock(h[i]);
                    towers.add(tower);
                }
            }
            Tower first = towers.first();
            Tower last = towers.last();
            int diff = Math.abs(first.height-last.height);
            if(diff<=x){
                println("YES");
                HashMap<Integer, LinkedList<Integer>> map = new HashMap<>();
                for (Tower tower : towers){
                    for(int block : tower.blocks){
                        LinkedList<Integer> list;
                        if(map.containsKey(block)){
                            list = map.get(block);
                        }else {
                            list = new LinkedList<>();
                            map.put(block, list);
                        }
                        list.add(tower.index);
                    }
                }
                for (int i = 0; i < n; i++) {
                    int block = org[i];
                    LinkedList<Integer> list = map.get(block);
                    if(!list.isEmpty()){
                        int e = list.pollFirst();;
                        print(e+" ");
                    }
                }
                println("");
            }else {
                println("NO");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        if (isOnlineJudge()) {
            in = new BufferedReader(new InputStreamReader(System.in));
            out = new BufferedWriter(new OutputStreamWriter(System.out));
            new C().solve();
            out.flush();
        } else {
            Thread judge = new Thread();
            in = new BufferedReader(new FileReader("input.txt"));
            out = new BufferedWriter(new FileWriter("output.txt"));
            judge.start();
            new C().solve();
            out.flush();
            judge.suspend();
        }
    }
    static boolean isOnlineJudge(){
        try {
            return System.getProperty("ONLINE_JUDGE")!=null
                    || System.getProperty("LOCAL")==null;
        }catch (Exception e){
            return true;
        }
    }
    // Fast Input & Output
    static BufferedReader in;
    static StringTokenizer st;
    static BufferedWriter out;
    static String getLine() throws IOException{
        return in.readLine();
    }
    static String getToken() throws IOException{
        if(st==null || !st.hasMoreTokens())
            st = new StringTokenizer(getLine());
        return st.nextToken();
    }
    static int getInt() throws IOException {
        return Integer.parseInt(getToken());
    }
    static long getLong() throws IOException {
        return Long.parseLong(getToken());
    }
    static void print(Object s) throws IOException{
        out.write(String.valueOf(s));
    }
    static void println(Object s) throws IOException{
        out.write(String.valueOf(s));
        out.newLine();
    }
}