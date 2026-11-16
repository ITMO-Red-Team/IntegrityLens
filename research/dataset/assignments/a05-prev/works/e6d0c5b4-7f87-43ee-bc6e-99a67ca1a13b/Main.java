import java.lang.reflect.Array;
import java.util.*;
public class Rough {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);   
        int t = sc.nextInt();
        while (t-->0) {
            int n = sc.nextInt();
            sc.nextLine();
            String s[] = new String[n];
            int f[][] = new int[n][5];
            for (int i = 0; i < n; i++) {
                s[i]  = sc.nextLine();

                for (int j = 0; j < s[i].length(); j++) {
                    f[i][s[i].charAt(j)-'a']++;
                }
            }

            int ans = 0;
            for ( int i = 0; i < 5; i++) {
                ArrayList<Integer> al = new ArrayList<>();

                for (int j = 0; j < n; j++) {
                    int o = 0;
                    for (int k = 0; k < 5; k++) {
                        if(k != i)
                            o+=f[j][k];
                    }
                    al.add(f[j][i]-o);
                }

                Collections.sort(al,Collections.reverseOrder());

                int max = 0;
                int x = 0;
                for (int j = 0; j < n; j++) {
                    x+=al.get(j);
                    if(x<=0)break;
                    max++;
                }
                ans = Math.max(max,ans);
            }

            System.out.println(ans);
        }
        sc.close();
    }
}