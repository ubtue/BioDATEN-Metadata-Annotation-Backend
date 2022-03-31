package de.unituebingen.metadata.metadata.entities;

import java.io.Serializable;
import java.util.UUID;

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
@Table(name="render_options")
@Data
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor

public class RenderOption {
    @Id
    @GeneratedValue(generator = "UUID",
        strategy = GenerationType.AUTO)
    @GenericGenerator(
        name = "id",
        strategy = "org.hibernate.id.UUIDGenerator"
    )
    private UUID id;
    private String schema;

    private String xpath;
    private String label;
    private String placeholder;
    private String prefilled;
    private boolean readonly;
    private boolean hide;

    private boolean active;

    public UUID getId() {
        return id;
    }
}
