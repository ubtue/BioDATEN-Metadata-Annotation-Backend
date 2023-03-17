package de.unituebingen.metadata.metadata.controller;

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
import de.unituebingen.metadata.metadata.dao.AutocompleteSchemaDAO;
import de.unituebingen.metadata.metadata.dao.RenderOptionDAO;
import de.unituebingen.metadata.metadata.entities.AutocompleteMapping;
import de.unituebingen.metadata.metadata.entities.AutocompleteSchema;
import de.unituebingen.metadata.metadata.entities.RenderOption;

@RequestMapping("autocomplete-schemas")
@RestController
public class autocompleteSchemaController {
    
    @Autowired
    private AutocompleteSchemaDAO autocompleteSchemaDAO;

    @Autowired
    private AutocompleteMappingDAO autocompleteMappingDAO;

    @Autowired
    private RenderOptionDAO renderOptionDAO;

    @GetMapping
    public List<AutocompleteSchema> autocompleteSchema(){
       return autocompleteSchemaDAO.findAll();
    }

    @GetMapping(value = "/{id}")
    public AutocompleteSchema getAutocompleteSchemaById(@PathVariable("id") UUID id){

        Optional<AutocompleteSchema> autocompleteSchema = autocompleteSchemaDAO.findById(id);

        if ( autocompleteSchema.isPresent() ) {
            return autocompleteSchema.get();
        } else {
            return null;
        }
    }

    @GetMapping(value = "/schema/{fileName}")
    public AutocompleteSchema getAutocompleteSchemaByFileName(@PathVariable("fileName") String fileName){

        Optional<AutocompleteSchema> autocompleteSchema = autocompleteSchemaDAO.findByFileName(fileName);

        if ( autocompleteSchema.isPresent() ) {
            return autocompleteSchema.get();
        } else {
            return null;
        }
    }

    @GetMapping(value = "/mappings/{schema}")
    public List<AutocompleteMapping> getAutocompleteMappingsBySchemaId(@PathVariable("schema") String schema) {
        return autocompleteMappingDAO.findBySchema(schema);
    }

    @GetMapping(value = "/render-options/{schema}")
    public List<RenderOption> getRenderOptionsBySchemaId(@PathVariable("schema") String schema) {
        return renderOptionDAO.findBySchema(schema);
    }

    @PostMapping
    public AutocompleteSchema addAutocompleteSchema(@RequestBody AutocompleteSchema autocompleteSchema){

        AutocompleteSchema newAutocompleteSchema = autocompleteSchemaDAO.save(autocompleteSchema);

        return newAutocompleteSchema;
    }

    @PutMapping
    public AutocompleteSchema updateAutocompleteSchema(@RequestBody AutocompleteSchema newAutocompleteSchema){

        AutocompleteSchema autocompleteSchema = autocompleteSchemaDAO.getById(newAutocompleteSchema.getId());

        autocompleteSchema = newAutocompleteSchema;

        autocompleteSchemaDAO.save(autocompleteSchema);

        return autocompleteSchema;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteAutocompleteSchema(@PathVariable("id") UUID id){
        
        autocompleteSchemaDAO.deleteById(id);
    }
}
