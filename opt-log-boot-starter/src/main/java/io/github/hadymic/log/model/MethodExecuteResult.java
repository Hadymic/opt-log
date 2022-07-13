package io.github.hadymic.log.model;

import lombok.Data;

/**
 * @author Hadymic
 */
@Data
public class MethodExecuteResult {
    private boolean success;
    private Throwable throwable;
    private String errorMsg;
    private Long operateTime;
    private Long executeTime;

    public MethodExecuteResult() {
        this.success = true;
        this.operateTime = System.currentTimeMillis();
    }

    public void success() {
        this.executeTime = System.currentTimeMillis() - this.operateTime;
    }

    public void fail(Throwable throwable) {
        this.success = false;
        this.executeTime = System.currentTimeMillis() - this.operateTime;
        this.throwable = throwable;
        this.errorMsg = throwable.getMessage();
    }
}
