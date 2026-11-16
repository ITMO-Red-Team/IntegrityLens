import java.util.*;

public class interestingstory {
    public static void main(String[] args) throws Exception {
       		// your code goes here
		try{    
            Scanner sc =new Scanner(System.in);
            int t=sc.nextInt();
            while(t-->0)
            {
                int n = sc.nextInt();
                int answer = 0;
                String [] string_arr = new String[n];    
                for (int i = 0; i< n; i++){
                    string_arr[i] = sc.next();
                }

                for(char c = 'a'; c <= 'e'; c++){
                    int [] diff = new int[n];

                    for (int j = 0; j < string_arr.length; j++){
                        for(int i = 0; i < string_arr[j].length(); i++){
                            if(string_arr[j].charAt(i) == c){
                                diff[j] += 1;
                            } else {
                                diff[j] += -1;
                            }
                        }
                    }

                    Arrays.sort(diff);
                    int sum = 0;
                    int ans = n;
                    for(int p = n-1; p >= 0;p--){
                        sum += diff[p];
                        if (sum <= 0) {
                            ans = n - p - 1;
                            break;
                        } 
                    }
                    answer = Math.max(answer, ans);
                }

                System.out.println(answer);
            }
         } catch(java.lang.Exception e){}
    }
}