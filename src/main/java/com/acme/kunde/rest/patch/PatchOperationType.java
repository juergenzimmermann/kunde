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
package com.acme.kunde.rest.patch;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum für die Patch-Operationen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public enum PatchOperationType {
    /**
     * Ersetzen eines vorhandenen singulären Wertes.
     */
    REPLACE("replace"),

    /**
     * Einen neuen zusätzlichen Wert zu einer listen- oder mengenwertigen Property hinzufügen.
     */
    ADD("add"),

    /**
     * Aus einer listen- oder mengenwertigen Property einen Wert entfernen.
     */
    REMOVE("remove");

    private final String value;

    PatchOperationType(final String value) {
        this.value = value;
    }

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben.
     *
     * @return Der interne Wert.
     */
    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
