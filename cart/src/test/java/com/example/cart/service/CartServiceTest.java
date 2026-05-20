package com.example.cart.service;

import com.example.cart.client.InventoryClient;
import com.example.cart.client.ProductClient;
import com.example.cart.exception.ResourceNotFoundException;
import com.example.cart.model.Cart;
import com.example.cart.model.CartItem;
import com.example.cart.payload.*;
import com.example.cart.repositories.CartItemRepository;
import com.example.cart.repositories.CartRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductClient productClient;
    @Mock private InventoryClient inventoryClient;

    @InjectMocks private CartService cartService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void getCart_ShouldReturnExistingCart() {
        Cart existingCart = new Cart();
        existingCart.setId(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(existingCart));

        Cart result = cartService.getCart(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getCart_ShouldCreateNewCart_WhenNoneExists() {
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart cart = cartService.getCart(1L);
        assertThat(cart.getUserId()).isEqualTo(1L);
    }

    @Test
    void addToCart_ShouldAddNewItem() {
        ProductDTO product = new ProductDTO();
        product.setProductId(10L);
        product.setProductName("Item");

        when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(product));

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);
        cart.setItems(new ArrayList<>());
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        CartItemRequest req = new CartItemRequest(10L, 2);

        cartService.addToCart(req, 1L);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void addToCart_ShouldUpdateQuantity_WhenItemExists() {
        ProductDTO product = new ProductDTO();
        product.setProductId(10L);

        when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(product));

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);

        CartItem item = new CartItem();
        item.setId(5L);
        item.setCart(cart);
        item.setProductId(10L);
        item.setQuantity(2);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 10L)).thenReturn(Optional.of(item));

        CartItemRequest req = new CartItemRequest(10L, 3);
        cartService.addToCart(req, 1L);

        assertThat(item.getQuantity()).isEqualTo(5);
    }

    @Test
    void addToCart_ShouldThrow_WhenProductNotFound() {
        CartItemRequest req = new CartItemRequest(99L, 1);
        when(productClient.getProductById(99L)).thenThrow(new RuntimeException("Product not found"));

        assertThatThrownBy(() -> cartService.addToCart(req, 1L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void removeItem_ShouldDeleteItem_WhenExists() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(1L);

        CartItem item = new CartItem();
        item.setId(3L);
        item.setCart(cart);
        item.setProductId(5L);

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 5L)).thenReturn(Optional.of(item));

        cartService.removeItem(1L, 5L);
        verify(cartItemRepository, times(1)).delete(item);
    }

    @Test
    void removeItem_ShouldThrow_WhenNotFound() {
        Cart cart = new Cart();
        cart.setId(1L);
        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));
        when(cartItemRepository.findByCartIdAndProductId(1L, 9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem(1L, 9L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void clearCart_ShouldDeleteAllItems() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setItems(new ArrayList<>(List.of(new CartItem(), new CartItem())));

        when(cartRepository.findByUserId(1L)).thenReturn(Optional.of(cart));

        cartService.clearCart(1L);

        verify(cartItemRepository, times(1)).deleteAll(anyList());
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void getCartResponse_ShouldMapAllFields() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(2L);

        CartItem item = new CartItem();
        item.setId(3L);
        item.setCart(cart);
        item.setProductId(10L);
        item.setQuantity(2);
        cart.setItems(List.of(item));

        when(cartRepository.findByUserId(2L)).thenReturn(Optional.of(cart));

        ProductDTO product = new ProductDTO();
        product.setProductId(10L);
        product.setProductName("ProductX");
        product.setPrice(100.0);
        product.setSpecialPrice(90.0);
        product.setSellerId(1L);

        when(productClient.getProductById(10L)).thenReturn(ResponseEntity.ok(product));
        when(inventoryClient.checkStock(10L)).thenReturn(new InventoryDto(10L, 5));

        CartResponse response = cartService.getCartResponse(2L);

        assertThat(response.getCartId()).isEqualTo(1L);
        assertThat(response.getTotalItems()).isEqualTo(1);
        assertThat(response.getItems().get(0).getProductName()).isEqualTo("ProductX");
        assertThat(response.getItems().get(0).getStockAvailable()).isEqualTo(5);
    }
}
