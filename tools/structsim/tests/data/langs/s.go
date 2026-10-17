func solve(items []int) int {
	total := 0
	for i := 0; i < len(items); i++ {
		if items[i] > 0 {
			total = total + items[i]
		}
	}
	return total
}
