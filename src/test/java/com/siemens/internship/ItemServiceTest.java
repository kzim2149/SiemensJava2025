package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import com.siemens.internship.service.ItemService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    public ItemServiceTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void findById_returnsItem() {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        // Mocking the repository to return the item when searched by ID
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        // Calling the service method to find the item by ID, and checking if it returns the expected item
        Optional<Item> result = itemService.findById(1L);
        assertTrue(result.isPresent());
        assertEquals("Item 1", result.get().getName());
    }

    @Test
    void save_savesItem() {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        // Mocking the repository to return the item when saved
        when(itemRepository.save(item)).thenReturn(item);

        // Calling the service method to save the item, and checking if it returns the expected item
        Item saved = itemService.save(item);
        assertEquals("Item 1", saved.getName());
    }

    @Test
    void deleteById_callsRepository() {
        itemService.deleteById(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }
}
