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

import com.acme.kunde.entity.Umsatz;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Geldbetrag und Währungseinheit für eine Umsatzangabe.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 *
 * @param betrag Der Betrag als unveränderliches Pflichtfeld.
 * @param waehrung Die Währungseinheit als unveränderliches Pflichtfeld.
 */
record UmsatzInput(BigDecimal betrag, Currency waehrung) {
    Umsatz toUmsatz() {
        return Umsatz
            .builder()
            .betrag(betrag)
            .waehrung(waehrung)
            .build();
    }
}
