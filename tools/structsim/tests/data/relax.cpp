void relax(int *dist, int v, int to, int w) {
    if (dist[v] + w < dist[to]) {
        dist[to] = dist[v] + w;
    }
}
