package de.unituebingen.metadata.metadata.controller;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.unituebingen.metadata.metadata.dao.UserInformationDAO;
import de.unituebingen.metadata.metadata.entities.UserInformation;


@RequestMapping("user-information")
@RestController
public class userInformationController {

    @Autowired
    private UserInformationDAO userInformationDAO;

    @GetMapping(value = "/{id}")
    public UserInformation getUserInformation(@PathVariable("id") UUID id){

        Optional<UserInformation> userInformation = userInformationDAO.findById(id);

        if ( userInformation.isPresent() ) {
            return userInformation.get();
        } else {
            return null;
        }
    }

    @PostMapping
    public UserInformation addUserInformation(@RequestBody UserInformation userInformation){

        UserInformation newUserInformation = userInformationDAO.save(userInformation);

        return newUserInformation;
    }

    @PutMapping
    public UserInformation updateUserInformation(@RequestBody UserInformation newUserInformation){

        UserInformation userInformation = userInformationDAO.getById(newUserInformation.getId());

        userInformation = newUserInformation;

        userInformationDAO.save(userInformation);

        return userInformation;
    }

    @DeleteMapping(value = "/{id}")
    public void deleteUserInformation(@PathVariable("id") UUID id){
        
        userInformationDAO.deleteById(id);
    }

    @GetMapping(value = "/user-id/{id}")
    public UserInformation getUserInformationByUserId(@PathVariable("id") String userId) {

        Optional<UserInformation> userInformation = userInformationDAO.findByUserId(userId);

        if ( userInformation.isPresent() ) {
            return userInformation.get();
        } else {
            return null;
        }
    }

    /**
     * encodeValue
     * 
     * Encodes a value so that it can be used in a url
     * 
     * @param value
     * @return
     * @throws UnsupportedEncodingException
    */
    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }
}
