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

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import static jakarta.persistence.FetchType.LAZY;

/**
 * Geldbetrag und Währungseinheit für eine Umsatzangabe.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Entity
@Table(name = "umsatz")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
@SuppressWarnings({"JavadocDeclaration", "RequireEmptyLineBeforeBlockTagGroup", "MissingSummary"})
public class Umsatz {
    @Id
    @GeneratedValue
    // Oracle: https://in.relation.to/2022/05/12/orm-uuid-mapping
    // @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @jakarta.persistence.Column(updatable = false)
    @JsonIgnore
    private UUID id;

    /**
     * Der Betrag beim Umsatz.
     * @param betrag Der Betrag.
     * @return Der Betrag.
     */
    private BigDecimal betrag;

    /**
     * Die Währung beim Umsatz.
     * @param waehrung Die Währung.
     * @return Die Währung.
     */
    private Currency waehrung;

    /**
     * Der zugehörige Kunde.
     * @param kunde Der zugehörige Kunde.
     * @return Der zugehörige Kunde.
     */
    @ManyToOne(optional = false, fetch = LAZY)
    @JoinColumn(updatable = false)
    @JsonIgnore
    @ToString.Exclude
    private Kunde kunde;
}
