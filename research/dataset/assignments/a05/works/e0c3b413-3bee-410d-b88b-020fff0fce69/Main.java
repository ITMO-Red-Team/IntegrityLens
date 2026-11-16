import java.io.*;
import java.util.*;

public class C {

    public static void main(String[] args) throws Exception{
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        try{
            int t = Integer.parseInt(br.readLine());
            while(t-->0){
                int n = Integer.parseInt(br.readLine());
                int lst[][] = new int[n][5];
                for(int i=0; i<n; i++){
                    String s = br.readLine();
                    for(int j=0; j<s.length(); j++){
                        lst[i][s.charAt(j)-'a']++;
                    }
                }
                int fans = Integer.MIN_VALUE;
                for(int i=0; i<5; i++){
                    int val[] = new int[n];
                    for(int k=0; k<n; k++){
                        int sum = 0;
                        for(int j=0; j<5; j++){
                            if(i==j){
                                sum += lst[k][j];
                            }else{
                                sum -= lst[k][j];
                            }
                        }
                        val[k] = sum;
                    }
                    Arrays.sort(val);
                    int sum = 0;
                    int ans = 0;
                    for(int x = n-1; x>=0; x--){
                        sum+=val[x];
                        if(sum>0){
                            ans++;
                        }else{
                            break;
                        }
                    }
                    fans = Math.max(fans, ans);
                }
                bw.write(fans+"\n");
            }
            bw.flush();
        }catch(Exception e){
            return;
        }
    }
    
}
