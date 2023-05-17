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
package com.acme.kunde.service;

import com.acme.kunde.entity.Kunde;
import com.acme.kunde.mail.Mailer;
import com.acme.kunde.repository.KundeRepository;
import com.acme.kunde.security.CustomUserDetailsService;
import com.acme.kunde.security.PasswordInvalidException;
import com.acme.kunde.security.UsernameExistsException;
import jakarta.validation.Validator;
import java.util.Objects;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Anwendungslogik für Kunden auch mit Bean Validation.
 * <img src="../../../../../asciidoc/KundeWriteService.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class KundeWriteService {
    private final KundeRepository repo;
    // https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#validation-beanvalidation
    private final Validator validator;
    private final CustomUserDetailsService userService;
    private final Mailer mailer;

    /**
     * Einen neuen Kunden anlegen.
     *
     * @param kunde Das Objekt des neu anzulegenden Kunden.
     * @param user Die Benutzerdaten für den neuen Kunden.
     * @return Der neu angelegte Kunden mit generierter ID
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws EmailExistsException Es gibt bereits einen Kunden mit der Emailadresse.
     * @throws PasswordInvalidException falls das Passwort ungültig ist
     * @throws UsernameExistsException falls der Benutzername bereits existiert
     */
    // https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
    @Transactional
    @SuppressWarnings("TrailingComment")
    public Kunde create(final Kunde kunde, final UserDetails user) {
        log.debug("create: {}", kunde); //NOSONAR
        log.debug("create: {}", kunde.getAdresse());
        log.debug("create: umsaetze={}", kunde.getUmsaetze());
        log.debug("create: {}", user);

        final var violations = validator.validate(kunde);
        if (!violations.isEmpty()) {
            log.debug("create: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }

        if (repo.existsByEmail(kunde.getEmail())) {
            throw new EmailExistsException(kunde.getEmail());
        }

        final var login = userService.save(user);
        log.trace("create: {}", login);
        final var kundeDB = repo.save(kunde);

        mailer.send(kundeDB);

        log.debug("create: {}", kundeDB);
        return kundeDB;
    }

    /**
     * Einen vorhandenen Kunden aktualisieren.
     *
     * @param kunde Das Objekt mit den neuen Daten (ohne ID)
     * @param id ID des zu aktualisierenden Kunden
     * @param version Die erforderliche Version
     * @return Aktualisierter Kunde mit erhöhter Versionsnummer
     * @throws ConstraintViolationsException Falls mindestens ein Constraint verletzt ist.
     * @throws NotFoundException Kein Kunde zur ID vorhanden.
     * @throws VersionOutdatedException Die Versionsnummer ist veraltet und nicht aktuell.
     * @throws EmailExistsException Es gibt bereits einen Kunden mit der Emailadresse.
     */
    @Transactional
    public Kunde update(final Kunde kunde, final UUID id, final int version) {
        log.debug("update: {}", kunde);
        log.debug("update: id={}, version={}", id, version);

        final var violations = validator.validate(kunde);
        if (!violations.isEmpty()) {
            log.debug("update: violations={}", violations);
            throw new ConstraintViolationsException(violations);
        }
        log.trace("update: Keine Constraints verletzt");

        final var kundeDbOptional = repo.findById(id);
        if (kundeDbOptional.isEmpty()) {
            throw new NotFoundException(id);
        }

        var kundeDb = kundeDbOptional.get();
        log.trace("update: version={}, kundeDb={}", version, kundeDb);
        if (version != kundeDb.getVersion()) {
            throw new VersionOutdatedException(version);
        }

        final var email = kunde.getEmail();
        // Ist die neue E-Mail bei einem *ANDEREN* Kunden vorhanden?
        if (!Objects.equals(email, kundeDb.getEmail()) && repo.existsByEmail(email)) {
            log.debug("update: email {} existiert", email);
            throw new EmailExistsException(email);
        }
        log.trace("update: Kein Konflikt mit der Emailadresse");

        kundeDb.set(kunde);
        kundeDb = repo.save(kundeDb);
        log.debug("update: {}", kundeDb);
        return kundeDb;
    }

    /**
     * Einen Kunden löschen.
     *
     * @param id Die ID des zu löschenden Kunden.
     */
    @Transactional
    public void deleteById(final UUID id) {
        log.debug("deleteById: id={}", id);
        final var kundeOptional = repo.findById(id);
        if (kundeOptional.isEmpty()) {
            log.debug("deleteById: id={} nicht vorhanden", id);
            return;
        }
        repo.delete(kundeOptional.get());
    }
}
