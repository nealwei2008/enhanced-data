package com.yoloho.enhanced.data.dao.api.filter;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.yoloho.enhanced.common.util.StringUtil;
import com.yoloho.enhanced.data.dao.api.dialect.SqlDialect;

/**
 * order by 条件命令。
 * <p>
 * V1 方言改造后，排序字段通过 {@link SqlDialect} 统一处理标识符引用。
 *
 * @author jason
 * @author neal_wei @ Apr 25, 2026
 */
public class SortCommandImpl implements QueryCommand{
    private static final long serialVersionUID = 1L;
    private String sortName;
	private boolean isDesc;

	public SortCommandImpl(String sortName, boolean isDesc) {
		this.sortName = sortName;
		this.isDesc = isDesc;
	}

	public String getSortName() {
		return sortName;
	}

	public void setSortName(String sortName) {
		this.sortName = sortName;
	}

	public boolean getIsDesc() {
		return isDesc;
	}

	public void setIsDesc(boolean isDesc) {
		this.isDesc = isDesc;
	}

	public int hashCode() {
        return (new HashCodeBuilder(0xfb187f93, 0xd642e94b))
                .append(sortName)
                .append(isDesc ? "desc" : "asc")
                .toHashCode();
	}
	
    /**
     * 使用历史 MySQL 风格渲染排序片段。
     *
     * @return order by 字段片段
     */
	public String getPartSql() {
        return getPartSql(null);
	}

    /**
     * 使用指定 SQL 方言渲染排序片段。
     *
     * @param dialect
     *      SQL 方言；为空时保留历史无引用渲染
     * @return order by 字段片段
     */
    public String getPartSql(SqlDialect dialect) {
        String columnName = StringUtil.toUnderline(sortName);
        if (dialect != null) {
            columnName = dialect.quoteIdentifier(columnName);
        }
        return new StringBuilder(columnName)
                .append(" ")
                .append(isDesc ? "desc" : "asc")
                .toString();
    }

}
