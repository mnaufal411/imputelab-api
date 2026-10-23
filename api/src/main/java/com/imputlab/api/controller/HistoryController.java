package com.imputlab.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.imputlab.api.dto.AnalysisHistoryDetail;
import com.imputlab.api.dto.AnalysisHistoryItem;
import com.imputlab.api.service.HistoryService;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public List<AnalysisHistoryItem> getAllHistory() {
        return historyService.getAllHistory();
    }

    @GetMapping("/{id}")
    public AnalysisHistoryDetail getHistoryById(
            @PathVariable Long id
    ) {
        return historyService.getHistoryById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteHistory(@PathVariable Long id) {
        historyService.deleteHistory(id);
    }

}
