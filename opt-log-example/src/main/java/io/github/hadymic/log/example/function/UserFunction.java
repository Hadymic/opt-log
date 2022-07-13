package io.github.hadymic.log.example.function;

import io.github.hadymic.log.annotation.OptLogFunc;
import io.github.hadymic.log.example.entity.Role;
import io.github.hadymic.log.example.entity.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Hadymic
 */
@OptLogFunc
@Component
public class UserFunction {

    private final Map<Integer, User> userMap = new HashMap<>();

    @PostConstruct
    public void init() {
        Role admin = Role.builder()
                .id(1)
                .name("admin")
                .build();
        Role user = Role.builder()
                .id(1)
                .name("user")
                .build();
        List<String> key1 = new ArrayList<>();
        List<String> key2 = new ArrayList<>();
        key1.add("ABC");
        key1.add("DEF");
        key2.add("DEF");
        userMap.put(1, new User(1, "张三", key1, admin));
        userMap.put(2, new User(2, "李四", key2, user));
        userMap.put(3, new User(3, "王五", key1, user));
    }

    @OptLogFunc("user")
    public User getUserById(int id) {
        return userMap.get(id);
    }

    @OptLogFunc("username")
    public String getUserNameById(int id) {
        User user = userMap.get(id);
        if (user == null) {
            return "未知操作者";
        }
        return user.getName();
    }

    @OptLogFunc
    public static String staticTest(String value) {
        return "_" + value + "_";
    }
}
