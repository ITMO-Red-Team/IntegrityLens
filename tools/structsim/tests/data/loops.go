package main

func f(x int, m int) int {
	for x&m != 0 {
		x = x ^ m
	}
	for i := 0; i < x; i++ {
		m += i
	}
	for {
		break
	}
	return x
}
