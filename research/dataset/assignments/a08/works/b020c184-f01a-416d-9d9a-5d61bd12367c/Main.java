import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.StringTokenizer;

public class MonstersAndSpells {
	
	static long rangeSum(long l, long r) {
		return ((r*(r+1))/2) - ((l*(l-1))/2);
	}
	
	static class Event implements Comparable<Event> {
		
		int time;
		int monsterNum;
		boolean isMonster;
		
		public Event(int time, int monsterNum, boolean isMonster) {
			this.time = time; this.monsterNum = monsterNum; this.isMonster = isMonster;
		}
		
		@Override
		public int compareTo(Event o) {
			if(time == o.time) {
				if(!(isMonster ^ o.isMonster)) {
					return monsterNum - o.monsterNum;
				}
				if(o.isMonster) {
					return -1;
				}
				return 1;
			}
			return time - o.time;
		}
		
	}
	
	public static void main(String[] args) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(System.out)));
		StringTokenizer st = new StringTokenizer(br.readLine());
		
		int t = Integer.parseInt(st.nextToken());
		
		for(int test = 0; test < t; test++) {
			
			st = new StringTokenizer(br.readLine());
			
			int n = Integer.parseInt(st.nextToken());
			
			int[] k = new int[n];
			int[] h = new int[n];
			
			st = new StringTokenizer(br.readLine());
			for(int i = 0; i < n; i++)
				k[i] = Integer.parseInt(st.nextToken());
			
			st = new StringTokenizer(br.readLine());
			for(int i = 0; i < n; i++)
				h[i] = Integer.parseInt(st.nextToken());
			
			int[] latestStart = new int[n];
			
			for(int i = 0; i < n; i++) {
				latestStart[i] = k[i] - h[i] + 1;
			}
			
			
			
			PriorityQueue<Event> pq = new PriorityQueue<Event>();
			for(int i = 0; i < n; i++) {
				pq.add(new Event(k[i],i,true));
				pq.add(new Event(latestStart[i],i,false));
			}
			
			long manaUsed = 0;
			int lastEvent = 0;
			
			HashMap<Integer, Event> waiting = new HashMap<Integer, Event>();
			
			long currentSpell = 1;
			
			//pw.println("NEW GAME");
			
			while(!pq.isEmpty()) {
				
				Event nextEvent = pq.poll();
				
				if(!waiting.isEmpty() && nextEvent.time > lastEvent) {
					manaUsed += rangeSum(currentSpell+1, currentSpell + nextEvent.time - lastEvent);
					currentSpell = currentSpell + nextEvent.time - lastEvent;
				}
				if(waiting.isEmpty()) {
					currentSpell = 1;
					manaUsed++;
				}
				
				//pw.println(manaUsed + " " + currentSpell + " " + nextEvent.monsterNum + " " + nextEvent.isMonster);
				
				if(nextEvent.isMonster) {
					waiting.remove(nextEvent.monsterNum);
					
				}
				
				if(!nextEvent.isMonster) {
					waiting.put(nextEvent.monsterNum,nextEvent);
				}
				
				lastEvent = nextEvent.time;
					
			}
			
			pw.println(manaUsed);
			
		}
		
		pw.close();
		
	}

}
