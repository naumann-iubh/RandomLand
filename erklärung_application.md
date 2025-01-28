# Erläuterung der Properites

Hier werden die Properties erklärt, an denen Änderungen vorgenommen werden kann.

## Primär- und Sekundärstraßen

*.angleMin/max: Minimaler und maximaler winkel in denen das nächste Straßensegement abweicht  
*.chance: Wahrscheinlichkeit von 0-1, ob ein Segment gebaut wird oder nicht  
*.mergeThreshold: Bereich in denen nach anderen Segmenten gesucht wird, um mit anderen Straßen eine Kreuzung zu bilden

```properties
highway.angleMin=7.0
highway.angleMax=21.0
highway.chance=0.8
highway.length=30.0
highway.mergeThreshold=25.0
street.angleMin=5.0
street.angleMax=20.0
street.chance=0.7
street.length=10
street.mergeThreshold=8.0
```

## Stadtgröße und Bevölkerungsdichte

*.width/height: Größe der Stadt in Meter  
*.offset: Verändert wie feingranular die Bevölkerungsdichte ausfällt von 0 bis 1    
umso näher es gegen 1 geht umso feingranularer werden die Felder

```properties
city.width=400
city.height=400
city.offset=0.007
```

## Flurstücke

Größe in Qudratmeter und Wahrscheinlichkeit von Landschaftsgärten

```properties
flurstueck.landschaftsgartenMinGroesse=212000.0
flurstueck.landschaftsgartenMaxGroesse=3750000.0
flurstueck.landschaftsgartenProzentualerAnteil=5.0
```

## Gebäudeverteilung

Prozentuale Verteilung von:

- Wohngebäuden
- Nicht-Wohngebäuden
- Anzahl der Zimmer in den Gebäuden

```properties
gebaeude.wohnen=0.9
gebaeude.nichtwohngebaeude=0.10
gebaeude.maxHeight=100
gebaeude.minHeight=3
wohnen.einzimmer=0.03
wohnen.zweizimmer=0.10
wohnen.dreizimmer=0.22
wohnen.vierzimmer=0.25
wohnen.fuenfzimmer=0.17
wohnen.sechszimmer=0.11
wohnen.siebenzimmer=0.12
```

# Energieversorgung

Prozentuale Verteilung von Energieversorgern

```properties
power.erdgas=0.61
power.fernwaerme=0.08
power.heizoel=0.31
```

# Baujahr

Prozentuale Verteilung vom Baujahr des Gebäudes

```properties
alter.vorkriegsbau=0.25
alter.nachkriegsbau=0.43
alter.ersteWaermeschutzverordnung=0.10
alter.zweiteWaermeschutzverordnung=0.09
alter.dritteWermeschutzverordnung=0.07
alter.ersteEnergieeinsparverordnung=0.04
alter.zweiteEnergieeinsparverordnung=0.02
```