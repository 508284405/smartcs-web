package com.leyue.smartcs.dto.data;

public enum ErrorCode{
    B_CUSTOMER_companyNameConflict("B_CUSTOMER_companyNameConflict", "客户公司名冲突"), 
    SESSION_WAITING_MAX_ONE("SESSION_WAITING_MAX_ONE", "客户等待中会话最多只能有一条");

    private final String errCode;
    private final String errDesc;

    private ErrorCode(String errCode, String errDesc) {
        this.errCode = errCode;
        this.errDesc = errDesc;
    }

    public String getErrCode() {
        return errCode;
    }

    public String getErrDesc() {
        return errDesc;
    }
}
