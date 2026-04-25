enhanced-data-dao
===

中文说明见 [README.zh-CN.md](README.zh-CN.md).

* [DAO Implementation](#dao-implementation)
* [Design](#design)
* [Feature](#feature)
* [Quick Start](#quick-start)
	* [Model](#model)
	* [Generate Dao Bean](#generate-dao-bean)
		* [Manually by Code](#manually-by-code)
		* [XML](#xml)
		* [Annotation](#annotation)
	* [SQL Dialect](#sql-dialect)
	* [Upgrade Guide](#upgrade-guide)
	* [Integration Tests](#integration-tests)
	* [Monitor the Druid Pool](#monitor-the-druid-pool)
		* [XML](#xml)
		* [Annotation](#annotation)
		* [Scan Manually](#scan-manually)
	* [Generate SqlSessionFactory](#generate-sqlsessionfactory)

# DAO Implementation
Brief: 
  
* EnhancedDao
* `<enhanced:dao scan-path="" sql-session-factory="" postfix="" />`
* Annotation support through `EnableEnhancedDao`

# Design
The tradition when we operate the rows in a table through `mybatis` is to create a Mapper or write the customized sql in mappers. The number of file and work grows when the table or logic grows. Then a simple modification or even a renaming may become kinds of annoyed work. (Which means we must modify the related files and logic one by one). Once a file missed once the bug will be buried in. While we also will work on the mapping work like transform `user_id` (name of column) into `userId` (property of bean).

So it's better to find a way to prevent these kinds of duplicated work and focus on the meaningful things.

`EnhancedDao` is a product under this kind of experiment.

# Feature
* Single primary and union primaries support (see `UnionPrimaryKey`)
* Return the new auto increment primary when doing insert
* Common insertion through `EnhancedDao#insert`
* MySQL specific insertion helpers through `MysqlEnhancedDao`: `insert ignore` / `replace`
* PostgreSQL specific conflict helper through `PostgreSqlEnhancedDao`: `on conflict do nothing`
* SQL dialect support: MySQL / PostgreSQL
* Auto mapping between property and column name (also support customizing)
* No need for mapper
* Multiple creations: `Manually` / `xml` / `annotation`
* Range updating / deleting support (Don't forget to set `limit` and default to `1000`) (Also support `order by`, performance sensitive)
* Inheriting beans support (Not recommended for models)

```
For inherited model especially multiple levels inheriting like A > B > C, you should place @Enhanced on which should be mapped. Though it may generates useless beans in process. So it's not recommended.
```

# Quick Start

## Model

Here is an example of definition of model.

```java
private static class UnitTestUserLog {
    @PrimaryKey(autoIncrement = true)
    private int id;
    private int uid;
    private String memo;
    private int dateline;
    @IgnoreKey
    private String someKeyNotInDB;
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getUid() {
        return uid;
    }
    public void setUid(int uid) {
        this.uid = uid;
    }
    public String getMemo() {
        return memo;
    }
    public void setMemo(String memo) {
        this.memo = memo;
    }
    public int getDateline() {
        return dateline;
    }
    public void setDateline(int dateline) {
        this.dateline = dateline;
    }
}
```

## Generate Dao Bean
### Manually by Code

```java
EnhancedDaoImpl<UnitTestUser, Integer> dao = new EnhancedDaoImpl<UnitTestUser, Integer>();
dao.setSqlSessionFactory(sqlSessionFactory);
dao.setTableName(UnitTestUser.class);
```

And you should set the `mapperLocations` of your mybatis factory by adding:
```
com/yoloho/enhanced/data/dao/xml/enhanced-dao-generic.xml
```

### XML
Schema:
```xml
http://www.dayima.org/schema/enhanced-dao http://www.dayima.org/schema/enhanced-dao/enhanced-dao.xsd
```

Bean:
```xml
<enhanced-dao:scan scan-path="com.xx.demo.model"
                   sql-session-factory="mybatisSessionFactory"
                   dialect="POSTGRESQL" />
```

### Annotation
```java
@EnableEnhancedDao(
        scanPath = "com.xx.demo.model", 
        sqlSessionFactory = "mybatisSessionFactory", 
        postfix = "Dao", // Default is EnhancedDao
        dialect = DialectType.POSTGRESQL // Default is AUTO
)
```

## SQL Dialect

`enhanced-dao` resolves SQL dialect when DAO beans are initialized. The resolution order is:

1. Explicit `dialect` configuration
2. JDBC URL from the bound `SqlSessionFactory` / `DataSource`
3. JDBC driver marker on classpath

`AUTO` is the default value. New PostgreSQL projects should prefer explicit `DialectType.POSTGRESQL`
or `dialect="POSTGRESQL"` configuration.

See [SQL Dialect Design](docs/SQL_Dialect_Design.md) for the selection strategy and implementation notes.

### Ability Matrix

| Ability | EnhancedDao | MysqlEnhancedDao | PostgreSqlEnhancedDao | Notes |
| --- | --- | --- | --- | --- |
| Basic CRUD | Supported | Supported | Supported | V1 common ability |
| Page query | Supported | Supported | Supported | Rendered by dialect |
| `insert ignore` | Not common | Supported | PostgreSQL conflict semantics | Use explicit conflict columns in PostgreSQL |
| `replace into` | Not common | Supported | Not a PostgreSQL ability | MySQL specific |
| `update/delete limit` | Not common | Supported | Not PostgreSQL common semantics | MySQL historical ability |
| `on conflict do nothing` | Not common | Not a MySQL ability | Supported | PostgreSQL specific |

### MySQL Specific DAO

Use `MysqlEnhancedDao` only when MySQL-specific behavior is required.

```java
MysqlEnhancedDao<User, Integer> userDao = ...;

userDao.insertIgnore(user);
userDao.replace(user);
```

The old `insert(bean, true)` style is kept only as a MySQL compatibility entry and is deprecated.

### PostgreSQL Specific DAO

Use `PostgreSqlEnhancedDao` only when PostgreSQL-specific behavior is required.

```java
PostgreSqlEnhancedDao<ProductDraft, Long> productDraftDao = ...;

productDraftDao.insertOnConflictDoNothing(productDraft, "id");
```

### Dynamic Query Filter

Common single-table queries continue to use `DynamicQueryFilter`.

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .equalPair("status", 1)
        .like("name", "shoe")
        .orderBy("id", false)
        .limit(20);

List<ProductDraft> list = productDraftDao.find(filter.getQueryData());
```

Dialect-specific single-table expressions use expression helpers.

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

MySQL expression example:

```java
DynamicQueryFilter filter = new DynamicQueryFilter()
        .expr(MysqlExpr.findInSet(Product.class, "tags", "sale"))
        .limit(20);

List<Product> list = productDao.find(filter.getQueryData(Dialects.mysql()));
```

For joins, CTEs, window functions, performance-sensitive SQL, or complex JSONB queries, write MyBatis XML
and call it through a repository or `getSqlSession()`.

```java
Map<String, Object> params = new HashMap<>();
params.put("json", "{\"channel\":\"amazon\"}");
params.put("limit", 20);

List<Map<String, Object>> rows = productDraftDao.getSqlSession()
        .selectList("ProductDraftMapper.findByJsonAttribute", params);
```

## Upgrade Guide

This version keeps the old MySQL-oriented usage working while adding explicit SQL dialect support.
Existing MySQL projects can usually upgrade without code changes. Projects that add PostgreSQL, multiple
JDBC drivers, or multiple data sources should review the points below.

### Existing MySQL Projects

Existing code using `EnhancedDao`, XML scan configuration, or `@EnableEnhancedDao` can keep the default
`AUTO` dialect when the bound data source URL is available. The DAO will resolve MySQL from JDBC URL and
continue to render MySQL quoting, paging, `insert ignore`, `replace`, and update/delete limit SQL.

For new MySQL code that needs MySQL-only write semantics, inject or cast to `MysqlEnhancedDao` and use
the explicit method names:

```java
MysqlEnhancedDao<User, Integer> userDao = ...;

userDao.insertIgnore(user);
userDao.replace(user);
```

The legacy `insert(bean, true)` and `insertAndReturn(bean, true)` entries are retained only for
compatibility and are deprecated. New code should prefer `insertIgnore` and `insertIgnoreAndReturn`.

### PostgreSQL Projects

PostgreSQL projects should configure dialect explicitly at the DAO scan point:

```java
@EnableEnhancedDao(
        scanPath = "com.example.model",
        sqlSessionFactory = "postgresSqlSessionFactory",
        dialect = DialectType.POSTGRESQL
)
```

or with XML:

```xml
<enhanced-dao:scan scan-path="com.example.model"
                   sql-session-factory="postgresSqlSessionFactory"
                   dialect="POSTGRESQL" />
```

When using the annotation helper `@EnableSqlSessionFactory` with PostgreSQL, set `charset = ""` so the
MySQL-specific connection initialization SQL is not applied:

```java
@EnableSqlSessionFactory(
        name = "postgresSqlSessionFactory",
        connectionUrl = "${postgres.jdbc.url}",
        username = "${postgres.jdbc.username}",
        password = "${postgres.jdbc.password}",
        charset = ""
)
```

Use `PostgreSqlEnhancedDao` for PostgreSQL-only write semantics:

```java
PostgreSqlEnhancedDao<ProductDraft, Long> productDraftDao = ...;

productDraftDao.insertOnConflictDoNothing(productDraft, "externalId");
```

### Notes

* In projects where both MySQL and PostgreSQL drivers are on the classpath, `AUTO` will use the bound
  `SqlSessionFactory` / `DataSource` JDBC URL when available. If no data source URL can be inspected,
  configure `dialect` explicitly.
* `DynamicQueryFilter` common conditions can still use `filter.getQueryData()`. Dialect-specific
  expression helpers should be rendered with `Dialects.mysql()` or `Dialects.postgresql()` when
  QueryData is built outside the DAO execution chain.
* MySQL `update/delete limit` is treated as a MySQL-specific behavior. Primary-key delete/update paths
  remain supported across dialects.
* PostgreSQL `jsonb` parameters should be passed using JDBC-compatible values such as `PGobject` when
  the field is written directly through generic insert/update.

## Integration Tests

Default `mvn test` excludes tests tagged as `external-integration` and `container-integration`.

Use external-service tests when a developer or CI environment provides MySQL/Redis through properties:

```bash
mvn test -Pdao-external-integration-tests
```

Use container tests when Docker is available locally:

```bash
mvn test -Pdao-container-integration-tests
```

The DAO container profile starts MySQL through Testcontainers for legacy DAO cases and PostgreSQL for
PostgreSQL dialect coverage. On Docker environments with older API negotiation, pass the Docker API
version explicitly:

```bash
mvn test -Pdao-container-integration-tests -Dapi.version=1.40
```

## Monitor the Druid Pool
If you use `druid` as the jdbc connection pool and want to monitor it we have integrated it.

Default receiver of monitor data is falcon client. You can config the target using properties file include definitions:

```
falcon.url=http://127.0.0.1:1988
```

Or to set it in property **BEFORE initializing**:
```
System.setProperty("falcon.url", "http://127.0.0.1:1988")
```

Or just add a startup parameter:
```shell
-Dfalcon.url=http://127.0.0.1:1988
```

### XML
```xml
<enhanced-dao:druid-monitor projectName="demo-project" />
```

Or if you want to customize the receiver, you could
```xml
<enhanced-dao:druid-monitor projectName="demo-project" callback="myReceiver" />
```

You can make you own implementation of `com.yoloho.enhanced.data.dao.monitor.MonitorCallback`.

### Annotation
```java
@EnableDruidMonitor(
	projectName = "demo-project"
)
```

### Scan Manually
```java
public class Bean implements BeanDefinitionRegistryPostProcessor {
   @Override
   public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
   }

   @Override
   public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
       EnhancedConfig config = new EnhancedConfig();
       config.setScanPath(Arrays.asList("com.demo.model"));
       config.setSqlSessionFactory("mybatisSessionFactory");
       EnhancedDaoParser.scan(config, registry);
   }
}
```

## Generate SqlSessionFactory
Create a `SqlSessionFactory` with connection pool support is a general demand and a little complex. So we make a scaffold to make it easy.

If we use XML to do this we may:

```xml
<bean id="dataSource" class="com.alibaba.druid.pool.DruidDataSource" init-method="init" destroy-method="close">
	<property name="url" value="jdbc:mysql://127.0.0.1:3306/test?useUnicode=true&amp;characterEncoding=utf-8&amp;allowMultiQueries=true" />
	<property name="username" value="test" />
	<property name="password" value="test" />

	<property name="initialSize" value="3" />
	<property name="minIdle" value="1" />
	<property name="maxActive" value="100" />

	<property name="maxWait" value="60000" />

	<property name="timeBetweenEvictionRunsMillis" value="60000" />

	<property name="minEvictableIdleTimeMillis" value="300000" />

	<property name="validationQuery" value="SELECT 'x'" />
	<property name="testWhileIdle" value="true" />
	<property name="testOnBorrow" value="false" />
	<property name="testOnReturn" value="false" />

	<property name="poolPreparedStatements" value="true" />
	<property name="maxPoolPreparedStatementPerConnectionSize" value="20" />

	<property name="filters" value="stat" />
</bean>
<bean id="mybatisSessionFactory" class="org.mybatis.spring.SqlSessionFactoryBean">
	<property name="dataSource" ref="dataSource" />
</bean>
```

By annotation we could use:
```java
@EnableSqlSessionFactory(
    name = "testSessionFactory", // Bean name
    connectionUrl = "jdbc:mysql://192.168.127.56:3306/test?useUnicode=true&characterEncoding=utf-8&allowMultiQueries=true", 
    username = "test",
    password = "test" 
)
```

# Extended Article
[Why We Need Monitoring Druid](docs/Why_We_Need_Monitoring_Druid.md)
