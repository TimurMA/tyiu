package com.tyiu.corn.model.entities.mappers;

import com.tyiu.corn.model.dto.UserDTO;
import io.r2dbc.spi.Row;
import org.springframework.stereotype.Component;

import java.util.function.BiFunction;
@Component
public class UserMapper implements BiFunction<Row, Object, UserDTO> {

    @Override
    public UserDTO apply(Row row, Object o) {
        return UserDTO.builder()
                .id(row.get("member_id", Long.class))
                .email(row.get("email", String.class))
                .firstName(row.get("first_name", String.class))
                .lastName(row.get("last_name", String.class))
                .build();
    }
}