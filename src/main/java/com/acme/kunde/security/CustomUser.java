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
package com.acme.kunde.security;

import java.util.Collection;
import java.util.List;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import static com.acme.kunde.security.Rolle.KUNDE;
import static com.acme.kunde.security.Rolle.ROLE_PREFIX;

/**
 * Klasse für Benutzerdaten für Spring Security.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@ToString(callSuper = true)
public class CustomUser extends User {
    /**
     * Konstruktor.
     *
     * @param username Benutzername
     * @param password Passwort
     */
    public CustomUser(final String username, final String password) {
        super(username, password, List.of(new SimpleGrantedAuthority(ROLE_PREFIX + KUNDE)));
    }

    /**
     * Konstruktor.
     *
     * @param username Benutzername
     * @param password Passwort
     * @param authorities Rollen bzw. Authorities gemäß Spring Security
     */
    public CustomUser(
        final String username,
        final String password,
        final Collection<SimpleGrantedAuthority> authorities
    ) {
        super(username, password, authorities);
    }
}
