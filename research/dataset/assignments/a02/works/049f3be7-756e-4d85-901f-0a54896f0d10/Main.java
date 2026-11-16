/*
  Challenge 1: Newbie to CM in 1year (Dec 2021 - Nov 2022)   🔥 5* Codechef
  Challenge 2: CM to IM in 1 year (Dec 2022 - Nov 2023)    🔥🔥 6* Codechef
  Challenge 3: IM to GM in 1 year (Dec 2023 - Nov 2024)  🔥🔥🔥 7* Codechef
  Goal: Become better in CP!
  Key: Consistency!
*/

import java.util.*;
import java.io.*;
import java.math.*;

public class Coder {

  static int n;
  static char a[];
  static char b[];
  static StringBuffer str=new StringBuffer();

  static void solve(){
    boolean flag=true;
    for(int i=0;i<n;i++) if(a[i]!=b[i]) flag=false;
    if(flag){str.append("0\n"); return;}
    int tot=0;
    int tot1=0;
    for(int i=0;i<n;i++) if(a[i]=='1') tot++;
    for(int i=0;i<n;i++) if(b[i]=='1') tot1++;
    if(tot==0){
      if(tot1!=0) str.append("-1\n");
      else str.append("0\n");
    }else{
      int x=tot-1;
      int ul=n-tot;
      if(tot!=tot1 && (ul+1)!=tot1) str.append("-1\n");
      else{
        int same=0, nsame=0;
        int zs, os;
        int zd, od;
        zs=os=zd=od=0;
        for(int i=0;i<n;i++){
          if(a[i]==b[i]){
            same++;
            if(a[i]=='0') zs++;
            else os++;
          }
          else{
            nsame++;
            if(a[i]=='0') zd++;
            else od++;
          }
        }
        
        // else if(zd>0 && od>0 && os>0 && zs>0) str.append(Math.min(nsame, same)).append("\n");
        // else if(zd>0 && od>0) str.append(nsame).append("\n");
        // else if(zs>0 && os>0) str.append(same).append("\n");
        // else str.append("-1\n");
        if(same==1 && nsame==n-1 && os==1) str.append("1\n");
        else if(zd==od && od>0 && os>0 && os-zs==1) str.append(Math.min(nsame, same)).append("\n");
        else if(zd==od && od>0) str.append(nsame).append("\n");
        else if(os>0 && os-zs==1) str.append(same).append("\n");
        else str.append("-1\n");
      }
    } 
  }

  public static void main(String[] args) throws java.lang.Exception {
    BufferedReader bf;
    PrintWriter pw;
    boolean lenv=false;
    if(lenv){
      bf = new BufferedReader(
                          new FileReader("input.txt"));
      pw=new PrintWriter(new
            BufferedWriter(new FileWriter("output.txt")));
    }else{
      bf = new BufferedReader(new InputStreamReader(System.in));
      pw = new PrintWriter(new OutputStreamWriter(System.out));
    }
    
    int t = Integer.parseInt(bf.readLine().trim());
    while (t-- > 0) {
      String st[]=bf.readLine().trim().split("\\s+");
      n=Integer.parseInt(st[0]);
      a=bf.readLine().trim().toCharArray();
      b=bf.readLine().trim().toCharArray();
      solve();
    }
    pw.println(str);
    pw.flush();
    // System.outin.print(str);
  }
}