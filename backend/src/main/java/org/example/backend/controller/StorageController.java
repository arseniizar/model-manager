package org.example.backend.controller;

import org.example.backend.dto.SavedResultDto;
import org.example.backend.dto.ScriptDto;
import org.example.backend.service.StorageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/storage")
public class StorageController {

    private final StorageService storageService;

    public StorageController(StorageService storageService) {
        this.storageService = storageService;
    }

    @PostMapping("/scripts")
    public ResponseEntity<ScriptDto> createScript(@RequestBody ScriptDto scriptDto) {
        ScriptDto savedScript = storageService.saveScript(scriptDto);
        return new ResponseEntity<>(savedScript, HttpStatus.CREATED);
    }

    @GetMapping("/scripts")
    public ResponseEntity<List<ScriptDto>> getAllScripts() {
        return ResponseEntity.ok(storageService.findAllScripts());
    }

    @GetMapping("/scripts/{id}")
    public ResponseEntity<ScriptDto> getScriptById(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.findScriptById(id));
    }

    @PostMapping("/results")
    public ResponseEntity<SavedResultDto> createResult(@RequestBody SavedResultDto resultDto) {
        SavedResultDto savedResult = storageService.saveResult(resultDto);
        return new ResponseEntity<>(savedResult, HttpStatus.CREATED);
    }

    @GetMapping("/results")
    public ResponseEntity<List<SavedResultDto>> getAllResults() {
        return ResponseEntity.ok(storageService.findAllResults());
    }



    @GetMapping("/results/{id}")
    public ResponseEntity<SavedResultDto> getResultById(@PathVariable Long id) {
        return ResponseEntity.ok(storageService.findResultById(id));
    }
}
