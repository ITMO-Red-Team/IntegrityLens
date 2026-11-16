/*
bts songs to dance to:
I need U
Run
ON
Filter
I'm fine
 */
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.abs;
import static java.lang.System.out;
import java.util.*;
import java.io.*;
import java.math.*;

public class x1515C
{
    public static void main(String hi[]) throws Exception
    {
        BufferedReader infile = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(infile.readLine());
        int T = Integer.parseInt(st.nextToken());
        StringBuilder sb = new StringBuilder();
        while(T-->0)
        {
            st = new StringTokenizer(infile.readLine());
            int N = Integer.parseInt(st.nextToken());
            int M = Integer.parseInt(st.nextToken());
            int X = Integer.parseInt(st.nextToken());
            int[] arr = readArr(N, infile, st);
            ArrayList<Unit> ls = new ArrayList<Unit>();
            for(int i=0; i < N; i++)
                ls.add(new Unit(arr[i], i));
            Collections.sort(ls);
            int[] res = new int[N];
            PriorityQueue<Unit> pq = new PriorityQueue<Unit>(Comparator.reverseOrder());
            for(int i=1; i <= M; i++)
                pq.add(new Unit(0, i));
            for(Unit add: ls)
            {
                Unit tower = pq.poll();
                tower.height += add.height;
                res[add.id] = tower.id;
                pq.add(tower);
            }
            int min = pq.poll().height;
            int max = min;
            while(pq.size() > 0)
                max = max(max, pq.poll().height);
            if(max-min > X)
                sb.append("NO\n");
            else
            {
                sb.append("YES\n");
                for(int x: res)
                    sb.append(x+" ");
                sb.append("\n");
            }
        }
        System.out.print(sb);
    }
    public static int[] readArr(int N, BufferedReader infile, StringTokenizer st) throws Exception
    {
        int[] arr = new int[N];
        st = new StringTokenizer(infile.readLine());
        for(int i=0; i < N; i++)
            arr[i] = Integer.parseInt(st.nextToken());
        return arr;
    }
}
class Unit implements Comparable<Unit>
{
    public int id;
    public int height;

    public Unit(int h, int i)
    {
        height = h;
        id = i;
    }
    public int compareTo(Unit oth)
    {
        return oth.height-height;
    }
}