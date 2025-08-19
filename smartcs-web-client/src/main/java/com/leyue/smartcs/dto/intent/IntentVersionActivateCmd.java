package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentVersionActivateCmd {
    private Long intentId;
    private Long versionId;
}