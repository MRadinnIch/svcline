package com.svcline.models.clocker;

public enum Operation {
    START(0), STOP(1);

    private final int value;
    Operation(int v) {
        this.value = v;
    }

    public int getValue(){
        return this.value;
    }
}
