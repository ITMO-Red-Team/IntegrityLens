def solve(items):
    total = 0
    for i in range(len(items)):
        if items[i] > 0:
            total = total + items[i]
    return total
