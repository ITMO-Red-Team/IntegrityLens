int solve(std::vector<int> items) {
    int total = 0;
    for (int i = 0; i < items.size(); i++) {
        if (items[i] > 0) {
            total = total + items[i];
        }
    }
    return total;
}
