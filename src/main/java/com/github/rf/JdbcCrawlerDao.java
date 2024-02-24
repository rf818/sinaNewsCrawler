package com.github.rf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.sql.*;

public final class JdbcCrawlerDao implements CrawlerDao {
    private final Connection connection;

    @SuppressFBWarnings("DMI_CONSTANT_DB_PASSWORD")
    public JdbcCrawlerDao() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/news?characterEncoding=utf8", "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insertProcessedLink(String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into LINK_ALREADY_PROCESSED (link) values (?)")) {
            statement.setString(1, link);
            statement.executeUpdate();
        }
    }

    @Override
    public boolean isProcessedLink(String link) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINK_ALREADY_PROCESSED where link = ?")) {
            statement.setString(1, link);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        }
    }

    @Override
    public void insertLink(String href) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into LINK_TO_BE_PROCESSED (link) values(?)")) {
            statement.setString(1, href);
            statement.executeUpdate();
        }
    }

    @Override
    public synchronized String getLinkAndDelete() throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("select link from LINK_TO_BE_PROCESSED limit  1")) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String link = resultSet.getString(1);
                deleteLink(link);
                return link;
            }
            return null;
        }
    }

    private void deleteLink(String link) throws SQLException {
        try (PreparedStatement statementDelete = connection.prepareStatement("delete from LINK_TO_BE_PROCESSED where link = ?")) {
            statementDelete.setString(1, link);
            statementDelete.executeUpdate();
        }
    }

    @Override
    public void updateNewsTable(String link, String title, String content) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("insert into NEWS (url,title, content, created_at, modified_at) values (?,?,?, now(), now())")) {
            statement.setString(1, link);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }
}
