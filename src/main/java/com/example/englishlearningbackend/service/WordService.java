package com.example.englishlearningbackend.service;

import com.example.englishlearningbackend.entity.Word;
import com.example.englishlearningbackend.repository.WordRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WordService {
    private final WordRepository wordRepository;

    public WordService(WordRepository wordRepository) {
        this.wordRepository = wordRepository;
    }

    public List<Word> getAllWords() {
        return wordRepository.findAll();
    }

    public Optional<Word> getWordWithDetails(String word) {
        return Optional.ofNullable(wordRepository.findWordWithDetails(word));
    }
}
