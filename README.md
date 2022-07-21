# opt-log

## 项目简介

opt-log是一款基于SpringBoot和SpEL表达式的通用操作日志组件，旨在通过注解+SpEL表达式的方式来优雅地记录操作日志。

## 快速使用

1. 添加依赖

```
<dependency>
    <groupId>io.github.hadymic</groupId>
    <artifactId>opt-log-boot-starter</artifactId>
    <version>1.2.1</version>
</dependency>
```

2. 添加`@EnableOptLog`注解，启用opt-log

```java
@EnableOptLog
@SpringBootApplication
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
```

3. 实现`IOptLogService`接口

```java
@Service
public class OptLogServiceImpl implements IOptLogService {
    @Override
    public void record(OptLogRecord optLogRecord) {
        // 实现入库逻辑或发送至消息中间件等
    }
}
```

4. 使用`@OptLog`注解

```java
@OptLog(success = "添加用户成功, 用户id: #{#user.id}, 用户名: #{#user.name}",
        fail = "添加用户失败, 失败原因: #{#_errMsg}",
        bizId = "#user.id",
        category = "'user'",
        operate = OptLogOperation.CREATE)
public void createUser(User user) {
}
```

## 项目特点

- 快速接入：基于SpringBoot，加入依赖即可使用

- SpEL解析：基于SpEL表达式解析

- 自定义函数：支持自定义函数，支持自定义函数嵌套

- 对象Diff：内置Diff函数，支持对象Diff

## 详细介绍

### @OptLog

操作日志切面注解，支持重复注解

| 参数                | 必填  | SpEL解析 | 备注                                                         |
|:-----------------:|:---:|:------:|:---------------------------------------------------------- |
| success           | 是   | 是      | 成功模板，切面方法没有抛出错误时调用的日志模板                                    |
| fail              | 否   | 是      | 失败模板，切面方法抛出错误时调用的日志模板                                      |
| operator          | 否   | 是      | 操作人，优先级高于IOperatorService的getOperator方法                    |
| bizId             | 否   | 是      | 业务Id                                                       |
| tenant            | 否   | 是      | 租户                                                         |
| category          | 否   | 是      | 分类                                                         |
| operate           | 否   | 是      | 操作类型，内置OptLogOperation方便使用                                 |
| extra             | 否   | 是      | 额外信息                                                       |
| condition         | 否   | 是      | 记录条件，如果解析结果不为true，则该日志不会被记录                                |
| recordBefore      | 否   | 否      | 是否在切面方法执行前记录日志                                             |
| parseBefore       | 否   | 否      | 是否在切面方法执行前解析函数，常用于update时自定义函数解析                           |
| enableParseBefore | 否   | 否      | 是否启用参数的parseBefore功能，当启用parseBefore参数时，该参数才有效果，默认只有success |

### 日志记录

只需实现`IOptLogService`接口就可以接收到日志记录对象，并自行实现接下来的逻辑

```java
@Service
public class OptLogServiceImpl implements IOptLogService {
    @Override
    public void record(OptLogRecord optLogRecord) {
        // 实现入库逻辑或发送至消息中间件等
    }
}
```

`OptLogRecord`详细介绍如下：

| 参数          | 类型           | 备注                                                               |
|:-----------:|:------------:| ---------------------------------------------------------------- |
| tenant      | String       | 租户，tenant参数解析结果                                                  |
| category    | String       | 分类，category参数解析结果                                                |
| operate     | String       | 操作类型，operate参数解析结果                                               |
| operator    | String       | 操作人                                                              |
| bizId       | String       | 业务id，bizId参数解析结果                                                 |
| status      | OptLogStatus | 操作状态，如果启用了recordBefore，返回的是BEFORE，否则会根据切面方法是否抛出错误来返回SUCCESS或FAIL |
| content     | String       | 日志内容，success模板或者fail模板的解析结果                                      |
| extra       | String       | 额外信息，extra参数解析结果                                                 |
| operateTime | Long         | 操作时间，切面方法开始执行时的时间戳                                               |
| executeTime | Long         | 切面方法的执行时长，单位ms                                                   |
| result      | Object       | 切面方法的返回结果                                                        |
| errorMsg    | String       | 切面方法的错误信息                                                        |

### 全局关闭@OptLog参数的SpEL解析

在`@EnableOptLog`的`enableSpEL`参数可定义是否全局启用SpEL解析

```java
/**
 * 是否全局启用@OptLog参数的SpEL解析功能
 * 默认全部启用
 */
OptLogSpEL[] enableSpEL() default {
        OptLogSpEL.SUCCESS, OptLogSpEL.FAIL,
        OptLogSpEL.OPERATOR, OptLogSpEL.BIZ_ID,
        OptLogSpEL.TENANT, OptLogSpEL.CATEGORY,
        OptLogSpEL.OPERATE, OptLogSpEL.EXTRA,
        OptLogSpEL.CONDITION
};
```

### 注册自定义函数

在自定义函数的类与方法上增加`@OptLogFunc`注解并交给Spring

**注意自定义函数不要重名！！！**

示例如下：

```java
@OptLogFunc
@Component
public class UserFunction {
    /**
     * 基于SpEL解析，支持多参的自定义函数
     */
    @OptLogFunc("user")
    public User getUser(int id, String name) {
    }

    /**
     * 当不填写value参数时，默认以方法名作为自定义函数名称
     */
    @OptLogFunc
    public String username(int id) {
    }
}
```

### 使用自定义函数

自定义函数通过`@自定义函数名(...)`进行调用，支持自定义函数的嵌套

示例如下：

```java
/**
 * user和username两个自定义函数看上方`注册自定义函数`章节里的定义
 */
@OptLog(success = "测试#{@user(#id, @username(#id))}，结果为#{#_result}"
        fail = "测试错误，#{#_errMsg}")
public User test(int id) {
}
```

> 此处的`#{...}`为SpEL的模板字符串
> 
> `#id`为SpEL的变量调用，默认所有入参都会定义到上下文中，如果变量是一个对象，还可以这样调用，如：`#user.id`
> 
> `#_result`为默认的方法的返回
> 
> `#_errMsg`为默认的方法的错误信息

### 全局获取操作人

可以通过实现`IOperatorService`接口全局获取操作人，而不需要在注解内填写operator参数

示例如下：

```java
@Service
public class OperatorServiceImpl implements IOperatorService {
    @Override
    public String getOperator() {
        ServletRequestAttributes attributes =
        (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // ...
        return "";
    }
}
```

### Diff函数

内置的Diff函数定义在`IDiffFunction`接口里，可以通过实现该接口来自定义Diff函数

Diff函数的定义如下：

```java
@OptLogFunc
public interface IDiffFunction {

    @OptLogFunc("Diff")
    default String diff(final Object source, final Object target) {
        // ...
    }
}
```

要使用Diff函数，必须在字段上增加`@OptLogField`注解，否则字段的变化会被忽略

`@OptLogField`的value参数用于Diff函数展示字段的名称，function参数用于将字段通过自定义函数进行转化

示例如下：

```java
public class User {
    private Integer id;
    @OptLogField("名称")
    private String name;
    @OptLogField(value = "角色", function = "@role(#__field)")
    private Integer roleId;
}
```

> `#__field`是指将roleId作为参数传入role自定义函数中

Diff函数的示例如下：

```java
/**
 * user自定义函数看上方`注册自定义函数`章节里的定义
 */
@OptLog(success = "@Diff(@user(#user.id, #user.name), #user)")
public User updateUser(User user) {
}
```

### 自定义上下文

通过`OptLogContext`的静态方法，可以在方法内自定义上下文

示例如下：

```java
@OptLog(success = "@Diff(#oldUser, #user)")
public User updateUser(User user) {
    User oldUser = getUserById(user.getId());
    OptLogContext.putVariable("oldUser", oldUser);
}
```

### 配置文件

默认配置如下：

```yaml
opt-log:
  variable:
    result: '_result'
    error-msg: '_errMsg'
  diff:
    field:
      # 会被替换成@OptLogField中的value
      field-name: '_field'
      source-value: '_source'
      target-value: '_target'
      add-values: '_addValues'
      del-values: '_delValues'
      of-word: '的'
      field-separator: '；'
      list-item-separator: '，'
      # 用于@OptLogField中的function
      function-field: '__field'
    template:
      add: '_field从空修改为_target'
      update: '_field从_source修改为_target'
      delete: '_field从_source修改为空'
      add-for-list: '_field添加了_addValues'
      update-for-list: '_field添加了_addValues，删除了_delValues'
      delete-for-list: '_field删除了_delValues'
```
