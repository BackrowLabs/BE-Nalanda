package com.schoolmanagement.dto.response;

import java.time.LocalDate;

public record AcademicYearResponse(Long id, String name, LocalDate startDate, LocalDate endDate, boolean isCurrent) {}
