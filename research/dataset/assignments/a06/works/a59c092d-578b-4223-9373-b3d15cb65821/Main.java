
import java.util.*;
import java.lang.*;
import java.io.*;

public class C {

//	            ***                          ++                      
//             +=-==+                      +++=-                     
//            +-:---==+                   *+=----=                   
//           +-:------==+               ++=------==                  
//           =-----------=++=========================                
//          +--:::::---:-----============-=======+++====             
//          +---:..:----::-===============-======+++++++++           
//          =---:...---:-===================---===++++++++++         
//          +----:...:-=======================--==+++++++++++        
//          +-:------====================++===---==++++===+++++      
//         +=-----======================+++++==---==+==-::=++**+     
//        +=-----================---=======++=========::.:-+*****    
//       +==::-====================--:  --:-====++=+===:..-=+*****   
//       +=---=====================-...  :=..:-=+++++++++===++*****  
//       +=---=====+=++++++++++++++++=-:::::-====+++++++++++++*****+ 
//      +=======++++++++++++=+++++++============++++++=======+****** 
//      +=====+++++++++++++++++++++++++==++++==++++++=:...  . .+**** 
//     ++====++++++++++++++++++++++++++++++++++++++++-.     ..-+**** 
//     +======++++++++++++++++++++++++++++++++===+====:.    ..:=++++ 
//     +===--=====+++++++++++++++++++++++++++=========-::....::-=++* 
//     ====--==========+++++++==+++===++++===========--:::....:=++*  
//     ====---===++++=====++++++==+++=======-::--===-:.  ....:-+++   
//     ==--=--====++++++++==+++++++++++======--::::...::::::-=+++    
//     ===----===++++++++++++++++++++============--=-==----==+++     
//     =--------====++++++++++++++++=====================+++++++     
//     =---------=======++++++++====+++=================++++++++     
//     -----------========+++++++++++++++=================+++++++    
//     =----------==========++++++++++=====================++++++++  
//     =====------==============+++++++===================+++==+++++ 
//     =======------==========================================++++++

//	 created by : Nitesh Gupta

	public static void main(String[] args) throws Exception {
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		int t = Integer.parseInt(br.readLine());
		StringBuilder sb = new StringBuilder();
		while (t-- > 0) {
			String[] scn = (br.readLine()).trim().split(" ");
			int n = Integer.parseInt(scn[0]);
			long[] arr = new long[n];
			scn = (br.readLine()).trim().split(" ");
			for (int i = 0; i < n; i++) {
				arr[i] = Long.parseLong(scn[i]);
			}
			long min;
			long hor = arr[0], ver = arr[1];
			long min1 = 0, min2 = 0;
			min = (hor + ver) * n;
			long x = 0, y = 0;
			for (int i = 2; i < n; i++) {
				if (i % 2 == 0) {
					x += 1;
					if (arr[i] >= hor) {
						min1 += arr[i];
					} else {
						min1 += hor;
						hor = arr[i];
					}
					
					
				} else {
					y += 1;
					if (arr[i] >= ver) {
						min2 += arr[i];
					} else {
						min2 += ver;
						ver = arr[i];
					}
				}
				long pro =  (n - x) * hor + (n - y) * ver;
				min = Math.min(min, min1 + min2 +pro);
			}
			sb.append(min);
			sb.append("\n");
		}
		System.out.println(sb);
		return;

	}

	public static void sort(long[] arr) {
		int n = arr.length;
		for (int i = 0; i < n; i++) {
			int idx = (int) Math.random() * n;
			long temp = arr[i];
			arr[i] = arr[idx];
			arr[idx] = temp;
		}
		Arrays.sort(arr);
	}

	public static void print(long[] dp) {
		for (long ele : dp) {
			System.out.print(ele + " ");
		}
		System.out.println();
	}

	public static void print(long[][] dp) {
		for (long[] a : dp) {
			for (long ele : a) {
				System.out.print(ele + " ");
			}
			System.out.println();
		}
	}

}
