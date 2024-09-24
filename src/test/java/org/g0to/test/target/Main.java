package org.g0to.test.target;

import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Main {
    @TestAnnotation
    public static void main(String[] args) {
        KotlinMain.INSTANCE.runKt();

        final String s1 = new Scanner(System.in).nextLine();
        final String s2 = new Scanner(System.in).nextLine();

        System.out.println(ThreadLocalRandom.current().nextLong() + s1);

        final Test test = new Test();

        System.out.println(test.test("test", 12, 877L));
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
    }
}
