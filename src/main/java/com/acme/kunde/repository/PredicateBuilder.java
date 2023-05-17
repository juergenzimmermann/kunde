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
package com.acme.kunde.repository;

import com.acme.kunde.entity.FamilienstandType;
import com.acme.kunde.entity.GeschlechtType;
import com.acme.kunde.entity.InteresseType;
import com.acme.kunde.entity.QKunde;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.Predicate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import static java.util.Locale.GERMAN;

/**
 * Singleton-Klasse, um Prädikate durch QueryDSL für eine WHERE-Klausel zu bauen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Component
@Slf4j
public class PredicateBuilder {
    /**
     * Prädikate durch QueryDSL für eine WHERE-Klausel zu bauen.
     *
     * @param queryParams als MultiValueMap
     * @return Predicate in QueryDSL für eine WHERE-Klausel
     */
    @SuppressWarnings("ReturnCount")
    public Optional<Predicate> build(final Map<String, ? extends List<String>> queryParams) {
        log.debug("build: queryParams={}", queryParams);

        final var qKunde = QKunde.kunde;
        final var booleanExprList = queryParams
            .entrySet()
            .stream()
            .map(entry -> toBooleanExpression(entry.getKey(), entry.getValue(), qKunde))
            .toList();
        if (booleanExprList.isEmpty() || booleanExprList.contains(null)) {
            return Optional.empty();
        }

        final var result = booleanExprList
            .stream()
            .reduce(booleanExprList.get(0), BooleanExpression::and);
        return Optional.of(result);
    }

    @SuppressWarnings({"CyclomaticComplexity", "UnnecessaryParentheses"})
    private BooleanExpression toBooleanExpression(
        final String paramName,
        final List<String> paramValues,
        final QKunde qKunde
    ) {
        log.trace("toSpec: paramName={}, paramValues={}", paramName, paramValues);

        if (paramValues == null || (!Objects.equals(paramName, "interesse") && paramValues.size() != 1)) {
            return null;
        }

        final var value = paramValues.get(0);
        return switch (paramName) {
            case "nachname" -> nachname(value, qKunde);
            case "email" ->  email(value, qKunde);
            case "kategorie" -> kategorie(value, qKunde);
            case "newsletter" -> newsletter(value, qKunde);
            case "geschlecht" -> geschlecht(value, qKunde);
            case "familienstand" -> familienstand(value, qKunde);
            case "interesse" -> interessen(paramValues, qKunde);
            case "plz" -> plz(value, qKunde);
            case "ort" -> ort(value, qKunde);
            default -> null;
        };
    }

    private BooleanExpression nachname(final String teil, final QKunde qKunde) {
        return qKunde.nachname.toLowerCase().matches("%" + teil.toLowerCase(GERMAN) + '%');
    }

    private BooleanExpression email(final String teil, final QKunde qKunde) {
        return qKunde.email.toLowerCase().matches("%" + teil.toLowerCase(GERMAN) + '%');
    }

    private BooleanExpression kategorie(final String kategorie, final QKunde qKunde) {
        final int kategorieInt;
        try {
            kategorieInt = Integer.parseInt(kategorie);
        } catch (final NumberFormatException e) {
            //noinspection ReturnOfNull
            return null;
        }
        return qKunde.kategorie.eq(kategorieInt);
    }

    private BooleanExpression newsletter(final String hasNewsletter, final QKunde qKunde) {
        return qKunde.hasNewsletter.eq(Boolean.parseBoolean(hasNewsletter));
    }

    private BooleanExpression geschlecht(final String geschlecht, final QKunde qKunde) {
        return qKunde.geschlecht.eq(GeschlechtType.of(geschlecht).orElse(null));
    }

    private BooleanExpression familienstand(final String familienstand, final QKunde qKunde) {
        return qKunde.familienstand.eq(FamilienstandType.of(familienstand).orElse(null));
    }

    private BooleanExpression interessen(final Collection<String> interessen, final QKunde qKunde) {
        if (interessen == null || interessen.isEmpty()) {
            return null;
        }

        final var expressionList = interessen
            .stream()
            .map(interesseStr -> InteresseType.of(interesseStr).orElse(null))
            .filter(Objects::nonNull)
            .map(interesse -> qKunde.interessenStr.matches("%" + interesse.name() + '%'))
            .toList();
        if (expressionList.isEmpty()) {
            return null;
        }

        return expressionList
            .stream()
            .reduce(expressionList.get(0), BooleanExpression::and);
    }

    private BooleanExpression plz(final String prefix, final QKunde qKunde) {
        return qKunde.adresse.plz.matches(prefix + '%');
    }

    private BooleanExpression ort(final String prefix, final QKunde qKunde) {
        return qKunde.adresse.ort.toLowerCase().matches("%" + prefix.toLowerCase(GERMAN) + '%');
    }
}
