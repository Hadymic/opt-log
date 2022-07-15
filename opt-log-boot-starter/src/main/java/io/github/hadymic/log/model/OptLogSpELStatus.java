package io.github.hadymic.log.model;

import io.github.hadymic.log.enums.OptLogSpEL;
import lombok.Data;

/**
 * @author Hadymic
 */
@Data
public class OptLogSpELStatus {
    private boolean success;
    private boolean fail;
    private boolean operator;
    private boolean bizId;
    private boolean tenant;
    private boolean category;
    private boolean operate;
    private boolean extra;
    private boolean condition;

    private OptLogSpELStatus() {
    }

    public static OptLogSpELStatus of(OptLogSpEL[] array) {
        OptLogSpELStatus status = new OptLogSpELStatus();
        for (OptLogSpEL spEL : array) {
            setStatus(status, spEL);
        }
        return status;
    }

    private static void setStatus(OptLogSpELStatus status, OptLogSpEL spEL) {
        switch (spEL) {
            case SUCCESS:
                status.setSuccess(true);
                break;
            case FAIL:
                status.setFail(true);
                break;
            case OPERATOR:
                status.setOperator(true);
                break;
            case BIZ_ID:
                status.setBizId(true);
                break;
            case TENANT:
                status.setTenant(true);
                break;
            case CATEGORY:
                status.setCategory(true);
                break;
            case OPERATE:
                status.setOperate(true);
                break;
            case EXTRA:
                status.setExtra(true);
                break;
            case CONDITION:
                status.setCondition(true);
                break;
            default:
        }
    }
}
