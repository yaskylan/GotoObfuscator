package org.g0to.test.target.nameobftest;

public class Sub2 extends Parent2 implements TFunctionalInterface {
    public final String field1 = "Parent3.field1";

    @Override
    public String abstractMethod() {
        return "Sub2.abstractMethod";
    }

    @Override
    public String abstractMethod1() {
        return "Sub2.abstractMethod1";
    }

    @Override
    public String abstractMethod2() {
        return "Sub2.abstractMethod2";
    }

    public String method4() {
        return "Sub2.method4";
    }
}
