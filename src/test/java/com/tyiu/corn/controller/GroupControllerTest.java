package com.tyiu.corn.controller;

import com.tyiu.corn.model.dto.GroupDTO;
import com.tyiu.corn.model.dto.UserDTO;
import com.tyiu.corn.model.entities.User;
import com.tyiu.corn.model.enums.Role;
import com.tyiu.corn.model.requests.RegisterRequest;
import com.tyiu.corn.model.responses.AuthenticationResponse;
import com.tyiu.corn.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = "de.flapdoodle.mongodb.embedded.version=5.0.5"
)
class GroupControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    private String jwt;


    @BeforeAll
    public void setUp() {
        RegisterRequest request = new RegisterRequest(
                "fakemail2", "fakename", "fakename", "fakepass", List.of(Role.ADMIN));
        AuthenticationResponse response = webTestClient
                .post()
                .uri("/api/v1/auth/register")
                .body(Mono.just(request), RegisterRequest.class)
                .exchange()
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();
        assertNotNull(response);
        jwt = response.getToken();
    }

    @Test
    void testCreateGroup() {
        GroupDTO group3 = GroupDTO.builder().name("title").build();
        GroupDTO response3 = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(group3), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();
        assertEquals(group3.getName(), response3.getName());
    }

    @Test
    void testUpdateGroup(){
        GroupDTO group5 = GroupDTO.builder().name("title").build();
        GroupDTO response4 = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(group5), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();
        assertEquals(group5.getName(), response4.getName());
        String id = response4.getId();
        group5 = GroupDTO.builder().name("title 2").build();
        GroupDTO response5 = webTestClient
                .put()
                .uri("/api/v1/group/update/{id}", id)
                .header("Authorization","Bearer " + jwt)
                .body(Mono.just(group5), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();
    }

    @Test
    void testDeleteGroup(){
        GroupDTO group6 = GroupDTO.builder()
                .name("title")
                .build();
        GroupDTO response6 = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(group6), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();
        assertEquals(group6.getName(), response6.getName());
        String id = response6.getId();
        webTestClient
                .delete()
                .uri("/api/v1/group/delete/{id}", id)
                .header("Authorization","Bearer " + jwt)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetGroupById(){
        GroupDTO group = GroupDTO.builder()
                .name("title")
                .build();
        GroupDTO addGroupResponse = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(group), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();
        String id = addGroupResponse.getId();
        assertEquals(group.getName(), addGroupResponse.getName());

        GroupDTO responseGet = webTestClient
                .get()
                .uri("/api/v1/group/{id}", id)
                .header("Authorization","Bearer " + jwt)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();
        assertEquals(group.getName(), responseGet.getName());
    }

    @Test
    void testGetAllGroups() {
        GroupDTO group1 = GroupDTO.builder()
                .name("title")
                .build();
        GroupDTO addGroupResponse1 = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(group1), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();

        GroupDTO group = GroupDTO.builder()
                .name("title 2")
                .build();
        GroupDTO addGroupResponse = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(group), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();

        List<GroupDTO> allGroup = webTestClient
                .get()
                .uri("api/v1/group/all")
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectBodyList(GroupDTO.class)
                .returnResult().getResponseBody();
        assertEquals(allGroup.size(), 5);
    }

    @Test
    void testGetAllUsersByGroup(){
        RegisterRequest request = new RegisterRequest("user@mail.com", "firstname", "lastname", "password", List.of(Role.ADMIN));
        AuthenticationResponse user = webTestClient
                .post()
                .uri("/api/v1/auth/register")
                .body(Mono.just(request), RegisterRequest.class)
                .exchange()
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();


        UserDTO userDTO = UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .lastName(user.getLastName())
                .firstName(user.getFirstName())
                .roles(user.getRoles())
                .build();

        GroupDTO expectedGroup = GroupDTO.builder()
                .name("Group 1")
                .roles(List.of(Role.ADMIN))
                .users(List.of(userDTO))
                .build();

        GroupDTO addGroupResponse = webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(expectedGroup), GroupDTO.class)
                .exchange()
                .expectBody(GroupDTO.class)
                .returnResult().getResponseBody();

        GroupDTO groupWithOutUser =  GroupDTO.builder()
                .name("Group 2")
                .roles(List.of(Role.ADMIN))
                .build();

        webTestClient
                .post()
                .uri("/api/v1/group/add")
                .header("Authorization", "Bearer " + jwt)
                .body(Mono.just(expectedGroup), GroupDTO.class)
                .exchange();

        List<GroupDTO> group = webTestClient
                .get()
                .uri("/api/v1/group/all/{userId}", user.getId())
                .header("Authorization", "Bearer " + jwt)
                .exchange()
                .expectBodyList(GroupDTO.class)
                .returnResult().getResponseBody();
    }
}

