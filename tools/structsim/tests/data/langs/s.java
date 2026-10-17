class S {
    static int solve(int[] items) {
        int total = 0;
        for (int i = 0; i < items.length; i++) {
            if (items[i] > 0) {
                total = total + items[i];
            }
        }
        return total;
    }
}
