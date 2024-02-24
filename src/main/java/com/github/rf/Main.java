package com.github.rf;

public class Main {
    public static void main(String[] args) {
        MybatisCrawlerDao dao = new MybatisCrawlerDao();
        for (int i = 0; i < 8; i++) {
            new Crawler(dao).start();
        }
    }
}
