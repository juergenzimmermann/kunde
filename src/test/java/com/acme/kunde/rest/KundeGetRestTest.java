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
package com.acme.kunde.rest;

import com.acme.kunde.entity.FamilienstandType;
import com.acme.kunde.entity.GeschlechtType;
import com.acme.kunde.entity.InteresseType;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import javax.net.ssl.TrustManagerFactory;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.netty.http.client.HttpClient;
import static com.acme.kunde.dev.DevConfig.DEV;
import static com.acme.kunde.rest.KundeGetController.REST_PATH;
import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.file.StandardOpenOption.READ;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowableOfType;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NOT_MODIFIED;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@Tag("integration")
@Tag("rest")
@Tag("rest-get")
@DisplayName("REST-Schnittstelle fuer GET-Requests")
@ExtendWith(SoftAssertionsExtension.class)
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles(DEV)
@EnabledForJreRange(min = JAVA_19, max = JAVA_21)
@SuppressWarnings({"WriteTag", "ClassFanOutComplexity", "MissingJavadoc", "MissingJavadocType", "JavadocVariable"})
public class KundeGetRestTest {
    public static final ReactorClientHttpConnector CLIENT_CONNECTOR = getReactorClientHttpConnector();
    public static final String SCHEMA = "https";
    public static final String HOST = "localhost";
    public static final String ADMIN_BASIC_AUTH;
    static final String USER_ADMIN = "admin";
    private static final String PASSWORD = "p";
    private static final String USER_KUNDE = "alpha";
    private static final String KUNDE_BASIC_AUTH;
    private static final String PASSWORD_FALSCH = "Falsches Passwort!";
    private static final String PASSWORD_FALSCH_BASIC_AUTH;

    private static final String ID_VORHANDEN = "00000000-0000-0000-0000-000000000001";
    private static final String ID_VORHANDEN_KUNDE = "00000000-0000-0000-0000-000000000001";
    private static final String ID_VORHANDEN_ANDERER_KUNDE = "00000000-0000-0000-0000-000000000020";
    private static final String ID_NICHT_VORHANDEN = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    private static final String NACHNAME_TEIL = "a";
    private static final String EMAIL_VORHANDEN = "alpha@acme.de";
    private static final String PLZ_VORHANDEN = "1";
    private static final String WEIBLICH = "W";
    private static final String VERHEIRATET = "VH";
    private static final String LESEN = "L";
    private static final String REISEN = "R";
    private static final String NACHNAME_PREFIX_A = "A";
    private static final String NACHNAME_PREFIX_D = "D";

    private static final String NACHNAME_PARAM = "nachname";
    private static final String EMAIL_PARAM = "email";
    private static final String PLZ_PARAM = "plz";
    private static final String GESCHLECHT_PARAM = "geschlecht";
    private static final String FAMILIENSTAND_PARAM = "familienstand";
    private static final String INTERESSE_PARAM = "interesse";

    private final KundeRepository kundeRepo;

    @InjectSoftAssertions
    private SoftAssertions softly;

    static {
        final String credentialsAdmin = USER_ADMIN + ":" + PASSWORD;
        ADMIN_BASIC_AUTH = "Basic " +
            new String(Base64.getEncoder().encode(credentialsAdmin.getBytes(ISO_8859_1)), ISO_8859_1);
        final String credentialsKunde = USER_KUNDE + ":" + PASSWORD;
        KUNDE_BASIC_AUTH = "Basic " +
            new String(Base64.getEncoder().encode(credentialsKunde.getBytes(ISO_8859_1)), ISO_8859_1);
        final String credentialsFalsch = USER_ADMIN + ":" + PASSWORD_FALSCH;
        PASSWORD_FALSCH_BASIC_AUTH = "Basic " +
            new String(Base64.getEncoder().encode(credentialsFalsch.getBytes(ISO_8859_1)), ISO_8859_1);
    }

    KundeGetRestTest(@LocalServerPort final int port, final ApplicationContext ctx) {
        final var getController = ctx.getBean(KundeGetController.class);
        assertThat(getController).isNotNull();

        final var uriComponents = UriComponentsBuilder.newInstance()
            .scheme(SCHEMA)
            .host(HOST)
            .port(port)
            .path(REST_PATH)
            .build();
        final var baseUrl = uriComponents.toUriString();

        // // https://docs.spring.io/spring-boot/docs/3.1.0-RC1/reference/htmlsingle/#io.rest-client.webclient.ssl
        final var client = WebClient
            .builder()
            .baseUrl(baseUrl)
            .clientConnector(CLIENT_CONNECTOR)
            .build();
        final var clientAdapter = WebClientAdapter.forClient(client);
        @SuppressWarnings("MagicNumber")
        final var proxyFactory = HttpServiceProxyFactory
            .builder(clientAdapter)
            .blockTimeout(Duration.ofSeconds(10))
            .build();
        kundeRepo = proxyFactory.createClient(KundeRepository.class);
    }

    // https://stackoverflow.com/questions/66759331/...
    //                ...spring-boot-webclient-accept-self-signed-certificate-but-not-with-insecuretrust#answer-66759884
    @SuppressWarnings({"LocalCanBeFinal", "LocalVariableNamingConvention"})
    private static ReactorClientHttpConnector getReactorClientHttpConnector() {
        final var truststorePath = Path.of("src", "main", "resources", "truststore.p12");

        final SslContext sslContext;
        try (var truststoreInputStream = Files.newInputStream(truststorePath, READ)) {
            final var truststore = KeyStore.getInstance(KeyStore.getDefaultType());
            truststore.load(truststoreInputStream, "zimmermann".toCharArray());
            final var trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(truststore);
            sslContext = SslContextBuilder
                .forClient()
                // alternativ: io.netty.handler.ssl.util.InsecureTrustManagerFactory.INSTANCE
                .trustManager(trustManagerFactory)
                .build();
        } catch (final IOException | KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            throw new IllegalStateException(e);
        }

        final var httpClient = HttpClient
            .create()
            .secure(sslContextSpec -> sslContextSpec.sslContext(sslContext));
        return new ReactorClientHttpConnector(httpClient);
    }

    @Test
    @DisplayName("Immer erfolgreich")
    void immerErfolgreich() {
        assertThat(true).isTrue();
    }

    @Test
    @DisplayName("Noch nicht fertig")
    @Disabled
    void nochNichtFertig() {
        //noinspection DataFlowIssue
        assertThat(false).isTrue();
    }

    @Test
    @DisplayName("Suche nach allen Kunden")
    void getAll() {
        // given
        final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();

        // when
        final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

        // then
        softly.assertThat(kunden).isNotNull();
        final var embedded = kunden._embedded();
        softly.assertThat(embedded).isNotNull();
        softly.assertThat(embedded.kunden())
            .isNotNull()
            .isNotEmpty();
    }

    @ParameterizedTest(name = "[{index}] Suche mit vorhandenem (Teil-) Nachnamen: teil={0}")
    @ValueSource(strings = NACHNAME_TEIL)
    @DisplayName("Suche mit vorhandenem (Teil-) Nachnamen")
    void getByNachnameTeil(final String teil) {
        // given
        final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
        suchkriterien.add(NACHNAME_PARAM, teil);

        // when
        final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

        // then
        assertThat(kunden).isNotNull();
        final var embedded = kunden._embedded();
        assertThat(embedded).isNotNull();
        final var kundenList = embedded.kunden();
        assertThat(kundenList)
            .isNotNull()
            .isNotEmpty();
        kundenList
            .stream()
            .map(KundeDownload::nachname)
            .forEach(nachname -> softly.assertThat(nachname).containsIgnoringCase(teil));
    }

    @ParameterizedTest(name = "[{index}] Suche mit vorhandener Email: email={0}")
    @ValueSource(strings = EMAIL_VORHANDEN)
    @DisplayName("Suche mit vorhandener Email")
    void getByEmail(final String email) {
        // given
        final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
        suchkriterien.add(EMAIL_PARAM, email);

        // when
        final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

        // then
        assertThat(kunden).isNotNull();
        final var embedded = kunden._embedded();
        assertThat(embedded).isNotNull();
        final var kundenList = embedded.kunden();
        assertThat(kundenList)
            .isNotNull()
            .hasSize(1);
        assertThat(kundenList.get(0))
            .extracting(KundeDownload::email)
            .isEqualTo(email);
    }

    @ParameterizedTest(name = "[{index}] Suche mit vorhandenem Nachnamen und PLZ: nachname={0}, plz={1}")
    @CsvSource(NACHNAME_TEIL + ',' + PLZ_VORHANDEN)
    @DisplayName("Suche mit vorhandenem Nachnamen und PLZ")
    void getByNachnamePLZ(final String nachname, final String plz) {
        // given
        final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
        suchkriterien.add(NACHNAME_PARAM, nachname);
        suchkriterien.add(PLZ_PARAM, plz);

        // when
        final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

        // then
        assertThat(kunden).isNotNull();
        assertThat(kunden._embedded()).isNotNull();
        final var kundenList = kunden._embedded().kunden();
        assertThat(kundenList)
            .isNotNull()
            .isNotEmpty();
        kundenList
            .forEach(kunde -> {
                softly.assertThat(kunde.nachname()).containsIgnoringCase(nachname);
                softly.assertThat(kunde.adresse().plz()).startsWith(plz);
            });
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer die Suche anhand der ID")
    class GetById {
        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID: id={0}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit vorhandener ID")
        void getById(final String id) {
            // when
            final var response = kundeRepo.getByIdOhneVersion(id, ADMIN_BASIC_AUTH);

            // then
            final var kunde = response.getBody();
            assertThat(kunde).isNotNull();
            softly.assertThat(kunde.nachname()).isNotNull();
            softly.assertThat(kunde.email()).isNotNull();
            softly.assertThat(kunde.adresse().plz()).isNotNull();
            softly.assertThat(kunde._links().self().href()).endsWith("/" + id);
        }

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID und vorhandener Version: id={0}, version={1}")
        @CsvSource(ID_VORHANDEN + ", 0")
        @DisplayName("Suche mit vorhandener ID und vorhandener Version")
        void getByIdVersionVorhanden(final String id, final String version) {
            // when
            final var response = kundeRepo.getById(id, "\"" + version + '"', ADMIN_BASIC_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NOT_MODIFIED);
        }

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID und Version als kunde: id={0}, version={1}")
        @CsvSource(ID_VORHANDEN_KUNDE + ", 0")
        @DisplayName("Suche mit vorhandener ID und Version als kunde")
        void getByIdVersionRolleKunde(final String id, final String version) {
            // when
            final var response = kundeRepo.getById(id, "\"" + version + '"', KUNDE_BASIC_AUTH);

            // then
            assertThat(response.getStatusCode()).isEqualTo(NOT_MODIFIED);
        }

        @ParameterizedTest(name = "[{index}] Suche mit vorhandener ID und Version unberechtigt: id={0}, version={1}")
        @CsvSource(ID_VORHANDEN_ANDERER_KUNDE + ", 0")
        @DisplayName("Suche mit vorhandener ID und Version unberechtigt")
        void getByIdAndererKunde(final String id, final String version) {
            // when
            final var exc = catchThrowableOfType(
                () -> kundeRepo.getById(id, "\"" + version + '"', KUNDE_BASIC_AUTH),
                WebClientResponseException.Forbidden.class
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(FORBIDDEN);
        }

        @ParameterizedTest(name = "[{index}] Suche mit nicht-vorhandener ID: {0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit nicht-vorhandener ID")
        void getByIdNichtVorhanden(final String id) {
            // when
            final var exc = catchThrowableOfType(
                () -> kundeRepo.getByIdOhneVersion(id, ADMIN_BASIC_AUTH),
                WebClientResponseException.NotFound.class
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(NOT_FOUND);
        }

        @ParameterizedTest(name = "[{index}] Suche mit nicht-vorhandener ID als Kunde: {0}")
        @ValueSource(strings = ID_NICHT_VORHANDEN)
        @DisplayName("Suche mit nicht-vorhandener ID als Kunde")
        void getByIdNichtVorhandenKunde(final String id) {
            // when
            final var exc = catchThrowableOfType(
                () -> kundeRepo.getByIdOhneVersion(id, KUNDE_BASIC_AUTH),
                WebClientResponseException.Forbidden.class
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(FORBIDDEN);
        }

        @ParameterizedTest(name = "[{index}] Suche mit ID, aber falschem Passwort: username={0}, password={1}, id={2}")
        @ValueSource(strings = ID_VORHANDEN)
        @DisplayName("Suche mit ID, aber falschem Passwort")
        void getByIdFalschesPasswort(final String id) {
            // when
            final var exc = catchThrowableOfType(
                () -> kundeRepo.getByIdOhneVersion(id, PASSWORD_FALSCH_BASIC_AUTH),
                WebClientResponseException.Unauthorized.class
            );

            // then
            assertThat(exc.getStatusCode()).isEqualTo(UNAUTHORIZED);
        }
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer die Suche mit Enums")
    class GetMitEnums {
        @ParameterizedTest(name = "[{index}] Suche mit Teil-Nachname und Geschlecht: nachname={0}, geschlecht={1}")
        @CsvSource(NACHNAME_TEIL + ',' + WEIBLICH)
        @DisplayName("Suche mit Teil-Nachname und Geschlecht")
        void getByNachnameGeschlecht(final String nachname, final String geschlecht) {
            // given
            final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
            suchkriterien.add(NACHNAME_PARAM, nachname);
            suchkriterien.add(GESCHLECHT_PARAM, geschlecht);

            // when
            final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

            // then
            assertThat(kunden).isNotNull();
            final var embedded = kunden._embedded();
            assertThat(embedded).isNotNull();
            final var kundenList = embedded.kunden();
            assertThat(kundenList)
                .isNotNull()
                .isNotEmpty();
            kundenList.forEach(kunde -> {
                softly.assertThat(kunde.nachname()).containsIgnoringCase(nachname);
                softly.assertThat(kunde.geschlecht()).isEqualTo(GeschlechtType.of(geschlecht).orElse(null));
            });
        }

        @ParameterizedTest(name = "[{index}] Suche mit Teil-Nachname und Familienstand: nachname={0},familienstand={1}")
        @CsvSource(NACHNAME_TEIL + ',' + VERHEIRATET)
        @DisplayName("Suche mit Teil-Nachname und Familienstand")
        void getByNachnameFamilienstand(final String nachname, final String familienstand) {
            // given
            final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
            suchkriterien.add(NACHNAME_PARAM, nachname);
            suchkriterien.add(FAMILIENSTAND_PARAM, familienstand);

            // when
            final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

            // then
            assertThat(kunden).isNotNull();
            final var embedded = kunden._embedded();
            assertThat(embedded).isNotNull();
            final var kundenList = embedded.kunden();
            assertThat(kundenList)
                .isNotNull()
                .isNotEmpty();
            kundenList.forEach(kunde -> {
                softly.assertThat(kunde.nachname()).containsIgnoringCase(nachname);
                softly.assertThat(kunde.familienstand()).isEqualTo(FamilienstandType.of(familienstand).orElse(null));
            });
        }

        @ParameterizedTest(name = "[{index}] Suche mit einem Interesse: interesse={0}")
        @ValueSource(strings = {LESEN, REISEN})
        @DisplayName("Suche mit einem Interesse")
        void getByInteresse(final String interesseStr) {
            // given
            final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
            suchkriterien.add(INTERESSE_PARAM, interesseStr);

            // when
            final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

            // then
            assertThat(kunden).isNotNull();
            final var embedded = kunden._embedded();
            assertThat(embedded).isNotNull();
            final var kundenList = embedded.kunden();
            assertThat(kundenList)
                .isNotNull()
                .isNotEmpty();
            @SuppressWarnings("OptionalGetWithoutIsPresent")
            final var interesse = InteresseType.of(interesseStr).get();
            kundenList.forEach(kunde -> {
                final var interessen = kunde.interessen();
                softly.assertThat(interessen)
                    .isNotNull()
                    .doesNotContainNull()
                    .contains(interesse);
            });
        }

        @ParameterizedTest(name = "[{index}] Suche mit mehreren Interessen: interesse1={0}, interesse1={1}")
        @CsvSource(LESEN + ',' + REISEN)
        @DisplayName("Suche mit mehreren Interessen")
        @SuppressWarnings("OptionalGetWithoutIsPresent")
        void getByInteressen(final String interesse1Str, final String interesse2Str) {
            // given
            final MultiValueMap<String, String> suchkriterien = new LinkedMultiValueMap<>();
            suchkriterien.add(INTERESSE_PARAM, interesse1Str);
            suchkriterien.add(INTERESSE_PARAM, interesse2Str);

            // when
            final var kunden = kundeRepo.get(suchkriterien, ADMIN_BASIC_AUTH);

            // then
            assertThat(kunden).isNotNull();
            final var embedded = kunden._embedded();
            assertThat(embedded).isNotNull();
            final var kundenList = embedded.kunden();
            assertThat(kundenList)
                .isNotNull()
                .isNotEmpty();
            final var interesse1 = InteresseType.of(interesse1Str).get();
            final var interesse2 = InteresseType.of(interesse2Str).get();
            final var interessenList = List.of(interesse1, interesse2);
            kundenList.forEach(kunde -> {
                final var interessen = kunde.interessen();
                softly.assertThat(interessen)
                    .isNotNull()
                    .doesNotContainNull()
                    .containsAll(interessenList);
            });
        }
    }

    @Nested
    @DisplayName("REST-Schnittstelle fuer die Suche nach Strings")
    class SucheNachStrings {
        @ParameterizedTest(name = "[{index}] Suche Nachnamen mit Praefix prefix={0}")
        @ValueSource(strings = {NACHNAME_PREFIX_A, NACHNAME_PREFIX_D})
        @DisplayName("Suche Nachnamen mit Praefix")
        void getNachnamen(final String prefix) {
            // when
            final var nachnamenStr = kundeRepo.getNachnamen(prefix, ADMIN_BASIC_AUTH);

            // then
            assertThat(nachnamenStr)
                .isNotNull()
                .isNotEmpty();
            final var tmp = nachnamenStr.replace(" ", "").substring(1);
            final var nachnamen = tmp.substring(0, tmp.length() - 1).split(",");
            assertThat(nachnamen)
                .isNotNull()
                .isNotEmpty();
            Arrays.stream(nachnamen)
                .forEach(nachname -> assertThat(nachname).startsWith(prefix));
        }
    }
}
