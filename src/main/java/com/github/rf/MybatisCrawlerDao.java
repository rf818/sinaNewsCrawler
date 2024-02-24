package com.github.rf;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class MybatisCrawlerDao implements CrawlerDao {
    private final SqlSessionFactory sqlSessionFactory;

    @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
    public MybatisCrawlerDao() {
        try {
            InputStream inputStream = Resources.getResourceAsStream("mybatisData/config.xml");
            sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void insertProcessedLink(String link) throws SQLException {
        Map<String, Object> tableMap = new HashMap<>();
        tableMap.put("tableName", "LINK_ALREADY_PROCESSED");
        tableMap.put("link", link);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.rf.myMapper.insertLink", tableMap);
        }
    }

    @Override
    public boolean isProcessedLink(String link) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession()) {
            int num = session.selectOne("com.github.rf.myMapper.countProcessedLink", link);
            return num > 0;
        }

    }

    @Override
    public void insertLink(String href) throws SQLException {
        Map<String, Object> tableMap = new HashMap<>();
        tableMap.put("tableName", "LINK_TO_BE_PROCESSED");
        tableMap.put("link", href);
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.rf.myMapper.insertLink", tableMap);
        }
    }

    @Override
    public synchronized String getLinkAndDelete() throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            String link = session.selectOne("com.github.rf.myMapper.selectLink");
            if (link != null) {
                session.delete("com.github.rf.myMapper.deleteLink", link);
                return link;
            }
            return null;
        }
    }

    @Override
    public void updateNewsTable(String link, String title, String content) throws SQLException {
        try (SqlSession session = sqlSessionFactory.openSession(true)) {
            session.insert("com.github.rf.myMapper.insertNews", new News(link, title, content, Instant.now(), Instant.now()));
        }
    }
}
