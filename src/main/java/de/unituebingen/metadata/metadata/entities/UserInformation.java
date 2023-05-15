package de.unituebingen.metadata.metadata.entities;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="user_information")
@Data
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor

public class UserInformation {
    @Id
    @GeneratedValue(generator = "UUID",
        strategy = GenerationType.AUTO)
    @GenericGenerator(
        name = "id",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;
   
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "fdat_key", nullable = true)
    private String fdatKey;

    public UUID getId() {
        return id;
    }
}
