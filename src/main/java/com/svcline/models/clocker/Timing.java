package com.svcline.models.clocker;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.Exclude;

public class Timing {
    private Operation operation;
    private Timestamp timestamp;

    public Timing() {
    }

    public Timing(Operation operation, Timestamp timestamp) {
        this.operation = operation;
        this.timestamp = timestamp;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Exclude
    public boolean operationExists(Operation operation) {
        return this.operation == operation;
    }

    @Override
    public String toString() {
        return "Timing{" +
               "operation=" + operation +
               ", timestamp=" + timestamp +
               '}';
    }
}
