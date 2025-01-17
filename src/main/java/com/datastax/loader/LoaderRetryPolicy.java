package com.datastax.loader;

import java.io.File;
import java.io.BufferedWriter;
import java.io.FileWriter;

import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.exceptions.DriverException;

class LoaderRetryPolicy implements RetryPolicy {
    private int numRetries;

    public LoaderRetryPolicy(int inNumRetries) {
        numRetries = inNumRetries;
    }

    // Taken from DefaultRetryPolicy
    public RetryDecision onReadTimeout(Statement statement, ConsistencyLevel cl,
                                       int requiredResponses, 
                                       int receivedResponses, 
                                       boolean dataRetrieved, int nbRetry) {
        System.out.println(String.format("onReadTimeout: nbRetry=%d numRetries=%d statement=%s", nbRetry, numRetries, statement.toString()));
        if (nbRetry != 0)
            return RetryDecision.rethrow();

        return receivedResponses >= requiredResponses && !dataRetrieved 
            ? RetryDecision.retry(cl) 
            : RetryDecision.rethrow();
    }

    // Taken from DefaultRetryPolicy
    public RetryDecision onUnavailable(Statement statement, ConsistencyLevel cl,
                                       int requiredReplica, int aliveReplica, 
                                       int nbRetry) {
        System.out.println(String.format("onUnavailable: nbRetry=%d numRetries=%d statement=%s", nbRetry, numRetries, statement.toString()));
        return RetryDecision.rethrow();
    }

    public RetryDecision onWriteTimeout(Statement statement, 
                                        ConsistencyLevel cl, 
                                        WriteType writeType, int requiredAcks, 
                                        int receivedAcks, int nbRetry) {
        System.out.println(String.format("onWriteTimeout: nbRetry=%d numRetries=%d statement=%s", nbRetry, numRetries, statement.toString()));

        if (nbRetry >= numRetries)
            return RetryDecision.rethrow();

        return RetryDecision.retry(cl);
    }

    public RetryPolicy.RetryDecision onRequestError(Statement statement,
                                                    ConsistencyLevel cl,
                                                    DriverException e,
                                                    int nbRetry) {
        System.out.println(String.format("onRequestError: nbRetry=%d exception=%s", nbRetry, e.toString()));
        return RetryDecision.tryNextHost(cl);
    }

    public void close() {
    }

    public void init(Cluster cluster) {
    }
}
