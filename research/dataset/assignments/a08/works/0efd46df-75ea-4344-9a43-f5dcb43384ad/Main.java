import java.util.Scanner;

public class Main {

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        int t = scanner.nextInt();
        while(t-- > 0)
        {
            int n = scanner.nextInt();
            int[] time = new int[n];
            int[] h = new int[n];
            for(int i  = 0; i < n; i++)
            {
                time[i] = scanner.nextInt();
            }
            for(int j = 0; j < n; j++)
            {
                h[j] = scanner.nextInt();
            }
            long prevMTime = time[n-1];
            long prevMHealth = h[n-1];
            long manaRequired = 0;
            long currMTime = time[n-1];
            long currMHealth = h[n-1];
            long spellCastingTime = prevMTime - prevMHealth + 1;

            for(int i = n-2; i >= 0; i--)
            {
                currMHealth = h[i];
                currMTime = time[i];
                //spellCastingTime = prevMTime - prevMHealth + 1;
                if(spellCastingTime <= currMTime && currMTime - spellCastingTime + 1 < currMHealth)
                {
                    spellCastingTime = currMTime - currMHealth + 1;
                }
                else if(spellCastingTime > currMTime)
                {
                    long hm = (prevMTime - spellCastingTime) + 1;
                    manaRequired += (hm * (hm+1)) / 2;
                    prevMTime =currMTime;
                    prevMHealth = currMHealth;
                    spellCastingTime = prevMTime - prevMHealth + 1;
                }
            }
            long hm = (prevMTime - spellCastingTime) + 1;
            manaRequired += (hm * (hm+1)) / 2;
            System.out.println(manaRequired);

        }
    }

}
