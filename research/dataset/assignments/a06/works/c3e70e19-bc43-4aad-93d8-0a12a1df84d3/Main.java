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
            int n;
            n = in.nextInt();
            int[] arr = new int[n];
            long[] array = new long[n];

            for (int i = 0; i < n; i++) {
                arr[i] = in.nextInt();
                if(i==0){
                    array[i] = arr[i];
                }
                else
                    array[i] = (long)arr[i] + array[i-1];
            }

            int evenMin = arr[0], oddMin = arr[1];

            long ans = ((long) n * (long)arr[0]) + ((long) n * (long)arr[1]);

            for (int i = 2; i < n; i++) {
                if(i%2==0){
                    if(evenMin>arr[i]) {
                        evenMin = arr[i];
                    }
                }
                else{
                    if(oddMin>arr[i]) {
                        oddMin = arr[i];
                    }
                }
                if(i%2==0){
                    ans = Math.min(ans, array[i] + (long) ((n - (i / 2)) - 1) * (long)evenMin + (long) ((n - ((i-1) / 2)) - 1) * (long)oddMin );
                }
                else{
                    ans = Math.min(ans, array[i] + (long) ((n - (i / 2)) - 1) * (long)oddMin + (long) ((n - ((i-1) / 2)) - 1) * (long)evenMin);
                }

            }
            out.println(ans);





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
            return this.a - o.a;
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
            return this.a - o.a;
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
        for (long i:a) l.add(i);
        Collections.sort(l);
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
