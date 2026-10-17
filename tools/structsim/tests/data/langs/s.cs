class S {
    static int Solve(int[] items) {
        int total = 0;
        for (int i = 0; i < items.Length; i++) {
            if (items[i] > 0) {
                total = total + items[i];
            }
        }
        return total;
    }
}
