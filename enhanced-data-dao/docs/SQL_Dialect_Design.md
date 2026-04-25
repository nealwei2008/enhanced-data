# SQL Dialect Design

中文说明见 [SQL_Dialect_Design.zh-CN.md](SQL_Dialect_Design.zh-CN.md).

This document describes how enhanced-dao selects SQL dialects and how dialect-specific behavior is
organized.

## Goals

The dialect layer keeps common single-table DAO behavior in `EnhancedDao`, while moving database-specific
SQL syntax into dialect implementations and dialect-specific DAO interfaces.

The main compatibility goal is that existing MySQL projects keep their historical behavior. New
PostgreSQL projects can opt into PostgreSQL behavior without inheriting MySQL syntax such as backtick
quoting, `insert ignore`, `replace into`, or update/delete `limit`.

## Selection Strategy

Dialect selection happens when a DAO bean is initialized and bound to its `SqlSessionFactory`.

The selection order is:

1. Explicit configuration from `@EnableEnhancedDao(dialect = ...)` or `<enhanced-dao:scan dialect="...">`.
2. JDBC URL inspected from the bound `SqlSessionFactory` / `DataSource`.
3. JDBC driver markers on the classpath.

Explicit configuration is the preferred mode for new projects and multi-data-source projects. `AUTO`
is retained for compatibility. When both MySQL and PostgreSQL drivers are present, the bound data source
JDBC URL is used. If the URL cannot be inspected, the project should configure the dialect explicitly.

## Implementation Outline

The public API module provides the small dialect abstraction:

* `DialectType` defines `AUTO`, `MYSQL`, and `POSTGRESQL`.
* `SqlDialect` defines rendering points for identifier quoting, paging, timestamp comparison, joined
  string matching, update/delete limit SQL, and capability flags.
* `Dialects` exposes the built-in dialect singletons.
* `MysqlDialect` and `PostgreSqlDialect` implement the actual SQL rendering differences.

The DAO implementation module resolves and uses the dialect:

* `SqlDialectResolver` selects the dialect at DAO initialization.
* `EnhancedDaoBuilder` chooses `MysqlEnhancedDaoImpl`, `PostgreSqlEnhancedDaoImpl`, or the default
  `EnhancedDaoImpl` based on configured dialect.
* `AbstractEnhancedDao` keeps the shared insert/find/count/sum/update/remove flow and delegates SQL
  fragments to the resolved `SqlDialect`.
* `MysqlEnhancedDao` exposes MySQL-specific methods such as `insertIgnore` and `replace`.
* `PostgreSqlEnhancedDao` exposes PostgreSQL-specific methods such as `insertOnConflictDoNothing`.

## Extension Model

To add a new SQL dialect:

1. Add a new `DialectType` value.
2. Add a `SqlDialect` implementation for identifier quoting, paging, timestamp handling, joined-string
   matching, and capability flags.
3. Register the dialect in `Dialects` and `SqlDialectResolver`.
4. Add a dialect-specific DAO interface and implementation only for database-specific operations.
5. Extend `EnhancedDaoBuilder` to select the implementation when the dialect is configured.
6. Add container or external integration tests that execute real SQL for that dialect.

The generic MyBatis XML remains intentionally small. Dialect-specific SQL should enter through
`SqlDialect`, explicit DAO extension methods, or a project-owned MyBatis XML mapper for complex queries.

## Query Expressions

`DynamicQueryFilter` still covers common single-table conditions. Database-specific predicates use
expression helper classes such as `MysqlExpr` and `PostgreSqlExpr`.

When a `DynamicQueryFilter` is passed directly to DAO helper methods, the DAO can render it with the
resolved dialect. When callers build `QueryData` themselves and use dialect-specific expressions, they
must pass the dialect explicitly:

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .expr(PostgreSqlExpr.jsonbContains(ProductDraft.class, "attributes", "{\"channel\":\"amazon\"}"));

QueryData queryData = filter.getQueryData(Dialects.postgresql());
```

For joins, CTEs, window functions, or complex JSONB operations, use project-owned MyBatis XML and call it
through a repository or `EnhancedDao#getSqlSession()`.
