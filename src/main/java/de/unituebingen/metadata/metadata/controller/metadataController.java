package de.unituebingen.metadata.metadata.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unituebingen.metadata.metadata.dao.AutocompleteMappingDAO;
import de.unituebingen.metadata.metadata.dao.MetadataDAO;
import de.unituebingen.metadata.metadata.entities.AutocompleteMapping;
import de.unituebingen.metadata.metadata.entities.Metadata;
import de.unituebingen.metadata.metadata.entities.Metadata.Status;

@RequestMapping("metadata")
@RestController
public class metadataController {
    
    @Autowired
    private MetadataDAO metadataDAO;

    @GetMapping
    public List<Metadata> metadata(){
       return metadataDAO.findAll();
    }

    @GetMapping(value = "/{id}")
    public Metadata getMetadataById(@PathVariable("id") UUID mets_id){

        Optional<Metadata> metadata = metadataDAO.findById(mets_id);

        if ( metadata.isPresent() ) {
            return metadata.get();
        } else {
            return null;
        }
    }

    @GetMapping(value = "/status/{id}")
    public Status getMetadataStatusById(@PathVariable("id") UUID mets_id){

        Optional<Metadata> metadata = metadataDAO.findById(mets_id);

        if ( metadata.isPresent() ) {
            return metadata.get().getStatus();
        } else {
            return null;
        }
    }

    @GetMapping(value = "/user_id/{id}")
    public List<Metadata> getMetadataByUserId(@PathVariable("id") String user_id){

        List<Metadata> metadata = metadataDAO.findByUserId(user_id);

        return metadata;
    }

    @PostMapping
    public Metadata addMetadata(@RequestBody Metadata metadata){

        Metadata newMetadata = metadataDAO.save(metadata);

        return newMetadata;
    }

    @PutMapping
    public Metadata updateMetadata(@RequestBody Metadata newMetadata){

        Metadata metadata = metadataDAO.findByMetsId(newMetadata.getMetsId()).get();

        // Get the created date and set it to the previous value
        Date created = metadata.getCreated();

        newMetadata.setCreated(created);

        metadata = newMetadata;

        metadataDAO.save(metadata);

        return metadata;
    }

    @PutMapping(value = "status/{id}")
    public Status updateMetadataStatus(@PathVariable("id") UUID mets_id, @RequestBody Status newStatus) {

        Optional<Metadata> metadata = metadataDAO.findById(mets_id);

        if ( metadata.isPresent() ) {
            metadata.get().setMetadata_status(newStatus);
            return metadata.get().getMetadata_status();
        } else {
            return null;
        }
    }

    @DeleteMapping(value = "/{id}")
    public void deleteMetadata(@PathVariable("id") UUID mets_id){
        
        metadataDAO.deleteById(mets_id);
    }
}
