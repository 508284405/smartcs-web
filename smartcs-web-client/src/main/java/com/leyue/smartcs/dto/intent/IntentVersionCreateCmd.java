package com.leyue.smartcs.dto.intent;

import lombok.Data;

@Data
public class IntentVersionCreateCmd {
    private Long intentId;
    private String version;
    private String versionName;
    private String description;
    private String changeNote;
}