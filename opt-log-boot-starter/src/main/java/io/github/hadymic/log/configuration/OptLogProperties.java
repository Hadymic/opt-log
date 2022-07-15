package io.github.hadymic.log.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 操作日志配置
 *
 * @author Hadymic
 */
@Data
@ConfigurationProperties(prefix = "opt-log")
public class OptLogProperties {

    private VariableProperties variable = new VariableProperties();
    private FieldProperties field = new FieldProperties();
    private DiffTemplateProperties diffTemplate = new DiffTemplateProperties();

    @Data
    public static class VariableProperties {
        private String result = "_result";
        private String errorMsg = "_errMsg";
        private String diffField = "__field";
    }

    @Data
    public static class FieldProperties {
        private String fieldName = "_field";
        private String sourceValue = "_source";
        private String targetValue = "_target";
        private String addValues = "_addValues";
        private String delValues = "_delValues";
        private String ofWord = "的";
        private String fieldSeparator = "；";
        private String listItemSeparator = "，";
    }

    @Data
    public static class DiffTemplateProperties {
        private String add = "_field从空修改为_target";
        private String addForList = "_field添加了_addValues";
        private String update = "_field从_source修改为_target";
        private String updateForList = "_field添加了_addValues，删除了_delValues";
        private String delete = "_field从_target修改为空";
        private String deleteForList = "_field删除了_delValues";
    }
}
