package com.yoloho.enhanced.data.dao.impl;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.postgresql.util.PGobject;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.google.common.collect.Maps;
import com.yoloho.enhanced.data.dao.annotations.EnableEnhancedDao;
import com.yoloho.enhanced.data.dao.annotations.EnableSqlSessionFactory;
import com.yoloho.enhanced.data.dao.api.PostgreSqlEnhancedDao;
import com.yoloho.enhanced.data.dao.api.PostgreSqlExpr;
import com.yoloho.enhanced.data.dao.api.UpdateEntry;
import com.yoloho.enhanced.data.dao.api.dialect.DialectType;
import com.yoloho.enhanced.data.dao.api.dialect.Dialects;
import com.yoloho.enhanced.data.dao.api.filter.DynamicQueryFilter;
import com.yoloho.enhanced.data.dao.testsupport.PostgreSqlContainerInitializer;

/**
 * PostgreSQL 容器版 enhanced-dao 方言集成测试。
 * <p>
 * 使用 {@link PostgreSqlContainerInitializer} 启动临时 PostgreSQL，并通过
 * {@link EnableEnhancedDao#dialect()} 显式绑定 {@link DialectType#POSTGRESQL}。
 * 使用 {@code mvn test -Pdao-container-integration-tests -Dtest=ContainerPostgreSqlEnhancedDaoTest}
 * 运行；本地 Docker 必须可用。
 * <p>
 * 覆盖内容包括 PostgreSQL DAO 实现选择、JSONB 方言表达式、分页排序、generated keys、
 * {@code on conflict do nothing}、普通 update/sum/remove，以及 PostgreSQL 下显式
 * update/delete limit 的保护行为。
 */
@SpringBootApplication
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = ContainerPostgreSqlEnhancedDaoTest.class)
@ContextConfiguration(initializers = PostgreSqlContainerInitializer.class)
@EnableSqlSessionFactory(
    name = "testSessionFactory",
    connectionUrl = "${dao.test.jdbc.url}",
    username = "${dao.test.jdbc.username}",
    password = "${dao.test.jdbc.password}",
    charset = ""
)
@EnableEnhancedDao(
    scanPath = "com.yoloho.enhanced.data.dao.impl",
    sqlSessionFactory = "testSessionFactory",
    dialect = DialectType.POSTGRESQL
)
@Tag("container-integration")
public class ContainerPostgreSqlEnhancedDaoTest {
    @Resource(name = "postgresUnitTestUserEnhancedDao")
    private PostgreSqlEnhancedDao<PostgreSqlUnitTestUser, Integer> dao;

    @Test
    public void generatedDaoShouldUsePostgreSqlImplementationTest() {
        Assertions.assertTrue(dao instanceof PostgreSqlEnhancedDaoImpl);
    }

    @Test
    public void queryShouldUsePostgreSqlDialectSqlTest() {
        DynamicQueryFilter filter = new DynamicQueryFilter()
                .expr(PostgreSqlExpr.jsonbContains(PostgreSqlUnitTestUser.class, "profile",
                        "{\"channel\":\"amazon\"}"))
                .orderBy("score", true)
                .limit(0, 1);

        List<PostgreSqlUnitTestUser> list = dao.find(filter.getQueryData(Dialects.postgresql()));

        Assertions.assertEquals(1, list.size());
        Assertions.assertEquals("gamma", list.get(0).getDisplayName());
        Assertions.assertEquals(2, dao.count(new DynamicQueryFilter()
                .expr(PostgreSqlExpr.jsonbContains(PostgreSqlUnitTestUser.class, "profile",
                        "{\"channel\":\"amazon\"}"))
                .getQueryData(Dialects.postgresql())));
    }

    @Test
    public void writeShouldSupportPostgreSqlConflictAndGeneratedKeysTest() throws SQLException {
        dao.remove(new DynamicQueryFilter().equalPair("uid", 1000).getQueryData());
        dao.remove(new DynamicQueryFilter().equalPair("uid", 1001).getQueryData());

        PostgreSqlUnitTestUser bean = new PostgreSqlUnitTestUser();
        bean.setUid(1000);
        bean.setDisplayName("delta");
        bean.setScore(40);
        bean.setProfile(jsonb("{\"channel\":\"amazon\",\"tier\":\"bronze\"}"));
        bean = dao.insertOnConflictDoNothingAndReturn(bean, "uid");
        Assertions.assertTrue(bean.getId() > 0);

        PostgreSqlUnitTestUser duplicate = new PostgreSqlUnitTestUser();
        duplicate.setUid(1000);
        duplicate.setDisplayName("delta-new");
        duplicate.setScore(99);
        duplicate.setProfile(jsonb("{\"channel\":\"amazon\",\"tier\":\"platinum\"}"));
        Assertions.assertEquals(0, dao.insertOnConflictDoNothing(duplicate, "uid"));

        PostgreSqlUnitTestUser saved = dao.get("uid", 1000);
        Assertions.assertEquals("delta", saved.getDisplayName());
        Assertions.assertEquals(40, saved.getScore());

        PostgreSqlUnitTestUser another = new PostgreSqlUnitTestUser();
        another.setUid(1001);
        another.setDisplayName("epsilon");
        another.setScore(55);
        another.setProfile(jsonb("{\"channel\":\"shopify\",\"tier\":\"gold\"}"));
        another = dao.insertAndReturn(another);
        Assertions.assertTrue(another.getId() > 0);

        saved.setScore(41);
        saved.setProfile(jsonb(saved.getProfile().toString()));
        Assertions.assertEquals(1, dao.update(saved));

        Map<String, UpdateEntry> data = Maps.newHashMap();
        data.put("score", new UpdateEntry().increse(1));
        Assertions.assertEquals(1, dao.update(data, new DynamicQueryFilter().equalPair("uid", 1001).getQueryData()));
        Assertions.assertEquals(56, dao.get("uid", 1001).getScore());
        Assertions.assertEquals(97, dao.sum("score", new DynamicQueryFilter().greaterOrEqual("uid", 1000).getQueryData()));

        Assertions.assertEquals(2, dao.remove(new DynamicQueryFilter().greaterOrEqual("uid", 1000).getQueryData()));
    }

    @Test
    public void updateDeleteLimitShouldRejectExplicitLimitTest() {
        Map<String, UpdateEntry> data = Maps.newHashMap();
        data.put("score", new UpdateEntry().increse(1));

        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dao.update(data, new DynamicQueryFilter().equalPair("uid", 1).limit(1).getQueryData()));
        Assertions.assertThrows(UnsupportedOperationException.class,
                () -> dao.remove(new DynamicQueryFilter().equalPair("uid", 1).limit(1).getQueryData()));
    }

    private PGobject jsonb(String value) throws SQLException {
        PGobject object = new PGobject();
        object.setType("jsonb");
        object.setValue(value);
        return object;
    }
}
