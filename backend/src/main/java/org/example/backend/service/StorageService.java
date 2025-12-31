package org.example.backend.service;

import org.example.backend.dto.SavedResultDto;
import org.example.backend.dto.ScriptDto;
import org.example.backend.entity.SavedResult;
import org.example.backend.entity.Script;
import org.example.backend.exception.ResourceNotFoundException;
import org.example.backend.repository.SavedResultRepository;
import org.example.backend.repository.ScriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StorageService {

    private final ScriptRepository scriptRepository;
    private final SavedResultRepository savedResultRepository;

    public StorageService(ScriptRepository scriptRepository, SavedResultRepository savedResultRepository) {
        this.scriptRepository = scriptRepository;
        this.savedResultRepository = savedResultRepository;
    }

    @Transactional
    public ScriptDto saveScript(ScriptDto dto) {
        Script script = new Script();
        script.setName(dto.getName());
        script.setContent(dto.getContent());
        Script savedScript = scriptRepository.save(script);
        return convertScriptToDto(savedScript, true);
    }

    @Transactional(readOnly = true)
    public List<ScriptDto> findAllScripts() {
        return scriptRepository.findAll().stream()
                .sorted(Comparator.comparing(Script::getCreatedAt).reversed())
                .map(script -> convertScriptToDto(script, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ScriptDto findScriptById(Long id) {
        return scriptRepository.findById(id)
                .map(script -> convertScriptToDto(script, true))
                .orElseThrow(() -> new ResourceNotFoundException("Script not found with id: " + id));
    }

    @Transactional
    public SavedResultDto saveResult(SavedResultDto dto) {
        SavedResult result = new SavedResult();
        result.setName(dto.getName());
        result.setContent(dto.getContent());
        SavedResult savedResult = savedResultRepository.save(result);
        return convertResultToDto(savedResult, true);
    }

    @Transactional(readOnly = true)
    public List<SavedResultDto> findAllResults() {
        return savedResultRepository.findAll().stream()
                .sorted(Comparator.comparing(SavedResult::getCreatedAt).reversed())
                .map(result -> convertResultToDto(result, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SavedResultDto findResultById(Long id) {
        return savedResultRepository.findById(id)
                .map(result -> convertResultToDto(result, true))
                .orElseThrow(() -> new ResourceNotFoundException("Result not found with id: " + id));
    }

    private ScriptDto convertScriptToDto(Script script, boolean includeContent) {
        ScriptDto dto = new ScriptDto();
        dto.setId(script.getId());
        dto.setName(script.getName());
        dto.setCreatedAt(script.getCreatedAt());
        if (includeContent) {
            dto.setContent(script.getContent());
        }
        return dto;
    }

    private SavedResultDto convertResultToDto(SavedResult result, boolean includeContent) {
        SavedResultDto dto = new SavedResultDto();
        dto.setId(result.getId());
        dto.setName(result.getName());
        dto.setCreatedAt(result.getCreatedAt());
        if (includeContent) {
            dto.setContent(result.getContent());
        }
        return dto;
    }
}
