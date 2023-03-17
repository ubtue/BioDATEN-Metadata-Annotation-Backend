package de.unituebingen.metadata.metadata.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.unituebingen.metadata.metadata.entities.AutocompleteSchema;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AutocompleteSchemaDAO extends JpaRepository<AutocompleteSchema, UUID> {
    Optional<AutocompleteSchema> findByFileName(@Param("fileName") String fileName); 
}