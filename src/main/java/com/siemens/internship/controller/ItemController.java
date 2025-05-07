package com.siemens.internship.controller;

import com.siemens.internship.exception.NotFoundException;
import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/items")
@Validated  // Enables method-level validation
public class ItemController {

    @Autowired
    private ItemService itemService;

    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<Item> createItem(@RequestBody @Valid Item item) {
        // should return 400 if validation fails, 201 if successful
        return new ResponseEntity<>(itemService.save(item), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable  @Positive Long id) {
        // should return 200 if found, 404 if not found
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElseThrow(() -> new NotFoundException("Item not found with id: " + id));
        // throwing NotFoundException will return 404
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable Long id, @RequestBody Item item) {
        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isEmpty()) {
            throw new NotFoundException("Item not found with id: " + id);
        }
        item.setId(id);
        // should return 200 if found and updated, 404 if not found
        return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        // should return 204 if deleted or not found (idempotent delete)
        itemService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        try {
            // Wait for async processing to complete and get the result
            List<Item> processedItems = itemService.processItemsAsync().get(); // blocks thread
            return new ResponseEntity<>(processedItems, HttpStatus.OK);
        }catch (InterruptedException | ExecutionException e) {
            // Log error and return a 500 Internal Server Error
            System.err.println("Failed to process items: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
