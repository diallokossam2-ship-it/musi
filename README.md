# Parole de Vie

Première base d'une application Android native écrite en Kotlin. Les APK de test sont compilés exclusivement par GitHub Actions.

## État actuel

- Application Android minimale, sans bibliothèque d'interface externe.
- Édition biblique visée : Parole de Vie (PDV).
- Le texte biblique intégral n'est pas inclus dans cette première version.
- Le workflow compile l'APK debug, vérifie qu'il ne dépasse pas 5 000 000 octets et publie l'APK comme artefact pendant 30 jours.

## Pourquoi le texte PDV n'est pas encore intégré ?

Les éditions PDV sont associées à la Société biblique française / Bibli'O et portent une mention de droits d'auteur. Avant de redistribuer le texte complet dans une application, il faut confirmer par écrit les autorisations applicables à l'intégration, au stockage hors ligne et à la distribution de l'application.

## Compiler

Un push sur `main` déclenche GitHub Actions. On peut aussi lancer manuellement le workflow depuis l'onglet **Actions** du dépôt.

L'APK de test se trouve dans l'artefact `parole-de-vie-apk-debug` du workflow. La limite de 5 Mo est vérifiée par la compilation ; elle n'est pas présumée atteinte tant que le premier build n'a pas réussi.

## Versions initiales

- Java 17
- Android Gradle Plugin 8.7.3
- Kotlin 2.0.21
- Gradle 8.9
- Android compileSdk 35
