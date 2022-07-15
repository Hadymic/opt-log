package io.github.hadymic.log.enums;

public enum OptLogSpEL {
    SUCCESS,
    FAIL,
    OPERATOR,
    BIZ_ID,
    TENANT,
    CATEGORY,
    OPERATE,
    EXTRA,
    CONDITION;

    public static final OptLogSpEL[] ALL = {
            OptLogSpEL.SUCCESS, OptLogSpEL.FAIL,
            OptLogSpEL.OPERATOR, OptLogSpEL.BIZ_ID,
            OptLogSpEL.TENANT, OptLogSpEL.CATEGORY,
            OptLogSpEL.OPERATE, OptLogSpEL.EXTRA,
            OptLogSpEL.CONDITION
    };

}
