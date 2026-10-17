func solve(_ items: [Int]) -> Int {
    var total = 0
    for i in 0..<items.count {
        if items[i] > 0 {
            total = total + items[i]
        }
    }
    return total
}
