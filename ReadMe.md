# Hinweise zum Programmierbeispiel

[Juergen Zimmermann](mailto:Juergen.Zimmermann@h-ka.de)

> Diese Datei ist in Markdown geschrieben und kann z.B. mit IntelliJ IDEA
> gelesen werden. Näheres zu Markdown gibt es z.B. bei
> [Markdown Guide](https://www.markdownguide.org) oder
> [JetBrains](https://www.jetbrains.com/help/hub/Markdown-Syntax.html)

> Bevor man mit der Projektarbeit an der 2. Abgabe beginnt, sichert man sich
> die 1. Abgabe, u.a. weil für die 2. Abgabe auch die Original-Implementierung
> aus der 1. Abgabe benötigt wird.

Inhalt

- [Eigener Namespace in Kubernetes](#Eigener-Namespace-in-Kubernetes)
- [Relationale Datenbanksysteme](#Relationale-Datenbanksysteme)
  - [PostgreSQL](#PostgreSQL)
  - [MySQL](#MySQL)
  - [Oracle](#Oracle)
- [Administration des Kubernetes-Clusters](#Administration-des-Kubernetes-Clusters)
- [Übersetzung und lokale Ausführung](#Übersetzung-und-lokale-Ausführung)
  - [Ausführung in IntelliJ IDEA](#Ausführung-in-IntelliJ-IDEA)
  - [Start und Stop des Servers in der Kommandozeile](#Start-und-Stop-des-Servers-in-der-Kommandozeile)
  - [Image erstellen](#Image-erstellen)
- [HTTP Client von IntelliJ IDEA](#HTTP-Client-von-IntelliJ-IDEA)
- [OpenAPI mit Swagger](#OpenAPI-mit-Swagger)
- [Unit Tests und Integrationstests](#Unit-Tests-und-Integrationstests)
- [Rechnername in der Datei hosts](#Rechnername-in-der-Datei-hosts)
- [Kubernetes, Helm und Skaffold](#Kubernetes,-Helm-und-Skaffold)
  - [WICHTIG: Schreibrechte für die Logdatei](#WICHTIG:-Schreibrechte-für-die-Logdatei)
  - [Image für Kubernetes erstellen](#Image-für-Kubernetes-erstellen)
  - [Helm als Package Manager für Kubernetes](#Helm-als-Package-Manager-für-Kubernetes)
  - [Installation mit helmfile und Port Forwarding](#Installation-mit-helmfile-und-Port-Forwarding)
  - [Continuous Deployment mit Skaffold](#Continuous-Deployment-mit-Skaffold)
  - [kubectl top](#kubectl-top)
  - [Validierung der Installation](#Validierung-der-Installation)
- [Statische Codeanalyse](#Statische-Codeanalyse)
  - [ktlint und Detekt](#ktlint-und-Detekt)
  - [SonarQube](#SonarQube)
- [Dokumentation](#Dokumentation)
  - [Dokumentation durch AsciiDoctor und PlantUML](#Dokumentation-durch-AsciiDoctor-und-PlantUML)
  - [API Dokumentation durch javadoc](#API-Dokumentation-durch-javadoc)
- [Einfache Lasttests mit Fortio](#Einfache-Lasttests-mit-Fortio)

---

## Eigener Namespace in Kubernetes

Genauso wie in Datenbanksystemen gibt es in Kubernetes _keine_ untergeordneten
Namespaces. Vor allem ist es in Kubernetes empfehlenswert für die eigene
Software einen _neuen_ Namespace anzulegen und __NICHT__ den Default-Namespace
zu benutzen. Das wurde bei der Installation von Kubernetes durch den eigenen
Namespace `acme` bereits erledigt. Außerdem wurde aus Sicherheitsgründen beim
defaultmäßigen Service-Account das Feature _Automounting_ deaktiviert und der
Kubernetes-Cluster wurde intern defaultmäßig so abgesichert, dass

- über das Ingress-Gateway keine Requests von anderen Kubernetes-Services zulässig sind
- über das Egress-Gateway keine Requests an andere Kubernetes-Services zulässig sind.

---

## Relationale Datenbanksysteme

### PostgreSQL

#### Docker Compose für PostgreSQL und pgadmin

Wenn man den eigenen Microservice direkt mit Windows - nicht mit Kubernetes -
laufen lässt, kann man PostgreSQL und das Administrationswerkzeug pgadmin
einfach mit _Docker Compose_ starten und später auch herunterfahren.

> ❗ Vor dem 1. Start von PostgreSQL muss das Skript `create-db-kunde.sql`
> aus dem Verzeichnis `extras\sql\postgres` nach
> `C:\Zimmermann\volumes\postgres\sql` kopieren und die Anleitung ausführen.
> Für die Windows-Verzeichnisse `C:\Zimmermann\volumes\postgres\data`,
> `C:\Zimmermann\volumes\postgres\tablespace` und
> `C:\Zimmermann\volumes\postgres\tablespace\kunde` muss außerdem Vollzugriff
> gewährt werden, was über das Kontextmenü mit _Eigenschaften_ und den
> Karteireiter _Sicherheit_ für die Windows-Gruppe _Benutzer_ eingerichtet
> werden kann.
> Übrigens ist das Emoji für das Ausrufezeichen von https://emojipedia.org.

```powershell
    # User "postgres" in docker-compose.yaml vor dem 1. Start auskommentieren
    docker compose up postgres pgadmin

    # 2. Shell: DB, DB-User und Tablespace anlegen
    docker compose exec postgres bash
      chown postgres:postgres /var/lib/postgresql/tablespace
      chown postgres:postgres /var/lib/postgresql/tablespace/kunde
    docker compose down
    # in docker-compose.yaml den User "postgres" wieder aktivieren, d.h. Kommentar entfernen
    # 1. Shell:
    docker compose up postgres
    # 2. Shell:
    docker compose exec postgres bash
      psql --dbname=postgres --username=postgres --file=/sql/create-db-kunde.sql

    # 2. Shell: Herunterfahren
    docker compose down
```

Der Name des Docker-Containers lautet `postgres` und ebenso lautet der
_virtuelle Rechnername_ `postgres`. Der virtuelle Rechnername `postgres`
wird später auch als Service-Name für PostgreSQL in Kubernetes verwendet.
Der neue Datenbank-User `kunde` wurde zum Owner der Datenbank `kunde`.

Statt eine PowerShell zu verwenden, kann man Docker Compose auch direkt in
IntelliJ aufrufen, indem man im Tool-Window _Services_ unterhalb von _Docker_
und dem Eintrag _Docker-compose: kunde_ den Service _postgres_ durch den
grünen Start-Button am linken Rand startet.

Jetzt läuft der PostgreSQL- bzw. DB-Server. Die Datenbank-URL für den eigenen
Microservice als DB-Client lautet: `postgresql://localhost/kunde`, dabei ist
`localhost` aus Windows-Sicht der Rechnername, der Port defaultmäßig `5432`
und der Datenbankname `kunde`.

Außerdem kann _pgadmin_ zur Administration verwendet werden. pgadmin läuft
ebenfalls als Docker-Container und ist über ein virtuelles Netzwerk mit dem
Docker-Container des DB-Servers verbunden. Deshalb muss beim Verbinden mit dem
DB-Server auch der virtuelle Rechnername `postgres` statt `localhost` verwendet
werden. pgadmin kann man mit einem Webbrowser und der URL `http://localhost:8888`
aufrufen. Die Emailadresse `pgadmin@acme.com` und das Passwort `p` sind voreingestellt.
Da pgadmin ist übrigens mit Chromium implementiert ist.

Beim 1. Einloggen konfiguriert man einen Server-Eintrag mit z.B. dem Namen
`localhost` und verwendet folgende Werte:

- Host: `postgres` (virtueller Rechnername des DB-Servers im Docker-Netzwerk.
  __BEACHTE__: `localhost` ist im virtuellen Netzwerk der Name des
  pgadmin-Containers selbst !!!)
- Port: `5432` (Defaultwert)
- Username: `postgres` (Superuser beim DB-Server)
- Password: `p`

Es empfiehlt sich, das Passwort abzuspeichern, damit man es künftig nicht jedes
Mal beim Einloggen eingeben muss.

#### Skaffold für PostgreSQL und pgadmin

Wenn der eigene Microservice in Kubernetes gestartet werden soll (s.u.), muss
_PostgreSQL_ zuvor in Kubernetes gestartet werden, was mit _Skaffold_ gemacht
werden kann. Wenn die Umgebungsvariable `SKAFFOLD_PROFILE` auf den Wert `dev`
gesetzt ist, dann wird das Profile `dev` verwendet, welches bei Helm zusätzlich
die Datei `dev.yaml` verwendet. Bis der Endpoint für PostgreSQL aktiviert ist,
muss man ein bisschen warten.

```powershell
    cd extras\postgres
    skaffold dev --no-prune=false --cache-artifacts=false
    <Strg>C
    skaffold delete
```

Dabei wurde auch das Administrationswerkzeug _pgadmin_ innerhalb von Kubernetes
gestartet und kann wegen Port-Forwarding mit `http://localhost:8888` aufgerufen
werden.

Mit `<Strg>C` kann die Installation wieder zurückgerollt werden und man ruft
abschließend `skaffold delete` auf.

Ohne die beiden Optionen muss man noch manuell die _PersistentVolumeClaims_
löschen, da bei `metadata.finalizers` der Wert auf `kubernetes.io/pvc-protection`
gesetzt ist und auch durch `kubectl patch pvc <PVC_NAME> -p '{"metadata":{"finalizers": []}}' --type=merge`
nicht entfernt werden kann. Dazu gibt es das PowerShell-Skript `delete-pvc.ps1`
im Verzeichnis `extras\postgres`.

#### helmfile für PostgreSQL und pgadmin

Statt _Skaffold_ kann man auch _helmfile_ mit manuellem Port-Forwarding verwenden:

```powershell
    cd extras\postgres
    helmfile apply
    .\port-forward.ps1

    # Deinstallieren
    helmfile destroy
    .\delete-pvc.ps1
```

---

### MySQL

#### Docker Compose für MySQL und phpMyAdmin

Wenn man den eigenen Microservice direkt mit Windows - nicht mit Kubernetes -
laufen lässt, kann man MySQL und das Administrationswerkzeug phpMyAdmin einfach
mit _Docker Compose_ starten und später auch herunterfahren.

> ❗ Vor dem 1. Start von MySQL muss man das Skript `create-db-kunde.sql` aus
> dem Projektverzeichnis `extras\sql\mysql` nach
> `C:\Zimmermann\volumes\mysql\sql` kopieren und die Anleitung ausführen.
> Dabei wird der DB-User `kunde` und dessen Datenbank `kunde` angelegt, d.h.
> der neue Datenbank-User `kunde` wird zum Owner der Datenbank `kunde`.
> Dazu muss man sich mit dem Docker-Container mit Namen `mysql` verbinden und
> im Docker-Container das SQL-Skript ausführen:

```powershell
    docker compose up mysql phpmyadmin

    # 2. Shell: DB-User "kunde" und dessen Datenbank "kunde" anlegen
    docker compose exec mysql sh
      mysql --user=root --password=p < /sql/create-db-kunde.sql

    # 2. Shell: Herunterfahren
    docker compose down
```

Statt eine PowerShell zu verwenden, kann man Docker Compose auch direkt in
IntelliJ aufrufen, indem man im Tool-Window _Services_ unterhalb von _Docker_
und dem Eintrag _Docker-compose: kunde_ den Service _mysql_ durch den
grünen Start-Button am linken Rand startet.

Jetzt läuft der DB-Server. Die Datenbank-URL für den eigenen Microservice als
DB-Client lautet: `mysql://localhost/kunde`. Dabei ist `localhost` aus
Windows-Sicht der Rechnername, der Port defaultmäßig `3306` und der
Datenbankname `kunde`.

Außerdem kann _phpMyAdmin_ oder _dbeaver_ zur Administration verwendet werden.
phpMyAdmin läuft ebenfalls als Docker-Container und ist über ein virtuelles
Netzwerk mit dem Docker-Container des DB-Servers verbunden. Deshalb muss beim
Verbinden mit dem DB-Server auch der virtuelle Rechnername `mysql` verwendet werden.
phpMyAdmin ruft man mit einem Webbrowser und der URL `http://localhost:8889`
auf. Zum Einloggen verwendet man folgende Werte:

- Server: `mysql` (virtueller Rechnername des DB-Servers im Docker-Netzwerk.
  __BEACHTE__: `localhost` ist im virtuellen Netzwerk der Name des
  phpMyAdmin-Containers selbst !!!)
- Benutzername: `root` (Superuser beim DB-Server)
- Password: `p`

#### Skaffold für MySQL und phpMyAdmin

Wenn der eigene Microservice in Kubernetes gestartet werden soll (s.u.), muss
_MySQL_ zuvor in Kubernetes gestartet werden, was mit _Skaffold_ gemacht werden
kann. Wenn die Umgebungsvariable `SKAFFOLD_PROFILE` auf den Wert `dev`
gesetzt ist, dann wird das Profile `dev` verwendet, welches bei Helm zusätzlich
die Datei `dev.yaml` verwendet. Bis der Endpoint für MySQL aktiviert ist,
muss man ein bisschen warten.

```powershell
    cd extras\mysql
    skaffold dev --no-prune=false --cache-artifacts=false
    <Strg>C
    skaffold delete
```

Dabei wurde auch das Administrationswerkzeug _phpMyAdmin_ innerhalb von Kubernetes
gestartet und kann wegen Port-Forwarding mit `http://localhost:8889` aufgerufen
werden.

Mit `<Strg>C` kann die Installation wieder zurückgerollt werden.

Ohne die beiden Optionen muss man noch manuell die _PersistentVolumeClaims_
löschen, da bei `metadata.finalizers` der Wert auf `kubernetes.io/pvc-protection`
gesetzt ist und auch durch `kubectl patch pvc <PVC_NAME> -p '{"metadata":{"finalizers": []}}' --type=merge`
nicht entfernt werden kann. Dazu gibt es das PowerShell-Skript `delete-pvc.ps1`
im Verzeichnis `extras\mysql`.


#### helmfile für MySQL und phpMyAdmin

Statt _Skaffold_ kann man auch _helmfile_ mit manuellem Port-Forwarding verwenden:

```powershell
    cd extras\mysql
    helmfile apply
    .\port-forward.ps1

    # Deinstallieren
    helmfile destroy
    .\delete-pvc.ps1
```

---

### Oracle

#### Docker Compose für Oracle

Wenn man den eigenen Microservice direkt mit Windows - nicht mit Kubernetes -
laufen lässt, kann man Oracle einfach mit _Docker Compose_ starten und später
auch herunterfahren.

> ❗ Das erstmalige Hochfahren von Oracle XE kann einige Minuten dauern.
> Dabei werden auch die beiden üblichen Oracle-User `SYS` und `SYSTEM` jeweils
> mit dem Passwort `p` angelegt.

```powershell
    docker compose up

    # Herunterfahren in einer 2. Shell:
    docker compose down
```

Der Name des Docker-Containers und des _virtuellen Rechners_ lautet `oracle`.
Der virtuelle Rechnername wird später auch als Service-Name für
Oracle in Kubernetes verwendet.

> ❗ Nach dem 1. Start des DB-Servers muss man einmalig den Datenbank-User
> `kunde`, den Tablespace `kundespace` und das Schema `kunde` für den gleichnamigen
> User anlegen. Dazu muss man ggf. _SQLcl_ von https://www.oracle.com/de/tools/downloads/sqlcl-downloads.html
> herunterladen und die ZIP-Datei in `C:\Zimmermann` auspacken, so dass es die
> Datei `C:\Zimmermann\sqlcl\bin\sql.exe` gibt. Außerdem muss man die Umgebungsvariable
> `PATH` um `C:\Zimmermann\sqlcl\bin` ergänzen. Danach kann man folgendes PowerShell-Skript
> ausführen:

```powershell
    cd extras\oracle
    .\create-kunde.ps1
```

Statt eine PowerShell zu verwenden, kann man Docker Compose auch direkt in
IntelliJ aufrufen, indem man im Tool-Window _Services_ unterhalb von _Docker_
und dem Eintrag _Docker-compose: kunde_ den Service _oracle_ durch den
grünen Start-Button am linken Rand startet.

Die Datenbank-URL für den eigenen Microservice und auch für _SQL Developer_
als grafischen DB-Client lautet: `oracle:thin:kunde/p@localhost/XEPDB1`.
Dabei ist

- `kunde` der Benutzername,
- `p` das Passwort
- `localhost` aus Windows-Sicht der Rechnername
- der Port defaultmäßig `1521` und
- `XEPDB1` (XE Portable Database) der Name der Default-Datenbank nach dem 1. Start.

---

## Administration des Kubernetes Clusters

Zur Administration des Kubernetes-Clusters ist für Entwickler*innen m.E. _Lens_
oder _Octant_ von VMware Tanzu oder _Kui_ von IBM gut geeignet.

---

## Übersetzung und lokale Ausführung

### Ausführung in IntelliJ IDEA

In der Menüleiste am rechten Rand ist ein Auswahlmenü, in dem i.a. _Application_
zu sehen ist. In diesem Auswahlmenü wählt man den ersten Menüpunkt
`Edit Configurations ...` aus.

Falls das Label _Program Arguments_ nicht sichtbar ist, kann man es
aktivieren, indem man auf _Modify options_ klickt und im Abschnitt _Java_
_Program Arguments_ auswählt. Dann trägt man Folgendes ein:

```Text
  --spring.output.ansi.enabled=ALWAYS --spring.config.location=classpath:/application.yml --spring.profiles.default=dev --spring.profiles.active=dev
```

Falls das Label _Environment variables_ nicht sichtbar ist, kann man es
aktivieren, indem man auf _Modify options_ klickt und im Abschnitt _Operating
System_ auswählt. Dann trägt man folgenden String ein:

```Text
    LOG_PATH=./build/log;APPLICATION_LOGLEVEL=trace;HIBERNATE_LOGLEVEL=debug
```

Wenn man für z.B. die Interaktion mit dem 2. Microservice "bestellung"
TLS deaktivieren möchte, dann gibt man im Eingabefeld _Program Arguments_ noch
zusätzlich folgenden String ein:

```Text
    --server.http2.enabled=false --server.ssl.enabled=false
```

Falls man den Port z.B. von `8080` auf `8081` beim Microservice "bestellung"
ändern möchte, dann ergänzt man außerdem noch ` --server.port=8081`.

### Ausführung in IntelliJ IDEA statt in der Kommandozeile

In der Menüleiste am rechten Rand ist ein Auswahlmenü, in dem i.a. _Application_
zu sehen ist. In diesem Auswahlmenü wählt man den ersten Menüpunkt
`Edit Configurations ...` aus.

Falls das Label _Program Arguments_ nicht sichtbar ist, kann man es
aktivieren, indem man auf _Modify options_ klickt und im Abschnitt `Java`
auswählt. Dann trägt man Folgendes ein:

```Text
  --spring.output.ansi.enabled=ALWAYS --spring.config.location=classpath:/application.yml --spring.profiles.default=dev --spring.profiles.active=dev
```

Falls das Label _Environment variables_ nicht sichtbar ist, kann man es
aktivieren, indem man auf _Modify options_ klickt und im Abschnitt `Operating
System` auswählt. Dann trägt man die Variablen mit ihren Werten ein:

```Text
  `LOG_PATH=./build/log;APPLICATION_LOGLEVEL=trace;HIBERNATE_LOGLEVEL=debug`
```

---

### Start und Stop des Servers in der Kommandozeile

Nachdem das Port-Forwarding für den DB-Server aufgerufen wurde, kann man in einer
Powershell den Server mit dem Profil `dev` starten:

```powershell
    .\gradlew bootRun
    # MySQL statt PostgreSQL
    .\gradlew bootRun -Ddb=mysql
    # H2 statt PostgreSQL
    .\gradlew bootRun -Ddb=h2
```

Mit `<Strg>C` kann man den Server herunterfahren, weil in der Datei
`application.yml` im Verzeichnis `src\main\resources` _graceful shutdown_
konfiguriert ist.

Mit dem Kommando `.\gradlew bootRun -Dport=8081` kann man den Server auf Port
`8081` statt auf Port `8080` laufen lassen.

Mit dem Kommando `.\gradlew bootRun -DnoTls=true` kann man den Server ohne TLS
laufen lassen. Das ist insbesondere dann notwendig, wenn ein 2. Server getestet
werden soll und dieser ohne TLS läuft.

---

### Image erstellen

Bei Verwendung der Buildpacks werden ggf. einige Archive von Github heruntergeladen,
wofür es leider kein Caching gibt. Ein solches Image kann mit dem Linux-User `cnb`
gestartet werden. Mit der Task bootBuildImage kann man im Verzeichnis für das
Projekt "bestellung" ebenfalls ein Docker-Image erstellen.

```powershell
    .\gradlew bootBuildImage -Dtag='2.0.0'
```

Mit _dive_ kann man dann ein Docker-Image und die einzelnen Layer inspizieren:

```powershell
    cd \Zimmermann\dive
    .\dive juergenzimmermann\kunde:2.0.0
```

Statt _dive_ kann man auch das Tool Window _Services_ von IntelliJ IDEA verwenden.

Wenn ein Docker-Image mit Buildpacks gebaut wurde, kann man mit folgendem
Kommando inspizieren, mit welchen Software-Paketen es gebaut wurde:

```PowerSkell
    pack inspect juergenzimmermann/kunde:2.0.0
```

Mit der PowerShell kann man Docker-Images folgendermaßen auflisten und löschen,
wobei das Unterkommando `rmi` die Kurzform für `image rm` ist:

```powershell
    docker images | sort
    docker rmi myImage:myTag
```

Im Laufe der Zeit kann es immer wieder Images geben, bei denen der Name
und/oder das Tag `<none>` ist, sodass das Image nicht mehr verwendbar und
deshalb nutzlos ist. Solche Images kann man mit dem nachfolgenden Kommando
herausfiltern und dann unter Verwendung ihrer Image-ID, z.B. `9dd7541706f0`
löschen:

```powershell
    docker rmi 9dd7541706f0
```

### Docker Compose für einen Container mit dem eigenen Server

Wenn das Image gebaut ist, kann man durch _Docker Compose_ die Services für
den DB-Server, den DB-Browser und den eigenen Microservice auf einmal starten.
Dabei ist der Service _kunde_ so konfiguriert, dass er erst dann gestartet wird,
wenn der "healthcheck" des DB-Servers "ready" meldet.

```powershell
    docker compose up postgres pgadmin kunde
    # oder MySQL statt PostgreSQL
    docker compose up mysql phpmyadmin kunde
```

Wenn MySQL statt PostgreSQL verwendet wird, muss man zuvor in `docker-compose.env`
den Eintrag für die Umgebungsvariable `SPRING_DATASOURCE_URL` für MySQL umstellen.
Außerdem muss man den Eintrag für die Umgebungsvariable `SPRING_FLYWAY_TABLESPACE`
auskommentieren.

Wenn H2 als _Main Memory_-Datenbank verwendet werden soll, so müssen in `docker-compose.env`
die Einträge für `SPRING_DATASOURCE_...` und `SPRING_FLYWAY_TABLESPACE` auskommentiert
und die initial auskommentieren Einträge für H2 aktiviert werden.

Wenn man auch noch _FakeSMTP_ als Mailserver für die Entwicklung nutzen möchte, so
muss auch noch der Service _fakesmtp_ gestartet werden, z.B.

```powershell
    docker compose up postgres pgadmin fakestmp kunde
```

## HTTP Client von IntelliJ IDEA

Im Verzeichnis `extras\http-client` gibt es Dateien mit der Endung `.http`, in
denen HTTP-Requests vordefiniert sind. Diese kann man mit verschiedenen
Umgebungen ("environment") ausführen, z.B. für https oder für http.

Zertifikate für TLS bzw. HTTPS werden in IntelliJ IDEA gespeichert und können über
"File > Settings > Tools > Server Certificates" inspiziert werden. Siehe auch
https://intellij-support.jetbrains.com/hc/en-us/articles/206544519-Directories-used-by-the-IDE-to-store-settings-caches-plugins-and-logs

---

### OpenAPI mit Swagger

Mit der URL `https://localhost:8080/swagger-ui.html` kann man in einem
Webbrowser den RESTful Web Service über eine Weboberfläche nutzen, die
von _Swagger_ auf der Basis von der Spezifikation _OpenAPI_ generiert wurde.
Die _Swagger JSON Datei_ kann man mit `https://localhost:8080/v3/api-docs`
abrufen.

## Unit Tests und Integrationstests

Wenn der [PostgreSQL-Server](#PostgreSQL) erfolgreich gestartet ist und das
Port-Forwarding aktiv ist, können auch die Unit- und Integrationstests
gestartet werden.

```powershell
    .\gradlew test
```

Falls der Server mit MySQL getestet werden soll, muss man `-Ddb=mysql` ergänzen.

__WICHTIGER__ Hinweis zu den Tests für den zweiten Microservice, der den ersten
Microservice aufruft:

- Da die Tests direkt mit Windows laufen, muss Port-Forwarding für den
  aufzurufenden, ersten Microservice gestartet sein.
- Außerdem muss in `build.gradle.kts` innerhalb von `tasks.test` der Name der
  Umgebungsvariable `KUNDE_SERVICE_HOST` auf den Namen des eigenen ersten
  Microservice angepasst werden, z.B. `SPORTVEREIN_SERVICE_HOST`.

Um das Testergebnis mit _Allure_ zu inspizieren, ruft man einmalig
`.\gradlew downloadAllure` auf. Fortan kann man den generierten Webauftritt mit
den Testergebnissen folgendermaßen aufrufen:

```powershell
    .\gradlew allureServe
```

---

## Rechnername in der Datei hosts

Wenn man mit Kubernetes arbeitet, bedeutet das auch, dass man i.a. über TCP
kommuniziert. Deshalb sollte man überprüfen, ob in der Datei
`C:\Windows\System32\drivers\etc\hosts` der eigene Rechnername mit seiner
IP-Adresse eingetragen ist. Zum Editieren dieser Datei sind Administrator-Rechte
notwendig.

---

## Kubernetes, Helm und Skaffold

### WICHTIG: Schreibrechte für die Logdatei

Wenn die Anwendung in Kubernetes läuft, ist die Log-Datei `application.log` im
Verzeichnis `C:\Zimmermann\volumes\kunde-v2`. Das bedeutet auch zwangsläufig,
dass diese Datei durch den _Linux-User_ vom (Kubernetes-) Pod angelegt und
geschrieben wird, wozu die erforderlichen Berechtigungen in Windows gegeben
sein müssen.

Wenn man z.B. die Anwendung zuerst mittels _Cloud Native Buildpacks_ laufen
lässt, dann wird `application.log` vom Linux-User `cnb` erstellt.

### Helm als Package Manager für Kubernetes

_Helm_ ist ein _Package Manager_ für Kubernetes mit einem _Template_-Mechanismus
auf der Basis von _Go_.

Zunächst muss man z.B. mit dem Gradle-Plugin von Spring Boot ein Docker-Image
erstellen ([s.o.](#Image-für-Kubernetes-erstellen)).

Die Konfiguration für Helm ist im Unterverzeichnis `extras\kunde`. Die
Metadaten für das _Helm-Chart_ sind in der Default-Datei `Chart.yaml` und die
einzelnen Manifest-Dateien für das Helm-Chart sind im Unterverzeichis
`templates` im Format YAML. In diesen Dateien gibt es Platzhalter ("templates")
mit der Syntax der Programmiersprache Go. Die Defaultwerte für diese Platzhalter
sind in der Default-Datei `values.yaml` und können beim Installieren durch weitere
YAML-Dateien überschrieben werden. Im unten stehenden Beispiel wird so ein
_Helm-Service_ dem _Release-Namen_ kunde mit dem Helm-Chart `Chart.yaml` aus
dem aktuellen Verzeicnis in Kubernetes installiert. Dabei muss die Umgebungsvariable
`HELM_NAMESPACE` auf den Wert `acme` gesetzt sein.

```powershell
    # Ueberpruefen, ob die Umgebungsvariable HELM_NAMESPACE gesetzt ist:
    Write-Output $env:HELM_NAMESPACE

    cd extras\kunde
    helm lint --strict .
    helm-docs

    # einfacher: helmfile oder Skaffold
    helm install kunde . -f values.yaml -f dev.yaml
    helm list
    helm status kunde

    # MySQL statt PostgreSQL:
    helm install kunde . -f values.yaml -f dev.yaml -f dev-mysql.yaml

    # H2 statt PostgreSQL:
    helm install kunde . -f values.yaml -f dev.yaml -f dev-h2.yaml
```

Später kann das Helm-Chart mit dem Release-Namen _kunde_ auch deinstalliert werden:

```powershell
    cd extras\kunde
    helm uninstall kunde
```

### Installation mit helmfile und Port Forwarding

```powershell
    helmfile apply

    helm list
    helm status kunde

    kubectl describe svc/kunde -n acme
    # in Lens: Network > Endpoints
    kubectl get ep -n acme

    .\port-forward.ps1

    # Deinstallieren
    helmfile destroy
```

Wenn _MySQL_ statt _PostgreSQL_ verwendet werden soll, muss man in der Datei
`helmfile` in der Zeile mit `dev-mysql.yaml` den Kommentar entfernen und die
andere Zeile mit `values:` auskommentieren. Analog für H2 statt PostgreSQL.

Gegenüber Skaffold (s.u.) hat helmfile allerdings folgende __Defizite__:

- helmfile funktioniert nur mit Helm, aber nicht mit _Kustomize_, _kubectl_, _kpt_
- Continuous Deployment wird nicht unterstützt
- Die Konsole des Kubernetes-Pods sieht man nicht in der aufrufenden PowerShell.
- Port-Forwarding muss man selbst einrichten bzw. aufrufen

Um beim Entwickeln von localhost (und damit von außen) auf einen
Kubernetes-Service zuzugreifen, ist _Port-Forwarding_ die einfachste
Möglichkeit, indem das nachfolgende Kommando für den installierten Service mit
Name _kunde_ aufgerufen wird. Alternativ kann auch das Skript `port-forward.ps1`
aufgerufen werden.

```powershell
    kubectl port-forward service/kunde 8080 --namespace acme
```

Nach dem Port-Forwarding kann man auf den in Kubernetes laufenden Service zugreifen:

- _HTTP Client_ von IntelliJ
- Cmdlet `Invoke-WebRequest` von PowerShell
- _cURL_

Ein _Ingress Controller_ ist zuständig für das _Traffic Management_ bzw. Routing
der eingehenden Requests zu den Kubernetes Services. Ein solcher Ingress Controller
wurde durch `extras\kunde\templates\ingress.yaml` installiert und kann von
außen z.B. folgendermaßen aufgerufen werden, falls der eigentliche Kommunikationsendpunkt
in Kubernetes verfügbar ist.

```powershell
    # ca. 2. Min. warten, bis der Endpoint bei kunde verfuegbar ist (in Lens: Network > Endpoints)
    kubectl get ep -n acme

    $secpasswd = ConvertTo-SecureString p -AsPlainText -Force
    $credential = New-Object System.Management.Automation.PSCredential('admin', $secpasswd)

    # GET-Request fuer REST-Schnittstelle mit Invoke-WebRequest:
    $response = Invoke-WebRequest https://kubernetes.docker.internal/kunden/00000000-0000-0000-0000-000000000001 `
        -Headers @{Accept = 'application/hal+json'} `
        -SslProtocol Tls13 -HttpVersion 2 -SkipCertificateCheck `
        -Authentication Basic -Credential $credential
    Write-Output $response.RawContent

    # GraphQL mit Invoke-WebRequest:
    $response = Invoke-WebRequest https://kubernetes.docker.internal/kunden/graphql `
        -Method Post -Body '{"query": "query { kunde(id: \"00000000-0000-0000-0000-000000000001\") { nachname } }"}' `
        -ContentType 'application/json' `
        -SslProtocol Tls13 -HttpVersion 2 -SkipCertificateCheck `
        -Authentication Basic -Credential $credential
    Write-Output $response.RawContent

    # GET-Request fuer REST-Schnittstelle mit cURL:
    curl --verbose --user admin:p --tlsv1.3 --http2 --insecure https://kubernetes.docker.internal/kunden/00000000-0000-0000-0000-000000000001

    # GraphQL mit cURL:
    curl --verbose --data '{"query": "query { kunde(id: \"00000000-0000-0000-0000-000000000001\") { nachname } }"}' `
        --header 'Content-Type: application/json' `
        --tlsv1.3 --insecure `
        --user admin:p `
        https://kubernetes.docker.internal/kunden/graphql
```

### Continuous Deployment mit Skaffold

_Skaffold_ ist ein Werkzeug, das den eigenen Quellcode beobachtet. Wenn Skaffold
Änderungen festestellt, wird das Image automatisch neu gebaut und ein Redeployment
in Kubernetes durchgeführt.

Um das Image mit dem Tag `2.0.0` zu bauen, muss die Umgebungsvariable `TAG` auf
den Wert `2.0.0` gesetzt werden. Dabei ist auf die Großschreibung bei der
Umgebungsvariablen zu achten.

In `skaffold.yaml` ist konfiguriert, dass das Image mit _Cloud Native
Buildpacks_ gebaut wird.

Weiterhin gibt es in Skaffold die Möglichkeit, _Profile_ zu definieren, um z.B.
verschiedene Werte bei der Installation mit Helm zu verwenden. Dazu ist in
skaffold.yaml beispielsweise konfiguriert, dass die Umgebungsvariable
`SKAFFOLD_PROFILE` auf `dev` gesetzt sein muss, um bei Helm zusätzlich die Datei
`dev.yaml` zu verwenden.

Das Deployment wird mit Skaffold nun folgendermaßen durchgeführt und kann mit
`<Strg>C` abgebrochen bzw. zurückgerollt werden:

```powershell
    $env:TAG = '2.0.0'
    skaffold dev

    helm list
    helm status kunde

    kubectl describe svc/kunde -n acme
    # in Lens: Network > Endpoints
    kubectl get ep -n acme

    <Strg>C
    skaffold delete
```

Bis das Port-Forwarding, das in `skaffold.yaml` konfiguriert ist und nicht
manuell eingerichtet werden muss, auch ausgeführt wird, muss man ggf. ein
bisschen warten. Aufgrund der Einstellungen für _Liveness_ und _Readiness_
kann es einige Minuten dauern, bis in der PowerShell angezeigt wird, dass die
Installation erfolgreich war. Mit Lens oder Octant kann man
jedoch die Log-Einträge inspizieren und so vorher sehen, ob die Installation
erfolgreich war. Sobald Port-Forwarding aktiv ist, sieht man in der PowerShell
auch die Konsole des gestarteten (Kubernetes-) Pods.

Außerdem generiert Skaffold noch ein SHA-Tag zusätzlich zu `2023.1.0`.
Das kann man mit `docker images | sort` sehen. Von Zeit zu Zeit sollte man
mittels `docker rmi <image:tag>` aufräumen.

Wenn man nun in IntelliJ IDEA den Quellcode des Microservice editiert und dieser
durch IJ unmittelbar übersetzt wird, dann überwacht dabei Skaffold die
Quellcode-Dateien, baut ein neues Image und führt einen neuen Deployment-Vorgang
aus. Deshalb spricht man von __Continuous Deployment__.

### kubectl top

Mit `kubectl top pods -n acme` kann man sich die CPU- und RAM-Belegung der Pods
anzeigen lassen. Ausgehend von diesen Werten kann man `resources.requests` und
`resources.limits` in `dev.yaml` ggf. anpassen. Voraussetzung für `kubectl top`
ist, dass der `metrics-server` für Kubernetes im Namespace `kube-system`
installiert wurde.

### Validierung der Installation

#### Polaris

Ob _Best Practices_ bei der Installation eingehalten wurden, kann man mit
_Polaris_ überprüfen. Um den Aufruf zu vereinfachen, gibt es im Unterverzeichnis
`extras\kubernetes` das Skript `polaris.ps1`:

```powershell
    cd extras\kubernetes
    .\polaris.ps1
```

Nun kann Polaris in einem Webbrowser mit der URL `http://localhost:8008`
aufgerufen werden.

---

## Statische Codeanalyse

### ktlint und Detekt

Eine statische Codeanalyse ist durch die beiden Werkzeuge _ktlint_ und _Detekt_
möglich, indem man die folgenden Gradle-Tasks aufruft:

```powershell
    .\gradlew ktlint detektMain detektTest
```

### SonarQube

Für eine statische Codeanalyse durch _SonarQube_ muss zunächst der
SonarQube-Server mit _Docker Compose_ als Docker-Container gestartet werden:

```powershell
    cd extras\sonarqube
    docker compose up
```

Wenn der Server zum ersten Mal gestartet wird, ruft man in einem Webbrowser die
URL `http://localhost:9000` auf. In der Startseite muss man sich einloggen und
verwendet dazu als Loginname `admin` und ebenso als Password `admin`. Danach
wird man weitergeleitet, um das initiale Passwort zu ändern. Das neue Passwort
trägt man dann in das Skript `sonar-scanner.ps1` im Wurzelverzeichnis ein.
Zur Konfiguration für künftige Aufrufe des _SonarQube-Scanners_ trägt man jetzt
noch in der Konfigurationsdatei `sonar-project.properties` den Projektnamen beim
der Property `sonar.projectKey` ein.

Nachdem der Server gestartet ist, wird der SonarQube-Scanner in einer zweiten
PowerShell mit dem Skript `sonar-scanner.ps1` gestartet. Das Resultat kann dann
in der Webseite des zuvor gestarteten Servers über die URL `http://localhost:9000`
inspiziert werden.

Abschließend wird der oben gestartete Server heruntergefahren.

```powershell
    cd extras\sonarqube
    docker compose down
```

---

## Dokumentation

### Dokumentation durch AsciiDoctor und PlantUML

Eine HTML- und PDF-Dokumentation aus AsciiDoctor-Dateien, die ggf. UML-Diagramme
mit PlantUML enthalten, wird durch folgende Gradle-Tasks erstellt:

```powershell
    .\gradlew asciidoctor asciidoctorPdf
```

---

### API Dokumentation durch javadoc

Eine API-Dokumentation in Form von HTML-Seiten kann man durch das Gradle-Plugin
_javadoc_ erstellen:

```powershell
    .\gradlew javadoc
```

---

## Einfache Lasttests mit Fortio

_Fortio_ als Werkzeug für Lasttests ist als eigenständiges Projekt aus _Istio_ hervorgegangen.
In `docker-compose.env` kommentiert man die folgenden Einträge aus, um keine unnötig teuren
Protokollausgaben in der Konsole zu erhalten, die sowieso nur für einen Entwicklungsrechner gedacht sind.

* REQUEST_RESPONSE_LOGLEVEL
* HIBERNATE_LOGLEVEL
* FLYWAY_LOGLEVEL
* APPLICATION_LOGLEVEL

Nun kann man den Fortio-Server in einer weiteren PowerShell starten:

```powershell
    cd extras
    .\fortio.ps1
```

Jetzt ruft man in einem Webbrowser die URL `http://localhost:8088/fortio` auf. Dort kann man
einen einfachen Lasttest konfigurieren, indem man die Beispielwerte eingibt, die beim Skript
`fortio.ps1` ausgegeben wurden. Bevor man auf den Button `Start` klickt, um den einfachen
Lasttest zu starten, empfiehlt es sich, vorab zumindest einen HTTP-Request abzuschicken,
weil man sonst den Lasttest nach einem "Kaltstart" durchführt.

Alternative und populäre Werkzeuge für Lasttests sind:

- _Locust_ von https://github.com/locustio/locust: Skripte werden in Python implementiert
- _k6_ aus dem Projekt Grafana https://github.com/grafana/k6: Skripte werden in JavaScript implementiert
- _ab_ aus dem Apache-Projekt httpd: https://httpd.apache.org/docs/current/programs/ab.html
- _Apache JMeter_ https://jmeter.apache.org
- _hey_: Go-Umgebung erforderlich
- _The Grinder_ von https://grinder.sourceforge.net: Skripte werden in Jython implementiert
- _wrk_ von https://github.com/wg/wrk: Skripte werden in Lua implementiert
