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

import java.util.List;
import java.util.Map;
import org.springframework.util.LinkedMultiValueMap;

/**
 * Eine Value-Klasse für Eingabedaten passend zu Suchkriterien aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 *
 * @param nachname Nachname
 * @param email Emailadresse
 */
record Suchkriterien(
    String nachname,
    String email
) {
    /**
     * Konvertierung in eine MultiValueMap.
     *
     * @return Das konvertierte MultiValueMap-Objekt
     */
    Map<String, List<String>> toMap() {
        final Map<String, List<String>> map = new LinkedMultiValueMap<>();
        if (nachname != null) {
            map.put("nachname", List.of(nachname));
        }
        if (email != null) {
            map.put("email", List.of(email));
        }
        return map;
    }
}
