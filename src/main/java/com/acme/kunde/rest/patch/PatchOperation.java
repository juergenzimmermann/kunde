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

/**
 * Hilfsklasse für HTTP PATCH mit Datensätzen, wie z.B.
 * {"op": "replace", "path": "/email", "value": "new.email@test.de"}.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 *
 * @param op PATCH-Operation, z.B. add, remove, replace.
 * @param path Pfad zur adressierten Property, z.B. /email.
 * @param value Der neue Wert für die Property.
 */
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public record PatchOperation(
    PatchOperationType op,
    String path,
    String value
) {
}
