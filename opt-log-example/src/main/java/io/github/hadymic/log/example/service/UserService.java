package io.github.hadymic.log.example.service;

import io.github.hadymic.log.annotation.OptLog;
import io.github.hadymic.log.constant.OptLogOperation;
import io.github.hadymic.log.context.OptLogContext;
import io.github.hadymic.log.example.entity.User;
import io.github.hadymic.log.example.function.UserFunction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Hadymic
 */
@Slf4j
@Service
public class UserService {

    @Autowired
    private UserFunction userFunction;

    /**
     * SpEL模板解析测试
     */
    @OptLog(success = "添加用户成功, 用户id: #{#user.id}, 用户名: #{#user.name}",
            fail = "添加用户失败, 失败原因: #{#_errMsg}",
            bizId = "#user.id",
            category = "'user'",
            operate = OptLogOperation.CREATE,
            tenant = "'createUser'")
    public void createUser(User user) {
        log.info("new user: {}", user);
    }

    /**
     * 自定义函数测试
     */
    @OptLog(success = "更新用户成功, 用户名由'#{#username(#newUser.id)}'变更为'#{#newUser.name}'",
            fail = "更新用户失败, 失败原因: #{#_errMsg}",
            bizId = "#newUser.id",
            category = "'user'",
            operate = OptLogOperation.UPDATE,
            tenant = "'updateUser'")
    public void updateUser(User newUser) {
        User oldUser = userFunction.getUserById(newUser.getId());
        log.info("new user: {}", newUser);
        log.info("old user: {}", oldUser);
    }

    /**
     * 静态方法测试
     */
    @OptLog(success = "静态测试成功, value: #{#staticTest('123')}",
            fail = "静态测试失败, 失败原因: #{#_errMsg}",
            operate = OptLogOperation.QUERY,
            tenant = "'staticTest'")
    public void staticTest() {
    }

    /**
     * Diff函数测试
     */
    @OptLog(success = "更新用户成功, #{#Diff(#oldUser, #newUser)}",
            fail = "更新用户失败, 失败原因: #{#_errMsg}",
            bizId = "#newUser.id",
            category = "'user'",
            operate = OptLogOperation.UPDATE,
            tenant = "'diffTest'")
    public void diffTest(User newUser) {
        User oldUser = userFunction.getUserById(newUser.getId());
        OptLogContext.putVariable("oldUser", oldUser);
    }
}
