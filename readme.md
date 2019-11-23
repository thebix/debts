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
