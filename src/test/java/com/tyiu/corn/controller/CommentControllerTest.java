package com.tyiu.corn.controller;

import com.tyiu.corn.model.dto.CommentDTO;
import com.tyiu.corn.model.dto.IdeaDTO;
import com.tyiu.corn.model.enums.Role;
import com.tyiu.corn.model.requests.RegisterRequest;
import com.tyiu.corn.model.responses.AuthenticationResponse;
import com.tyiu.corn.model.responses.ErrorResponse;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class CommentControllerTest {
    @Autowired
    private WebTestClient webTestClient;
    private String jwt;
    private Long firstIdeaId;
    private Long secondIdeaId;
    private String initiatorEmail;
    private String initiatorJwt;
    @BeforeAll
    public void setUp(){
        RegisterRequest initiator = new RegisterRequest(
                "initiator","fakename","fakename","fakepass", List.of(Role.INITIATOR));
        AuthenticationResponse initiatorResponse = webTestClient
                .post()
                .uri("/api/v1/auth/register")
                .body(Mono.just(initiator), RegisterRequest.class)
                .exchange()
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();
        assertNotNull(initiatorResponse);
        RegisterRequest projectOffice = new RegisterRequest(
                "projectOffice","fakename","fakename","fakepass", List.of(Role.PROJECT_OFFICE));
        AuthenticationResponse projectOfficeResponse = webTestClient
                .post()
                .uri("/api/v1/auth/register")
                .body(Mono.just(projectOffice), RegisterRequest.class)
                .exchange()
                .expectBody(AuthenticationResponse.class)
                .returnResult().getResponseBody();
        assertNotNull(projectOfficeResponse);
        IdeaDTO idea = new IdeaDTO();
        idea.setName("Пирожки");
        idea.setBudget(1351L);
        idea.setCustomer("f");
        IdeaDTO ideaResponse = webTestClient.post()
        .uri("/api/v1/idea/initiator/add")
        .header("Authorization","Bearer " + initiatorResponse.getToken())
        .body(Mono.just(idea), IdeaDTO.class)
        .exchange().expectBody(IdeaDTO.class)
        .returnResult().getResponseBody();

        assertNotNull(ideaResponse);
        firstIdeaId = ideaResponse.getId();
        jwt = projectOfficeResponse.getToken();
        initiatorEmail = initiator.getEmail();
        initiatorJwt = initiatorResponse.getToken();
        
        //For getAllIdeaComments test
        IdeaDTO secondIdea = new IdeaDTO();
        secondIdea.setBudget(123424525L);
        secondIdea.setName("Биометрические данные");
        secondIdea.setCustomer("f");
        IdeaDTO secondIdeaResponse = webTestClient.post()
        .uri("/api/v1/idea/initiator/add")
        .header("Authorization","Bearer " + initiatorJwt)
        .body(Mono.just(secondIdea), IdeaDTO.class)
        .exchange().expectBody(IdeaDTO.class)
        .returnResult().getResponseBody();
        assertNotNull(secondIdeaResponse);
        secondIdeaId = secondIdeaResponse.getId();
        List<Map<String, String>> senders = List.of(
            Map.of("sender", "first@gmail.su", "comment", "Потрясно"),
            Map.of("sender", "second@gmail.su", "comment", "Изящно"),
            Map.of("sender", "third@gmail.su", "comment", "В современном мире хранить полные данные человека в базе данных не безопасно")
        );

        senders.stream().forEach(sender -> {
            RegisterRequest registerRequest = new RegisterRequest(
                sender.get("sender"),"fakename","fakename","fakepass", List.of(Role.PROJECT_OFFICE));
            AuthenticationResponse authenticationResponse = webTestClient
                    .post()
                    .uri("/api/v1/auth/register")
                    .body(Mono.just(registerRequest), RegisterRequest.class)
                    .exchange()
                    .expectBody(AuthenticationResponse.class)
                    .returnResult().getResponseBody();
            assertNotNull(authenticationResponse);

            CommentDTO comment = CommentDTO.builder()
            .comment(sender.get("comment")).build();

            CommentDTO response = webTestClient.post()
            .uri("/api/v1/comment/add/{secondIdeaId}", secondIdeaId)
            .header("Authorization","Bearer " + authenticationResponse.getToken())
            .body(Mono.just(comment), CommentDTO.class)
            .exchange().expectBody(CommentDTO.class)
            .returnResult().getResponseBody();

            assertNotNull(response);
            assertEquals(authenticationResponse.getEmail(), response.getSender());
            assertEquals(List.of(authenticationResponse.getEmail()), response.getCheckedBy());
            assertEquals(comment.getComment(), response.getComment());
            assertEquals(secondIdeaId, response.getIdeaId());
        });
    }

    private boolean containsEmail(List<String> list, String email){
        return list.stream().filter(listEmail -> listEmail != email).findFirst().isPresent();
    }
    
    @Test
    void createComment(){
        CommentDTO comment = CommentDTO.builder().comment("Доделай").build();
        
        CommentDTO response = webTestClient.post()
        .uri("/api/v1/comment/add/{firstIdeaId}", firstIdeaId)
        .header("Authorization","Bearer " + jwt)
        .body(Mono.just(comment), CommentDTO.class)
        .exchange().expectBody(CommentDTO.class)
        .returnResult().getResponseBody();

        assertNotNull(response);
        assertEquals("projectOffice", response.getSender());
        assertEquals(List.of("projectOffice"), response.getCheckedBy());
        assertEquals(comment.getComment(), response.getComment());
        assertEquals(firstIdeaId, response.getIdeaId());
    }

    @Test
    void createCommentIfIdeaNotExist(){
        CommentDTO comment = CommentDTO.builder().comment("Доделай").build();
        Long id = 1314513L;
        ErrorResponse response = webTestClient.post()
        .uri("/api/v1/comment/add/{id}", id)
        .header("Authorization","Bearer " + jwt)
        .body(Mono.just(comment), CommentDTO.class)
        .exchange().expectBody(ErrorResponse.class)
        .returnResult().getResponseBody();

        assertNotNull(response);
        assertEquals(String.format("Идеи с id %d не существует", id), response.getError());
    }

    int counter = 0;
    @Test
    void getAllIdeaComments(){
        Map<String, List<CommentDTO>> comments = webTestClient.get()
            .uri("/api/v1/comment/get-idea-comments/{secondIdeaId}", secondIdeaId)
            .header("Authorization","Bearer " + jwt)
            .exchange().expectBody(new ParameterizedTypeReference<Map<String, List<CommentDTO>>>() {})
            .returnResult().getResponseBody();
        
        assertNotNull(comments);

        List<String> emails = List.of("first@gmail.su", "second@gmail.su", "third@gmail.su");
        
        comments.get("comments").stream()
        .filter(comment -> containsEmail(emails, comment.getSender()))
        .forEach(comment -> {
            counter++;
        });
        assertEquals(3, counter);
        counter = 0;
    }    

    @Test
    void getAllIdeaCommentsIfIdeaNotExist(){
        ErrorResponse errorResponse = webTestClient.get()
            .uri("/api/v1/comment/get-idea-comments/12413513")
            .header("Authorization","Bearer " + jwt)
            .exchange().expectBody(ErrorResponse.class)
            .returnResult().getResponseBody();
        assertNotNull(errorResponse);
        assertEquals("Идеи с id 12413513 не существует", errorResponse.getError());
    }

    @Test
    void checkComment(){
        CommentDTO commentRequest = CommentDTO.builder().comment("Неплохо").build();
        
        CommentDTO response = webTestClient.post()
        .uri("/api/v1/comment/add/{firstIdeaId}", firstIdeaId)
        .header("Authorization","Bearer " + jwt)
        .body(Mono.just(commentRequest), CommentDTO.class)
        .exchange().expectBody(CommentDTO.class)
        .returnResult().getResponseBody();

        assertNotNull(response);
        assertEquals("projectOffice", response.getSender());
        assertEquals(List.of("projectOffice"), response.getCheckedBy());
        assertEquals(commentRequest.getComment(), response.getComment());
        assertEquals(firstIdeaId, response.getIdeaId());

        webTestClient.put()
        .uri("/api/v1/comment/check/" + response.getId())
        .header("Authorization","Bearer " + initiatorJwt)
        .exchange().expectStatus().isOk().expectBody(Void.class);

        Map<String, List<CommentDTO>> comments = webTestClient.get()
            .uri("/api/v1/comment/get-idea-comments/{firstIdeaId}", firstIdeaId)
            .header("Authorization","Bearer " + jwt)
            .exchange().expectBody(new ParameterizedTypeReference<Map<String, List<CommentDTO>>>() {})
            .returnResult().getResponseBody();
        
        assertNotNull(comments);

        CommentDTO finalValue = comments.get("comments").stream()
        .filter(ideaComment -> ideaComment.getId() == response.getId())
        .findFirst().get();

        assertEquals(finalValue.getComment(), response.getComment());
        assertTrue(containsEmail(finalValue.getCheckedBy(), "projectOffice"));
        assertTrue(containsEmail(finalValue.getCheckedBy(), initiatorEmail));
    }
    @Test
    void deleteYourComment(){
        CommentDTO commentRequest = CommentDTO.builder().comment("Все плохо").build();
        
        CommentDTO commentResponse = webTestClient.post()
        .uri("/api/v1/comment/add/{firstIdeaId}", firstIdeaId)
        .header("Authorization","Bearer " + jwt)
        .body(Mono.just(commentRequest), CommentDTO.class)
        .exchange().expectBody(CommentDTO.class)
        .returnResult().getResponseBody();

        assertNotNull(commentResponse);
        assertEquals("projectOffice", commentResponse.getSender());
        assertEquals(List.of("projectOffice"), commentResponse.getCheckedBy());
        assertEquals(commentRequest.getComment(), commentResponse.getComment());

        Long commentId = commentResponse.getId();
        Map<String,String> response = webTestClient.delete()
                .uri("/api/v1/comment/delete/{commentId}", commentId)
                .header("Authorization","Bearer " + jwt)
                .exchange().expectBody(new ParameterizedTypeReference<Map<String,String>>() {})
                .returnResult().getResponseBody();
        
        assertNotNull(response);
        assertNull(response.get("error"));
        assertNotNull(response.get("success"));
        assertEquals("Успешное удаление комментария", response.get("success"));
    }

    @Test
    void deleteNotYourComment(){
        CommentDTO commentRequest = CommentDTO.builder().comment("Все плохо").build();
        
        CommentDTO commentResponse = webTestClient.post()
        .uri("/api/v1/comment/add/{firstIdeaId}", firstIdeaId)
        .header("Authorization","Bearer " + jwt)
        .body(Mono.just(commentRequest), CommentDTO.class)
        .exchange().expectBody(CommentDTO.class)
        .returnResult().getResponseBody();

        assertNotNull(commentResponse);
        assertEquals("projectOffice", commentResponse.getSender());
        assertEquals(List.of("projectOffice"), commentResponse.getCheckedBy());
        assertEquals(commentRequest.getComment(), commentResponse.getComment());

        Long commentId = commentResponse.getId();
        ErrorResponse response = webTestClient.delete()
                .uri("/api/v1/comment/delete/{commentId}", commentId)
                .header("Authorization","Bearer " + initiatorJwt)
                .exchange().expectBody(ErrorResponse.class)
                .returnResult().getResponseBody();
        
        assertNotNull(response);
        assertNotNull(response.getError());
        assertEquals("Доступ запрещен", response.getError());
    }
}
