package com.siemens.internship;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class InternshipApplicationTests {

    // the port number of the server
    @LocalServerPort
    private int port;

    // TestRestTemplate is used to perform HTTP requests in tests
    @Autowired
    private TestRestTemplate restTemplate;

    // ItemRepository is used to interact with the database
    @Autowired
    private ItemRepository itemRepository;

    // baseUrl is the base URL for the API
    private String baseUrl;

    // setup() method is called before each test
    @BeforeEach
    void setup() {
        baseUrl = "http://localhost:" + port + "/api/items";
        itemRepository.deleteAll();
    }


    // contextLoads() method is used to verify that the Spring context starts correctly
    @Test
    void contextLoads() {
        // Verifies Spring context starts
    }

    // createAndGetItem_successfully() method tests the creation and retrieval of an item
    @Test
    void createAndGetItem_successfully() {
        Item item = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        // postForEntity() method is used to send a POST request to create an item
        ResponseEntity<Item> response = restTemplate.postForEntity(baseUrl, item, Item.class);

        // Verifies that the response status is 201 Created
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        // Verifies that the response body is not null and contains the created item
        Item created = response.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getName()).isEqualTo(item.getName());

        // Verifies that the item is saved in the database
        ResponseEntity<Item> getResponse = restTemplate.getForEntity(baseUrl + "/" + created.getId(), Item.class);
        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
    }

    // getItemById_notFound() method tests the retrieval of an item by ID when it does not exist
    @Test
    void getAllItems_returnsList() {
        Item item1 = new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com");
        itemRepository.save(item1);

        // getForEntity() method is used to send a GET request to retrieve all items
        ResponseEntity<Item[]> response = restTemplate.getForEntity(baseUrl, Item[].class);

        // Verifies that the response status is 200 OK
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    // updateItem_updatesSuccessfully() method tests the update of an item
    @Test
    void updateItem_updatesSuccessfully() {
        Item item = itemRepository.save(new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com"));
        item.setName("AfterUpdate");

        // HttpHeaders is used to set the content type of the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // HttpEntity is used to wrap the item and headers in the request
        HttpEntity<Item> request = new HttpEntity<>(item, headers);

        // exchange() method is used to send a PUT request to update the item
        ResponseEntity<Item> response = restTemplate.exchange(
                baseUrl + "/" + item.getId(),
                HttpMethod.PUT,
                request,
                Item.class
        );

        // Verifies that the response status is 200 OK and the response body contains the updated item
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).getName()).isEqualTo("AfterUpdate");
    }

    // deleteItem_returnsNoContent() method tests the deletion of an item
    @Test
    void deleteItem_returnsNoContent() {
        Item item = itemRepository.save(new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com"));
        restTemplate.delete(baseUrl + "/" + item.getId());

        // verifies that the item is deleted from the database
        assertThat(itemRepository.findById(item.getId())).isEmpty();
    }

    // processItemsAsync_shouldUpdateStatus() method tests the asynchronous processing of items
    @Test
    void processItemsAsync_shouldUpdateStatus() throws InterruptedException {
        Item item1 = itemRepository.save(new Item(1L, "Item 1", "Description 1", "UNPROCESSED", "email@email.com"));
        Item item2 = itemRepository.save(new Item(2L, "Item 2", "Description 2", "UNPROCESSED", "email@email.com"));


        // getForEntity() method is used to send a GET request to process items
        ResponseEntity<Item[]> response = restTemplate.getForEntity(baseUrl + "/process", Item[].class);

        // simulate processing time
        Thread.sleep(500);

        // Verifies that the response status is 200 OK and the response body contains the processed items
        List<Item> updatedItems = itemRepository.findAll();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedItems).hasSize(2);
        assertThat(updatedItems).allMatch(i -> "PROCESSED".equals(i.getStatus()));
    }

    // createItem_shouldReturnBadRequest_whenValidationFails() method tests the creation of an item with invalid data
    @Test
    void createItem_shouldReturnBadRequest_whenValidationFails() {
        Item invalidItem = new Item(1L, "", "Description 1", "UNPROCESSED", "email@email.com");

        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, invalidItem, String.class);

        // Verifies that the response status is 400 Bad Request
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    // getItemById_shouldReturnNotFound_whenItemDoesNotExist() method tests the retrieval of an item by ID when it does not exist
    @Test
    void getItemById_shouldReturnNotFound_whenItemDoesNotExist() {
        long nonExistentId = 999L;

        ResponseEntity<String> response = restTemplate.getForEntity(baseUrl + "/" + nonExistentId, String.class);

        // Verifies that the response status is 404 Not Found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // updateItem_shouldReturnNotFound_whenItemDoesNotExist() method tests the update of an item when it does not exist
    @Test
    void updateItem_shouldReturnNotFound_whenItemDoesNotExist() {
        Item item = new Item(1L, "", "Description 1", "UNPROCESSED", "email@email.com");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Item> request = new HttpEntity<>(item, headers);

        ResponseEntity<String> response = restTemplate.exchange(
                baseUrl + "/" + item.getId(),
                HttpMethod.PUT,
                request,
                String.class
        );

        // Verifies that the response status is 404 Not Found
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteItem_shouldReturnNoContent_whenItemDoesNotExist() {
        long nonExistentId = 12345L;

        ResponseEntity<Void> response = restTemplate.exchange(
                baseUrl + "/" + nonExistentId,
                HttpMethod.DELETE,
                null,
                Void.class
        );

        // Verifies that the response status is 204 No Content
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

}
