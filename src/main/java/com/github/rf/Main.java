package com.github.rf;

public class Main {
    public static void main(String[] args) {
        for (int i = 0; i < 8; i++) {
            new Thread(() -> new Crawler().run()).start();
        }
    }
}
