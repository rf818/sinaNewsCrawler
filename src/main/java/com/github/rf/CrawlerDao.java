package com.github.rf;

import java.sql.SQLException;

public interface CrawlerDao {
    void insertProcessedLink(String link) throws SQLException;

    boolean isProcessedLink(String link) throws SQLException;

    void insertLink(String href) throws SQLException;

    String getLinkAndDelete() throws SQLException;

    void updateNewsTable(String link, String title, String content) throws SQLException;
}
