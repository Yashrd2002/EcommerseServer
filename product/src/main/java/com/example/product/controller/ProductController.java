package com.example.product.controller;

import com.example.product.exceptions.ResourceNotFoundException;
import com.example.product.model.Product;
import com.example.product.payload.ProductDTO;
import com.example.product.services.ImageUploadService;
import com.example.product.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ImageUploadService imageUploadService;

    // ✅ Create Product (with multiple images)
    @PostMapping(value = "/seller/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Product> createProduct(
            @RequestPart("product") @Valid ProductDTO product,
            @RequestPart("images") List<MultipartFile> images,
            @RequestHeader("X-User-Id") Long userId
    ) throws IOException {
        List<String> imageUrls = new ArrayList<>();
        for (MultipartFile image : images) {
            String url = imageUploadService.uploadImage(image);
            imageUrls.add(url);
        }
        product.setImages(imageUrls);
        Product created = productService.addProduct(product, userId);
        return ResponseEntity.ok(created);
    }

    // ✅ Unified View Endpoint with Filters, Search, Sort, and Pagination
    @GetMapping("/public/view")
    public ResponseEntity<Map<String, Object>> getAllProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "price") String sortBy,  // price | rating | name
            @RequestParam(defaultValue = "asc") String sortOrder, // asc | desc
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<ProductDTO> productsPage = productService.getFilteredProducts(
                categoryId, minPrice, maxPrice, minRating, inStock, search, sortBy, sortOrder, page, size
        );

        Map<String, Object> response = new HashMap<>();
        response.put("products", productsPage.getContent());
        response.put("currentPage", productsPage.getNumber());
        response.put("totalItems", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());
        response.put("pageSize", productsPage.getSize());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // ✅ Single Product by ID
    @GetMapping("/public/view/{productId}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long productId) {
        return productService.getProductById(productId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
    }

    // ✅ Update Product
    @PutMapping(
    	    value = "/seller/update/{productId}",
    	    consumes = { MediaType.MULTIPART_FORM_DATA_VALUE }
    	)
    	public ResponseEntity<Product> updateProduct(
    	        @PathVariable Long productId,
    	        @RequestPart(value = "product", required = true) String productJson,
    	        @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages
    	) throws IOException {

    	    // Deserialize JSON manually
    	    ObjectMapper mapper = new ObjectMapper();
    	    ProductDTO productDto = mapper.readValue(productJson, ProductDTO.class);

    	    // Upload new images
    	    List<String> newImageUrls = new ArrayList<>();
    	    if (newImages != null && !newImages.isEmpty()) {
    	        for (MultipartFile image : newImages) {
    	            String uploadedUrl = imageUploadService.uploadImage(image);
    	            newImageUrls.add(uploadedUrl);
    	        }
    	    }

    	    // Merge existing + new
    	    List<String> finalImages = new ArrayList<>();
    	    if (productDto.getImages() != null) {
    	        finalImages.addAll(productDto.getImages());
    	    }
    	    finalImages.addAll(newImageUrls);
    	    productDto.setImages(finalImages);

    	    Product updatedProduct = productService.updateProduct(productId, productDto);
    	    return ResponseEntity.ok(updatedProduct);
    	}



    // ✅ Delete Product
    @DeleteMapping("/seller/delete/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

 // ✅ Products by Seller (Admin) with Pagination + Category Filter + Search
    @GetMapping("/admin/{sellerId}")
    public ResponseEntity<Map<String, Object>> getProductsBySellerAdmin(
            @PathVariable Long sellerId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<ProductDTO> productsPage = productService.getProductsBySellerAdmin(sellerId, categoryId, search, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productsPage.getContent());
        response.put("currentPage", productsPage.getNumber());
        response.put("totalItems", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());
        response.put("pageSize", productsPage.getSize());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    
    @GetMapping("/seller/getProducts")
    public ResponseEntity<Map<String, Object>> getProductsForSeller(
            @RequestHeader("X-User-Id") Long userId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        Page<ProductDTO> productsPage = productService.getProductsForSeller(userId, search, page, size);

        Map<String, Object> response = new HashMap<>();
        response.put("products", productsPage.getContent());
        response.put("currentPage", productsPage.getNumber());
        response.put("totalItems", productsPage.getTotalElements());
        response.put("totalPages", productsPage.getTotalPages());
        response.put("pageSize", productsPage.getSize());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
