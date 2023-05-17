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

--
-- docker compose exec oracle sqlplus kunde/p@XEPDB1 '@/sql/create-schema-kunde.sql'
--

-- SELECT sys_context( 'userenv', 'current_schema' ) FROM dual;

-- https://docs.oracle.com/en/database/oracle/oracle-database/21/sqlrf/CREATE-SCHEMA.html
-- Schema-Name wird auf den Namen des DB-Users gesetzt
CREATE SCHEMA AUTHORIZATION kunde;

exit
