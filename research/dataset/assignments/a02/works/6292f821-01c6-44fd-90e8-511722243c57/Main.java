/**
 * check out my youtube channel sh0rkyboy
 * https://tinyurl.com/zdxe2y4z
 * I do screencasts, solutions, and other fun stuff in the future
 */

import java.util.*;
import java.io.*;
import static java.lang.Math.min;
import static java.lang.Math.abs;
import static java.lang.Math.max;
public class EdB {
	static long[] mods = {1000000007, 998244353, 1000000009};
	static long mod = mods[0];
	public static MyScanner sc;
    public static PrintWriter out;
	public static void main(String[] largewang) throws Exception{
		// TODO Auto-generated method stub
 		sc = new MyScanner();
 		out = new PrintWriter(System.out);
 		int t = sc.nextInt();
 		while (t-->0) {
 			int n = sc.nextInt();
 			char[] a = sc.next().toCharArray();
 			char[] b = sc.next().toCharArray();
 			int counta = 0;
 			int countb = 0;
 			for (char j : a) {
 				if (j == '1')
 					counta++;
 			}
 			for (char j : b) {
 				if (j == '1')
 					countb++;
 			}
 			long ans = 0;
 			if (counta == countb && n-counta+1 == countb){
 				for (int j = 0;j<n;j++){
 					if (b[j] == '1' && a[j] == '0') {
 						ans += 2;
 					}
 				}
 				long tempans = 0;
 				int j=0;
 				for(j = 0;j<n;j++){
 					if (a[j] == '1' && b[j] == '1')
 						break;
 				}
 				for(int i = 0;i<n;i++){
 					if (i == j)
 						continue;
 					if (a[i] == '1')
 						a[i] = '0';
 					else
 						a[i] = '1';
 				}
 				tempans += 1;
 				for (j = 0;j<n;j++){
 					if (b[j] == '1' && a[j] == '0') {
 						tempans += 2;
 					}
 				}
 				ans = min(ans, tempans);
 			} else if (counta == countb) {
 				for (int j = 0;j<n;j++){
 					if (b[j] == '1' && a[j] == '0') {
 						ans += 2;
 					}
 				}
 			} else if (n-counta+1 == countb) {
 				int j=0;
 				for(j = 0;j<n;j++){
 					if (a[j] == '1' && b[j] == '1')
 						break;
 				}
 				for(int i = 0;i<n;i++){
 					if (i == j)
 						continue;
 					if (a[i] == '1')
 						a[i] = '0';
 					else
 						a[i] = '1';
 				}
 				ans += 1;
 				for (j = 0;j<n;j++){
 					if (b[j] == '1' && a[j] == '0') {
 						ans += 2;
 					}
 				}
 			} else {
 				ans = -1;
 			}
 			out.println(ans);
  		}
 		out.close();
 		
 	}
	
	public static void sort(int[] array){
		ArrayList<Integer> copy = new ArrayList<>();
		for (int i : array)
			copy.add(i);
		Collections.sort(copy);
		for(int i = 0;i<array.length;i++)
			array[i] = copy.get(i);
	}
	static String[] readArrayString(int n){
		String[] array = new String[n];
		for(int j =0 ;j<n;j++)
			array[j] = sc.next();
		return array;
	}
	static int[] readArrayInt(int n){
    	int[] array = new int[n];
    	for(int j = 0;j<n;j++)
    		array[j] = sc.nextInt();
    	return array;
    }
	static int[] readArrayInt1(int n){
		int[] array = new int[n+1];
		for(int j = 1;j<=n;j++){
			array[j] = sc.nextInt();
		}
		return array;
	}
	static long[] readArrayLong(int n){
		long[] array = new long[n];
		for(int j =0 ;j<n;j++)
			array[j] = sc.nextLong();
		return array;
	}
	static double[] readArrayDouble(int n){
		double[] array = new double[n];
		for(int j =0 ;j<n;j++)
			array[j] = sc.nextDouble();
		return array;
	}
	static int minIndex(int[] array){
		int minValue = Integer.MAX_VALUE;
		int minIndex = -1;
		for(int j = 0;j<array.length;j++){
			if (array[j] < minValue){
				minValue = array[j];
				minIndex = j;
			}
		}
		return minIndex;
	}
	static int minIndex(long[] array){
		long minValue = Long.MAX_VALUE;
		int minIndex = -1;
		for(int j = 0;j<array.length;j++){
			if (array[j] < minValue){
				minValue = array[j];
				minIndex = j;
			}
		}
		return minIndex;
	}
	static int minIndex(double[] array){
		double minValue = Double.MAX_VALUE;
		int minIndex = -1;
		for(int j = 0;j<array.length;j++){
			if (array[j] < minValue){
				minValue = array[j];
				minIndex = j;
			}
		}
		return minIndex;
	}
	public static int power(long x, long y, long mod){
		long ans = 1;
		while(y>0){
			if (y%2==1)
				ans = (ans*x)%mod;
			x = (x*x)%mod;
			y/=2;
		}
		return (int)(ans);
	}
	static void verdict(boolean a){
        out.println(a ? "YES" : "NO");
    }
    public static class MyScanner {
        BufferedReader br;
        StringTokenizer st;
        public MyScanner() {
            br = new BufferedReader(new InputStreamReader(System.in));
        }
        String next() { 
            while (st == null || !st.hasMoreElements()) {
                try {
                    st = new StringTokenizer(br.readLine());
                } 
                catch (IOException e) {
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
            try{
                str = br.readLine();
            } 
            catch (IOException e) {
                e.printStackTrace();
            }
            return str;
        }
        
    }	
}

//StringJoiner sj = new StringJoiner(" "); 
//sj.add(strings)
//sj.toString() gives string of those stuff w spaces or whatever that sequence is