import java.io.*;
import java.util.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.abs;

public class C {

    static int fval(String s, char x){
        int fx = 0, oth = 0;
        for(int i = 0; i<s.length(); i++){
            if(x == s.charAt(i)) fx++;
            else oth++;
        }
        return fx-oth;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder sb = new StringBuilder();
        //int tst = 1;
        int tst = Integer.parseInt(br.readLine());
        long mod = (long)1e9+7;
        while(tst-->0){
            int n = Integer.parseInt(br.readLine());
            ArrayList<String> lst = new ArrayList<>();
            for(int i = 0; i<n; i++){
                lst.add(br.readLine());
            }
            int ans = 0;
            for(int i = 0; i<5; i++){
                char x = (char)(97+i);
                ArrayList<Integer> vals = new ArrayList<>();
                for(int j = 0; j<n; j++){
                    vals.add(fval(lst.get(j), x));
                }
                Collections.sort(vals, Collections.reverseOrder());
                int pt = -1, sum = 0;
                while(pt+1<n && sum+vals.get(pt+1)>0){
                    sum+=vals.get(++pt);
                }
                ans = max(ans, pt+1);
            }
            sb.append(ans).append('\n');
        }
        System.out.println(sb);
    }
}
/**
 * min deletions
 * for each letter calc max len of words
 * Its desirable to add someting that has more of x and less of other letters -> f(x, s)=>fx-sum(fz)
 */
