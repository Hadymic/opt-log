package io.github.hadymic.log.example.entity;

import io.github.hadymic.log.annotation.OptLogField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author Hadymic
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Role {
    private Integer id;

    @OptLogField("名称")
    private String name;
}
