//package Codeforces;

import java.io.*;
import java.util.*;

public class Menorah {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);

        int t = sc.nextInt();
        StringBuilder sb = new StringBuilder();
        while (t-->0){
            int n = sc.nextInt();
            char[] a = sc.next().toCharArray();
            char[] b = sc.next().toCharArray();

            int a1=0, b1=0;
            for(int i=0;i<n;i++){
                if(a[i]=='1')
                    a1++;
                if(b[i]=='1')
                    b1++;
            }

            int min = 100000000;

            if(a1==b1){
                int c = 0;
                for(int i=0;i<n;i++){
                    if(a[i]!=b[i])
                        c++;
                }

                min = Math.min(min, c);
            }

            if(b1==(n-a1+1)){
                int ind = -1;
                for(int i=0;i<n;i++){
                    if(a[i]==b[i] && a[i]=='1'){
                        ind = i;
                        break;
                    }
                }

                int c = 0;
                for(int i=0;i<n;i++){
                    if(i==ind)
                        continue;

                    if(a[i]==b[i])
                        c++;
                }

                min = Math.min(min, c + 1);
            }

            if(min == 100000000)
                sb.append("-1\n");
            else sb.append(min).append("\n");
        }

        System.out.println(sb);

        sc.close();
    }
}
