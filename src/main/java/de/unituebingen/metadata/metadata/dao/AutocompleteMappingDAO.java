package de.unituebingen.metadata.metadata.dao;

import java.util.List;
import java.util.UUID;

import de.unituebingen.metadata.metadata.entities.AutocompleteMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface AutocompleteMappingDAO extends JpaRepository<AutocompleteMapping, UUID> {
    
    List<AutocompleteMapping> findBySchema(@Param("schema") String schema); 
}