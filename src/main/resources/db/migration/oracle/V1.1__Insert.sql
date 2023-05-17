-- Copyright (C) 2022 - present Juergen Zimmermann, Hochschule Karlsruhe
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- This program is distributed in the hope that it will be useful,
-- but WITHOUT ANY WARRANTY; without even the implied warranty of
-- MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
-- GNU General Public License for more details.
--
-- You should have received a copy of the GNU General Public License
-- along with this program.  If not, see <https://www.gnu.org/licenses/>.

-- docker compose exec oracle sqlplus kunde/p@XEPDB1

INSERT ALL
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000000','admin','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','ADMIN,KUNDE,ACTUATOR')
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000001','alpha','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','KUNDE')
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000020','alpha2','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','KUNDE')
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000030','alpha3','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','KUNDE')
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000040','delta','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','KUNDE')
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000050','epsilon','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','KUNDE')
  INTO login (id, username, password, rollen)
    VALUES ('30000000-0000-0000-0000-000000000060','phi','{argon2id}$argon2id$v=19$m=16384,t=3,p=1$QHb5SxDhddjUiGboXTc9S9yCmRoPsBejIvW/dw50DKg$WXZDFJowwMX5xsOun2BT2R3hv2aA9TSpnx3hZ3px59sTW0ObtqBwX7Sem6ACdpycArUHfxmFfv9Z49e7I+TI/g','KUNDE')

  -- admin
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000000',0,'Admin','admin@acme.com',0,1,TO_DATE('2022-01-31 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.com','WEIBLICH','VERHEIRATET','LESEN','admin',TO_TIMESTAMP('2022-01-31 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-31 12:00:00', 'yyyy-MM-dd hh:mi:ss'))
  -- HTTP GET
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000001',0,'Alpha','alpha@acme.de',1,1,TO_DATE('2022-01-01 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.de','MAENNLICH','LEDIG','SPORT,LESEN','alpha',TO_TIMESTAMP('2022-01-01 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-01 12:00:00', 'yyyy-MM-dd hh:mi:ss'))
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000020',0,'Alpha','alpha@acme.edu',2,1,TO_DATE('2022-01-02 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.edu','WEIBLICH','GESCHIEDEN',null,'alpha2',TO_TIMESTAMP('2022-01-02 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-02 12:00:00', 'yyyy-MM-dd hh:mi:ss'))
  -- HTTP PUT
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000030',0,'Alpha','alpha@acme.ch',3,1,TO_DATE('2022-01-03 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.ch','MAENNLICH','VERWITWET','SPORT,REISEN','alpha3',TO_TIMESTAMP('2022-01-03 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-03 12:00:00', 'yyyy-MM-dd hh:mi:ss'))
  -- HTTP PATCH
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000040',0,'Delta','delta@acme.uk',4,1,TO_DATE('2022-01-04 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.uk','WEIBLICH','VERHEIRATET','LESEN,REISEN','delta',TO_TIMESTAMP('2022-01-04 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-04 12:00:00', 'yyyy-MM-dd hh:mi:ss'))
  -- HTTP DELETE
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000050',0,'Epsilon','epsilon@acme.jp',5,1,TO_DATE('2022-01-05 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.jp','MAENNLICH','LEDIG',null,'epsilon',TO_TIMESTAMP('2022-01-05 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-05 12:00:00', 'yyyy-MM-dd hh:mi:ss'))
  -- zur freien Verfuegung
  INTO kunde (id, version, nachname, email, kategorie, has_newsletter, geburtsdatum, homepage, geschlecht, familienstand, interessen, username, erzeugt, aktualisiert)
    VALUES ('00000000-0000-0000-0000-000000000060',0,'Phi','phi@acme.cn',6,1,TO_DATE('2022-01-06 12:00:00', 'yyyy-MM-dd hh:mi:ss'),'https://www.acme.cn','DIVERS','LEDIG',null,'phi',TO_TIMESTAMP('2022-01-06 12:00:00', 'yyyy-MM-dd hh:mi:ss'),TO_TIMESTAMP('2022-01-06 12:00:00', 'yyyy-MM-dd hh:mi:ss'))

  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000000','00000','Aachen','00000000-0000-0000-0000-000000000000')
  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000001','11111','Augsburg','00000000-0000-0000-0000-000000000001')
  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000020','22222','Aalen','00000000-0000-0000-0000-000000000020')
  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000030','33333','Ahlen','00000000-0000-0000-0000-000000000030')
  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000040','44444','Dortmund','00000000-0000-0000-0000-000000000040')
  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000050','55555','Essen','00000000-0000-0000-0000-000000000050')
  INTO adresse (id, plz, ort, kunde_id)
    VALUES ('20000000-0000-0000-0000-000000000060','66666','Freiburg','00000000-0000-0000-0000-000000000060')

  INTO umsatz (id, betrag, waehrung, kunde_id, idx)
    VALUES ('10000000-0000-0000-0000-000000000000',0,'EUR','00000000-0000-0000-0000-000000000000', 0)
  INTO umsatz (id, betrag, waehrung, kunde_id, idx)
    VALUES ('10000000-0000-0000-0000-000000000001',10,'EUR','00000000-0000-0000-0000-000000000001', 0)
  INTO umsatz (id, betrag, waehrung, kunde_id, idx)
    VALUES ('10000000-0000-0000-0000-000000000020',20,'USD','00000000-0000-0000-0000-000000000020', 0)
  INTO umsatz (id, betrag, waehrung, kunde_id, idx)
    VALUES ('10000000-0000-0000-0000-000000000030',30,'CHF','00000000-0000-0000-0000-000000000030', 0)
  INTO umsatz (id, betrag, waehrung, kunde_id, idx)
    VALUES ('10000000-0000-0000-0000-000000000031',31,'CHF','00000000-0000-0000-0000-000000000030', 1)
  INTO umsatz (id, betrag, waehrung, kunde_id, idx)
    VALUES ('10000000-0000-0000-0000-000000000040',40,'GBP','00000000-0000-0000-0000-000000000040', 0)

SELECT * FROM dual;
