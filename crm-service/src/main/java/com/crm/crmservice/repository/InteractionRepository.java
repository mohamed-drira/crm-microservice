package com.crm.crmservice.repository;

import com.crm.crmservice.entity.Interaction;
import com.crm.crmservice.entity.enums.InteractionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface InteractionRepository extends JpaRepository<Interaction, Long> {

    List<Interaction> findByContactId(Long contactId);

    List<Interaction> findByContactIdOrderByInteractionDateDesc(Long contactId);

    List<Interaction> findByContactIdAndInteractionType(Long contactId, InteractionType interactionType);

    List<Interaction> findByContactIdAndInteractionDateBetween(Long contactId, LocalDateTime start, LocalDateTime end);
}