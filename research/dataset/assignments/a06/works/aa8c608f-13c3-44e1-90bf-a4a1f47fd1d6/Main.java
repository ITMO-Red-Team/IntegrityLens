import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;


public class Solution {
    static class MyScanner {
        BufferedReader br;
        StringTokenizer st;

        public MyScanner() {
            br = new BufferedReader(new InputStreamReader(System.in));
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

        String nextLine(){
            String str = "";
            try {
                str = br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }

    }
    static class HashMultiSet<T> implements Iterable<T>
    { private final HashMap<T,Integer> map;
        private int size;
        public HashMultiSet(){map=new HashMap<>(); size=0;}
        public void clear(){map.clear(); size=0;}
        public int size(){return size;}
        public int setSize(){return map.size();}
        public boolean contains(T a){return map.containsKey(a);}
        public boolean isEmpty(){return size==0;}
        public Integer get(T a){return map.getOrDefault(a,0);}
        public void add(T a, int count)
        {
            int cur=get(a);map.put(a,cur+count); size+=count;
            if(cur+count==0) map.remove(a);
        }
        public void addOne(T a){add(a,1);}
        public void remove(T a, int count){add(a,Math.max(-get(a),-count));}
        public void removeOne(T a){remove(a,1);}
        public void removeAll(T a){remove(a,Integer.MAX_VALUE-10);}

        public Iterator<T> iterator()
        {
            return new Iterator<>()
            {
                private final Iterator<T> iter = map.keySet().iterator();
                private int count = 0; private T curElement;

                public boolean hasNext(){return iter.hasNext()||count>0;}
                public T next()
                {
                    if(count==0)
                    {
                        curElement=iter.next();
                        count=get(curElement);
                    }
                    count--; return curElement;
                }
            };
        }
    }
    static class DisjointUnionSets {
        int[] rank, parent;
        int n;

        // Constructor
        public DisjointUnionSets(int n)
        {
            rank = new int[n];
            parent = new int[n];
            this.n = n;
            makeSet();
        }

        // Creates n sets with single item in each
        void makeSet()
        {
            for (int i = 0; i < n; i++) {
                // Initially, all elements are in
                // their own set.
                parent[i] = i;
            }
        }

        // Returns representative of x's set
        int find(int x)
        {
            // Finds the representative of the set
            // that x is an element of
            if (parent[x] != x) {
                // if x is not the parent of itself
                // Then x is not the representative of
                // his set,
                parent[x] = find(parent[x]);

                // so we recursively call Find on its parent
                // and move i's node directly under the
                // representative of this set
            }

            return parent[x];
        }

        // Unites the set that includes x and the set
        // that includes x
        void union(int x, int y)
        {
            // Find representatives of two sets
            int xRoot = find(x), yRoot = find(y);

            // Elements are in the same set, no need
            // to unite anything.
            if (xRoot == yRoot)
                return;

            // If x's rank is less than y's rank
            if (rank[xRoot] < rank[yRoot])

                // Then move x under y  so that depth
                // of tree remains less
                parent[xRoot] = yRoot;

                // Else if y's rank is less than x's rank
            else if (rank[yRoot] < rank[xRoot])

                // Then move y under x so that depth of
                // tree remains less
                parent[yRoot] = xRoot;

            else // if ranks are the same
            {
                // Then move y under x (doesn't matter
                // which one goes where)
                parent[yRoot] = xRoot;

                // And increment the result tree's
                // rank by 1
                rank[xRoot] = rank[xRoot] + 1;
            }
        }
    }
    private  static  boolean try1(String s, int index,int prev){
        int n = s.length();
        for(int i = index;i<n;i++){
            if(s.charAt(i) == '0' && prev == i-1){
                return false;
            }else if(s.charAt(i) == '0'){
                prev = i;
            }
        }
        return true;
    }
    public static void main(String[] args) {
        MyScanner sc = new MyScanner();
        int t = sc.nextInt();
        while (t-- != 0) {
            int n = sc.nextInt();
            long a[] = new long[n];
            for(int i=0;i<n;i++){
                a[i] = sc.nextLong();
            }
            long mineA = a[0];
            long mineB = a[1];
            long accA =0 ;
            long accB = 0;
            long remA = n;
            long remB = n;
            long ans = (mineA * n) + (mineB *n);
            boolean check = true;
            for(int i =2;i<n;i++){
                if(check){
                    check = false;
                    long x = Math.min(a[i],mineA);
                    long y = Math.max(a[i],mineA);
                    mineA = x;
                    accA += y;
                    remA --;
                    long sug = (accA +  remA * mineA) + (accB + remB * mineB);
                    ans = Math.min(sug , ans);
                }else{
                    check = true;
                    long x = Math.min(a[i],mineB);
                    long y = Math.max(a[i],mineB);
                    mineB = x;
                    accB += y;
                    remB --;
                    long sug = (accA +  remA * mineA) + (accB + remB * mineB);
                    ans = Math.min(sug,ans);
                }
            }
            System.out.println(ans);
        }
    }
}
