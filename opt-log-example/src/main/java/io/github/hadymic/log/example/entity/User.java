package io.github.hadymic.log.example.entity;

import io.github.hadymic.log.annotation.OptLogField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author Hadymic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @OptLogField("ID")
    private Integer id;

    @OptLogField("名称")
    private String name;

    @OptLogField("用户KEY")
    private List<String> keys;

    @OptLogField("角色")
    private Role role;
}
