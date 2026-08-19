package com.crm.notification.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UnreadCountResponse {

    private long count;
}
