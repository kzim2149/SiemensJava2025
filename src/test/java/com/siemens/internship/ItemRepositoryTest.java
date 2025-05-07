package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

// DataJpaTest is used to test JPA repositories
@DataJpaTest
public class ItemRepositoryTest {
    @Autowired
    private ItemRepository itemRepository;

    @Test
    void CRUD_worksCorrectly() {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        Item saved = itemRepository.save(item);

        assertNotNull(saved.getId());

        Item found = itemRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(item.getName(), found.getName());

        item.setName("Updated Item");
        Item updated = itemRepository.save(item);
        assertEquals("Updated Item", updated.getName());

        itemRepository.deleteById(saved.getId());
        Item deleted = itemRepository.findById(saved.getId()).orElse(null);
        assertNull(deleted);
    }
}
