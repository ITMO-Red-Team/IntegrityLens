// package com.company;


import com.sun.security.jgss.GSSUtil;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.*;

//ankit singh

public class Main {

   static int inf=Integer.MAX_VALUE/2;
    public static void main(String[] args) {
       Scanner input=new Scanner(System.in);
       int nn=input.nextInt();
        int a[]=new int[nn];
        ArrayList<Integer> one=new ArrayList<>();
        ArrayList<Integer> zero=new ArrayList<>();
        for (int i = 0; i <nn ; i++) {
            a[i]=input.nextInt();
            if(a[i]==1){
                one.add(i);
            }else zero.add(i);
        }
        if (one.size()==0) {
            System.out.println(0);
            return;
        }
        int n=one.size();
        int m=zero.size();
        int dp[][]=new int[n+1][m+1];
        int ans=0;
        for (int i = 1; i <=n ; i++) {
             dp[i][0]=inf;
            for (int j = 1; j <=m ; j++) {
                dp[i][j]=inf;

                //take the current
                dp[i][j]=Math.min(dp[i][j],dp[i-1][j-1]+Math.abs(one.get(i-1)-zero.get(j-1)));

                //take previos
                dp[i][j]=Math.min(dp[i][j],dp[i][j-1]);


            }
        }
        ans=inf;
        for (int i = 1; i <=m ; i++) {
            ans=Math.min(ans,dp[n][i]);
        }
        System.out.println(ans);



    }

}