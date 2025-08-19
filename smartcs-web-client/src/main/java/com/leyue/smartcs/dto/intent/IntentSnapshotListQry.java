package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentSnapshotListQry {
    private String status;
    private String keyword;
    private Integer pageNum = 1;
    private Integer pageSize = 10;
}