package org.g0to.test.target;

public class Main {
    public static void main(String[] args) {
        KotlinMain.INSTANCE.runKt();

        final Iface iface = new Test();
        final Iface2 iface2 = new Test();

        System.out.println(iface.test("123", 1, 11L));
        System.out.println(iface2.test("321", 2, 10L));
    }

    public static class Test implements Iface, Iface2 {
        @Override
        public String test(String s, int i, Long j) {
            return s + i + j;
        }
    }
}
