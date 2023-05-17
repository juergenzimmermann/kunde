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
package com.acme.kunde.mail;

import com.acme.kunde.MailProps;
import com.acme.kunde.entity.Kunde;
import jakarta.mail.internet.InternetAddress;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;
import static jakarta.mail.Message.RecipientType.TO;

/**
 * Mail-Client.
 *
 * @author <a href="mailto:Juergen.Zimmermann@h-ka.de">Jürgen Zimmermann</a>
 */
@Service
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("ClassNamePrefixedWithPackageName")
public class Mailer {
    private static final boolean SMTP_DEACTIVATED = Objects.equals(System.getenv("SMTP_DEACTIVATED"), "true") ||
        Objects.equals(System.getProperty("smtp-deactivated"), "true");

    private final JavaMailSender mailSender;
    private final MailProps props;

    /**
     * Email senden, dass es einen neuen Kunden gibt.
     *
     * @param neuerKunde Das Objekt des neuen Kunden.
     */
    public void send(final Kunde neuerKunde) {
        if (SMTP_DEACTIVATED) {
            log.warn("SMTP ist deaktiviert.");
        }
        final MimeMessagePreparator preparator = mimeMessage -> {
            mimeMessage.setFrom(new InternetAddress(props.from()));
            mimeMessage.setRecipient(TO, new InternetAddress(props.sales()));
            mimeMessage.setSubject("Neuer Kunde " + neuerKunde.getId());
            final var body = "<strong>Neuer Kunde:</strong> <em>" + neuerKunde.getNachname() + "</em>";
            log.trace("send: body={}", body);
            mimeMessage.setText(body);
            mimeMessage.setHeader("Content-Type", "text/html");
        };

        try {
            mailSender.send(preparator);
        } catch (final MailSendException | MailAuthenticationException e) {
            // TODO Wiederholung, um die Email zu senden
            log.warn("Email nicht gesendet: Ist der Mailserver erreichbar?");
        }
    }
}
