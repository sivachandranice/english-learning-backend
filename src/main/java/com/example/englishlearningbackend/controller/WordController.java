package com.example.englishlearningbackend.controller;

import com.example.englishlearningbackend.entity.Word;
import com.example.englishlearningbackend.service.WordService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/words")
public class WordController {
    private final WordService wordService;

    public WordController(WordService wordService) {
        this.wordService = wordService;
    }

    @GetMapping
    public List<Word> getAllWords() {
        return wordService.getAllWords();
    }

    @GetMapping("/{word}")
    public ResponseEntity<Word> getWordWithDetails(@PathVariable String word) {
        return wordService.getWordWithDetails(word)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
