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

import java.util.Collection;
import java.util.Map;
import java.util.stream.IntStream;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import static com.acme.kunde.entity.Adresse.PLZ_PATTERN;
import static com.acme.kunde.entity.Kunde.NACHNAME_PATTERN;
import static com.acme.kunde.rest.KundeGetController.ID_PATTERN;
import static com.acme.kunde.rest.KundeGetRestTest.CLIENT_CONNECTOR;
import static com.acme.kunde.rest.KundeGetRestTest.HOST;
import static com.acme.kunde.rest.KundeGetRestTest.SCHEMA;
import static com.acme.kunde.rest.KundeGetRestTest.ADMIN_BASIC_AUTH;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Tag("integration")
@Tag("graphql")
@Tag("query")
@DisplayName("GraphQL-Schnittstelle fuer Lesen")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_19, max = JAVA_21)
@SuppressWarnings({"WriteTag", "MissingJavadoc", "RedundantSuppression"})
class KundeQueryTest {
    static final String GRAPHQL_PATH = "/graphql";

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ID_NICHT_VORHANDEN = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final String NACHNAME_VORHANDEN = "Alpha";
    private static final String NACHNAME_NICHT_VORHANDEN = "Nachname-Nichtvorhanden";
    private static final String EMAIL_VORHANDEN = "admin@acme.com";
    private static final String EMAIL_NICHT_VORHANDEN = "nicht.vorhanden@acme.com";

    private final HttpGraphQlClient client;

    @InjectSoftAssertions
    private SoftAssertions softly;

    KundeQueryTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var getController = ctx.getBean(KundeQueryController.class);
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
    @DisplayName("Suche nach allen Kunden")
    void findAll() {
        // given
        final var query = """
            {
                kunden {
                    id
                    nachname
                    email
                }
            }""";

        // when
        final var response = client
            .mutate()
            .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
            .build()
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        softly.assertThat(response.isValid()).isTrue();
        softly.assertThat(response.getErrors()).isEmpty();

        final Collection<Map<String, String>> kunden = response.field("kunden").getValue();
        assertThat(kunden).isNotEmpty();
        IntStream.range(0, kunden.size())
            .forEach(i -> {
                final var id = response.field("kunden[%d].id".formatted(i)).toEntity(String.class);
                softly.assertThat(id).matches(ID_PATTERN);
                final var nachname = response.field("kunden[%d].nachname".formatted(i)).toEntity(String.class);
                softly.assertThat(nachname).matches(NACHNAME_PATTERN);
                final var email = response.field("kunden[%d].email".formatted(i)).toEntity(String.class);
                softly.assertThat(email).contains("@");
            });
    }

    @ParameterizedTest
    @ValueSource(strings = NACHNAME_VORHANDEN)
    @DisplayName("Suche mit vorhandenem Nachnamen")
    void findByNachname(final CharSequence nachname) {
        // given
        final var query = """
            {
                kunden(input: {nachname: "$nachname"}) {
                    id
                    email
                }
            }""".replace("$nachname", nachname);

        // when
        final var response = client
            .mutate()
            .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
            .build()
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrors()).isEmpty();

        final Collection<Map<String, String>> kunden = response.field("kunden").getValue();
        assertThat(kunden).isNotEmpty();
        IntStream.range(0, kunden.size())
            .forEach(i -> {
                final var id = response.field("kunden[%d].id".formatted(i)).toEntity(String.class);
                softly.assertThat(id).matches(ID_PATTERN);
                final var email = response.field("kunden[%d].email".formatted(i)).toEntity(String.class);
                softly.assertThat(email).contains("@");
            });
    }

    @ParameterizedTest
    @ValueSource(strings = NACHNAME_NICHT_VORHANDEN)
    @DisplayName("Suche mit nicht-vorhandenem Nachnamen")
    void findByNachnameNichtVorhanden(final CharSequence nachname) {
        // given
        final var query = """
            {
                kunden(input: {nachname: "$nachname"}) {
                    id
                }
            }""".replace("$nachname", nachname);

        // when
        final var response = client
            .mutate()
            .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
            .build()
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

        final var errors = response.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
            .extracting(ResponseError::getErrorType)
            .isEqualTo(ErrorType.NOT_FOUND);
    }

    @ParameterizedTest
    @ValueSource(strings = EMAIL_VORHANDEN)
    @DisplayName("Suche mit vorhandener Email")
    void findByEmail(final CharSequence email) {
        // given
        final var query = """
            {
                kunden(input: {email: "$email"}) {
                    id
                    nachname
                }
            }""".replace("$email", email);

        // when
        final var response = client
            .mutate()
            .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
            .build()
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat(response.getErrors()).isEmpty();

        final Collection<Map<String, String>> kunden = response.field("kunden").getValue();
        assertThat(kunden).isNotEmpty();
        IntStream.range(0, kunden.size())
            .forEach(i -> {
                final var id = response.field("kunden[%d].id".formatted(i)).toEntity(String.class);
                softly.assertThat(id).matches(ID_PATTERN);
                final var nachname = response.field("kunden[%d].nachname".formatted(i)).toEntity(String.class);
                softly.assertThat(nachname).matches(NACHNAME_PATTERN);
            });
    }

    @ParameterizedTest
    @ValueSource(strings = EMAIL_NICHT_VORHANDEN)
    @DisplayName("Suche mit nicht-vorhandener Email")
    void findByEmailNichtVorhanden(final CharSequence email) {
        // given
        final var query = """
            {
                kunden(input: {email: "$email"}) {
                    id
                }
            }""".replace("$email", email);

        // when
        final var response = client
            .mutate()
            .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
            .build()
            .document(query)
            .execute()
            .block();

        // then
        assertThat(response).isNotNull();
        assertThat(response.isValid()).isTrue();
        assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

        final var errors = response.getErrors();
        assertThat(errors).hasSize(1);
        assertThat(errors.get(0))
            .extracting(ResponseError::getErrorType)
            .isEqualTo(ErrorType.NOT_FOUND);
    }

    @Nested
    @DisplayName("GraphQL-Schnittstelle fuer die Suche anhand der ID")
    class FindById {
        @ParameterizedTest
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit vorhandener ID")
        void findById(final CharSequence id) {
            // given
            final var query = """
                {
                    kunde(id: "$id") {
                        nachname
                        email
                        adresse {
                            plz
                        }
                    }
                }""".replace("$id", id);

            // when
            final var response = client
                .mutate()
                .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
                .build()
                .document(query)
                .execute()
                .block();

            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.isValid()).isTrue();
            softly.assertThat(response.getErrors()).isEmpty();

            final var nachname = response.field("kunde.nachname").toEntity(String.class);
            softly.assertThat(nachname)
                .isNotEmpty()
                .matches(NACHNAME_PATTERN);

            final var email = response.field("kunde.email").toEntity(String.class);
            softly.assertThat(email)
                .isNotEmpty()
                .contains("@");

            final var plz = response.field("kunde.adresse.plz").toEntity(String.class);
            softly.assertThat(plz)
                .isNotEmpty()
                .matches(PLZ_PATTERN);
        }

        @ParameterizedTest
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit nicht-vorhandener ID")
        void findByIdNichtVorhanden(final CharSequence id) {
            // given
            final var query = """
                {
                    kunde(id: "$id") {
                        nachname
                    }
                }""".replace("$id", id);

            // when
            final var response = client
                .mutate()
                .header(AUTHORIZATION, ADMIN_BASIC_AUTH)
                .build()
                .document(query)
                .execute()
                .block();

            // then
            assertThat(response).isNotNull();
            softly.assertThat(response.isValid()).isTrue();
            assertThat((Map<?, ?>) response.getData()).isNotNull().isEmpty();

            final var errors = response.getErrors();
            assertThat(errors).hasSize(1);
            assertThat(errors.get(0))
                .extracting(ResponseError::getErrorType)
                .isEqualTo(ErrorType.NOT_FOUND);
        }
    }
}
