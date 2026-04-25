# SQL 方言设计

中文说明。英文版见 [SQL_Dialect_Design.md](SQL_Dialect_Design.md)。

## 目标

方言层的目标是把通用单表 DAO 能力保留在 `EnhancedDao` 中，把数据库特有 SQL 语法收敛到
方言实现和方言专属 DAO 接口中。

兼容目标是：老 MySQL 项目保持历史行为；新 PostgreSQL 项目可以显式选择 PostgreSQL 方言，
避免继承 MySQL 的反引号引用、`insert ignore`、`replace into`、update/delete `limit` 等语法。

## 方言选择策略

DAO Bean 初始化并绑定 `SqlSessionFactory` 时解析方言。顺序如下：

1. 显式配置：`@EnableEnhancedDao(dialect = ...)` 或 `<enhanced-dao:scan dialect="...">`。
2. 从绑定的 `SqlSessionFactory` / `DataSource` 读取 JDBC URL。
3. 扫描 classpath 中的 JDBC Driver 标记。

新项目和多数据源项目推荐显式配置。`AUTO` 主要用于兼容老项目。若 classpath 中同时存在
MySQL 和 PostgreSQL 驱动，且能读取到绑定数据源的 JDBC URL，则以 JDBC URL 为准。若无法读取
URL，则应显式配置方言。

## 实现结构

API 模块提供最小方言抽象：

* `DialectType` 定义 `AUTO`、`MYSQL`、`POSTGRESQL`。
* `SqlDialect` 定义标识符引用、分页、时间戳比较、逗号分隔字符串匹配、update/delete limit
  渲染和能力开关。
* `Dialects` 提供内置方言单例入口。
* `MysqlDialect` 和 `PostgreSqlDialect` 实现具体 SQL 差异。

DAO 实现模块负责解析和使用方言：

* `SqlDialectResolver` 在 DAO 初始化阶段选择方言。
* `EnhancedDaoBuilder` 根据配置选择 `MysqlEnhancedDaoImpl`、`PostgreSqlEnhancedDaoImpl`
  或默认 `EnhancedDaoImpl`。
* `AbstractEnhancedDao` 保留公共 insert/find/count/sum/update/remove 流程，把 SQL 片段渲染委托给
  已解析的 `SqlDialect`。
* `MysqlEnhancedDao` 暴露 MySQL 专属方法，例如 `insertIgnore`、`replace`。
* `PostgreSqlEnhancedDao` 暴露 PostgreSQL 专属方法，例如 `insertOnConflictDoNothing`。

## 扩展方式

新增一个 SQL 方言时按以下步骤处理：

1. 增加新的 `DialectType` 值。
2. 增加 `SqlDialect` 实现，覆盖标识符引用、分页、时间戳处理、逗号分隔字符串匹配和能力开关。
3. 在 `Dialects` 和 `SqlDialectResolver` 中注册新方言。
4. 仅在存在数据库专属操作时，增加方言专属 DAO 接口和实现。
5. 在 `EnhancedDaoBuilder` 中按配置选择对应实现类。
6. 增加外部服务或容器集成测试，执行真实 SQL 验证。

通用 MyBatis XML 保持尽量小。方言相关 SQL 通过 `SqlDialect`、显式 DAO 扩展方法，或项目自己的
MyBatis XML Mapper 进入系统。

## 查询表达式

`DynamicQueryFilter` 继续覆盖普通单表条件。数据库特有谓词通过 helper 提供，例如
`MysqlExpr` 和 `PostgreSqlExpr`。

当调用方把 `DynamicQueryFilter` 直接交给 DAO 内部辅助方法时，DAO 可以使用已绑定方言渲染。
当调用方自行构造 `QueryData` 且使用了方言表达式时，需要显式传入方言：

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .expr(PostgreSqlExpr.jsonbContains(ProductDraft.class, "attributes", "{\"channel\":\"amazon\"}"));

QueryData queryData = filter.getQueryData(Dialects.postgresql());
```

对于 join、CTE、窗口函数或复杂 JSONB 操作，应使用项目自己的 MyBatis XML，并通过 Repository
或 `EnhancedDao#getSqlSession()` 调用。

## 运行期行为

方言解析完成后，DAO 会在以下位置使用方言：

* 表名、列名和别名的引用方式。
* 查询分页 SQL。
* `sum`、排序、表达式条件等字段引用。
* MySQL update/delete limit 行为保护。
* PostgreSQL `insert into ... on conflict (...) do nothing` 后缀拼接。

这种设计让公共 CRUD 尽量稳定，同时把数据库专属能力放到清晰的扩展点中。
