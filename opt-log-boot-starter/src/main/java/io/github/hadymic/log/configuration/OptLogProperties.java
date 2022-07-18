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
    private DiffProperties diff = new DiffProperties();

    @Data
    public static class VariableProperties {
        private String result = "_result";
        private String errorMsg = "_errMsg";
    }

    @Data
    public static class DiffProperties {
        private FieldProperties field = new FieldProperties();
        private TemplateProperties template = new TemplateProperties();

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
            private String functionField = "__field";
        }

        @Data
        public static class TemplateProperties {
            private String add = "_field从空修改为_target";
            private String update = "_field从_source修改为_target";
            private String delete = "_field从_source修改为空";

            private String addForList = "_field添加了_addValues";
            private String updateForList = "_field添加了_addValues，删除了_delValues";
            private String deleteForList = "_field删除了_delValues";
        }
    }
}
