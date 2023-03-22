# Sistemas TH Quickstart Sample App

## Introdução

Este Quickstart é usado para demonstrar como integrar as features do SDK da Sistemas TH no aplicativo de sua empresa.

## Lista de Features

Algumas features estão inclusas neste Quickstart App:
* [Liveness](https://sistemasth.com.br) - Detecta a face em tempo real, captura alguns frames e faz a verificação de Prova de Vida (Liveness).
* [CNH OCR](https://sistemasth.com.br) - Esta feature extrai os dados contidos em uma imagem de CNH.
* [Face Match](https://sistemasth.com.br) - Esta feature requer um documento e um frame da face do usuário capturada na feature de Prova de Vida (Liveness).

<img src="../screenshots/quickstart-picker.png" width="220"/> <img src="../screenshots/quickstart-image-labeling.png" width="220"/> <img src="../screenshots/quickstart-object-detection.png" width="220"/> <img src="../screenshots/quickstart-pose-detection.png" width="220"/>

## Getting Started

* Rode o app the exemplo no seu dispositivo Android ou no emulador
* Você pode tentar extender o código para adicionar novas features e funcionalidades

## Como usar o Aplicativo

O aplicativo suporte 3 cenários de uso: Liveness, Face Match e OCR de carteira de habilitação brasileira.
Os cenários também podem ser intercambiáveis. Ou seja, você pode usar simultaneamente duas features ao mesmo tempo.

### Cenário Liveness 
O aplicativo solicitará ao usuário que posicione a sua face dentro de um quadrado vermelho. Assim que posicionar, um ponto surgirá na tela. O aplicativo irá instruir o usuário para que posicione a ponta de seu nariz no ponto que surgiu.
Após isso feito, iniciará o processo de Prova de Vida que roda nos servidores da Sistemas TH. O usuário deverá aguardar o resultado que será exibido em tela.

### Cenário OCR 
Neste cenário o aplicativo solicitará que o usuário tire foto de uma CNH.
O aplicativo verificará se na foto realmente consta uma CNH. Caso positivo, iniciará um processo de extração dos dados da carteira para exibir em uma tela de resultados.

### Cenário Face Match
Aqui o aplicativo solicitará a Prova de Vida e em seguida a foto da CNH. 
Esta feature no SDK está separada e espera o input de duas imagens. A comparação será feita e o resultado será retornado em uma tela específica.

## Suporte

* [Documentação](https://sistemasth.com.br)
* [API referencia](https://sistemasth.com.br)
