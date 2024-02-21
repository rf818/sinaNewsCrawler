package com.github.rf;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.stream.Collectors;

public class Crawler {
    private final JdbcCrawlerDao crawlerDao = new JdbcCrawlerDao();

    public static void main(String[] args) {
        new Crawler().run();
    }

    public void run() {
        String link;
        try {
            while ((link = getLinkThenDelete()) != null) {
                Document doc = startHttpClientAndParseHtml(link);
                parseDocumentAndStoreHrefIntoDb(doc);
                storeArticleIntoDb(doc, link);
                crawlerDao.insertProcessedLink(link);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private void storeArticleIntoDb(Document doc, String link) {
        doc.select("article").forEach(article -> {
            String title = article.select("h1").text();
            String content = article.select("p").stream().map(Element::text).collect(Collectors.joining("\n"));
            try {
                crawlerDao.insertNews(link, title, content);
                System.out.println(link);
                System.out.println(title);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void parseDocumentAndStoreHrefIntoDb(Document doc) {
        doc.select("a").stream().map(aTag -> aTag.attr("href")).forEach(href -> {
            if (isInterest(href) && !isProcessed(href)) {
                try {
                    if (href.startsWith("//")) {
                        href = "https:" + href;
                    }
                    crawlerDao.insertLink(href);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private boolean isInterest(String href) {
        return ("https://sina.cn".equals(href) || href.contains("news.sina.cn") || href.contains("k.sina.cn")) && !href.contains("passport") && !href.contains("javascript");
    }

    private Document startHttpClientAndParseHtml(String link) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(link);
            httpGet.setHeader("User-Agent", "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10_6_8; en-us) AppleWebKit/534.50 (KHTML, like Gecko) Version/5.1 Safari/534.50");
            try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {
                return Jsoup.parse(EntityUtils.toString(httpResponse.getEntity(), StandardCharsets.UTF_8));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private boolean isProcessed(String link) {
        try {
            return crawlerDao.countProcessedLink(link) != 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized String getLinkThenDelete() {
        String link;
        try {
            link = crawlerDao.selectLink();
            if (link != null) {
                crawlerDao.deleteLink(link);
                return link;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }
}
