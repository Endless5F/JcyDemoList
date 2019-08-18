package com.android.javalib;

public class TestClass {

    public static void main(String[] args) {
        Source response = getResponse(1001);
        response.read();
    }

    public static Source getResponse(int i) {
        int j = i;
        Source source = new Source() {
            @Override
            void read() {
                System.out.println(j);
            }
        };
        return source;
    }

    public static abstract class Source {
        abstract void read();
    }
}
