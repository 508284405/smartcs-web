package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentSampleImportCmd {
    private Long versionId;
    private String fileContent;
    private String fileType;
}