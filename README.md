![](https://github.com/thebix/debts/workflows/Android%20CI/badge.svg)

# Debts app

## Developer setup
- create apps `net.thebix.debts` and `net.thebix.debts.debug` in Firebase and save `/app/google-services.json`
- put `debts.keystore` to `/app/debts.keystore`
- make file `./private/debts_credentials.properties` with
```text
    DEBTS_STORE_KEY_ALIAS=
    DEBTS_STORE_KEY_ALIAS_PASSWORD=
    DEBTS_STORE_KEY_FILE=debts.keystore
    DEBTS_STORE_KEY_PASSWORD=
```
- Prepare file `app/google-play-publisher.json`. More info: [Google Play Publisher](https://github.com/Triple-T/gradle-play-publisher)

## Github setup
1. Convert `debts.keystore` file to base64
```shell script
openssl base64 -in app/debts.keystore -out debts.keystore.base64
```
2. Convert `google-services.json` file to base64
```shell script
openssl base64 -in app/google-services.json -out google-services.json.base64
```
3. Convert `google-play-publisher.json` file to base64
```shell script
openssl base64 -in ./app/google-play-publisher.json -out ./google-play-publisher.json.base64
```
4. create [secrets](https://github.com/thebix/debts/settings/secrets/)
- `DEBTS_STORE_KEY_ALIAS`
- `DEBTS_STORE_KEY_ALIAS_PASSWORD`
- `DEBTS_STORE_KEY_FILE`
- `DEBTS_STORE_KEY_PASSWORD`
- `DEBTS_KEY_FILE_BASE64` with content of `debts.keystore.base64`
- `DEBTS_GOOGLE_SERVICES_FILE_BASE64` with content of `google-services.json.base64`
- `GOOGLE_PLAY_PUBLISHER` with content of `google-play-publisher.json`

## Google Play Store
### [Gradle Play Publisher ](https://github.com/Triple-T/gradle-play-publisher)
```shell script
# publish
./gradlew publishReleaseBundle
./gradlew publishReleaseApk
# all available tasks
./gradlew tasks --group publishing
# fetch all the media and texts from play store
./gradlew bootstrap
```

## Release
### Steps
1. Change release notes and other text in `app/src/main/play` folder
2. Change app version
3. Push to remote
4. Tag release with `release/*.*.*`
```shell script
git tag -a release/*.*.* -m "Release/*.*.*"
git push origin release/*.*.*
```

### Internal
```
git push --delete origin internal/release
git tag -d internal/release
git tag -a internal/release -m "internal release"
git push --follow-tags
```
