package org.g0to.test.target.nameobftest;

public class Parent3 extends Parent2 {
    public String field1 = "Parent3.field1";

    @Override
    public String abstractMethod() {
        return "Parent3.abstractMethod";
    }

    @Override
    public String abstractMethod1() {
        return "Parent3.abstractMethod1";
    }

    @Override
    public String abstractMethod2() {
        return "Parent3.abstractMethod2";
    }

    public String function(String var0, String var1, String var2, String var3) {
        return "Parent3.function" + var0 + var1 + var2 + var3;
    }
}
