package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentSampleExportResultDTO {
    private String downloadUrl;
    private String fileName;
    private Integer totalCount;
}