package com.yoloho.enhanced.data.dao.impl;

import java.util.List;

import com.google.common.collect.Lists;
import com.yoloho.enhanced.data.dao.api.filter.QueryData;

/**
 * enhanced-dao 通用 MyBatis XML 的参数对象。
 * <p>
 * 该类在 {@link AbstractEnhancedDao} 内部使用，用于统一保存表名、字段、分页、
 * 更新/删除限制和 insert suffix 等通用 SQL 模板参数。
 *
 * @author jason
 * @author neal_wei @ Apr 25, 2026
 */
public class EnhancedCondition extends QueryData {
    private static final long serialVersionUID = 1L;
    /**
     * 为了规避一些sql解析引擎对表名字`table`的识别性所做的临时处理
     */
    @SuppressWarnings("unused")
    private static final List<String> innerKeywords = Lists.newArrayList(
            "select", "delete", "update", "insert", "replace", 
            "create", "alter"
            );

    public EnhancedCondition(String genericFields, String genericTableName) {
        put("genericFields", genericFields);
        //受影响的mycat版本是1.5，1.4/1.6均无此问题，目前先恢复为escape方式
        /*if (innerKeywords.contains(genericTableName)) {
            put("genericTableName", String.format("`%s`", genericTableName));
        } else {*/
        put("genericTableName", genericTableName);
        //}
    }

    public void setPagingSql(String pagingSql) {
        put("PagingSQL", pagingSql);
    }

    public void setUpdateLimitSql(String updateLimitSql) {
        put("UpdateLimitSQL", updateLimitSql);
    }

    public void setDeleteLimitSql(String deleteLimitSql) {
        put("DeleteLimitSQL", deleteLimitSql);
    }

    public void setInsertSuffixSql(String insertSuffixSql) {
        put("InsertSuffixSQL", insertSuffixSql);
    }
    
    public EnhancedCondition(EnhancedCondition condition) {
        putAll(condition);
    }
}
