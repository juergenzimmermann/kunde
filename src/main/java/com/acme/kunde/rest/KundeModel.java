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
package com.acme.kunde.rest;

import com.acme.kunde.entity.Adresse;
import com.acme.kunde.entity.FamilienstandType;
import com.acme.kunde.entity.GeschlechtType;
import com.acme.kunde.entity.InteresseType;
import com.acme.kunde.entity.Kunde;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

/**
 * Model-Klasse für Spring HATEOAS. @lombok.Data fasst die Annotationen @ToString, @EqualsAndHashCode, @Getter, @Setter
 * und @RequiredArgsConstructor zusammen.
 * <img src="../../../../../asciidoc/KundeModel.svg" alt="Klassendiagramm">
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@JsonPropertyOrder({
    "nachname", "email", "kategorie", "hasNewsletter", "geburtsdatum", "homepage", "geschlecht", "familienstand",
    "adresse", "umsatz", "interessen"
})
@Relation(collectionRelation = "kunden", itemRelation = "kunde")
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString(callSuper = true)
class KundeModel extends RepresentationModel<KundeModel> {
    private final String nachname;

    @EqualsAndHashCode.Include
    private final String email;

    private final int kategorie;
    private final boolean hasNewsletter;
    private final LocalDate geburtsdatum;
    private final URL homepage;
    private final GeschlechtType geschlecht;
    private final FamilienstandType familienstand;
    private final List<InteresseType> interessen;
    private final Adresse adresse;

    KundeModel(final Kunde kunde) {
        nachname = kunde.getNachname();
        email = kunde.getEmail();
        kategorie = kunde.getKategorie();
        hasNewsletter = kunde.isHasNewsletter();
        geburtsdatum = kunde.getGeburtsdatum();
        homepage = kunde.getHomepage();
        geschlecht = kunde.getGeschlecht();
        familienstand = kunde.getFamilienstand();
        interessen = kunde.getInteressen();
        adresse = kunde.getAdresse();
    }
}
