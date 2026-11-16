import java.io.OutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class First {

    public static void main(String[] args) {
        InputStream inputStream = System.in;
        OutputStream outputStream = System.out;
        InputReader in = new InputReader(inputStream);
        PrintWriter out = new PrintWriter(outputStream);
        TaskA solver = new TaskA();
        //int a = 1;
        int t;
        t = in.nextInt();
        //t = 1;
        while (t > 0) {
            //out.print("Case #"+(a++)+": ");
            solver.call(in,out);
            t--;
        }
        out.close();

    }

    static class TaskA {
        public void call(InputReader in, PrintWriter out) {
            int n, m, x;
            n = in.nextInt();
            m = in.nextInt();
            x = in.nextInt();

            int[] arr = new int[n];
            answer[] array = new answer[n];
            int[] ar = new int[n];

            for (int i = 0; i < n; i++) {
                arr[i] = in.nextInt();
                array[i] = new answer(arr[i],i);
            }

            long[] ans = new long[m];
            Arrays.sort(array);


            int a = 0 , b = 0;
            while(true){
                for (int i = 0; i < m; i++) {
                    ar[b] = i+1;
                    b++;
                    if(b==n){
                        break;
                    }
                }
                if(b==n){
                    break;
                }
                for (int i = m-1; i >= 0; i--) {
                    ar[b] = i+1;
                    b++;
                    if(b==n){
                        break;
                    }
                }

                if(b==n){
                    break;
                }
            }

            for (int i = 0; i < n; i++) {
                ans[ar[i]-1] += array[i].a;
            }

            for (int i = 0; i < m-1; i++) {
                if(Math.abs(ans[i]- ans[i+1])>x){
                    out.println("NO");
                    return;
                }
            }
            out.println("YES");

            int[] answer = new int[n];

            for (int i = 0; i < n; i++) {
                answer[array[i].b] = ar[i];
            }

            for (int i = 0; i < n; i++) {
                out.print(answer[i]+" ");
            }
            out.println();

        }
    }

    static int gcd(int a, int b)
    {
        if (a == 0)
            return b;
        return gcd(b % a, a);
    }

    static int lcm(int a, int b)
    {
        return (a / gcd(a, b)) * b;
    }

    static class answer implements Comparable<answer>{
        int a;
        int b;

        public answer(int a, int b) {
            this.a = a;
            this.b = b;
        }

        @Override
        public int compareTo(answer o) {
            return o.a - this.a;
        }

        @Override
        public boolean equals(Object o){
            if(o instanceof answer){
                answer c = (answer)o;
                return a == c.a && b == c.b;
            }
            return false;
        }
    }

    static class answer1 implements Comparable<answer1>{
        int a, b, c;

        public answer1(int a, int b, int c) {
            this.a = a;
            this.b = b;
            this.c = c;

        }

        @Override
        public int compareTo(answer1 o) {
            if(o.c==this.c){
                return this.a - o.a;
            }
            return o.c - this.c;
        }
    }

    static long gcd(long a, long b)
    {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }

    static void sort(long[] a) {
        ArrayList<Long> l=new ArrayList<>();
        for (Long i:a) l.add(i);
        l.sort(Collections.reverseOrder());
        for (int i=0; i<a.length; i++) a[i]=l.get(i);
    }

    static final Random random=new Random();

    static void shuffleSort(int[] a) {
        int n=a.length;
        for (int i=0; i<n; i++) {
            int oi=random.nextInt(n), temp=a[oi];
            a[oi]=a[i]; a[i]=temp;
        }
        Arrays.sort(a);
    }

    static class InputReader {
        public BufferedReader reader;
        public StringTokenizer tokenizer;

        public InputReader(InputStream stream) {
            reader = new BufferedReader(new InputStreamReader(stream), 32768);
            tokenizer = null;
        }

        public String next() {
            while (tokenizer == null || !tokenizer.hasMoreTokens()) {
                try {
                    tokenizer = new StringTokenizer(reader.readLine());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return tokenizer.nextToken();
        }

        public int nextInt() {
            return Integer.parseInt(next());
        }
        public long nextLong(){
            return Long.parseLong(next());
        }
        public double nextDouble() {
            return Double.parseDouble(next());
        }

    }
}
