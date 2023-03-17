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
@Table(name="autocomplete_schemas")
@Data
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor

public class AutocompleteSchema {
    @Id
    @GeneratedValue(generator = "UUID",
        strategy = GenerationType.AUTO)
    @GenericGenerator(
        name = "id",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;

    private String schema;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Column(name = "tab_name", nullable = false)
    private String tabName;

    private boolean active;

    public UUID getId() {
        return id;
    }
}
