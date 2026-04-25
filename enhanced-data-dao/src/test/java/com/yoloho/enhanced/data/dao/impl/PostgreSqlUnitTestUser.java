package com.yoloho.enhanced.data.dao.impl;

import com.yoloho.enhanced.data.dao.api.Enhanced;
import com.yoloho.enhanced.data.dao.api.PrimaryKey;

/**
 * PostgreSQL 方言容器测试使用的实体。
 * <p>
 * 对应 {@code postgresql-unittest.sql} 中的 {@code postgres_unit_test_user} 表，用于覆盖
 * PostgreSQL 标识符引用、serial generated keys、唯一键冲突和 JSONB 字段写入/查询。
 */
@Enhanced(name = "postgresUnitTestUserEnhancedDao", tableName = "postgres_unit_test_user")
public class PostgreSqlUnitTestUser {
    @PrimaryKey(autoIncrement = true)
    private Integer id;
    private int uid;
    private String displayName;
    private int score;
    private Object profile;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public Object getProfile() {
        return profile;
    }

    public void setProfile(Object profile) {
        this.profile = profile;
    }
}
