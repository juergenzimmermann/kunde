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

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static java.util.Locale.GERMAN;

/**
 * Service-Klasse, um Benutzerkennungen zu suchen und neu anzulegen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    private static final int MIN_LENGTH = 8;
    private static final Pattern UPPERCASE = Pattern.compile(".*[A-Z].*"); // NOSONAR
    private static final Pattern LOWERCASE = Pattern.compile(".*[a-z].*"); // NOSONAR
    private static final Pattern NUMBERS = Pattern.compile(".*\\d.*"); // NOSONAR
    @SuppressWarnings("RegExpRedundantEscape")
    private static final Pattern SYMBOLS = Pattern.compile(".*[!-/:-@\\[-`{-\\~].*");

    private final LoginRepository repo;
    private final PasswordEncoder passwordEncoder;

    /**
     * Zu einem gegebenen Username wird der zugehörige User gesucht.
     *
     * @param username Username des gesuchten Users
     * @return Der gesuchte User oder null
     */
    @Override
    public UserDetails loadUserByUsername(final String username) {
        log.debug("findByUsername: {}", username);
        final var loginOpt = repo.findByUsername(username);
        if (loginOpt.isEmpty()) {
            //noinspection ReturnOfNull
            return null;
        }
        final var userDetails = loginOpt.get().toUserDetails();
        log.debug("findByUsername: {}", userDetails);
        return userDetails;
    }

    /**
     * Ein Login-Objekt bauen und in der DB abspeichern.
     *
     * @param user Der neu anzulegende User
     * @return Ein neu gebautes Login-Objekt
     * @throws PasswordInvalidException falls das Passwort ungültig ist
     * @throws UsernameExistsException falls der Benutzername bereits existiert
     */
    public Login save(final UserDetails user) {
        final var login = userDetailsToLogin(user);
        repo.save(login);
        return login;
    }

    private Login userDetailsToLogin(final UserDetails user) {
        log.debug("userDetailsToLogin: {}", user);

        final var password = user.getPassword();
        if (!checkPassword(password)) {
            throw new PasswordInvalidException(password);
        }

        final var username = user.getUsername();
        final var isUsernameExisting = repo.existsByUsername(username);
        if (isUsernameExisting) {
            throw new UsernameExistsException(username);
        }

        // Die Account-Informationen des Kunden transformieren: in Account-Informationen fuer die Security-Komponente
        final var login = new Login();
        login.setUsername(username.toLowerCase(GERMAN));

        final var encodedPassword = passwordEncoder.encode(password);
        login.setPassword(encodedPassword);

        final var rollen = user.getAuthorities()
            .stream()
            .map(grantedAuthority -> {
                final var rolleStr = grantedAuthority
                    .getAuthority()
                    .substring(Rolle.ROLE_PREFIX.length());
                return Rolle.valueOf(rolleStr);
            })
            .toList();
        login.setRollen(rollen);

        log.trace("userDetailsToLogin: login = {}", login);
        return login;
    }

    // https://github.com/making/yavi/blob/develop/src/main/java/am/ik/yavi/constraint/password/PasswordPolicy.java
    @SuppressWarnings("ReturnCount")
    private boolean checkPassword(final CharSequence password) {
        if (password.length() < MIN_LENGTH) {
            return false;
        }
        if (!UPPERCASE.matcher(password).matches()) {
            return false;
        }
        if (!LOWERCASE.matcher(password).matches()) {
            return false;
        }
        if (!NUMBERS.matcher(password).matches()) {
            return false;
        }
        return SYMBOLS.matcher(password).matches();
    }
}
