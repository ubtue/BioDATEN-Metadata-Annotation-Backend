package de.unituebingen.metadata.metadata.dao;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import de.unituebingen.metadata.metadata.entities.UserInformation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

public interface UserInformationDAO extends JpaRepository<UserInformation, UUID> {
    Optional<UserInformation> findByUserId(@Param("user_id") String userId);
}