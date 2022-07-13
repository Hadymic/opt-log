package io.github.hadymic.log.example;

import io.github.hadymic.log.example.entity.Role;
import io.github.hadymic.log.example.entity.User;
import io.github.hadymic.log.example.service.UserService;
import io.github.hadymic.log.model.OptLogRecord;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author Hadymic
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class OptLogTest {

    @Autowired
    private UserService userService;

    @Autowired
    private OptLogTestService optLogTestService;

    @Test
    public void createUser() {
        User user = User.builder()
                .id(1)
                .name("张三")
                .build();
        userService.createUser(user);
        OptLogRecord record = optLogTestService.getRecord("createUser");
        Assert.assertEquals("添加用户成功, 用户id: 1, 用户名: 张三", record.getContent());
    }

    @Test
    public void updateUser() {
        User user = User.builder()
                .id(1)
                .name("张小三")
                .build();
        userService.updateUser(user);
        OptLogRecord record = optLogTestService.getRecord("updateUser");
        Assert.assertEquals("更新用户成功, 用户名由'张三'变更为'张小三'", record.getContent());
    }

    @Test
    public void staticTest() {
        userService.staticTest();
        OptLogRecord record = optLogTestService.getRecord("staticTest");
        Assert.assertEquals("静态测试成功, value: _123_", record.getContent());
    }

    @Test
    public void diffTest() {
        Role role = Role.builder()
                .id(1)
                .name("admin")
                .build();
        User user = User.builder()
                .id(1)
                .name("张小三")
                .keys(Lists.list("ABC"))
                .role(role)
                .build();
        userService.diffTest(user);
        OptLogRecord record = optLogTestService.getRecord("diffTest");
        Assert.assertEquals("更新用户成功, 用户KEY删除了DEF；名称从张三修改为张小三", record.getContent());
    }
}
