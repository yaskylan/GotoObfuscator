package org.g0to.test.target.nameobftest;

public class Sub1 extends Parent3 implements TFunctionalInterface {
    public String field1 = "Sub1.field1";

    @TAnnotation(value = 66666, name = "Sub1.abstractMethod name", desc = "Sub1.abstractMethod desc")
    @Override
    public String abstractMethod() {
        return "Sub1.abstractMethod";
    }

    @TAnnotation(value = 77777, name = "Sub1.abstractMethod1 name")
    @Override
    public String abstractMethod1() {
        return "Sub1.abstractMethod1";
    }

    @TAnnotation(88888)
    public String method4() {
        return "Sub1.method4";
    }
}
