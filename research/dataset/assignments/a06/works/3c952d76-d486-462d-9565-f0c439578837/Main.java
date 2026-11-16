// package com.company;

import java.util.Scanner;

public class Main {

    public static void solution1(){
        Scanner scanner = new Scanner(System.in);
        int t = scanner.nextInt();
        while (t-- != 0){
            int n, k1, k2, w, b;
            n = scanner.nextInt();
            k1 = scanner.nextInt();
            k2 = scanner.nextInt();
            w = scanner.nextInt();
            b = scanner.nextInt();
            if (k1 < k2){
                int temp = k1;
                k1 = k2;
                k2 = temp;
            }
            int white_dominoes = k2 + (k1 - k2) / 2;
            int black_dominoes = (n - k1) + (k1 - k2) / 2;
            if (white_dominoes >= w && black_dominoes >= b)
            {
                System.out.println("YES");
            }
            else
                System.out.println("NO");
        }
        scanner.close();
    }

    public static void solution2(){
        Scanner scanner = new Scanner(System.in);
        int t = scanner.nextInt();
        scanner.nextLine();
        while (t-- != 0) {
            String s;
            s = scanner.nextLine();
            boolean flag = true;
            int first_ones = -1, last_zeroes = -1;
            for (int i=0; i<s.length() - 1; i++){
                if (s.charAt(i) == '1' && s.charAt(i+1) == '1'){
                    first_ones = i;
                    break;
                }
            }
            for (int i=s.length() - 1; i>0; i--){
                if (s.charAt(i) == '0' && s.charAt(i-1) == '0'){
                    last_zeroes = i-1;
                    break;
                }
            }
            if (first_ones != -1 && last_zeroes != -1 && first_ones < last_zeroes) flag = false;
            if (flag) System.out.println("YES");
            else System.out.println("NO");
        }
        scanner.close();
    }

    public static void solution3(){
        Scanner scanner = new Scanner(System.in);
        int t = scanner.nextInt();
        while (t-- > 0){
            int n;
            n = scanner.nextInt();
            int[] costs = new int[n];
            for (int i=0; i<n; i++) costs[i] = scanner.nextInt();
            //starts with Even index 0
            long minEven = costs[0];
            long minOdd = costs[1];
            long totalEven = minEven;
            long totalOdd = minOdd;
            long minCost = minEven * n + minOdd * n;
            for (int i=2; i<n; i++){
                if (i%2 == 1){
                    minOdd = Math.min(minOdd, costs[i]);
                    totalOdd += costs[i];
                }
                else{
                    minEven = Math.min(minEven, costs[i]);
                    totalEven += costs[i];
                }
                long this_cost = totalEven - minEven + minEven * (n - (i+2)/2 + 1) + totalOdd - minOdd + minOdd * (n - (i+1)/2 + 1);
                minCost = Math.min(minCost, this_cost);
            }
            System.out.println(minCost);
        }
        scanner.close();
    }

    public static long power(int a, int b){
        long ans = 1;
        while (b > 0){
            if (b%2 == 1){
                ans *= a;
            }
            a *= a;
            b >>= 1;
        }
        return ans;
    }

    public static void main(String[] args) {
        solution3();
    }
}