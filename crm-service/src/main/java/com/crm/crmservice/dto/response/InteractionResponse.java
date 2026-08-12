package com.crm.crmservice.dto.response;

import com.crm.crmservice.entity.Interaction;
import com.crm.crmservice.entity.enums.InteractionType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InteractionResponse {

    private Long id;
    private Long contactId;
    private String contactName;
    private InteractionType interactionType;
    private String description;
    private LocalDateTime interactionDate;
    private LocalDateTime createdAt;

    public static InteractionResponse from(Interaction interaction) {
        return InteractionResponse.builder()
                .id(interaction.getId())
                .contactId(interaction.getContact().getId())
                .contactName(interaction.getContact().getFirstName() + " " + interaction.getContact().getLastName())
                .interactionType(interaction.getInteractionType())
                .description(interaction.getDescription())
                .interactionDate(interaction.getInteractionDate())
                .createdAt(interaction.getCreatedAt())
                .build();
    }
}