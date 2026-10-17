def total(items, flag):
    s = 0
    for i in range(len(items)):
        if items[i] > 1 and flag:
            s += items[i]
        elif items[i] == 2:
            continue
        else:
            break
    while s > 100:
        s = s - 1
    name = "done"
    pair = (s, flag)
    out = [s, 0]
    return total(items, False) if flag else s
