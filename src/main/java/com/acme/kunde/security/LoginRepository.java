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

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository, um Benutzerkennungen zu suchen und für den Service bereitzustellen.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Repository
public interface LoginRepository extends JpaRepository<Login, UUID> {
    /**
     * Zu einem gegebenen Username wird das zugehörige Login-Objekt gesucht.
     *
     * @param username Username des gesuchten Login-Objekts
     * @return Das gesuchte Login-Objekt in einem ggf. leeren Optional
     */
    Optional<Login> findByUsername(String username);

    /**
     * Prüfung, ob es bereits einen User mit gegebenem Benutzernamen gibt.
     *
     * @param username Der zu überprüfende Benutzername.
     * @return true, falls es bereits den Benutzernamen gibt, false sonst.
     */
    @SuppressWarnings("BooleanMethodNameMustStartWithQuestion")
    boolean existsByUsername(String username);
}
