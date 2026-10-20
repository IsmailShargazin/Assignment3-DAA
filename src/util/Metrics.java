package util;

// Create a new Metrics object for each algorithm run.
public class Metrics {
    public long edgeExaminations;
    public long comparisons;
    public long queueOperations;
    public long findCalls;
    public long parentTraversals;
    public long unionCalls;

    public void add(Metrics other) {
        edgeExaminations += other.edgeExaminations;
        comparisons += other.comparisons;
        queueOperations += other.queueOperations;
        findCalls += other.findCalls;
        parentTraversals += other.parentTraversals;
        unionCalls += other.unionCalls;
    }
}