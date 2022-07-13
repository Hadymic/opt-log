package io.github.hadymic.log.model;

import lombok.Data;

/**
 * @author Hadymic
 */
@Data
public class OptLogOps {
    private String success;
    private String fail;
    private String operator;
    private String bizId;
    private String tenant;
    private String category;
    private String operate;
    private String extra;
    private String condition;
    private boolean before;
}
