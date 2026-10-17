fn solve(items: &[i32]) -> i32 {
    let mut total = 0;
    for i in 0..items.len() {
        if items[i] > 0 {
            total = total + items[i];
        }
    }
    total
}
