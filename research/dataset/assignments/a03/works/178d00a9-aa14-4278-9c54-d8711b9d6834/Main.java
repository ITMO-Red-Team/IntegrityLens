
//package com.company;

import  java.math.*;
import java.util.*;
import java.lang.*;
import java.io.*;



public final class Main {

    FastReader s;


    public static void main (String[] args) throws java.lang.Exception
    {

        new Main().run();

    }
    void run()
    {
        s = new FastReader();
        solve();
    }
   StringBuffer sb;
    void solve()
    {
         sb = new StringBuffer();
        for(int T = s.nextInt();T > 0;T--)
            start();
       // System.out.print(sb);

    }


    void start()
    {
        int n = s.nextInt();
        int m = s.nextInt();
        long arr[] = longArr(m);
        long arr2[] = longArr(m);
        long ans [] = new long[n];
        Arrays.fill(ans,Integer.MAX_VALUE);
        for(int i = 0; i<m; i++)
        {
            ans[(int)arr[i]-1] = arr2[i];
        }
        long left [] = new long[n];
        long right [] = new long[n];
        left[0] = ans[0];
        for(int i = 1; i<n; i++)
        {
            left[i] = Math.min(ans[i], left[i-1]+1);
        }
        right[n-1] = ans[n-1];
        for(int i = n-2; i>=0; i--)
        {
            right[i] = Math.min(right[i+1]+1, ans[i]);
        }
        StringBuffer x = new StringBuffer();
        for(int i =0; i<n; i++)
        {
            long p = Math.min(left[i],right[i]);
            x.append(p+" ");
        }
        System.out.println(x);

    }
    long log(long m)
    {
        long y = 0;
        while(m>0)
        {
            m/=2;
            y++;
        }
        return y;

    }
    long gcd(long a, long b)
    {
        if (b == 0)
            return a;
        return gcd(b, a % b);
    }
    long power(long x, long y, long p)
    {
        long res = 1; // Initialize result

        // Update x if it is more
        // than or equal to p
        x = x % p;

        while (y > 0)
        {
            // If y is odd, multiply
            // x with the result
            if ((y & 1) > 0)
                res = (res * x) % p;

            // y must be even now
            y = y >> 1; // y = y/2
            x = (x * x) % p;
        }
        return res;
    }

    int lower_bound(int [] arr , int key)
    {

        int i = 0;
        int j = arr.length-1;
          //if(arr[i] > key)return -1;
         if(arr[j] < key)return -1;

        while(i<j)
        {
            int mid = (i+j)/2;

            if(arr[mid] == key)
            {
                j = mid;
            }

            else if(arr[mid] < key)
            {
                i = mid+1;
            }
            else
                j = mid-1;
      }

        return i;
    }

    int upper_bound(int [] arr , int key)
    {

        int i = 0;
        int j = arr.length-1;
        if(arr[j] <= key)return j+1;

        while(i<j)
        {
            int mid = (i+j)/2;



             if(arr[mid] <= key)
            {
                i = mid+1;
            }
            else
                j = mid;
        }

        return i;
    }


    static void sort(int[] A){
        int n = A.length;
        Random rnd = new Random();
        for(int i=0; i<n; ++i){
            int tmp = A[i];
            int randomPos = i + rnd.nextInt(n-i);
            A[i] = A[randomPos];
            A[randomPos] = tmp;
        }
        Arrays.sort(A);
    }
    static void sort(long[] A){
        int n = A.length;
        Random rnd = new Random();
        for(int i=0; i<n; ++i){
            long tmp = A[i];
            int randomPos = i + rnd.nextInt(n-i);
            A[i] = A[randomPos];
            A[randomPos] = tmp;
        }
        Arrays.sort(A);
    }
    static String sort(String s) {
        Character ch[]=new Character[s.length()];
        for(int i=0;i<s.length();i++) {
            ch[i]=s.charAt(i);
        }
        Arrays.sort(ch);
        StringBuffer st=new StringBuffer("");
        for(int i=0;i<s.length();i++) {
            st.append(ch[i]);
        }
        return st.toString();
    }

    //long array input
    public long [] longArr(int len)
    {
        // long arr input
        long [] arr = new long[len];
        String [] strs =  s.nextLine().trim().split("\\s+");
        for(int i =0; i<len; i++)
        {
            arr[i] = Long.parseLong(strs[i]);

        }
        return arr;
    }
    // int arr input
    public int [] intArr(int len)
    {
        // long arr input
        int [] arr = new int[len];
        String [] strs =  s.nextLine().trim().split("\\s+");
        for(int i =0; i<len; i++)
        {
            arr[i] = Integer.parseInt(strs[i]);

        }
        return arr;
    }

    static class FastReader
    {
        BufferedReader br;
        StringTokenizer st;

        public FastReader()
        {
            br = new BufferedReader(new
                    InputStreamReader(System.in));
        }

        String next()
        {
            while (st == null || !st.hasMoreElements())
            {
                try
                {
                    st = new StringTokenizer(br.readLine());
                }
                catch (IOException  e)
                {
                    e.printStackTrace();
                }
            }
            return st.nextToken();
        }

        int nextInt()
        {
            return Integer.parseInt(next());
        }

        long nextLong()
        {
            return Long.parseLong(next());
        }

        double nextDouble()
        {
            return Double.parseDouble(next());
        }

        String nextLine()
        {
            String str = "";
            try
            {
                str = br.readLine();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            return str;
        }
    }

}
