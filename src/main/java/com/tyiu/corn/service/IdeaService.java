package com.tyiu.corn.service;

import com.tyiu.corn.config.exception.ErrorException;
import com.tyiu.corn.model.dto.IdeaDTO;
import com.tyiu.corn.model.entities.Group;
import com.tyiu.corn.model.entities.Idea;
import com.tyiu.corn.model.entities.Profile;
import com.tyiu.corn.model.entities.Rating;
import com.tyiu.corn.model.entities.mappers.IdeaMapper;
import com.tyiu.corn.model.enums.StatusIdea;
import com.tyiu.corn.model.requests.StatusIdeaRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.stereotype.Service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@CacheConfig(cacheNames = "ideas")
public class IdeaService {

    private final ReactiveMongoTemplate template;
    private final R2dbcEntityTemplate template2;
    private final ModelMapper mapper;

    @Cacheable
    public Mono<IdeaDTO> getIdea(Long ideaId) {
        String query = "SELECT idea.*, e.name e_name, e.id e_id, p.name p_name, p.id p_id" +
                " FROM idea LEFT JOIN groups e ON idea.group_expert_id = e.id" +
                " LEFT JOIN groups p ON idea.group_project_office_id = p.id" +
                " WHERE idea.id =: ideaId";
        IdeaMapper ideaMapper = new IdeaMapper();
        return template2.getDatabaseClient()
                .sql(query)
                .bind("ideaId", ideaId)
                .map(ideaMapper::apply)
                .first()
                .onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }

    @Cacheable
    public Flux<IdeaDTO> getListIdea() {
        return template2.select(Idea.class).all()
                .flatMap(i -> Flux.just(mapper.map(i, IdeaDTO.class)))
                .onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }

    @CacheEvict(allEntries = true)
    public Mono<IdeaDTO> saveIdea(IdeaDTO ideaDTO, String initiator) {
        Idea idea = mapper.map(ideaDTO, Idea.class);
        idea.setInitiator(initiator);
        idea.setStatus(StatusIdea.NEW);
        idea.setCreatedAt(Instant.now());
        idea.setGroupExpertId(ideaDTO.getExperts().getId());
        idea.setGroupProjectOfficeId(ideaDTO.getProjectOffice().getId());
        return template.save(idea).flatMap(savedIdea ->
                {
                    IdeaDTO savedDTO = mapper.map(savedIdea, IdeaDTO.class);
                    return template.findById(savedIdea.getGroupExpertId(), Group.class).flatMap(g -> {
                        g.getUsersId()
                                .forEach(r ->
                                        template.save(Rating.builder()
                                                .expert(r)
                                                .ideaId(savedIdea.getId())
                                                .confirmed(false)
                                                .build()
                                        ).subscribe()
                                );
                        savedDTO.setExperts(g);
                        return Mono.empty();
                    }).then(template.findById(savedIdea.getGroupProjectOfficeId(), Group.class).flatMap(p -> {
                                savedDTO.setProjectOffice(p);
                                return Mono.empty();
                    })).then(template.findOne(Query.query(Criteria.where("userEmail").is(initiator)), Profile.class)
                                    .flatMap(p -> {
                                        if (!p.getUserIdeasId().isEmpty()) {
                                            p.getUserIdeasId().add(savedIdea.getId());
                                        }
                                        else {
                                            p.setUserIdeasId(List.of(savedIdea.getId()));
                                        }
                                        return template.save(p).then();
                                    }))
                            .then(Mono.just(savedDTO));
                }).onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }
    @CacheEvict(allEntries = true)
    public Mono<Void> deleteIdea(String id) {
        return template.remove(Query.query(Criteria.where("id").is(id)), Idea.class).then()
                .onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }
    @CacheEvict(allEntries = true)
    public Mono<Void> updateStatusByInitiator (String id, String initiator){
        return template.findById(id, Idea.class).flatMap(i -> {
            if (initiator.equals(i.getInitiator())) {
                i.setStatus(StatusIdea.ON_APPROVAL);
                return template.save(i).then();
            }
            return Mono.empty();
        }).onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }
    @CacheEvict(allEntries = true)
    public Mono<Void> updateIdeaByInitiator(String id, IdeaDTO updatedIdea) {
        return template.findById(id, Idea.class).flatMap(i -> {
            i.setName(updatedIdea.getName());
            i.setProjectType(updatedIdea.getProjectType());
            i.setProblem(updatedIdea.getProblem());
            i.setSolution(updatedIdea.getSolution());
            i.setResult(updatedIdea.getResult());
            i.setCustomer(updatedIdea.getCustomer());
            i.setContactPerson(updatedIdea.getContactPerson());
            i.setDescription(updatedIdea.getDescription());
            i.setTechnicalRealizability(updatedIdea.getTechnicalRealizability());
            i.setSuitability(updatedIdea.getSuitability());
            i.setBudget(updatedIdea.getBudget());
            i.setPreAssessment(updatedIdea.getPreAssessment());
            i.setModifiedAt(Instant.now());
            return template.save(i).then();
        }).onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }
    @CacheEvict(allEntries = true)
    public Mono<Void> updateStatusByProjectOffice(String id, StatusIdeaRequest newStatus){
        return template.findById(id, Idea.class).flatMap(i -> {
            i.setStatus(newStatus.getStatus());
            return template.save(i).then();
        }).onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }

    @CacheEvict(allEntries = true)
    public Mono<Void> updateIdeaByAdmin(String id, IdeaDTO updatedIdea) {
        return template.findById(id, Idea.class).flatMap(i -> {
            i.setName(updatedIdea.getName());
            i.setProjectType(updatedIdea.getProjectType());
            i.setGroupExpertId(updatedIdea.getExperts().getId());
            i.setProblem(updatedIdea.getProblem());
            i.setSolution(updatedIdea.getSolution());
            i.setResult(updatedIdea.getResult());
            i.setCustomer(updatedIdea.getCustomer());
            i.setDescription(updatedIdea.getDescription());
            i.setSuitability(updatedIdea.getSuitability());
            i.setBudget(updatedIdea.getBudget());
            i.setTechnicalRealizability(updatedIdea.getTechnicalRealizability());
            i.setStatus(updatedIdea.getStatus());
            i.setRating(updatedIdea.getRating());
            return template.save(i).then();
        }).onErrorResume(ex -> Mono.error(new ErrorException("Not success!")));
    }
}