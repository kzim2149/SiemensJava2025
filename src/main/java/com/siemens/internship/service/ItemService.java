package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    // @Qualifier("taskExecutor") is used to inject the custom executor bean defined in AsyncConfig
    // this allows for better control over the thread pool configuration
    @Autowired
    @Qualifier("taskExecutor")
    private Executor taskExecutor;
    private final ExecutorService executor = Executors.newFixedThreadPool(10);

    // private List<Item> processedItems = new ArrayList<>();
    // private int processedCount = 0;
    // these variables are not thread-safe (shared mutable state) and can cause issues in a concurrent environment


    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    //  @Async and manual CompletableFuture.runAsync(..., executor) used together, the second one is redundant
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();

        // CompletableFuture.completedFuture(processedItems) is returned immediately, instead of waiting for all items to be processed
        // this is not the intended behavior, as it will return an empty list immediately

        // create a list of CompletableFutures for each item ID
        // using CompletableFuture.supplyAsync() to process each item in parallel
        // this will run the processItem method asynchronously in the executor thread pool
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> processItem(id), executor))
                .toList();

        // wait for all futures to complete, collect the results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(voided -> futures.stream()
                        .map(CompletableFuture::join) // join is safe now because all futures are done
                        .filter(Objects::nonNull) // filter out null items
                        .collect(Collectors.toList()));
    }

    // processes an item by its ID, returns the processed item or null if not found or exception occurs
    private Item processItem(Long id) {
        try {
            Thread.sleep(100);

            Optional<Item> optionalItem = itemRepository.findById(id);
            if (optionalItem.isEmpty()) return null;

            Item item = optionalItem.get();
            item.setStatus("PROCESSED");
            return itemRepository.save(item);

        } catch (Exception e) {
            System.err.println("Failed to process item with ID " + id + ": " + e.getMessage());
            return null;
        }
    }

}

