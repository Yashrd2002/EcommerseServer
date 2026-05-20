package com.example.authservice.service;

import com.example.authservice.dto.AddressRequest;
import com.example.authservice.dto.AddressResponse;
import com.example.authservice.exceptions.APIException;
import com.example.authservice.exceptions.ResourceNotFoundException;
import com.example.authservice.model.Address;
import com.example.authservice.model.User;
import com.example.authservice.repository.AddressRepository;
import com.example.authservice.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class AddressServiceTest {

    @Mock private AddressRepository addressRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AddressService addressService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addAddress_ShouldSaveAddress() {
        User user = new User();
        user.setId(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        AddressRequest req = new AddressRequest("Street", "City", "State", "12345", "Country");
        addressService.addAddress(req, 1L);

        verify(addressRepository, times(1)).save(any(Address.class));
    }

    @Test
    void addAddress_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        AddressRequest req = new AddressRequest("Street", "City", "State", "12345", "Country");

        assertThatThrownBy(() -> addressService.addAddress(req, 99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void editAddress_ShouldUpdateAddress_WhenAuthorized() {
        User user = new User();
        user.setId(1L);
        Address address = Address.builder().id(10L).user(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        AddressRequest req = new AddressRequest("New St", "City", "State", "12345", "Country");
        addressService.editAddress(req, 1L, 10L);

        verify(addressRepository, times(1)).save(address);
    }

    @Test
    void editAddress_ShouldThrow_WhenUnauthorized() {
        User user = new User();
        user.setId(1L);
        User another = new User();
        another.setId(2L);

        Address address = Address.builder().id(10L).user(another).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        AddressRequest req = new AddressRequest("New St", "City", "State", "12345", "Country");

        assertThatThrownBy(() -> addressService.editAddress(req, 1L, 10L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deleteAddress_ShouldDelete_WhenAuthorized() {
        User user = new User();
        user.setId(1L);
        Address address = Address.builder().id(10L).user(user).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        addressService.deleteAddress(1L, 10L);
        verify(addressRepository, times(1)).delete(address);
    }

    @Test
    void deleteAddress_ShouldThrow_WhenUnauthorized() {
        User user = new User();
        user.setId(1L);
        User another = new User();
        another.setId(2L);

        Address address = Address.builder().id(10L).user(another).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findById(10L)).thenReturn(Optional.of(address));

        assertThatThrownBy(() -> addressService.deleteAddress(1L, 10L))
                .isInstanceOf(APIException.class);
    }

    @Test
    void getAddresses_ShouldReturnList_WhenUserExists() {
        User user = new User();
        user.setId(1L);

        Address addr1 = Address.builder()
                .id(1L).street("Street1").city("City1").state("State1").postalCode("11111").country("Country1").user(user)
                .build();

        Address addr2 = Address.builder()
                .id(2L).street("Street2").city("City2").state("State2").postalCode("22222").country("Country2").user(user)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(addressRepository.findByUserId(1L)).thenReturn(List.of(addr1, addr2));

        List<AddressResponse> responses = addressService.getAddresses(1L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getCity()).isEqualTo("City1");
        verify(userRepository, times(1)).findById(1L);
        verify(addressRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getAddresses_ShouldThrow_WhenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> addressService.getAddresses(1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User");
    }

    @Test
    void getAddressById_ShouldReturnMappedResponse() {
        Address address = Address.builder()
                .id(1L).street("Street").city("City").state("State").postalCode("12345").country("Country")
                .build();

        when(addressRepository.findById(1L)).thenReturn(Optional.of(address));

        AddressResponse resp = addressService.getAddressById(1L);
        assertThat(resp.getStreet()).isEqualTo("Street");
    }

    @Test
    void getAddressById_ShouldThrow_WhenNotFound() {
        when(addressRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> addressService.getAddressById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Address");
    }
}
