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
package com.acme.kunde.dev;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

/**
 * WebFilter zur Protokollierung des Request-Headers.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
interface LogRequestHeaders {
    /**
     * WebFilter zur Protokollierung des Request-Headers.
     *
     * @return CommonsRequestLoggingFilter, der den Request-Header protokolliert.
     */
    @Bean
    default CommonsRequestLoggingFilter logFilter() {
        // https://www.baeldung.com/spring-http-logging
        // https://stackoverflow.com/questions/33744875...
        // .../spring-boot-how-to-log-all-requests-and-responses-with-exceptions-in-single-pl
        // https://github.com/zalando/logbook
        final var filter = new CommonsRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludeHeaders(true);
        return filter;
    }
}
