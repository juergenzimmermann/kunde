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
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package com.acme.kunde.graphql;

import java.util.List;
import java.util.Map;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.graphql.ResponseError;
import org.springframework.graphql.client.HttpGraphQlClient;
import org.springframework.graphql.execution.ErrorType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import static com.acme.kunde.dev.DevConfig.DEV;
import static com.acme.kunde.graphql.KundeQueryTest.GRAPHQL_PATH;
import static com.acme.kunde.rest.KundeGetController.ID_PATTERN;
import static com.acme.kunde.rest.KundeGetRestTest.CLIENT_CONNECTOR;
import static com.acme.kunde.rest.KundeGetRestTest.HOST;
import static com.acme.kunde.rest.KundeGetRestTest.SCHEMA;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@Tag("integration")
@Tag("graphql")
@Tag("mutation")
@DisplayName("GraphQL-Schnittstelle fuer Schreiben")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_19, max = JAVA_21)
@SuppressWarnings("WriteTag")
class KundeMutationTest {
    private final HttpGraphQlClient client;

    @InjectSoftAssertions
    private SoftAssertions softly;

    KundeMutationTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var getController = ctx.getBean(KundeMutationController.class);
        assertThat(getController).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(GRAPHQL_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();
        final var webClient = WebClient
            .builder()
            .baseUrl(baseUrl)
            .clientConnector(CLIENT_CONNECTOR)
            .build();
        client = HttpGraphQlClient.builder(webClient)
            .build();
    }

    @Test
    @DisplayName("Neuanlegen eines neuen Kunden")
    void create() {
        // given
        final var mutation = """
            mutation {
                create(
                    input: {
                        nachname: "Neuernachname-Graphql"
                        email: "neu.email.graphql@test.de"
                        kategorie: 1
                        hasNewsletter: true
                        geburtsdatum: "2022-01-31"
                        homepage: "https://test.de"
                        geschlecht: WEIBLICH
                        familienstand: LEDIG
                        adresse: {
                            plz: "12345"
                            ort: "Neuerortgraphql"
                        }
                        umsaetze: [{
                            betrag: "1"
                            waehrung: "EUR"
                        }]
                        interessen: [LESEN, REISEN]
                        username: "neugraphql",
                        password: "Pass123."
                    }
                ) {
                    id
                }
            }""";

        // when
        final var response = client
            .document(mutation)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        softly.assertThat(response.isValid()).isTrue();
        softly.assertThat(response.getErrors()).isEmpty();

        final var id = response.field("create.id").toEntity(String.class);
        assertThat(id).matches(ID_PATTERN);
    }

    @Test
    @DisplayName("Neuanlegen mit ungueltigen Werten")
    void createInvalid() {
        // given
        final var paths = List.of(
            "input.nachname",
            "input.email",
            "input.kategorie",
            "input.adresse.plz",
            "input.interessen"
        );

        final var mutation = """
            mutation {
                create(
                    input: {
                        nachname: "?!$"
                        email: "email@"
                        kategorie: 11
                        hasNewsletter: true
                        geburtsdatum: "2022-01-31"
                        homepage: "https://test.de"
                        geschlecht: WEIBLICH
                        familienstand: LEDIG
                        adresse: {
                            plz: "1"
                            ort: "Neuerortgraphql"
                        }
                        umsaetze: [{
                            betrag: "1"
                            waehrung: "EUR"
                        }]
                        interessen: [SPORT, SPORT]
                        username: "test"
                        password: "Pass123."
                    }
                ) {
                    id
                }
            }""";

        // when
        final var response = client
            .document(mutation)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat((Map<?, ?>) response.getData())
            .isNotNull()
            .isEmpty();

        final var errors = response.getErrors();
        assertThat(errors).isNotEmpty().hasSize(paths.size());
        errors.stream()
            .map(ResponseError::getErrorType)
            .forEach(errorType -> softly.assertThat(errorType).isEqualTo(ErrorType.BAD_REQUEST));

        final var pathsActual = errors.stream()
            .map(ResponseError::getPath)
            .toList();
        assertThat(pathsActual)
            .hasSameSizeAs(paths)
            .hasSameElementsAs(paths);
    }
}
