package com.example.idcard.controller;

import com.example.idcard.model.UserCard;
import com.example.idcard.service.CardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
public class CardController {

    private final CardService cardService;

    public CardController(CardService cardService) {
        this.cardService = cardService;
    }

    @PostMapping(path = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @org.springframework.web.bind.annotation.ResponseStatus(HttpStatus.CREATED)
    public UserCard register(
            @RequestParam String name,
            @RequestParam String bloodGroup,
            @RequestParam String phoneNumber,
            @RequestParam(required = false) String validUntil,
            @RequestParam MultipartFile profilePic) {
        return cardService.register(name, bloodGroup, phoneNumber, validUntil, profilePic);
    }

    @GetMapping("/{gymId}")
    public UserCard getByGymId(@PathVariable String gymId) {
        try {
            return cardService.getByGymId(gymId);
        } catch (CardService.CardNotFoundException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
