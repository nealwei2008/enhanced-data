# enhanced-data-dao

中文说明。英文版见 [README.md](README.md)。

## 模块说明

`enhanced-data-dao` 是基于 MyBatis 的通用单表 DAO 实现，用于减少简单表访问场景下重复编写
Mapper 和 XML 的成本。它会根据实体类字段自动生成常见的插入、查询、统计、更新和删除 SQL。

本版本新增 MySQL / PostgreSQL 方言支持，同时保留老 MySQL 项目的默认行为。

## 功能概览

* 支持单主键和联合主键，联合主键见 `UnionPrimaryKey`。
* 支持插入后回填自增主键。
* 公共 CRUD 通过 `EnhancedDao` 使用。
* MySQL 特有能力通过 `MysqlEnhancedDao` 使用：`insert ignore`、`replace into`。
* PostgreSQL 特有能力通过 `PostgreSqlEnhancedDao` 使用：`on conflict do nothing`。
* 支持 MySQL / PostgreSQL 方言：标识符引用、分页、部分表达式和能力开关由方言控制。
* 支持属性名与列名自动转换，也支持显式字段映射。
* 支持手工创建、XML 扫描、注解扫描三种 DAO 生成方式。
* 支持按条件批量更新/删除；MySQL 保留历史 `limit` 语义，PostgreSQL 不使用 MySQL 风格的
  update/delete limit。

## 快速使用

### 实体定义

```java
@Enhanced(tableName = "unit_test_user_log")
public class UnitTestUserLog {
    @PrimaryKey(autoIncrement = true)
    private int id;
    private int uid;
    private String memo;
    private int dateline;
    @IgnoreKey
    private String someKeyNotInDB;
}
```

### 手工创建 DAO

```java
EnhancedDaoImpl<UnitTestUserLog, Integer> dao = new EnhancedDaoImpl<>();
dao.setSqlSessionFactory(sqlSessionFactory);
dao.setTableName(UnitTestUserLog.class);
```

MyBatis `mapperLocations` 需要包含通用 XML：

```text
com/yoloho/enhanced/data/dao/xml/enhanced-dao-generic.xml
```

### XML 扫描

```xml
<enhanced-dao:scan scan-path="com.example.model"
                   sql-session-factory="mybatisSessionFactory"
                   dialect="POSTGRESQL" />
```

### 注解扫描

```java
@EnableEnhancedDao(
        scanPath = "com.example.model",
        sqlSessionFactory = "mybatisSessionFactory",
        postfix = "Dao",
        dialect = DialectType.POSTGRESQL
)
```

`dialect` 默认值为 `AUTO`。新 PostgreSQL 项目和多数据源项目建议显式配置。

## SQL 方言

DAO Bean 初始化时会解析并绑定 SQL 方言。解析顺序：

1. `@EnableEnhancedDao(dialect = ...)` 或 `<enhanced-dao:scan dialect="...">` 显式配置。
2. 从绑定的 `SqlSessionFactory` / `DataSource` 读取 JDBC URL。
3. 扫描 classpath 中的 JDBC Driver 标记。

详细原理见 [SQL 方言设计](docs/SQL_Dialect_Design.zh-CN.md)。

### 能力矩阵

| 能力 | EnhancedDao | MysqlEnhancedDao | PostgreSqlEnhancedDao | 说明 |
| --- | --- | --- | --- | --- |
| 基础 CRUD | 支持 | 支持 | 支持 | V1 公共能力 |
| 分页查询 | 支持 | 支持 | 支持 | 由方言渲染 |
| `insert ignore` | 非公共能力 | 支持 | 使用 PostgreSQL 冲突语义 | PostgreSQL 使用显式冲突列 |
| `replace into` | 非公共能力 | 支持 | 非 PostgreSQL 能力 | MySQL 专属 |
| `update/delete limit` | 非公共能力 | 支持 | 非 PostgreSQL 公共语义 | MySQL 历史行为 |
| `on conflict do nothing` | 非公共能力 | 非 MySQL 能力 | 支持 | PostgreSQL 专属 |

### MySQL 专属 DAO

只有需要 MySQL 专属语义时使用 `MysqlEnhancedDao`。

```java
MysqlEnhancedDao<User, Integer> userDao = ...;

userDao.insertIgnore(user);
userDao.replace(user);
```

老的 `insert(bean, true)` / `insertAndReturn(bean, true)` 入口保留为兼容入口，已经标记废弃。
新代码应使用 `insertIgnore` 和 `insertIgnoreAndReturn`。

### PostgreSQL 专属 DAO

只有需要 PostgreSQL 专属语义时使用 `PostgreSqlEnhancedDao`。

```java
PostgreSqlEnhancedDao<ProductDraft, Long> productDraftDao = ...;

productDraftDao.insertOnConflictDoNothing(productDraft, "externalId");
```

## DynamicQueryFilter

普通单表条件继续使用 `DynamicQueryFilter`：

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .equalPair("status", 1)
        .like("name", "shoe")
        .orderBy("id", false)
        .limit(20);

List<ProductDraft> list = productDraftDao.find(filter.getQueryData());
```

方言表达式通过 helper 创建。调用方自行生成 `QueryData` 时，需要传入方言：

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .equalPair("status", 1)
        .expr(PostgreSqlExpr.jsonbContains(
                ProductDraft.class,
                "attributes",
                "{\"channel\":\"amazon\"}"
        ))
        .limit(20);

List<ProductDraft> list = productDraftDao.find(filter.getQueryData(Dialects.postgresql()));
```

MySQL 示例：

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .expr(MysqlExpr.findInSet(Product.class, "tags", "sale"))
        .limit(20);

List<Product> list = productDao.find(filter.getQueryData(Dialects.mysql()));
```

对于 join、CTE、窗口函数、复杂 JSONB 查询或性能敏感 SQL，推荐写项目自己的 MyBatis XML，
再通过 Repository 或 `getSqlSession()` 调用。

## 升级说明

### 老 MySQL 项目

老 MySQL 项目通常不需要改业务代码。默认 `AUTO` 方言会优先从绑定数据源的 JDBC URL 识别
MySQL，继续保留 MySQL 的标识符引用、分页、`insert ignore`、`replace into` 和 update/delete
limit 行为。

升级后建议逐步把新代码中的布尔入口替换为语义明确的方法：

```java
// 兼容但不推荐
dao.insert(bean, true);

// 推荐
mysqlDao.insertIgnore(bean);
```

### PostgreSQL 项目

PostgreSQL 项目应显式配置方言：

```java
@EnableEnhancedDao(
        scanPath = "com.example.model",
        sqlSessionFactory = "postgresSqlSessionFactory",
        dialect = DialectType.POSTGRESQL
)
```

使用 `@EnableSqlSessionFactory` 生成 PostgreSQL 数据源时，需要关闭 MySQL 字符集初始化：

```java
@EnableSqlSessionFactory(
        name = "postgresSqlSessionFactory",
        connectionUrl = "${postgres.jdbc.url}",
        username = "${postgres.jdbc.username}",
        password = "${postgres.jdbc.password}",
        charset = ""
)
```

### 注意事项

* 同时引入 MySQL 和 PostgreSQL 驱动时，`AUTO` 会优先按绑定数据源 JDBC URL 识别方言；
  无法读取数据源 URL 的场景应显式配置 `dialect`。
* 方言表达式在调用方自行生成 `QueryData` 时要传入 `Dialects.mysql()` 或
  `Dialects.postgresql()`。
* PostgreSQL 的 `jsonb` 字段直接写入时，应使用 JDBC 兼容类型，例如 `PGobject`。
* MySQL 的 update/delete limit 是历史专属行为。跨方言代码应优先使用主键或明确条件控制影响范围。

## 测试说明

默认测试不依赖外部服务：

```bash
mvn test
```

外部服务版本测试用于连接开发者或 CI 提供的 MySQL / Redis：

```bash
mvn test -Pdao-external-integration-tests
mvn test -Psharding-external-integration-tests
mvn test -Pcache-external-integration-tests
```

容器版本测试通过 Testcontainers 启动 MySQL / PostgreSQL / Redis，本机需要可用 Docker：

```bash
mvn test -Pdao-container-integration-tests
mvn test -Psharding-container-integration-tests
mvn test -Pcache-container-integration-tests
```

部分 Docker 环境需要指定 API version：

```bash
mvn test -Pdao-container-integration-tests -Dapi.version=1.40
```

## Druid 监控

Druid 监控用法保持不变，可继续使用 XML 或注解配置：

```xml
<enhanced-dao:druid-monitor projectName="demo-project" />
```

```java
@EnableDruidMonitor(projectName = "demo-project")
```
