# Einleitung

Der Daten Logger für RCT Wechselrichter wurde entwickelt um die Daten nicht nur in der von RCT bereitgestellten mobilen App zur Verfügung zu haben sondern auch in eigenen Smart Home Lösungen zur Verfügung zu haben. Von dieser Github Seite braucht ihr nur die neuste jar Datei rctDataServer-<version>.jar ladet die Datei  mit der höchsten Versionsnummer herunter (siehe Verzeichnis release). Die Anwendung wurde in Java entwickelt und sollte daher unter Windows, Linux und MacOS laufen. Diese jar Datei kann irgendwo auf der Festplatte abgespeichert werden.

# Voraussetzungen
## Java
Java muss installiert sein. Falls ihr nicht sicher seit ob ihr Java installiert habt einfach in einem Kommandozeilenfenster "java -version" (ohne die Anführungszeichen) eingegeben um zu sehen ob Java installiert ist und wenn in welcher Version. Getestet wurde bisher mit 

- Sun Java 1.8 64-bit auf Windows 
- Sun Java 1.8 64-bit auf Raspberry Pi
- openJDK 11 auf Linux

Andere Java Versionen mögen funktionieren oder auch nicht.

## Konfigurationsdatei
Eine Konfigurationsdatei muss vor dem ersten Start angelegt und angepasst werden. Diese Datei muss den Namen rctDataServer.properties haben, bitte auf Groß- und Kleinschreibung achten.

Diese Konfigurationsdatei unter Windows bitte in C:\Users\<User-Name>\AppData\Local\rctDataServerConfig ablegen. Das Unterverzeichnis rctDataServerConfig muss selbst angelegt werden. Falls Linux benutzt wird bitte das File in /home/<User-Name>/.local/share/rctDataServerConfig/ ablegen, falls Mac genutzt wird bitte in <user-home>\AppData\Local\rctDataServerConfig\ ablegen. Auch hier wieder bitte auf Groß-  und Kleinschreibung achten.

Der minimale Inhalt der Datei sollte so aussehen:

```
hostnameInverter = 1.1.1.1
portInverter = 8899
logLevel = info
timeoutInverter = 3000
```
Achtung, ihr müsst auf jeden Fall den ersten Wert anpassen und dort die IP Adresse eures Wechselrichters eintragen. Eventuell müsst ihr auch noch den zweiten Parameter anpassen, 8899 ist der Standard-Port des Wechselrichters wenn euch also nicht bewusst ist dass ihr ihn geändert habt den Wert bitte so belassen. Der Parameter timeoutInverter beschreibt nach wievielen ms nicht mehr auf eine Antwort vom Wechselrichter wartet, aus unbekannten Gründen scheint der Wechselrichter nicht immer zu antworten, es kommt nur selten vor sollte aber den weiteren Ablauf nicht blockieren. Falls ihr im Log viele Timeout Meldungen seht könnte man den Wert etwas hoch drehen. Der Parameter logLevel benutzt die üblichen Werte. Am wichtigsten sind  _error_, _info_ und _debug_. Im produktiven Betrieb nutzt error, um anfänglich zu sehen was passiert info und bei Entwicklungsproblemen debug.

Optional können noch die folgenden Parameter angegeben werden

```
panelPower = 200
panelsA = 10
panelsB = 12
```
Es handelt sich um rein optionale Parameter und werden genutzt um zu prüfen ob die ermittelten Werte stimmig sind. Falls Unstimmigkeiten gefunden werden, werden diese geloggt. PanelPower ist dabei die Leistung eines einzelnen Solarmoduls in Watt, panelsA und panelsB geben die Anzahl der Module an die man im Strang A bzw. B hat. Die obigen Werte sind hier nur beispielhaft und müssen durch die tatsächliche Installation angepasst werden.

# Anwendung
## Start-up
Je nachdem wie euer Rechner konfiguriert ist kann man Java Programme unterschiedlich starten. Auf manchen Rechner kann man diese einfach per Doppelklick starten, ich würde sie immer auf der Kommandozeile starten um besser verfolgen zu können was passiert.
Öffnet ein Kommandozeilenfenster,dann in der Kommandozeile in das Verzeichnis navigieren wo ihr die jar Datei abgelegt habt. In dem Verzeichnis  "java -jar rctDataServer-<version>.jar" aufrufen (ohne die Anführungszeichen eingeben und <version> durch die Versionsnummer in eurer Datei ersetzen). Es werden einige Meldungen über den Bildschirm laufen, nach ein paar Sekunden sollte die Ausgabe anhalten und es sollte angezeigt werden 

```
<<<<< INFO >>>>> Inverter::connect Connected successfully to inverter. 
```
Jetzt ist die Verbindung aufgebaut.

## Einfache Benutzung
Es gibt diverse Möglichkeiten die Anwendung zu nutzen. Die einfachste Möglichkeit ist über einen Browser
(getestet mit Chrome und Firefox) die URL `http://localhost:8080/charts.html` aufzurufen. Damit hat man Zugriff auf ein einfaches UI. Dieses einfach UI hat aber nur eingeschränkte Funktionalität und soll keine vollständige Berichtsanwendung ersetzen.

## Influx Anbindung
Hauptsächlich wurde das Tool gebaut um die Daten in einer Influx DB zu speichern. Die Daten der Influx DB können dann beispielsweise über Grafana (oder andere Anwendungen) ausgelesen und ausgewertet werden. Um die Anbindung an Influx zu realisieren müssen einfach nur die Verbindungsparameter in der obigen Konfigurationsdatei angegeben werden

```
influxdbServer = localhost
influxdbPort = 8086
```
Der Servername und der Serverport müssen gemäß der Influx Installation natürlich angepasst werden. Standardmäßig wird jede Minute ein Messwert angefordert und in der InfluxDB gespeichert. Ein mögliches Beispiel eines Grafana Dashboards könnte beispielsweise wie folgt aussehen:

![Screenshot Grafana](images/Grafana_Screenshot.png)

Im Verzeichnis _Grafana_ findet ihr ein Grafana Dashboard welches ihr importieren könnt um einen Anfang zu haben. Solltet ihr eure eigenen Dashboards erstellen und diese mit anderen Nutzern teilen wollen lege ich sie ebenfalls gerne hier ab. Das Dashboard holt seine Daten von einer DataSource namens InfluxDB, diese DataSource muss in Grafana zu erst manuell angelegt werden, bitte genau diesen Namen verwenden. Beim Anlegen der DataSource als Influx DB bitte den Namen rctdb verwenden. Zum Import in Grafana auf dem Dashboard Home Screen auf das Home Icon oben links klicken und dann im Menu Import Dashboard auswählen.

## Technischer Test
Eine weitere Möglichkeit Daten abzurufen ist es über einen Browser bestimmte Werte abzufragen. Im wesentlichen ist das eine gute Möglichkeit zu testen ob die Anwendung grundsätzlich läuft. Ansonsten kann man diese http Schnittstelle auch nutzen wenn man die Daten in eigenen Anwendungen integrieren will.

Um beispielsweise die Eingangspannung am Strang A abzurufen öffnet im Browser die URL

```
http://localhost:8080/getData?magicNumber=B55BA2CE
```
Wie immer bitte auf Groß- und Kleinschreibung achten. Ihr bekommt im Browser ein sogenannten JSON File angezeigt


```json
{"type":"short","magicNumber":"B55BA2CE","value":530.0684,"text":"DC input voltage A in V","timestamp":1601104412,"sucess":true}
```
Wichtig ist erstmals nur der Wert in `value`, bzw. dass ihr überhaupt eine solche Antwort bekommt. Weitere Details sind nur wichtig wenn ihr eine eigene Anwendung schreiben wollt und die Daten so abfragen wollt. Für weitere Details bitte in die Entwickler-Dokumentation schauen.
