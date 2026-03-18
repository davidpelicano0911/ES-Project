package com.operimus.Marketing.dto;

import lombok.Data;
import java.util.Date;

@Data
public class LeadSyncStatusDTO {
    public boolean updatesAvailable;
    public int pendingUpdates;
    public int pendingCreates;
    public Date lastSyncedAt;
}

