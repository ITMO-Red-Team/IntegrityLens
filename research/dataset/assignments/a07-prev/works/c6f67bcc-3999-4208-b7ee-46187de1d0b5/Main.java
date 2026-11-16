import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int n = in.nextInt();

        int[] a = new int[n];

        for(int i=0; i<n; i++) {
            a[i] = in.nextInt();
        }

        System.out.println(getMinimumTime(a, n));
    }

    public static int getMinimumTime(int[] a, int n) {
        int[][] minTime = new int[n][n];
        int[] posOfPerson = new int[n];

        int totalOccupied = 0;
        for(int i=0; i<n; i++) {
            if(a[i] == 1) {
                posOfPerson[totalOccupied] = i;
            }
            totalOccupied += a[i];
        }

        for(int i=0; i<n; i++) {
            Arrays.fill(minTime[i], 1, n, Integer.MAX_VALUE);
        }

        if(a[0] == 0) {
            minTime[0][1] = Math.abs(0-posOfPerson[0]);
        }

        int unoccupiedPlaces = 1-a[0];
        for(int i=1; i<n; i++) {
            if(a[i] == 0) {
                unoccupiedPlaces++;
            }

            for(int j=1; j<=Math.min(totalOccupied, unoccupiedPlaces); j++) {
                if(a[i] == 1) {
                    minTime[i][j] = minTime[i-1][j];
                }
                else if(j == unoccupiedPlaces) {
                    minTime[i][j] = minTime[i-1][j-1] + Math.abs(i-posOfPerson[j-1]);
                }
                else {
                    minTime[i][j] = Math.min(minTime[i-1][j], minTime[i-1][j-1] + Math.abs(i-posOfPerson[j-1]));
                }
            }
        }

        return minTime[n-1][totalOccupied];
    }
}
