package org.g0to.test.target;

public class Main {
    public static void main(String[] args) {
        final Main main = new Main();

        main.virtualMethod();
    }

    private void virtualMethod() {
        final Iface iface = ((s, i, j) -> {
            sout(s);
            sout(i);
            sout(j);

            return "hello";
        });

        sout(iface.test("123231213", 123, 321L));
    }

    private static void sout(Object o) {
        System.out.println(o);
    }
}
