![](https://github.com/thebix/debts/workflows/Android%20CI/badge.svg)

# Debts app

## prepare env
- create apps `net.thebix.debts` and `net.thebix.debts.debug` in Firebase and save `/app/google-services.json`
- connect project to the Firebase
- put `debts.keystore` to `/app/debts.keystore`
- make file `./debts_credentials.properties` with 

```
    DEBTS_STORE_KEY_ALIAS=
    DEBTS_STORE_KEY_ALIAS_PASSWORD=
    DEBTS_STORE_KEY_FILE=
    DEBTS_STORE_KEY_PASSWORD=
    DEBTS_FABRIC_API_KEY=
```

## github actions
- create [secrets](https://github.com/thebix/debts/settings/secrets/)
```
    DEBTS_STORE_KEY_ALIAS=
    DEBTS_STORE_KEY_ALIAS_PASSWORD=
    DEBTS_STORE_KEY_FILE=
    DEBTS_STORE_KEY_PASSWORD=
    DEBTS_FABRIC_API_KEY=
    DEBTS_GOOGLE_SERVICES_FILE_BASE64=base64 representation of google-services.json
    DEBTS_KEY_FILE_BASE64=base64 representation of debts.keystore
```

base64 file encode
```
openssl base64 -in ./debts.keystore -out ./debts.keystore.base64
openssl base64 -in ./google-services.json -out ./google-services.json.base64
```

## Google Play Store
### [Gradle Play Publisher ](https://github.com/Triple-T/gradle-play-publisher)
```shell script
# publish
./gradlew publishReleaseBundle
# all available tasks
./gradlew tasks --group publishing
# fetch all the media and texts from play store
./gradlew bootstrap
```

## Release
### Steps
1. Change release notes and other text in `app/src/main/play` folder
1. Change app version
