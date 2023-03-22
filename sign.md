## GERAR A CHAVE (SO UMA VEZ - DEPOIS UTILIZA A MESMA CHAVE PARA ASSINAR TODOS OS APPS)
keytool -genkey -v -keystore D:\Temp\app-th-keystore.keystore -alias app-th -keyalg RSA -keysize 2048 -validity 10000

## ASSINAR O APP

jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore D:\TEMP\app-th-keystore.keystore -signedjar "D:\repos\biometria\Sistemas TH\App Android\sistemasth-android-liveness-sample\app\build\outputs\bundle\release\vision-quickstart-release-signed.aab" "D:\repos\biometria\Sistemas TH\App Android\sistemasth-android-liveness-sample\app\build\outputs\bundle\release\vision-quickstart-release.aab" app-th 