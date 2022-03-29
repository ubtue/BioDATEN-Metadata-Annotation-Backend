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

import de.unituebingen.metadata.metadata.dao.RenderOptionDAO;
import de.unituebingen.metadata.metadata.entities.RenderOption;

@RequestMapping("render-option")
@RestController
public class renderOptionController {
    
    @Autowired
    private RenderOptionDAO renderOptionDAO;

    @GetMapping
    public List<RenderOption> renderOption(){
       return renderOptionDAO.findAll();
    }

    @GetMapping(value = "/{id}")
    public RenderOption getRenderOptionById(@PathVariable("id") UUID id){

        Optional<RenderOption> renderOption = renderOptionDAO.findById(id);

        if ( renderOption.isPresent() ) {
            return renderOption.get();
        } else {
            return null;
        }
    }

    @PostMapping
    public RenderOption addRenderOption(@RequestBody RenderOption renderOption){

        RenderOption newRenderOption = renderOptionDAO.save(renderOption);

        return newRenderOption;
    }

    @PutMapping
    public RenderOption updateRenderOption(@RequestBody RenderOption newRenderOption){

        RenderOption renderOption = renderOptionDAO.getById(newRenderOption.getId());

        renderOption = newRenderOption;

        renderOptionDAO.save(renderOption);

        return renderOption;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteRenderOption(@PathVariable("id") UUID id){
        
        renderOptionDAO.deleteById(id);
    }
}
