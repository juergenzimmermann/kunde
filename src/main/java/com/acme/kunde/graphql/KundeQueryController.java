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
import com.acme.kunde.service.KundeReadService;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import static java.util.Collections.emptyMap;

/**
 * Eine Controller-Klasse für das Lesen mit der GraphQL-Schnittstelle und den Typen aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequiredArgsConstructor
@Slf4j
class KundeQueryController {
    private final KundeReadService service;

    /**
     * Suche anhand der Kunde-ID.
     *
     * @param id ID des zu suchenden Kunden
     * @param authentication Authentication-Objekt für Security
     * @return Der gefundene Kunde
     */
    @QueryMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'KUNDE')")
    Kunde kunde(@Argument final UUID id, final Authentication authentication) {
        final var user = (UserDetails) authentication.getPrincipal();
        log.debug("kunde: id={}, user={}", id, user);
        final var kunde = service.findById(id, user);
        log.debug("kunde: {}", kunde);
        return kunde;
    }

    /**
     * Suche mit diversen Suchkriterien.
     *
     * @param input Suchkriterien und ihre Werte, z.B. `nachname` und `Alpha`
     * @return Die gefundenen Kunden als Collection
     */
    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    Collection<Kunde> kunden(@Argument final Optional<Suchkriterien> input) {
        log.debug("kunden: input={}", input);
        final var suchkriterien = input.map(Suchkriterien::toMap).orElse(emptyMap());
        final var kunden = service.find(suchkriterien);
        log.debug("kunden: {}", kunden);
        return kunden;
    }
}
