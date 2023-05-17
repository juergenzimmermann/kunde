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
package com.acme.kunde.entity;

import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Enum für Interessen. Dazu können auf der Clientseite z.B. Checkboxen realisiert werden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
public enum InteresseType {
    /**
     * Sport mit dem internen Wert S für z.B. das Mapping in einem JSON-Datensatz oder das Abspeichern in einer DB.
     */
    SPORT("S"),
    /**
    * Lesen mit dem internen Wert L für z.B. das Mapping in einem JSON-Datensatz oder das Abspeichern in einer DB.
    */
    LESEN("L"),

    /**
    * Reisen mit dem internen Wert R für z.B. das Mapping in einem JSON-Datensatz oder das Abspeichern in einer DB.
    */
    REISEN("R");

    private final String value;

    InteresseType(final String value) {
        this.value = value;
    }

    /**
     * Konvertierung eines Strings in einen Enum-Wert.
     *
     * @param value Der String, zu dem ein passender Enum-Wert ermittelt werden soll.
     * @return Passender Enum-Wert oder null.
     */
    public static Optional<InteresseType> of(final String value) {
        return Stream.of(values())
            .filter(interesse -> interesse.value.equalsIgnoreCase(value))
            .findFirst();
    }

    /**
     * Einen enum-Wert als String mit dem internen Wert ausgeben.
     * Dieser Wert wird durch Jackson in einem JSON-Datensatz verwendet.
     * [<a href="https://github.com/FasterXML/jackson-databind/wiki">Wiki-Seiten</a>]
     *
     * @return Interner Wert
     */
    @JsonValue
    @Override
    public String toString() {
        return value;
    }
}
