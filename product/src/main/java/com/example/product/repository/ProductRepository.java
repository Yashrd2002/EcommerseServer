package com.example.product.repository;



import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.product.model.Product;



@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
	List<Product> findBySellerId(Long sellerId);

	List<Product> findByCategoryCategoryId(Long categoryId);

	@Query(value = "SELECT * FROM products WHERE product_name LIKE CONCAT('%', :product, '%')", nativeQuery = true)
	List<Product> searchProduct(@Param("product") String product);

	Page<Product> findAll(Pageable pageable);
}
