fun solve(items: IntArray): Int {
    var total = 0
    for (i in items.indices) {
        if (items[i] > 0) {
            total = total + items[i]
        }
    }
    return total
}
