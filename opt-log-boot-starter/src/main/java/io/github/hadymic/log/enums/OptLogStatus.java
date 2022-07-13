package io.github.hadymic.log.enums;

public enum OptLogStatus {
    BEFORE, SUCCESS, FAIL;

    public static boolean isBefore(OptLogStatus status) {
        return BEFORE.equals(status);
    }

    public static boolean isSuccess(OptLogStatus status) {
        return SUCCESS.equals(status);
    }

    public static boolean isFail(OptLogStatus status) {
        return FAIL.equals(status);
    }
}
