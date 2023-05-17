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
import com.acme.kunde.repository.KundeRepository;
import com.acme.kunde.repository.PredicateBuilder;
import com.acme.kunde.security.Rolle;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.acme.kunde.security.Rolle.ADMIN;

/**
 * Anwendungslogik für Kunden.
 * <img src="../../../../../asciidoc/KundeReadService.svg" alt="Klassendiagramm">
 * Schreiboperationen werden mit Transaktionen durchgeführt und Lese-Operationen mit Readonly-Transaktionen:
 * <a href="https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions">siehe Dokumentation</a>.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
// https://docs.spring.io/spring-data/jpa/docs/current/reference/html/#transactions
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class KundeReadService {
    private final KundeRepository repo;
    private final PredicateBuilder predicateBuilder;

    /**
     * Einen Kunden anhand seiner ID suchen.
     *
     * @param id Die Id des gesuchten Kunden
     * @param user UserDetails-Objekt
     * @return Der gefundene Kunde
     * @throws NotFoundException Falls kein Kunde gefunden wurde
     * @throws AccessForbiddenException Falls die erforderlichen Rollen nicht gegeben sind
     */
    public @NonNull Kunde findById(final UUID id, final UserDetails user) {
        log.debug("findById: id={}, user={}", id, user);
        final var kundeOpt = repo.findById(id);
        if (kundeOpt.isPresent() && Objects.equals(kundeOpt.get().getUsername(), user.getUsername())) {
            // eigene Kundendaten
            return kundeOpt.get();
        }

        final var rollen = user
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .map(str -> str.substring(Rolle.ROLE_PREFIX.length()))
            .map(Rolle::valueOf)
            .toList();
        if (!rollen.contains(ADMIN)) {
            // nicht admin, aber keine eigenen (oder keine) Kundendaten
            throw new AccessForbiddenException(rollen);
        }

        // admin: Kundendaten evtl. nicht gefunden
        final var kunde = kundeOpt.orElseThrow(() -> new NotFoundException(id));
        log.debug("findById: {}", kunde);
        return kunde;
    }

    /**
     * Kunden anhand von Suchkriterien als Collection suchen.
     *
     * @param suchkriterien Die Suchkriterien
     * @return Die gefundenen Kunden oder eine leere Liste
     * @throws NotFoundException Falls keine Kunden gefunden wurden
     */
    @SuppressWarnings({"ReturnCount", "NestedIfDepth", "CyclomaticComplexity"})
    public @NonNull Collection<Kunde> find(@NonNull final Map<String, List<String>> suchkriterien) {
        log.debug("find: suchkriterien={}", suchkriterien);

        if (suchkriterien.isEmpty()) {
            return repo.findAll();
        }

        if (suchkriterien.size() == 1) {
            final var nachnamen = suchkriterien.get("nachname");
            if (nachnamen != null && nachnamen.size() == 1) {
                final var kunden = repo.findByNachname(nachnamen.get(0));
                if (kunden.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                log.debug("find (nachname): {}", kunden);
                return kunden;
            }

            final var emails = suchkriterien.get("email");
            if (emails != null && emails.size() == 1) {
                final var kunde = repo.findByEmail(emails.get(0));
                if (kunde.isEmpty()) {
                    throw new NotFoundException(suchkriterien);
                }
                final var kunden = List.of(kunde.get());
                log.debug("find (email): {}", kunden);
                return kunden;
            }
        }

        final var predicate = predicateBuilder
            .build(suchkriterien)
            .orElseThrow(() -> new NotFoundException(suchkriterien));
        final var kunden = repo.findAll(predicate);
        if (kunden.isEmpty()) {
            throw new NotFoundException(suchkriterien);
        }
        log.debug("find: {}", kunden);
        return kunden;
    }

    /**
     * Abfrage, welche Nachnamen es zu einem Präfix gibt.
     *
     * @param prefix Nachname-Präfix.
     * @return Die passenden Nachnamen.
     * @throws NotFoundException Falls keine Nachnamen gefunden wurden.
     */
    public @NonNull Collection<String> findNachnamenByPrefix(final String prefix) {
        log.debug("findNachnamenByPrefix: {}", prefix);
        final var nachnamen = repo.findNachnamenByPrefix(prefix);
        if (nachnamen.isEmpty()) {
            throw new NotFoundException();
        }
        log.debug("findNachnamenByPrefix: {}", nachnamen);
        return nachnamen;
    }
}
