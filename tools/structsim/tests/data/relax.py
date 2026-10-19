def relax(dist, v, to, w):
    if dist[v] + w < dist[to]:
        dist[to] = dist[v] + w
