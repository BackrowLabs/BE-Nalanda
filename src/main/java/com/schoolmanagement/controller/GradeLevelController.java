package com.schoolmanagement.controller;

import com.schoolmanagement.entity.Grade;
import com.schoolmanagement.repository.GradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/grade-levels")
@RequiredArgsConstructor
public class GradeLevelController {

    private final GradeRepository gradeRepository;

    @GetMapping
    public List<GradeResponse> list() {
        return gradeRepository.findAllByOrderByOrderNumAsc().stream()
                .map(g -> new GradeResponse(g.getId(), g.getName(), g.getOrderNum()))
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public GradeResponse create(@RequestBody CreateGradeRequest req) {
        var grade = Grade.builder()
                .name(req.name())
                .orderNum(req.orderNum())
                .build();
        var saved = gradeRepository.save(grade);
        return new GradeResponse(saved.getId(), saved.getName(), saved.getOrderNum());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        gradeRepository.deleteById(id);
    }

    public record GradeResponse(Long id, String name, int orderNum) {}
    public record CreateGradeRequest(String name, int orderNum) {}
}
