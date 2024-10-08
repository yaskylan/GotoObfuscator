package org.g0to.test.target;

import java.util.Arrays;

public class Main {
    private static final EnumTest ENUMBB = EnumTest.BB;

    @TestAnnotation
    public static void main(String[] args) {
        KotlinMain.INSTANCE.runKt();

        Test.a("123", "321", "1");
        new Test().a("321", "123");

        final RecordTest recordTest = new RecordTest("ok ENUMBB == ", "EnumTest.BB");

        System.out.println(recordTest.hashCode());
        System.out.println(recordTest.toString());

        if (ENUMBB == EnumTest.BB) {
            System.out.println(recordTest.sum());
        }

        System.out.println(recordTest.data1());
        System.out.println(recordTest.data2());

        System.out.println(Arrays.toString(EnumTest.values()));

        final Sub1 sub1 = new Sub2();
        sub1.simpleMethod();
    }

    public static class GenericTest<T> {
        private T value;

        public GenericTest(T value) {
            this.value = value;
        }

        @TestAnnotation(xxx = 222)
        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    public static class Test implements Iface, Iface2 {
        @Override
        public String test(String s, int i, Long j) {
            return s + i + j;
        }

        public static void a(String s1, String s2, String s3) {
            System.out.println("INVOKESTATIC" + s1 + s2 + s3);
        }

        public void a(String s1, String s2) {
            System.out.println("INVOKEVIRTUAL" + s1 + s2);
        }
    }
}
