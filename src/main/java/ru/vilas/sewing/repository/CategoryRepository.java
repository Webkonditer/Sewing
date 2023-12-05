package ru.vilas.sewing.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vilas.sewing.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}

