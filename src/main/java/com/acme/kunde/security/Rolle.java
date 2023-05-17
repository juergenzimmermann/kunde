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

/**
 * Singleton für verfügbare Rollen als Strings für das Spring-Interface GrantedAuthority.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
public enum Rolle {
    /**
     * Die Rolle ADMIN.
     */
    ADMIN("ADMIN"),

    /**
     * Die Rolle KUNDE.
     */
    KUNDE("KUNDE"),

    /**
     * Die Rolle ACTUATOR.
     */
    ACTUATOR("ACTUATOR");

    /**
     * Präfix für die Rollennamen gemäß Spring Security.
     */
    public static final String ROLE_PREFIX = "ROLE_";

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private final String value;

    Rolle(final String value) {
        this.value = value;
    }
}
