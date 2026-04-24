package com.yoloho.enhanced.data.util;

import org.junit.Assert;
import org.junit.Test;

import com.yoloho.enhanced.data.dao.api.IgnoreKey;
import com.yoloho.enhanced.data.dao.api.dialect.Dialects;
import com.yoloho.enhanced.data.dao.util.ColumnUtil;

/**
 * 字段占位符解析测试。
 * <p>
 * 覆盖历史 MySQL 反引号兼容行为，以及显式传入 PostgreSQL 方言后的标识符引用行为。
 *
 * @author neal_wei @ Apr 23, 2026
 */
public class ColumnUtilTest {
    @SuppressWarnings("unused")
    private static class Demo {
        String id;
        String displayName;
        @IgnoreKey
        String tmpName;
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getDisplayName() {
            return displayName;
        }
        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }
        public String getTmpName() {
            return tmpName;
        }
        public void setTmpName(String tmpName) {
            this.tmpName = tmpName;
        }
        
    }
    
    @Test
    public void parseColumnNamesTest() {
        String str = "@__self__@ + 1";
        try {
            ColumnUtil.parseColumnNames(null, str, Demo.class);
        } catch (RuntimeException e) {
            Assert.assertTrue(true);
        }
        Assert.assertEquals("`id` + 1", ColumnUtil.parseColumnNames("id", str, Demo.class));
        Assert.assertEquals("`display_name` + 1", ColumnUtil.parseColumnNames("displayName", str, Demo.class));
        Assert.assertEquals("@__self__@ + 1", ColumnUtil.parseColumnNames("tmpName", str, Demo.class));
        str = "@id@ + 1";
        Assert.assertEquals("`id` + 1", ColumnUtil.parseColumnNames(null, str, Demo.class));
        Assert.assertEquals("`id` + 1", ColumnUtil.parseColumnNames("displayName", str, Demo.class));
        str = "round(@id@)";
        Assert.assertEquals("round(`id`)", ColumnUtil.parseColumnNames(null, str, Demo.class));
        Assert.assertEquals("round(`id`)", ColumnUtil.parseColumnNames("displayName", str, Demo.class));
        str = "@tmpName@ + 1";
        Assert.assertEquals("@tmpName@ + 1", ColumnUtil.parseColumnNames(null, str, Demo.class));
    }

    @Test
    public void parseColumnNamesWithDialectTest() {
        String str = "@__self__@ + @id@";
        Assert.assertEquals("\"display_name\" + \"id\"",
                ColumnUtil.parseColumnNames("displayName", str, Demo.class, Dialects.postgresql()));
        Assert.assertEquals("`display_name` + `id`",
                ColumnUtil.parseColumnNames("displayName", str, Demo.class, Dialects.mysql()));
    }

}
