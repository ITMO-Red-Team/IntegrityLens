
import java.io.*;
import java.util.*;

public class Asd {

    static PrintWriter w = new PrintWriter(System.out);
    static FastScanner s = new FastScanner();
    static boolean sd = false;
  
    public static void main(String[] args) {

       int t = s.nextInt();
        //int t=1;
        while (t-- > 0) {
            solve();
        }
        w.close();

    }
  public static class Student {
    public int i1;
    public int value;
          
    // A parameterized student constructor
    public Student(int i1,int i2) {
      
        this.i1 = i1;
        this.value=i2;
    }
      
    public int getkey() {
        return i1;
    } 
    public int getValue() {
        return value;
    } 
}
   static class StudentComparator implements Comparator<Student>{
              
            // Overriding compare()method of Comparator 
                        // for descending order of cgpa
        @Override   
        public int compare(Student s1, Student s2) {
                if (s1.i1 < s2.i1)
                    return -1;
                else if (s1.i1 >s2.i1)
                    return 1;
                                return 0;
                }
        }
    
    /* Function to print all the permutations of the string
     static String swap(String str, int i, int j)
     {
     char ch;
     char[] array = str.toCharArray();
     ch = array[i];
     array[i] = array[j];
     array[j] = ch;
     return String.valueOf(array);
     }
    
     static void permute(String str,int low,int high)
     {
     if(low == high)
     list.add(Long.parseLong(str));
 
     int i;
     for(i = low; i<=high; i++){
     str = swap(str,low,i);
     permute(str, low+1,high);
     str = swap(str,low,i);
     }
     }
     use  permute(str2,0,str2.length()-1); to perform combinations
     */

    public static void solve() {
        int n=s.nextInt();
        int m=s.nextInt();
        int x=s.nextInt();
        int arr[]=new int[n];int res[]=new int[n];
        for(int i=0;i<n;i++)
            arr[i]=s.nextInt();
        
        PriorityQueue<Student> pq=new PriorityQueue<Student>(new StudentComparator());
        for(int i=0;i<m;i++){
            pq.add(new Student(arr[i],i));res[i]=i;}
        for(int i=m;i<n;i++)
        {
            Student s1=pq.poll();
            int k2=s1.getkey()+arr[i];
            int v2=s1.getValue();res[i]=v2;
            pq.add(new Student(k2,v2));
        }
        w.println("YES");
        for(int i=0;i<n;i++)
            w.print(res[i]+1+" ");
        w.println();
       
    }

    static void call(ArrayList<Integer> t, ArrayList<Integer> m) {
        if (t.size() == 0 && m.size() == 0) {
            sd = true;
            return;
        }
        t.remove(0);
        t.remove(t.size() - 1);
        call(t, new ArrayList<Integer>(m.subList(0, m.size() - 1)));
        call(t, new ArrayList<Integer>(m.subList(1, m.size())));
    }

    static long gcd(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    static int noofdivisors(int n) {
            //it counts no of divisors of every number upto number n

        int arr[] = new int[n + 1];
        for (int i = 1; i <= (n); i++) {
            for (int j = i; j <= (n); j = j + i) {
                arr[j]++;
            }
        }
        return arr[0];
    }

    static char[] reverse(char arr[]) {
        char[] b = new char[arr.length];
        int j = arr.length;
        for (int i = 0; i < arr.length; i++) {
            b[j - 1] = arr[i];
            j = j - 1;
        }
        return b;
    }

    static long factorial(int n) {
        long su = 1;
        for (int i = 1; i <= n; i++) {
            su *= (long) i;
        }
        return su;
    }

    static class FastScanner {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer("");

        public String next() {
            while (!st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        long[] readArray(int n) {
            long[] a = new long[n];
            for (int i = 0; i < n; i++) {
                a[i] = nextLong();
            }
            return a;
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
    }

}
