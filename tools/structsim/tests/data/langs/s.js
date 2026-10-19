function solve(items) {
    let total = 0;
    for (let i = 0; i < items.length; i++) {
        if (items[i] > 0) {
            total = total + items[i];
        }
    }
    return total;
}
