package io.github.hadymic.log.model;

import io.github.hadymic.log.enums.OptLogStatus;
import lombok.Data;

/**
 * @author Hadymic
 */
@Data
public class OptLogRecord {
    /**
     * 租户
     */
    private String tenant;
    /**
     * 分类
     */
    private String category;
    /**
     * 操作类型
     */
    private String operate;
    /**
     * 操作人
     */
    private String operator;
    /**
     * 业务id
     */
    private String bizId;
    /**
     * 操作状态
     * 如果是执行前, 则返回BEFORE
     * 如果是执行后, 操作成功则返回SUCCESS, 操作失败则返回FAIL
     */
    private OptLogStatus status;
    /**
     * 日志内容
     */
    private String content;
    /**
     * 额外信息
     */
    private String extra;
    /**
     * 操作时间
     */
    private Long operateTime;
    /**
     * 执行时间
     */
    private Long executeTime;
    /**
     * 方法执行后的结果
     */
    private Object result;
    /**
     * 方法错误信息
     */
    private String errorMsg;
}
