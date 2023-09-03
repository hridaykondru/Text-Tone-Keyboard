# Text-Tone-Keyboard
An Android keyboard that visually updates the user in real time on the tone of the sentences being typed. The keyboard displays various emojis representing the mood of the sentences and hence the mood of the user. The Android app also persists the text typed by the user and allows them to analyse their mood over an extended period of time. A [poster presentation](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/314c81c2225c0b4f67180bc2d41a604a2c29fc9a/Poster%20Presentation.pdf) of this project had been showcased at IIT Padharo, a prestigious open-house event to demonstrate the scientific and engineering capacities of IIT Jodhpur.

## Description:

The keyboard uses TensorFlow Lite Model Maker “mobilebert\_classifier” text classification model architecture which was trained on the International Survey on Emotion Antecedents and Reactions (ISEAR) dataset. 

The ISEAR database constructed by the Swiss National Centre of Competence in Research and lead by Wallbott and Scherer consists of seven emotion labels (joy, sadness, fear, anger, guilt, disgust, and shame) obtained as a result of gathering series of data from cross- cultural questionnaire studies in 37 countries. Three thousand (3000) participants from varying cultural backgrounds were made to fill questionnaires about their experiences and reactions toward events. The final dataset reports a total of 7665 sentences labelled with emotions.  

The trained model is imported into an android app and integrated with the keyboard component. The keyboard component intercepts the user’s input and feeds the input to the TensorFlow model for predicting the user’s mood. The mood is mapped to various emojis that are displayed on the keyboard. 

The Android app also has functionality to persist the text typed by the user in an sqlite database and allows the user to analyse his mood over an extended period of time. 

## App Explanation with Screenshots:


The emoji at the bottom right of the keyboard shows real-time Text Tone Predictions based on the text present in the current text field. 

The **TEXT DATABASE** page shows the database which stores all the sentences typed and even sentences deleted by the user. 

The **AVERAGE TEXT TONE** page shows the overall text tone which allows them to analyse their mood over an extended period of time. 

<img src="https://github.com/hridaykondru/Text-Tone-Keyboard/blob/314c81c2225c0b4f67180bc2d41a604a2c29fc9a/Readme%20Images/Eg.jpg" width="300"> <img src="https://github.com/hridaykondru/Text-Tone-Keyboard/blob/314c81c2225c0b4f67180bc2d41a604a2c29fc9a/Readme%20Images/TextDB.jpg" width="300">  <img src="https://github.com/hridaykondru/Text-Tone-Keyboard/blob/314c81c2225c0b4f67180bc2d41a604a2c29fc9a/Readme%20Images/AvgStats.jpg" width="300">



| Emotion                   | Emoji Displayed                                      |
| :-------------------------: | :------------------------------------------: |
| Joy        | ![](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Joy.png)             |
| Sadness           | ![](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Sadness.png)             |
| Fear           | ![I](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Fear.png)             |
| Anger           | ![I](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Anger.png)             |
| Guilt           | ![I](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Guilt.png)             |
| Disgust           | ![I](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Disgust.png)             |
| Shame           | ![I](https://github.com/hridaykondru/Text-Tone-Keyboard/blob/af9e319f804caaa68b2a59e6ab00037c62f639ea/Readme%20Images/Shame.png)             |
