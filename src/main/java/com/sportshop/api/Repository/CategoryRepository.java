package com.sportshop.api.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.sportshop.api.Domain.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
}