package com.company.chatbot.controller;

import com.company.chatbot.dto.SkillMapResponse;
import com.company.chatbot.service.SkillMappingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/skills")
@RequiredArgsConstructor
public class SkillMapController {

    private final SkillMappingService skillMappingService;

    @GetMapping("/map")
    public ResponseEntity<SkillMapResponse> getSkillMap(
        @RequestParam Long userId,
        @RequestParam(defaultValue = "false") boolean refresh
    ) {
        return ResponseEntity.ok(skillMappingService.getSkillMap(userId, refresh));
    }
}
