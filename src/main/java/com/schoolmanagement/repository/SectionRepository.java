package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SectionRepository extends JpaRepository<Section, Long> {

    @Query("SELECT s FROM Section s JOIN FETCH s.grade ORDER BY s.grade.orderNum, s.name")
    List<Section> findAllWithGrade();
}
