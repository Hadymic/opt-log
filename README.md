# opt-log

## 项目简介

opt-log是一款基于SpringBoot和SpEL表达式的通用操作日志组件，旨在通过注解+SpEL表达式的方式来优雅地记录操作日志。

## 如何使用

1. 添加依赖

```
<dependency>
    <groupId>io.github.hadymic</groupId>
    <artifactId>opt-log-boot-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

2. 添加`@EnableOptLog`注解，以启用opt-log

```java
@SpringBootApplication
@EnableOptLog
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```

3. 使用`@OptLog`注解

```
@OptLog(success = "添加用户成功, 用户id: #{#user.id}, 用户名: #{#user.name}",
        fail = "添加用户失败, 失败原因: #{#_errMsg}",
        operator = "#user.name",
        bizId = "#user.id",
        category = "'user'",
        operate = OptLogOperation.CREATE,
        tenant = "'createUser'")
public void createUser(User user) {
    log.info("new user: {}", user);
}
```
