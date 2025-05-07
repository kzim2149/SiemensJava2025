package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.controller.ItemController;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    // MockMvc is used to perform HTTP requests in tests
    @Autowired
    private MockMvc mockMvc;

    // MockBean is used to create a mock of the ItemService
    @MockBean
    private ItemService itemService;

    // ObjectMapper is used to convert Java objects to JSON and vice versa
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllItems_returnsOk() throws Exception {
        // Mocking the ItemService to return a list of items
        Mockito.when(itemService.findAll()).thenReturn(List.of(new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com")));

        // Performing a GET request to the /api/items endpoint and expecting a 200 OK response
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk());
    }

    @Test
    void createItem_valid_returnsCreated() throws Exception {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        // Mocking the ItemService to return the created item
        Mockito.when(itemService.save(Mockito.any())).thenReturn(item);

        // Performing a POST request to the /api/items endpoint with the item data
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated());
    }

    @Test
    void getItemById_found_returnsItem() throws Exception {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        // Mocking the ItemService to return the item when searched by ID
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(item));

        // Performing a GET request to the /api/items/{id} endpoint and expecting a 200 OK response
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_found_returnsUpdatedItem() throws Exception {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");

        // Mocking the ItemService to return the item when searched by ID
        Mockito.when(itemService.findById(1L)).thenReturn(Optional.of(item));
        // Mocking the ItemService to return the updated item
        Mockito.when(itemService.save(Mockito.any())).thenReturn(item);

        // Performing a PUT request to the /api/items/{id} endpoint with the item data, and expecting a 200 OK response
        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteItem_returnsNoContent() throws Exception {
        // Performing a DELETE request to the /api/items/{id} endpoint, and expecting a 204 No Content response
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());
    }

}
