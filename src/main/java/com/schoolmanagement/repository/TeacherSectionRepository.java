package com.schoolmanagement.repository;

import com.schoolmanagement.entity.TeacherSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface TeacherSectionRepository extends JpaRepository<TeacherSection, Long> {

    @Query("SELECT ts.section.id FROM TeacherSection ts WHERE ts.teacher.id = :teacherId AND ts.academicYear.id = :yearId")
    List<Long> findSectionIdsByTeacherAndYear(@Param("teacherId") UUID teacherId,
                                              @Param("yearId") Long yearId);
}
