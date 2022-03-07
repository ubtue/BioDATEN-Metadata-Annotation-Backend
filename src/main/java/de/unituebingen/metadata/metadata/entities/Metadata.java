package de.unituebingen.metadata.metadata.entities;

import java.io.Serializable;
import java.sql.SQLXML;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.AnyType;
import org.springframework.beans.factory.annotation.Autowired;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name="metadata")
@Data
@AllArgsConstructor(onConstructor = @__(@Autowired))
@NoArgsConstructor

public class Metadata {
    @Id
    @GeneratedValue(generator = "UUID",
        strategy = GenerationType.AUTO)
    @GenericGenerator(
        name = "mets_id",
        strategy = "org.hibernate.id.UUIDGenerator"
    )

    @Column(name = "mets_id", nullable = false)
    private UUID metsId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    private Long hpc_job_id;

    public static enum Status {created, progress, finished, pub};

    @Enumerated(EnumType.STRING)
    private Status metadata_status;

    private String mets_xml;

    private Date created;
    private Date lastmodified;

    public UUID getId() {
        return metsId;
    }
}
