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

import java.net.URI;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import static com.acme.kunde.rest.KundeWriteController.PROBLEM_PATH;
import static com.acme.kunde.rest.ProblemType.UNPROCESSABLE;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

/**
 * Exception, falls es mindestens ein verletztes Constraint gibt.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
class InvalidPatchOperationException extends ErrorResponseException {
    InvalidPatchOperationException(final URI uri) {
        super(UNPROCESSABLE_ENTITY, asProblemDetail(uri), null);
    }

    private static ProblemDetail asProblemDetail(final URI uri) {
        final var problemDetail = ProblemDetail.forStatusAndDetail(
            UNPROCESSABLE_ENTITY,
            "Mindestens eine ungueltige Patch-Operation"
        );
        problemDetail.setType(URI.create(PROBLEM_PATH + UNPROCESSABLE.getValue()));
        problemDetail.setInstance(uri);
        return problemDetail;
    }
}
