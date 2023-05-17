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
package com.acme.kunde.rest;

import com.acme.kunde.rest.patch.PatchOperation;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PatchExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.http.HttpHeaders.IF_NONE_MATCH;
import static org.springframework.http.HttpHeaders.IF_MATCH;

@HttpExchange
@SuppressWarnings("WriteTag")
interface KundeRepository {

    @GetExchange("/{id}")
    ResponseEntity<KundeDownload> getById(
        @PathVariable String id,
        @RequestHeader(IF_NONE_MATCH) String version,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @GetExchange("/{id}")
    ResponseEntity<KundeDownload> getByIdOhneVersion(
        @PathVariable String id,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @GetExchange
    KundenDownload get(
        @RequestParam MultiValueMap<String, String> suchkriterien,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @PostExchange
    ResponseEntity<Void> post(@RequestBody KundeUserDTO kunde);

    @PutExchange("/{id}")
    ResponseEntity<Void> put(
        @PathVariable String id,
        @RequestBody KundeDTO kunde,
        @RequestHeader(IF_MATCH) String version,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @PatchExchange("/{id}")
    ResponseEntity<Void> patch(
        @PathVariable String id,
        @RequestBody List<PatchOperation> patch,
        @RequestHeader(IF_MATCH) String version,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @PutExchange("/{id}")
    void putOhneVersion(
        @PathVariable String id,
        @RequestBody KundeDTO kunde,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @PatchExchange("/{id}")
    void patchOhneVersion(
        @PathVariable String id,
        @RequestBody List<PatchOperation> patch,
        @RequestHeader(AUTHORIZATION) String authorization
    );

    @DeleteExchange("/{id}")
    ResponseEntity<Void> deleteById(@PathVariable String id, @RequestHeader(AUTHORIZATION) String authorization);

    @GetExchange("/nachname/{prefix}")
    String getNachnamen(@PathVariable String prefix, @RequestHeader(AUTHORIZATION) String authorization);
}
