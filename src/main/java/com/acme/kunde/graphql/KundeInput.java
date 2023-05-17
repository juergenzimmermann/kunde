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

import com.acme.kunde.entity.FamilienstandType;
import com.acme.kunde.entity.GeschlechtType;
import com.acme.kunde.entity.InteresseType;
import com.acme.kunde.entity.Kunde;
import com.acme.kunde.entity.Umsatz;
import com.acme.kunde.security.CustomUser;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Eine Value-Klasse für Eingabedaten passend zu KundeInput aus dem GraphQL-Schema.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 * @param nachname Nachname
 * @param email Emailadresse
 * @param kategorie Kategorie
 * @param hasNewsletter Newsletter-Abonnement
 * @param geburtsdatum Geburtsdatum
 * @param homepage URL der Homepage
 * @param geschlecht Geschlecht
 * @param familienstand Familienstand
 * @param adresse Adresse
 * @param umsaetze Umsätze
 * @param interessen Interessen als Liste
 * @param username Benutzername
 * @param password Passwort
 */
@SuppressWarnings("RecordComponentNumber")
record KundeInput(
    String nachname,
    String email,
    int kategorie,
    boolean hasNewsletter,
    String geburtsdatum,
    URL homepage,
    GeschlechtType geschlecht,
    FamilienstandType familienstand,
    AdresseInput adresse,
    List<UmsatzInput> umsaetze,
    List<InteresseType> interessen,
    String username,
    String password
) {
    /**
     * Konvertierung in ein Objekt der Entity-Klasse Kunde.
     *
     * @return Das konvertierte Kunde-Objekt
     */
    Kunde toKunde() {
        final LocalDate geburtsdatumDate;
        geburtsdatumDate = LocalDate.parse(geburtsdatum);
        final var adresseEntity = adresse().toAdresse();
        final List<Umsatz> umsaetzeEntity = umsaetze == null
            ? new ArrayList<>(1)
            : umsaetze.stream()
                .map(UmsatzInput::toUmsatz)
                .collect(Collectors.toList());

        final var kunde = Kunde
            .builder()
            .id(null)
            .nachname(nachname)
            .email(email)
            .kategorie(kategorie)
            .hasNewsletter(hasNewsletter)
            .geburtsdatum(geburtsdatumDate)
            .homepage(homepage)
            .geschlecht(geschlecht)
            .familienstand(familienstand)
            .adresse(adresseEntity)
            .umsaetze(umsaetzeEntity)
            .interessen(interessen)
            .username(username)
            .build();

        // Rueckwaertsverweise
        kunde.getAdresse().setKunde(kunde);
        umsaetzeEntity.forEach(umsatz -> umsatz.setKunde(kunde));

        return kunde;
    }

    /**
     * Konvertierung in ein Objekt der Entity-Klasse CustomUser.
     *
     * @return Das konvertierte CustomUser-Objekt
     */
    UserDetails toUserDetails() {
        return new CustomUser(username, password);
    }
}
