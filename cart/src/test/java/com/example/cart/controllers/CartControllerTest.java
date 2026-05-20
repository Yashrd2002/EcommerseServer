package com.example.cart.controllers;

import com.example.cart.model.Cart;
import com.example.cart.payload.CartItemRequest;
import com.example.cart.payload.CartResponse;
import com.example.cart.service.CartService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private CartService cartService;

    private CartResponse cartResponse;
    private Cart cart;

    @BeforeEach
    void setup() {
        cartResponse = new CartResponse();
        cartResponse.setCartId(1L);
        cartResponse.setUserId(2L);
        cartResponse.setTotalItems(2);
        cartResponse.setItems(List.of());
        cart = new Cart();
        cart.setId(1L);
    }

    @Test
    void getCart_ShouldReturnCartResponse() throws Exception {
        when(cartService.getCartResponse(2L)).thenReturn(cartResponse);

        mockMvc.perform(get("/cart/user/getCart").header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cartId").value(1L))
                .andExpect(jsonPath("$.totalItems").value(2));
    }

    @Test
    void addToCart_ShouldReturnCart() throws Exception {
        CartItemRequest req = new CartItemRequest(10L, 2);
        when(cartService.addToCart(any(CartItemRequest.class), eq(2L))).thenReturn(cart);

        mockMvc.perform(post("/cart/user/add")
                .header("X-User-Id", "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void removeItem_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/cart/user/remove")
                .header("X-User-Id", "2")
                .param("productId", "10"))
                .andExpect(status().isOk())
                .andExpect(content().string("Item removed"));

        verify(cartService).removeItem(2L, 10L);
    }

    @Test
    void clearCart_ShouldReturnOk() throws Exception {
        mockMvc.perform(delete("/cart/user/clear").header("X-User-Id", "2"))
                .andExpect(status().isOk())
                .andExpect(content().string("Cart cleared"));

        verify(cartService).clearCart(2L);
    }
}
