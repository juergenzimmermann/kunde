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
package com.acme.kunde.service;

import com.acme.kunde.MailProps;
import com.acme.kunde.entity.Adresse;
import com.acme.kunde.entity.Kunde;
import com.acme.kunde.entity.Umsatz;
import com.acme.kunde.mail.Mailer;
import com.acme.kunde.repository.KundeRepository;
import com.acme.kunde.security.CustomUser;
import com.acme.kunde.security.CustomUserDetailsService;
import com.acme.kunde.security.LoginRepository;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.InjectSoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import static com.acme.kunde.entity.FamilienstandType.LEDIG;
import static com.acme.kunde.entity.GeschlechtType.WEIBLICH;
import static com.acme.kunde.entity.InteresseType.LESEN;
import static com.acme.kunde.entity.InteresseType.REISEN;
import static java.math.BigDecimal.ONE;
import static java.time.LocalDateTime.now;
import static java.util.Locale.GERMANY;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.catchThrowableOfType;
import static org.junit.jupiter.api.condition.JRE.JAVA_19;
import static org.junit.jupiter.api.condition.JRE.JAVA_21;
import static org.mockito.Mockito.when;

// https://junit.org/junit5/docs/current/user-guide
// https://assertj.github.io/doc

@Tag("unit")
@Tag("service-write")
@DisplayName("Anwendungskern fuer Schreiben")
@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
@EnabledForJreRange(min = JAVA_19, max = JAVA_21)
@SuppressWarnings({"InnerTypeLast", "ClassFanOutComplexity", "MethodOnlyUsedFromInnerClass", "WriteTag"})
class KundeWriteServiceTest {
    private static final String ID_NICHT_VORHANDEN = "99999999-9999-9999-9999-999999999999";
    private static final String ID_UPDATE = "00000000-0000-0000-0000-000000000020";
    private static final String PLZ = "12345";
    private static final String ORT = "Testort";
    private static final String NACHNAME = "Nachname-Test";
    private static final String EMAIL = "theo@test.de";
    private static final LocalDate GEBURTSDATUM = LocalDate.of(2022, 1, 1);
    private static final Currency WAEHRUNG = Currency.getInstance(GERMANY);
    private static final String HOMEPAGE = "https://test.de";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "Pass123.";
    private static final String VERSION_ALT = "-1";

    @Mock
    private KundeRepository repo;

    private final Validator validator;

    @Mock
    @SuppressWarnings({"unused", "UnusedVariable"})
    private LoginRepository loginRepo;

    @Mock
    @SuppressWarnings({"unused", "UnusedVariable"})
    private DelegatingPasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @Mock
    @SuppressWarnings({"unused", "UnusedVariable"})
    private JavaMailSender mailSender;

    @Mock
    @SuppressWarnings({"unused", "UnusedVariable"})
    private MailProps mailProps;

    @InjectMocks
    private Mailer mailer;

    private KundeWriteService service;

    @InjectSoftAssertions
    private SoftAssertions softly;

    @SuppressWarnings("RedundantModifier")
    KundeWriteServiceTest() {
        try (final ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void beforeEach() {
        service = new KundeWriteService(repo, validator, userDetailsService, mailer);
    }

    @Nested
    @DisplayName("Anwendungskern fuer Erzeugen")
    class Erzeugen {
        @ParameterizedTest(name = "[{index}] Neuanlegen eines neuen Kunden: nachname={0}, email={1}, plz={2}")
        @CsvSource(NACHNAME + ',' + EMAIL + ',' + PLZ + ',' + USERNAME + ',' + PASSWORD)
        @DisplayName("Neuanlegen eines neuen Kunden")
        void create(final ArgumentsAccessor args) {
            // given
            final var nachname = args.getString(0);
            final var email = args.getString(1);
            final var plz = args.getString(2);
            final var username = args.getString(3);
            final var password = args.getString(4);

            when(repo.existsByEmail(email)).thenReturn(false);
            final var user = createCustomUserMock(username, password);
            final var kundeMock = createKundeMock(randomUUID(), nachname, email, plz);
            when(repo.save(kundeMock)).thenReturn(kundeMock);

            // when
            final var kunde = service.create(kundeMock, user);

            // then
            assertThat(kunde).isNotNull();
            softly.assertThat(kunde.getId()).isNotNull();
            softly.assertThat(kunde.getNachname()).isEqualTo(nachname);
            softly.assertThat(kunde.getEmail()).isEqualTo(email);
            softly.assertThat(kunde.getAdresse().getPlz()).isEqualTo(plz);
            softly.assertThat(kunde.getUsername()).isEqualTo(username);
        }

        @ParameterizedTest(name = "[{index}] Neuanlegen mit existierender Email: nachname={0}, email={1}, plz={2}")
        @CsvSource(NACHNAME + ',' + EMAIL + ',' + PLZ + ',' + USERNAME + ',' + PASSWORD)
        @DisplayName("Neuanlegen mit existierender Email")
        void createEmailExists(final ArgumentsAccessor args) {
            // given
            final var nachname = args.getString(0);
            final var email = args.getString(1);
            final var plz = args.getString(2);
            final var username = args.getString(3);
            final var password = args.getString(4);

            when(repo.existsByEmail(email)).thenReturn(true);
            final var user = createCustomUserMock(username, password);
            final var kundeMock = createKundeMock(nachname, email, plz);

            // when
            final var emailExistsException = catchThrowableOfType(
                () -> service.create(kundeMock, user),
                EmailExistsException.class
            );

            // then
            assertThat(emailExistsException).isNotNull();
            assertThat(emailExistsException.getEmail()).contains(email);
        }
    }

    @Nested
    @DisplayName("Anwendungskern fuer Aendern")
    class Aendern {
        @ParameterizedTest(name = "[{index}] Aendern eines Kunden: id={0}, nachname={1}, email={2}, plz={3}")
        @CsvSource(ID_UPDATE + ',' +  NACHNAME + ',' +  EMAIL + ',' +  PLZ)
        @DisplayName("Aendern eines Kunden")
        void update(final String idStr, final String nachname, final String email, final String plz) {
            // given
            final var id = UUID.fromString(idStr);
            final var kundeMock = createKundeMock(id, nachname, email, plz);
            when(repo.findById(id)).thenReturn(Optional.of(kundeMock));
            when(repo.save(kundeMock)).thenReturn(kundeMock);

            // when
            final var kunde = service.update(kundeMock, id, kundeMock.getVersion());

            // then
            assertThat(kunde)
                .isNotNull()
                .extracting(Kunde::getId)
                .isEqualTo(kundeMock.getId());
        }

        @ParameterizedTest(name = "[{index}] Aendern eines nicht-vorhandenen Kunden: id={0}, nachname={1}, email={2}")
        @CsvSource(ID_NICHT_VORHANDEN + ',' + NACHNAME + ',' + EMAIL + ',' + PLZ)
        @DisplayName("Aendern eines nicht-vorhandenen Kunden")
        void updateNichtVorhanden(final String idStr, final String nachname, final String email, final String plz) {
            // given
            final var id = UUID.fromString(idStr);
            final var kundeMock = createKundeMock(id, nachname, email, plz);
            when(repo.findById(id)).thenReturn(Optional.empty());

            // when
            final var notFoundException = catchThrowableOfType(
                () -> service.update(kundeMock, id, kundeMock.getVersion()),
                NotFoundException.class
            );

            // then
            assertThat(notFoundException).isNotNull();
        }

        @ParameterizedTest(name = "[{index}] Aendern mit alter Versionsnummer: id={0}, version={4}")
        @CsvSource(ID_UPDATE + ',' + NACHNAME + ',' + EMAIL + ',' + PLZ + ',' + VERSION_ALT)
        @DisplayName("Aendern mit alter Versionsnummer")
        void updateVersionOutdated(final ArgumentsAccessor args) {
            // given
            final var idStr = args.getString(0);
            final var id = UUID.fromString(idStr);
            final var nachname = args.getString(1);
            final var email = args.getString(2);
            final var plz = args.getString(3);
            final var version = args.getInteger(4);
            final var kundeMock = createKundeMock(id, nachname, email, plz);
            when(repo.findById(id)).thenReturn(Optional.of(kundeMock));

            // when
            @SuppressWarnings("LocalVariableNamingConvention")
            final var versionOutdatedException = catchThrowableOfType(
                () -> service.update(kundeMock, id, version),
                VersionOutdatedException.class
            );

            // then
            assertThat(versionOutdatedException)
                .isNotNull()
                .extracting(VersionOutdatedException::getVersion)
                .isEqualTo(version);
        }
    }

    // -------------------------------------------------------------------------
    // Hilfsmethoden fuer Mock-Objekte
    // -------------------------------------------------------------------------
    private Kunde createKundeMock(final String nachname, final String email, final String plz) {
        return createKundeMock(null, nachname, email, plz);
    }

    private Kunde createKundeMock(final UUID id, final String nachname, final String email, final String plz) {
        final URL homepage;
        try {
            homepage = URI.create(HOMEPAGE).toURL();
        } catch (final MalformedURLException e) {
            throw new IllegalStateException(e);
        }
        final var umsaetze = List.of(
            Umsatz.builder()
                .id(randomUUID())
                .betrag(ONE)
                .waehrung(WAEHRUNG)
                .build()
        );
        final var adresse = Adresse.builder()
            .id(randomUUID())
            .plz(plz)
            .ort(ORT)
            .build();
        return Kunde.builder()
            .id(id)
            .version(0)
            .nachname(nachname)
            .email(email)
            .kategorie(1)
            .hasNewsletter(true)
            .geburtsdatum(GEBURTSDATUM)
            .homepage(homepage)
            .geschlecht(WEIBLICH)
            .familienstand(LEDIG)
            .adresse(adresse)
            .umsaetze(umsaetze)
            .interessen(List.of(LESEN, REISEN))
            .username(USERNAME)
            .erzeugt(now(ZoneId.of("Europe/Berlin")))
            .aktualisiert(now(ZoneId.of("Europe/Berlin")))
            .build();
    }

    private CustomUser createCustomUserMock(final String username, final String password) {
        return new CustomUser(username, password);
    }
}
