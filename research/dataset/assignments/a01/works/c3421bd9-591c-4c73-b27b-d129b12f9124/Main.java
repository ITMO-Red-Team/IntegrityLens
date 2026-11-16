import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class C {

    private static final boolean TEST_MODE = true;
    private static Node[] tree;
    private static long[] lWt, rWt;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        int T = getInt(br);
        for (int t=0; t<T; t++) {
            int N = getInt(br);

            tree = new Node[N+1];
            for (int v = 1; v <= N; v++) {
                String s = br.readLine();
                tree[v] = new Node(s);
            }

            for (int e=1; e<=N-1; e++) {
                int[] arr = getIntArray(2, br);
                int u = arr[0];
                int v = arr[1];
                tree[u].addNbr(v);
                tree[v].addNbr(u);
            }
            rootTheTree();

            lWt = new long[N + 1];
            rWt = new long[N + 1];
            Arrays.fill(lWt, -1);
            Arrays.fill(rWt, -1);
            getMaxBeauty();
        }
    }

    private static void rootTheTree() {
       // Arbitrarily pick the root as #1.
        Node root = tree[1];
        root.parent = 0;
        Queue<Integer> q = new LinkedList<>();
        q.add(1);

        // Constraint: Every node that is in the queue has parent already assigned.
        while (!q.isEmpty()) {
            int currId = q.poll();
            Node currNode = tree[currId];
            // Add all neighbours as children.
            for (int nbrId : currNode.nbrs) {
                if (nbrId == currNode.parent) {
                    continue;
                }
                tree[nbrId].parent = currId;
                currNode.addChild(nbrId);
                q.add(nbrId);
            }
//            currNode.nbrs.clear();
        }
    }

    private static void getMaxBeauty() throws Exception {
        Stack<Integer> stack = new Stack<>();
        stack.add(1);

        while (!stack.isEmpty()) {
            int currId = stack.pop();
            Node curr = tree[currId];

            // Is leaf ?
            if (curr.children.size() == 0) {
                lWt[currId] = 0;
                rWt[currId] = 0;
                continue;
            }
            // Are all children processed ?
            boolean allChildrenProcessed = (lWt[curr.children.get(0)] >= 0);
            if (allChildrenProcessed) {
                lWt[currId] = 0;
                rWt[currId] = 0;
                // Compute lwt and rwt maximized against each child.
                for (int childId : curr.children) {
                    Node childNode = tree[childId];
                    long ll = Math.abs(curr.left - childNode.left) + lWt[childId];
                    long lr = Math.abs(curr.left - childNode.right) + rWt[childId];
                    lWt[currId] += Math.max(ll, lr);

                    long rl = Math.abs(curr.right - childNode.left) + lWt[childId];
                    long rr = Math.abs(curr.right - childNode.right) + rWt[childId];
                    rWt[currId] += Math.max(rl, rr);
                }
            } else { // Add all unvisited children.
                stack.add(currId);
                stack.addAll(curr.children);
            }
        }
        long res = Math.max(lWt[1], rWt[1]);
        System.out.println(res);
    }

    private static int getLeftId(int nodeId) {
        return tree[nodeId].left;
    }

    private static int getRightId(int nodeId) {
        return tree[nodeId].right;
    }

    private static Integer getInt(BufferedReader br) throws Exception {
        return Integer.parseInt(br.readLine());
    }

    private static Long getLong(BufferedReader br) throws Exception {
        return Long.parseLong(br.readLine());
    }

    private static int[] getIntArray(int N, BufferedReader br) throws Exception {
        String s = br.readLine();
        String[] tokens = s.split(" ");
        int[] result = new int[N];

        for (int i=0; i<N; i++) {
            result[i] = Integer.parseInt(tokens[i]);
        }
        return result;
    }

    private static long[] getLongArray(int N, BufferedReader br) throws Exception {
        String s = br.readLine();
        String[] tokens = s.split(" ");
        long[] result = new long[N+1];

        for (int i=0; i<N; i++) {
            result[i+1] = Long.parseLong(tokens[i]);
        }
        return result;
    }

    private static void printArray(String message, Object[] arr) {
        if (!TEST_MODE) {
            return;
        }
        System.out.print(message + " : ");
        for (int i=0; i<arr.length; i++) {
            System.out.print(arr[i] + " ");
        }
        System.out.println();
    }
}

class Node {
    int parent = -1;
    List<Integer> nbrs = new ArrayList<>();
    List<Integer> children = new ArrayList<>();
    int left, right;

    public Node(int l, int r) {
        this.left = l;
        this.right = r;
    }

    public Node(String s) {
        String[] tokens = s.split(" ");
        this.left = Integer.parseInt(tokens[0]);
        this.right = Integer.parseInt(tokens[1]);
    }

    public void addNbr(int nbr) {
        nbrs.add(nbr);
    }

    public void addChild(int child) {
        children.add(child);
    }
}