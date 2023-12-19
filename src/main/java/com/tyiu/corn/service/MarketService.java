package com.tyiu.corn.service;

import com.tyiu.corn.config.exception.AccessException;
import com.tyiu.corn.model.dto.MarketDTO;
import com.tyiu.corn.model.entities.Market;
import com.tyiu.corn.model.entities.User;
import com.tyiu.corn.model.enums.MarketStatus;
import com.tyiu.corn.model.enums.Role;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;
import static org.springframework.data.relational.core.query.Update.update;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final R2dbcEntityTemplate template;
    private final ModelMapper mapper;

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Yekaterinburg")
    private void checkFinalDate(){
        template.select(query(where("finish_date").is(LocalDate.now())), Market.class)
                .flatMap(m -> {
                    m.setStatus(MarketStatus.DONE);
                    String QUERY = """
                        UPDATE idea SET status = 'CONFIRMED', is_active = false
                                WHERE id IN (
                                    SELECT idea_id FROM idea_market
                                    WHERE market_id = :marketId AND status = 'RECRUITMENT_IS_OPEN'
                                )
                        """;
                    return template.getDatabaseClient()
                            .sql(QUERY)
                            .bind("marketId", m.getId())
                            .map((row, rowMetadata) -> Mono.empty())
                            .all()
                            .then(template.update(m));
                }).subscribe();
    }

    ///////////////////////
    //  _____   ____ ______
    // / ___/  / __//_  __/
    /// (_ /  / _/   / /
    //\___/  /___/  /_/
    ///////////////////////

    public Flux<MarketDTO> getAll(){
        return template.select(Market.class).all()
                .flatMap(m -> Flux.just(mapper.map(m, MarketDTO.class)));
    }

    public Flux<MarketDTO> getActiveMarkets(){
        return template.select(query(where("status").is(MarketStatus.ACTIVE)), Market.class)
                .flatMap(m -> Flux.just(mapper.map(m, MarketDTO.class)));
    }

    public Mono<MarketDTO> getMarket(String marketId){
        return template.selectOne(query(where("id").is(marketId)), Market.class)
                .flatMap(m -> Mono.just(mapper.map(m, MarketDTO.class)));
    }

    //////////////////////////////
    //   ___   ____    ____ ______
    //  / _ \ / __ \  / __//_  __/
    // / ___// /_/ / _\ \   / /
    ///_/    \____/ /___/  /_/
    //////////////////////////////

    public Mono<MarketDTO> createMarket(MarketDTO marketDTO){
        marketDTO.setStatus(MarketStatus.NEW);
        return template.insert(mapper.map(marketDTO, Market.class))
                .flatMap(m -> {
                    marketDTO.setId(m.getId());
                    return Mono.just(marketDTO);
        });
    }

    ///////////////////////////////////////////
    //   ___    ____   __    ____ ______   ____
    //  / _ \  / __/  / /   / __//_  __/  / __/
    // / // / / _/   / /__ / _/   / /    / _/
    ///____/ /___/  /____//___/  /_/    /___/
    ///////////////////////////////////////////

    public Mono<Void> deleteMarket(String id){
        return template.delete(query(where("id").is(id)),Market.class).then();
    }

    ////////////////////////
    //   ___   __  __ ______
    //  / _ \ / / / //_  __/
    // / ___// /_/ /  / /
    ///_/    \____/  /_/
    ////////////////////////

    public Mono<MarketDTO> updateMarket(String marketId, MarketDTO marketDTO){
        return template.selectOne(query(where("id").is(marketId)), Market.class)
                .flatMap(m -> {
                    m.setName(marketDTO.getName());
                    m.setStartDate(marketDTO.getStartDate());
                    m.setFinishDate(marketDTO.getFinishDate());
                    return template.update(m).thenReturn(mapper.map(m, MarketDTO.class));
                });
    }

    public Mono<MarketDTO> updateStatus(String id, MarketStatus status, User user){
        return template.selectOne(query(where("id").is(id)), Market.class)
                .flatMap(m -> {
                    m.setStatus(status);
                    if (status == MarketStatus.ACTIVE){
                        return template.update(m)
                                .thenReturn(mapper.map(m, MarketDTO.class));
                    }
                    else if (status == MarketStatus.DONE) {
                        String QUERY = """
                                UPDATE idea SET status = 'CONFIRMED', is_active = false
                                        WHERE id IN (
                                            SELECT idea_id FROM idea_market
                                            WHERE market_id = :marketId AND status = 'RECRUITMENT_IS_OPEN'
                                        )
                                """;
                        return template.getDatabaseClient()
                                .sql(QUERY)
                                .bind("marketId", id)
                                .map((row, rowMetadata) -> Mono.empty())
                                .all()
                                .then(template.update(m))
                                .thenReturn(mapper.map(m, MarketDTO.class));
                    }
                    else if (status == MarketStatus.NEW && user.getRoles().contains(Role.ADMIN)) {
                        return template.update(m).thenReturn(mapper.map(m, MarketDTO.class));
                    }
                    return Mono.error(new AccessException("Нет Прав"));
                });
    }
}
