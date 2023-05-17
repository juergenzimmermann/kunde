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
package com.acme.kunde.security;

import com.acme.kunde.rest.CustomUserDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Collection;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.security.Principal;
import static com.acme.kunde.security.AuthController.AUTH_PATH;
import static org.springframework.http.HttpStatus.UNAUTHORIZED;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;
import static org.springframework.http.ResponseEntity.status;

/**
 * Controller für die Abfrage von Werten.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Controller
@RequestMapping(AUTH_PATH)
@Tag(name = "Authentifizierung API")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    /**
     * Pfad für Authentifizierung.
     */
    @SuppressWarnings("TrailingComment")
    public static final String AUTH_PATH = "/auth"; //NOSONAR

    private final LoginRepository repo;
    private final PasswordEncoder passwordEncoder;

    /**
     * "Einloggen" bei Basic Authentication.
     *
     * @param customUserDTO Benutzerkennung und Passwort.
     * @return Response mit der Collection der Rollen oder Statuscode 401.
     */
    @PostMapping(path = "login", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Einloggen bei Basic Authentifizierung", tags = "Auth")
    @ApiResponse(responseCode = "200", description = "Eingeloggt")
    @ApiResponse(responseCode = "401", description = "Fehler bei Username oder Passwort")
    ResponseEntity<Collection<Rolle>> login(final CustomUserDTO customUserDTO) {
        log.debug("login: {}", customUserDTO);
        final var username = customUserDTO.username();
        final var password = customUserDTO.password();

        final var userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            return status(UNAUTHORIZED).build();
        }
        final var user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return status(UNAUTHORIZED).build();
        }

        final var rollen = user.getRollen();
        log.debug("login: rollen={}", rollen);
        return ok(rollen);
    }
    private Optional<Login> findByUsername(final String username) {
        return repo.findByUsername(username);
    }

    /**
     * Die Rollen zur eigenen Benutzerkennung ermitteln.
     *
     * @param principal Benutzerkennung als Objekt zum Interface Principal.
     * @return Response mit den eigenen Rollen oder Statuscode 401, falls man nicht eingeloggt ist.
     */
    @GetMapping(path = "/rollen", produces = APPLICATION_JSON_VALUE)
    @Operation(summary = "Abfrage der eigenen Rollen", tags = "Auth")
    @ApiResponse(responseCode = "200", description = "Rollen ermittelt")
    @ApiResponse(responseCode = "401", description = "Fehler bei Authentifizierung")
    ResponseEntity<Collection<Rolle>> findEigeneRollen(final Principal principal) {
        if (principal == null) {
            return status(UNAUTHORIZED).build();
        }

        final var username = principal.getName();
        log.debug("findEigeneRollen: username={}", username);

        final var userOpt = findByUsername(username);
        if (userOpt.isEmpty()) {
            return status(UNAUTHORIZED).build();
        }
        final var user = userOpt.get();

        final var rollen = user.getRollen();
        log.debug("findEigeneRollen: rollen={}", rollen);
        return ok(rollen);
    }
}
