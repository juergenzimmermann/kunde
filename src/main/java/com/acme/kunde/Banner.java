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
package com.acme.kunde;

import com.github.lalyos.jfiglet.FigletFont;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Objects;
import org.springframework.boot.SpringBootVersion;
import org.springframework.core.SpringVersion;
import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * Banner als String-Konstante für den Start des Servers.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@SuppressWarnings({
    "AccessOfSystemProperties",
    "CallToSystemGetenv",
    "UtilityClassCanBeEnum",
    "UtilityClass",
    "ClassUnconnectedToPackage"
})
@SuppressFBWarnings("NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE")
final class Banner {
    private static final String JAVA = Runtime.version().toString() + " - " + System.getProperty("java.vendor");
    private static final String OS_VERSION = System.getProperty("os.name");
    private static final InetAddress LOCALHOST = getLocalhost();
    private static final long MEGABYTE = 1024L * 1024L;
    private static final Runtime RUNTIME = Runtime.getRuntime();
    private static final String SERVICE_HOST = System.getenv("KUNDE_SERVICE_HOST");
    private static final String SERVICE_PORT = System.getenv("KUNDE_SERVICE_PORT");
    private static final String KUBERNETES = SERVICE_HOST == null
        ? "N/A"
        : "KUNDE_SERVICE_HOST=" + SERVICE_HOST + ", KUNDE_SERVICE_PORT=" + SERVICE_PORT;
    private static final String USERNAME = System.getProperty("user.name");

    /**
     * Banner für den Server-Start.
     */
    static final String TEXT = """

        $figlet
        (C) Juergen Zimmermann, Hochschule Karlsruhe
        Version             2023.1.1
        Spring Boot         $springBoot
        Spring Security     $springSecurity
        Spring Framework    $spring
        Hibernate           $hibernate
        Java                $java
        Betriebssystem      $os
        Rechnername         $rechnername
        IP-Adresse          $ip
        Heap: Size          $heapSize MiB
        Heap: Free          $heapFree MiB
        Kubernetes          $kubernetes
        Username            $username
        JVM Locale          $locale
        GraphiQL            /graphiql   {"Authorization": "Basic YWRtaW46cA=="}
        OpenAPI             /swagger-ui.html /v3/api-docs.yaml
        H2 Console          $h2Console
        """
        .replace("$figlet", getFiglet())
        .replace("$springBoot", SpringBootVersion.getVersion())
        .replace("$springSecurity", SpringSecurityCoreVersion.getVersion())
        .replace("$spring", Objects.requireNonNull(SpringVersion.getVersion()))
        .replace("$hibernate", org.hibernate.Version.getVersionString())
        .replace("$java", JAVA)
        .replace("$os", OS_VERSION)
        .replace("$rechnername", LOCALHOST.getHostName())
        .replace("$ip", LOCALHOST.getHostAddress())
        .replace("$heapSize", String.valueOf(RUNTIME.totalMemory() / MEGABYTE))
        .replace("$heapFree", String.valueOf(RUNTIME.freeMemory() / MEGABYTE))
        .replace("$kubernetes", KUBERNETES)
        .replace("$username", USERNAME)
        .replace("$locale", Locale.getDefault().toString())
        .replace("$h2Console", getH2Console());

    @SuppressWarnings("ImplicitCallToSuper")
    private Banner() {
    }

    private static String getFiglet() {
        try {
            return FigletFont.convertOneLine("kunde v2");
        } catch (final IOException ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    private static InetAddress getLocalhost() {
        try {
            return InetAddress.getLocalHost();
        } catch (final UnknownHostException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static String getH2Console() {
        return Objects.equals(System.getProperty("spring.h2.console.enabled"), "true") ||
            Objects.equals(System.getenv("SPRING_H2_CONSOLE_ENABLED"), "true") ? "/h2-console" : "N/A";
    }
}
