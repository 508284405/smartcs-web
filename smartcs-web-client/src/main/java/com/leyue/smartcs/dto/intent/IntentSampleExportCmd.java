package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentSampleExportCmd {
    private Long versionId;
    private String type;
    private String format;
}