package org.g0to.test.target.nameobftest;

public abstract class Parent1 {
    public abstract String abstractMethod1();
    public abstract String abstractMethod2();

    public String method1() {
        return "Parent1.method1";
    }

    public final String method2() {
        return "Parent1.method2";
    }

    private String method3() {
        return "Parent1.method3";
    }
}
