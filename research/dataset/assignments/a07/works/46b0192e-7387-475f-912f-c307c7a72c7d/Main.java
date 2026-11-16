import java.util.*;

public class Solution {
	
	public static int minMoves(int[] input) {
		List<Integer> people = new ArrayList<Integer>();
		List<Integer> chairs = new ArrayList<Integer>();
		
		for (int i = 0; i < input.length; i++) {
			if (input[i] == 1) {
				people.add(i);
			} else {
				chairs.add(i);
			}
		}
		
		int[] memo = new int[chairs.size() + 1];
		

		for (int p = 1; ((!people.isEmpty()) && (p <= people.size())); p++) {
			int prev = memo[p];
			memo[p] = memo[p - 1] + Math.abs(people.get(p - 1) - chairs.get(p - 1));
			for (int c = p + 1; c <= chairs.size(); c++) {
				int tmp = memo[c];
				memo[c] = Math.min(memo[c - 1], prev + Math.abs(people.get(p - 1) - chairs.get(c - 1)));
				prev = tmp;
			}
		}
		return memo[memo.length - 1];
	}
	
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		int n = sc.nextInt();
		int[] input = new int[n];
		for (int i = 0; i < n; i++) {
			input[i] = sc.nextInt();
		}
		
		System.out.println(Solution.minMoves(input));
		
	}
}