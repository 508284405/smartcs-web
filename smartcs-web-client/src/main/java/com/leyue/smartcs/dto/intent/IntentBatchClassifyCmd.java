package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentBatchClassifyCmd {
    private String[] texts;
    private String channel;
    private String tenant;
}