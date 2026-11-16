import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class Main {
    static String f1(String s, int index) {
        StringBuffer sb = new StringBuffer();
        for(int i =0;i<s.length();i++){
            if( i == index) sb.append(s.charAt(i));
            else if (s.charAt(i) == '0') sb.append('1');
            else sb.append('0');
        }
        return sb.toString();
    }
    static long solve(String s1, String s2) {
        if(s1.equals(s2)) return 0;
        int o1 = 0, o2 = 0, diff = 0;
        for (int i = 0; i < s1.length(); i++) {
            if (s1.charAt(i) == '1') o1++;
            if (s2.charAt(i) == '1') o2++;
            if (s1.charAt(i) != s2.charAt(i)) diff++;
        }
        return o1 == o2? diff : Integer.MAX_VALUE;
    }
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int t = Integer.parseInt(br.readLine());
        while (t-- > 0) {
            int n = Integer.parseInt(br.readLine());
            String s1 = br.readLine();
            String s2 = br.readLine();
            long res = solve(s1, s2);
            for(int i=0;i<s1.length();i++) {
                if(s1.charAt(i) == '1' && s2.charAt(i) == '1') {
                    res = Math.min(res, 1 + solve(f1(s1, i), s2));
                    break;
                }
            }
            for(int i=0;i<s1.length();i++) {
                if(s1.charAt(i) == '1' && s2.charAt(i) == '0') {
                    res = Math.min(res,1 + solve(f1(s1, i), s2));
                    break;
                }
            }
            if(res == Integer.MAX_VALUE) System.out.println("-1");
            else System.out.println(res);
        }
    }
}