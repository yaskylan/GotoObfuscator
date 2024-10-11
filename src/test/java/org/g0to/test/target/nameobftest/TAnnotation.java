package org.g0to.test.target.nameobftest;

public @interface TAnnotation {
    int value();

    String name() default "Default name";

    String desc() default "Default descriptor";
}
