package dsu;

import util.Metrics;

public class DisjointSetUnion {
    private final int[] parent;
    private final int[] rank;

    private int components;

    private final Metrics metrics;

    public DisjointSetUnion(int size, Metrics metrics) {
        if (size < 0) {
            throw new IllegalArgumentException(
                    "Size cannot be negative."
            );
        }

        this.metrics = metrics;

        parent = new int[size];
        rank = new int[size];

        components = size;

        for (int i = 0; i < size; i++) {
            parent[i] = i;
        }
    }

    public int find(int vertex) {
        if (vertex < 0 || vertex >= parent.length) {
            throw new IllegalArgumentException(
                    "Invalid DSU vertex: " + vertex
            );
        }

        metrics.findCalls++;

        int root = vertex;

        while (root != parent[root]) {
            metrics.parentTraversals++;
            root = parent[root];
        }

        // Path compression.
        while (vertex != root) {
            int next = parent[vertex];

            metrics.parentTraversals++;

            parent[vertex] = root;
            vertex = next;
        }

        return root;
    }

    public boolean union(int first, int second) {
        metrics.unionCalls++;

        int rootA = find(first);
        int rootB = find(second);

        if (rootA == rootB) {
            return false;
        }

        // Union by rank.
        if (rank[rootA] < rank[rootB]) {
            parent[rootA] = rootB;
        } else if (rank[rootA] > rank[rootB]) {
            parent[rootB] = rootA;
        } else {
            parent[rootB] = rootA;
            rank[rootA]++;
        }

        components--;

        return true;
    }

    public int getComponents() {
        return components;
    }
}