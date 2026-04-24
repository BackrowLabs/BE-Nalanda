package com.schoolmanagement.repository;

import com.schoolmanagement.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findAllByOrderByOrderNumAsc();
}
