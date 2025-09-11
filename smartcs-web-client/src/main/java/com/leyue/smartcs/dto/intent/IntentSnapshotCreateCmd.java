package com.leyue.smartcs.dto.intent;

import lombok.Data;
import java.util.List;

@Data
public class IntentSnapshotCreateCmd {
    private String name;
    private String description;
    private List<Long> versionIds;
    private String scope;
}