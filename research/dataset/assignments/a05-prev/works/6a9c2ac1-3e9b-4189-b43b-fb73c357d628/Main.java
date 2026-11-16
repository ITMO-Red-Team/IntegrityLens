
import java.util.*;
import java.io.*;
public class experiment {
	 static class FastReader {
	        BufferedReader br;
	        StringTokenizer st;
	 
	        public FastReader()
	        {
	            br = new BufferedReader(
	                new InputStreamReader(System.in));
	        }
	 
	        String next()
	        {
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
	 
	        int nextInt() { return Integer.parseInt(next()); }
	 
	        long nextLong() { return Long.parseLong(next()); }
	 
	        double nextDouble()
	        {
	            return Double.parseDouble(next());
	        }
	 
	        String nextLine()
	        {
	            String str = "";
	            try {
	                str = br.readLine();
	            }
	            catch (IOException e) {
	                e.printStackTrace();
	            }
	            return str;
	        }
	    }
	
		 public static void main(String[] args) throws IOException  {
			FastReader sc = new FastReader();
			PrintWriter out = new PrintWriter(new BufferedOutputStream(System.out));
			int t = sc.nextInt();
			while( t-- > 0) {
				char arr[] = new char[5];
				for( int i = 0 ;i< 5 ;i++) {
					arr[i] = (char)(i + 97);
				}
				int n =sc.nextInt();
				ArrayList<String> input = new ArrayList<>();
				for( int i = 0 ;i< n;i++) {
					input.add( sc.next());
				}
				int max = 0;
				for( int i = 0 ; i< 5 ;i++) {
					int test = 0;
					int sum = 0;
					char now = arr[i];
					ArrayList<Integer> temp = new ArrayList<>();
					for( int j = 0 ; j < input.size(); j++) {
						int local = 0;
						for( int k = 0 ; k < input.get(j).length(); k++) {
							if( input.get(j).charAt(k) == now) {
								local++;
							}
						}
						temp.add( local - (input.get(j).length() - local));
					}
					Collections.sort(temp , Collections.reverseOrder());
					//out.println( temp);
					for( int j = 0 ; j < n ; j++) {
						sum+=temp.get(j);
						if( sum<=0) {
							break;
						}
						test++;
					}
					max = Math.max(max, test);
				}
				out.println( max);
			}
			out.flush();
				
		 }
}
