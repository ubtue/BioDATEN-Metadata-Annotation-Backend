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
import de.unituebingen.metadata.metadata.entities.AutocompleteMapping;

@RequestMapping("autocomplete-mapping")
@RestController
public class autocompleteMappingController {
    
    @Autowired
    private AutocompleteMappingDAO autocompleteMappingDAO;

    @GetMapping
    public List<AutocompleteMapping> autocompleteMapping(){
       return autocompleteMappingDAO.findAll();
    }

    @GetMapping(value = "/{id}")
    public AutocompleteMapping getAutocompleteMappingById(@PathVariable("id") UUID id){

        Optional<AutocompleteMapping> autocompleteMapping = autocompleteMappingDAO.findById(id);

        if ( autocompleteMapping.isPresent() ) {
            return autocompleteMapping.get();
        } else {
            return null;
        }
    }

    @PostMapping
    public AutocompleteMapping addAutocompleteMapping(@RequestBody AutocompleteMapping autocompleteMapping){

        AutocompleteMapping newAutocompleteMapping = autocompleteMappingDAO.save(autocompleteMapping);

        return newAutocompleteMapping;
    }

    @PutMapping
    public AutocompleteMapping updateAutocompleteMapping(@RequestBody AutocompleteMapping newAutocompleteMapping){

        AutocompleteMapping autocompleteMapping = autocompleteMappingDAO.getById(newAutocompleteMapping.getId());

        autocompleteMapping = newAutocompleteMapping;

        autocompleteMappingDAO.save(autocompleteMapping);

        return autocompleteMapping;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteAutocompleteMapping(@PathVariable("id") UUID id){
        
        autocompleteMappingDAO.deleteById(id);
    }
}
