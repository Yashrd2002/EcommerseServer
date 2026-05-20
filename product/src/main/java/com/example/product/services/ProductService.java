package com.example.product.services;

import com.example.product.client.InventoryClient;
import com.example.product.exceptions.ResourceNotFoundException;
import com.example.product.model.Category;
import com.example.product.model.Product;
import com.example.product.payload.ProductDTO;
import com.example.product.repository.CategoryRepository;
import com.example.product.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private InventoryClient inventoryClient;

    public Product addProduct(ProductDTO productDTO, Long userId) {
        Category category = categoryRepository.findById(productDTO.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", productDTO.getCategoryId()));

        Product product = new Product();
        product.setProductName(productDTO.getProductName());
        product.setImages(productDTO.getImages());
        product.setDescription(productDTO.getDescription());
        product.setPrice(productDTO.getPrice());
        product.setDiscount(productDTO.getDiscount());
        product.setSpecialPrice(productDTO.getSpecialPrice());
        product.setCategory(category);
        product.setSellerId(userId);

        return productRepository.save(product);
    }

    private ProductDTO mapToDTO(Product product) {
        ProductDTO dto = new ProductDTO();
        dto.setProductId(product.getProductId());
        dto.setProductName(product.getProductName());
        dto.setImages(product.getImages());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setDiscount(product.getDiscount());
        dto.setSpecialPrice(product.getSpecialPrice());
        dto.setCategoryId(product.getCategory().getCategoryId());
        dto.setCategoryName(product.getCategory().getCategoryName());
        dto.setSellerId(product.getSellerId());

        if (product.getRatings() != null && !product.getRatings().isEmpty()) {
            double avg = product.getRatings().stream()
                    .mapToDouble(r -> r.getRatingValue())
                    .average()
                    .orElse(0.0);
            dto.setAverageRating(avg);
        } else {
            dto.setAverageRating(0.0);
        }

        try {
            dto.setStock(inventoryClient.checkStock(product.getProductId()).getQuantityAvailable());
        } catch (Exception e) {
            dto.setStock(0);
        }

        return dto;
    }

    // ✅ Combined Filters + Pagination + Search + Sort
    public Page<ProductDTO> getFilteredProducts(
            Long categoryId, Double minPrice, Double maxPrice, Double minRating,
            Boolean inStock, String search, String sortBy, String sortOrder,
            Integer page, Integer size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        List<Product> allProducts = productRepository.findAll();

        List<ProductDTO> filtered = allProducts.stream()
                .map(this::mapToDTO)
                .filter(p -> categoryId == null || p.getCategoryId().equals(categoryId))
                .filter(p -> minPrice == null || p.getPrice() >= minPrice)
                .filter(p -> maxPrice == null || p.getPrice() <= maxPrice)
                .filter(p -> minRating == null || p.getAverageRating() >= minRating)
                .filter(p -> inStock == null || (inStock ? p.getStock() > 0 : p.getStock() == 0))
                .filter(p -> search == null || p.getProductName().toLowerCase().contains(search.toLowerCase())
                        || p.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        // ✅ Sorting Logic
        Comparator<ProductDTO> comparator = switch (sortBy.toLowerCase()) {
            case "rating" -> Comparator.comparing(ProductDTO::getAverageRating);
            case "name" -> Comparator.comparing(ProductDTO::getProductName, String.CASE_INSENSITIVE_ORDER);
            default -> Comparator.comparing(ProductDTO::getPrice);
        };

        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }

        filtered.sort(comparator);

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ProductDTO> paged = start < filtered.size() ? filtered.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(paged, pageable, filtered.size());
    }

    public Optional<ProductDTO> getProductById(Long productId) {
        return productRepository.findById(productId).map(this::mapToDTO);
    }

    public Product updateProduct(Long productId, ProductDTO updatedProduct) {
        Category category = categoryRepository.findById(updatedProduct.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", updatedProduct.getCategoryId()));

        return productRepository.findById(productId).map(product -> {
            product.setProductName(updatedProduct.getProductName());
            product.setImages(updatedProduct.getImages());
            product.setDescription(updatedProduct.getDescription());
            product.setPrice(updatedProduct.getPrice());
            product.setDiscount(updatedProduct.getDiscount());
            product.setSpecialPrice(updatedProduct.getSpecialPrice());
            product.setCategory(category);
            return productRepository.save(product);
        }).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }


    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        productRepository.delete(product);
    }

    public List<ProductDTO> getProductsBySeller(Long sellerId) {
        List<Product> products = productRepository.findBySellerId(sellerId);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product", "sellerId", sellerId);
        }
        return products.stream().map(this::mapToDTO).toList();
    }
    public Page<ProductDTO> getProductsForSeller(Long userId, String search, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        List<Product> products = productRepository.findBySellerId(userId);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product", "sellerId", userId);
        }

        // Map to DTO and filter by search keyword if provided
        List<ProductDTO> filtered = products.stream()
                .map(this::mapToDTO)
                .filter(p -> search == null || 
                        p.getProductName().toLowerCase().contains(search.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        // Pagination manually since we are filtering in memory
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ProductDTO> paged = start < filtered.size() ? filtered.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(paged, pageable, filtered.size());
    }
    
    public Page<ProductDTO> getProductsBySellerAdmin(Long sellerId, Long categoryId, String search, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);

        // Get all products of that seller
        List<Product> products = productRepository.findBySellerId(sellerId);
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product", "sellerId", sellerId);
        }

        // Map and apply filters
        List<ProductDTO> filtered = products.stream()
                .map(this::mapToDTO)
                .filter(p -> categoryId == null || p.getCategoryId().equals(categoryId))
                .filter(p -> search == null || 
                        p.getProductName().toLowerCase().contains(search.toLowerCase()) ||
                        p.getDescription().toLowerCase().contains(search.toLowerCase()))
                .collect(Collectors.toList());

        // Apply pagination manually
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), filtered.size());
        List<ProductDTO> paged = start < filtered.size() ? filtered.subList(start, end) : Collections.emptyList();

        return new PageImpl<>(paged, pageable, filtered.size());
    }


}
