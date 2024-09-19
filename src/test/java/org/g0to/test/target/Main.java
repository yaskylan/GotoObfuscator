package org.g0to.test.target;

public class Main {
    public static void main(String[] args) {
        KotlinMain.INSTANCE.runKt();

        System.out.println("Genshin impact");
        System.out.println("你说得对, 但是原神后面忘了");

        final GenericTest<KotlinMain.KotlinObject> genericTest = new GenericTest<>(new KotlinMain.KotlinObject());
        System.out.println(genericTest.getValue());
        genericTest.setValue(new KotlinMain.KotlinObject());
        System.out.println(genericTest.getValue());
    }

    public static class GenericTest<T> {
        private T value;

        public GenericTest(T value) {
            this.value = value;
        }

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
