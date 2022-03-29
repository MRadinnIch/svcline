package com.svcline.Routler;


// This Class just keeps track of a Route SubPath/PathVal pair, defining a pair
// as Route: /{SubPath/:SubPathVal:}/{SubPath/:SubPathVal:}/ and repeating.
// If a SubBath lacks a SubPathVal the value for v will be whatever it is set to.
public class Pair {
    String k;
    String v;

    public Pair(String k, String v) {
        this.k = k;
        this.v = v;
    }

    public String getK() {
        return k;
    }

    public String getV() {
        return v;
    }
}
