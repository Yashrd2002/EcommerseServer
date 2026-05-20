
package com.example.cart.service;

import com.example.cart.client.InventoryClient;
import com.example.cart.client.ProductClient;
import com.example.cart.client.UserClient;
import com.example.cart.exception.APIException;
import com.example.cart.exception.ResourceNotFoundException;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.payload.CartItemRequest;
import com.example.cart.payload.CartItemResponse;
import com.example.cart.payload.CartResponse;
import com.example.cart.payload.InventoryDto;
import com.example.cart.payload.ProductDTO;
import com.example.cart.payload.UserResponse;
import com.example.cart.repositories.CartItemRepository;
import com.example.cart.repositories.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductClient productClient;


    @Autowired
    private InventoryClient inventoryClient;


    public Cart getCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = new Cart();
                    newCart.setUserId(userId);
                    return cartRepository.save(newCart);
                });
    }

    public Cart addToCart(CartItemRequest request, Long userId) {
        ProductDTO product;
        try {
            product = productClient.getProductById(request.getProductId()).getBody();
        } catch (Exception e) {
            throw new ResourceNotFoundException("Product", "productId", request.getProductId());
        }


        Cart cart = getCart(userId);

        Optional<CartItem> existingItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), request.getProductId());

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItem newItem = new CartItem();
            newItem.setProductId(request.getProductId());
            newItem.setQuantity(request.getQuantity());
            newItem.setCart(cart);
            cart.getItems().add(newItem);
            cartItemRepository.save(newItem);
        }

        return cartRepository.save(cart);
    }

    public void removeItem(Long userId, Long productId) {
        Cart cart = getCart(userId);
        Optional<CartItem> item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);
        if (item.isEmpty()) {
            throw new ResourceNotFoundException("CartItem", "productId", productId);
        }
        cartItemRepository.delete(item.get());
    }

    public void clearCart(Long userId) {
        Cart cart = getCart(userId);
        cartItemRepository.deleteAll(cart.getItems());
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public CartResponse getCartResponse(Long userId) {
        Cart cart = getCart(userId);

        CartResponse response = new CartResponse();
        response.setCartId(cart.getId());
        response.setUserId(cart.getUserId());

        List<CartItemResponse> itemResponses = cart.getItems().stream().map(item -> {
            CartItemResponse itemResponse = new CartItemResponse();
            itemResponse.setCartItemId(item.getId());
            itemResponse.setProductId(item.getProductId());
            itemResponse.setQuantity(item.getQuantity());
            
            try {
                ProductDTO product = productClient.getProductById(item.getProductId()).getBody();
                itemResponse.setProductName(product.getProductName());
                itemResponse.setImages(product.getImages());
                itemResponse.setPrice(product.getPrice());
                itemResponse.setSpecialPrice(product.getSpecialPrice());
                itemResponse.setSellerId(product.getSellerId());
            } catch (Exception e) {
                itemResponse.setProductName("Product not found");
                itemResponse.setPrice(0.0);
                itemResponse.setSpecialPrice(0.0);
            }
            try {
                InventoryDto stock = inventoryClient.checkStock(item.getProductId());
                itemResponse.setStockAvailable(stock.getQuantityAvailable());
            } catch (Exception e) {
                itemResponse.setStockAvailable(0);
            }


            return itemResponse;
        }).collect(Collectors.toList());

        response.setItems(itemResponses);
        response.setTotalItems(itemResponses.size());
        response.setTotalQuantity(itemResponses.stream().mapToInt(CartItemResponse::getQuantity).sum());

        return response;
    }
}
