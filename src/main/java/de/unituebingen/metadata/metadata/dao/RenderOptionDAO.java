package de.unituebingen.metadata.metadata.dao;

import java.util.List;
import java.util.UUID;

import de.unituebingen.metadata.metadata.entities.RenderOption;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface RenderOptionDAO extends JpaRepository<RenderOption, UUID> {
    
    List<RenderOption> findByXpath(@Param("xpath") String xpath); 
    List<RenderOption> findBySchema(@Param("schema") String schema); 
}