
import java.io.*;
import java.lang.*;
import java.util.*;

public class C1499 {
    public static void main(String[] args) throws IOException{
        StringBuffer ans = new StringBuffer();
        StringTokenizer st;
        BufferedReader f = new BufferedReader(new InputStreamReader(System.in));
        st = new StringTokenizer(f.readLine());
        int t = Integer.parseInt(st.nextToken());
        for(int i = 0; i < t; i++){
            st = new StringTokenizer(f.readLine());
            int n = Integer.parseInt(st.nextToken());
            long op = Long.MAX_VALUE;
            long[] arr = new long[n];
            st = new StringTokenizer(f.readLine());
            for(int x = 0; x < n; x++){
                arr[x] = Integer.parseInt(st.nextToken());
            }
            long sum = arr[0];
            long min = arr[0];
            long min1 = arr[1];
            long howMany = 1;
            long howMany1 = 0;
            long osum = sum;
            for(int x = 1; x < n; x++){
                osum+=arr[x];
                if(x % 2 != 0){
                    sum+= (n - howMany1) *arr[x];
                    sum+=( min *(n-howMany));
                    min1 = Math.min(arr[x], min1);
                    howMany1++;
                }else{
                    sum+= (n - howMany) *arr[x];
                    sum+=( min1 *(n-howMany1));
                    min = Math.min(arr[x], min);
                    howMany++;
                }
                //System.out.println(min1 + " " + min1);
                //System.out.println(sum);
                op = Math.min(op, sum);
                sum = osum;
            }

            ans.append(op);
            ans.append("\n");

        }
        f.close();



        System.out.println(ans);
    }
    public static class point implements Comparable<point>{
        int x;
        int y;
        public point(int x,int y){
            this.x = x;
            this.y = y;
        }
        public String toString(){
            return(x + " " + y);
        }
        public boolean equals(Object x){
            point y = ((point)(x));
            if (this.x == y.x && this.y == y.y){
                return true;
            }
            return false;
        }
        public int hashCode(){
            return Objects.hash(x,y);
        }
        public int compareTo(point other){
            if(this.x < other.x){
                return -1;
            }else if(this.x == other.x && this.y == other.y){
                return 0;
            }
            return 1;
        }
    }
}