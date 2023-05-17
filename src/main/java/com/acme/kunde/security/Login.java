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
package com.acme.kunde.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import static java.util.Collections.emptyList;

/**
 * Entity-Klasse, um Benutzerkennungen bestehend aus Benutzername,
 * Passwort und Rollen zu repräsentieren, die in der DB verwaltet
 * werden.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Entity
@Table(name = "login")
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@Getter
@Setter
@ToString
@SuppressWarnings("MissingSummary")
public class Login {
    @Id
    @GeneratedValue
    // Oracle: https://in.relation.to/2022/05/12/orm-uuid-mapping
    // @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @EqualsAndHashCode.Include
    private UUID id;

    private String username;

    private String password;

    @Transient
    private List<Rolle> rollen;

    @Column(name = "rollen")
    private String rollenStr;

    /**
     * Konvertierungsfunktion, um ein User-Objekt aus der DB in ein User-Objekt für Spring Security zu konvertieren.
     *
     * @return Ein Objekt von [CustomUser] für Spring Security
     */
    UserDetails toUserDetails() {
        final List<SimpleGrantedAuthority> authorities = rollen == null || rollen.isEmpty()
            ? emptyList()
            : rollen.stream()
                .map(rolle -> new SimpleGrantedAuthority(Rolle.ROLE_PREFIX + rolle))
                .toList();
        return new CustomUser(username, password, authorities);
    }

    @PrePersist
    private void buildRollenStr() {
        final var rollenStrList = rollen.stream().map(Enum::name).toList();
        rollenStr = String.join(",", rollenStrList);
    }

    @PostLoad
    private void loadRollen() {
        final var rollenArray = rollenStr.split(",");
        rollen = Arrays.stream(rollenArray).map(Rolle::valueOf).toList();
    }
}
