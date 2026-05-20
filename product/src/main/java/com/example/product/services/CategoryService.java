package com.example.product.services;

import com.example.product.exceptions.ResourceNotFoundException;
import com.example.product.model.Category;
import com.example.product.model.Product;
import com.example.product.repository.CategoryRepository;
import com.example.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    public Category addCategory(Category category) {
        categoryRepository.findByCategoryNameIgnoreCase(category.getCategoryName())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Category with name '" + category.getCategoryName() + "' already exists.");
                });
        return categoryRepository.save(category);
    }

    @Transactional
    public Category updateCategory(Long categoryId, Category updatedCategory) {
        Category existing = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        existing.setCategoryName(updatedCategory.getCategoryName());
        existing.setDescription(updatedCategory.getDescription());
        return categoryRepository.save(existing);
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        List<Product> products = productRepository.findByCategoryCategoryId(categoryId);
        if (!products.isEmpty()) {
            throw new IllegalStateException("Cannot delete category with existing products.");
        }

        categoryRepository.delete(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
    }
}
