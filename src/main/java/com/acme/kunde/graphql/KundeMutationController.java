/*
 * Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.acme.kunde.graphql;

import com.acme.kunde.entity.Kunde;
import com.acme.kunde.security.UsernameExistsException;
import com.acme.kunde.service.ConstraintViolationsException;
import com.acme.kunde.service.EmailExistsException;
import com.acme.kunde.service.KundeWriteService;
import graphql.GraphQLError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.graphql.execution.ErrorType.BAD_REQUEST;

/**
 * Eine Controller-Klasse für das Schreiben mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequiredArgsConstructor
@Slf4j
class KundeMutationController {
    private final KundeWriteService service;

    /**
     * Einen neuen Kunden anlegen.
     *
     * @param input Die Eingabedaten für einen neuen Kunden
     * @return Die generierte ID für den neuen Kunden als Payload
     */
    @MutationMapping
    CreatePayload create(@Argument final KundeInput input) {
        log.debug("create: input={}", input);
        final var id = service
            .create(input.toKunde(), input.toUserDetails())
            .getId();
        log.debug("create: id={}", id);
        return new CreatePayload(id);
    }

    @GraphQlExceptionHandler
    GraphQLError onEmailExists(final EmailExistsException ex) {
        final List<Object> path = List.of("input", "email");
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message("Die Emailadresse " + ex.getEmail() + " existiert bereits.")
            .path(path)
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onUsernameExists(final UsernameExistsException ex) {
        final List<Object> path = List.of("input", "username");
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message("Der Username " + ex.getUsername() + " existiert bereits.")
            .path(path)
            .build();
    }

    @GraphQlExceptionHandler
    GraphQLError onDateTimeParseException(final DateTimeParseException ex) {
        final List<Object> path = List.of("input", "geburtsdatum");
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message("Das Datum " + ex.getParsedString() + " ist nicht korrekt.")
            .path(path)
            .build();
    }

    @GraphQlExceptionHandler
    Collection<GraphQLError> onConstraintViolations(final ConstraintViolationsException ex) {
        return ex.getViolations()
            .stream()
            .map(this::violationToGraphQLError)
            .collect(Collectors.toList());
    }

    private GraphQLError violationToGraphQLError(final ConstraintViolation<Kunde> violation) {
        final List<Object> path = new ArrayList<>(5);
        path.add("input");
        for (final Path.Node node: violation.getPropertyPath()) {
            path.add(node.toString());
        }
        return GraphQLError.newError()
            .errorType(BAD_REQUEST)
            .message(violation.getMessage())
            .path(path)
            .build();
    }
}
