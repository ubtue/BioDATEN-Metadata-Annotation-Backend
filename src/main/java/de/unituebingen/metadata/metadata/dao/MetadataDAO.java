package de.unituebingen.metadata.metadata.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.unituebingen.metadata.metadata.entities.Metadata;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface MetadataDAO extends JpaRepository<Metadata, UUID> {
    
    Optional<Metadata> findByMetsId(@Param("id") UUID id); 

    List<Metadata> findByUserId(@Param("id") String id);
}