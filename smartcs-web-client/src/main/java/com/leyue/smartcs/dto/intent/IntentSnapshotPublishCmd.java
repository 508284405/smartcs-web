package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentSnapshotPublishCmd {
    private Long snapshotId;
    private String publishType;
    private Integer grayPercent;
}