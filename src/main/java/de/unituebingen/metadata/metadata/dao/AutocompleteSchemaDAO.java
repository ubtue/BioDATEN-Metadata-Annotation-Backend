package de.unituebingen.metadata.metadata.dao;

import java.util.UUID;

import de.unituebingen.metadata.metadata.entities.AutocompleteSchema;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AutocompleteSchemaDAO extends JpaRepository<AutocompleteSchema, UUID> {
    
}