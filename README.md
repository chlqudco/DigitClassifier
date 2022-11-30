## 설명 ##
- 사용자가 작성한 숫자를 인식하는 모델입니다.
- 사용자는 화면 윗쪽에 숫자를 직접 그릴수 있습니다. 그리는 화면은 깃허브에 올라온 drawView 라이브러리를 사용했습니다. 
- 숫자 분류에는 MLP 모델과 CNN 모델을 사용해 보았습니다. 
- 로직은 사용자가 그린 숫자 화면을 비트맵 형식으로 바꿔서 classifier에 전달하면, classifier 클래스에서 모델이 사용할 수 있는 28x28 크기, byteBuffer 형식, 1채널로 변환합니다. 변환을 완료한 뒤 interpreter가 추론을 하면 classifier는 결과를 받아온 뒤 가장 확률이 높은 숫자를 반환하여 화면에 보여주게 됩니다. 
- tflite 라이브러리는 간편한 함수를 많이 제공해주어 쉽게 사용할 수 있습니다.  

## 예시 ##
- ![KakaoTalk_20221130_160126235](https://user-images.githubusercontent.com/68932465/204822578-cd2ef0a8-c465-4950-abfe-432fa85dfaea.jpg)
![KakaoTalk_20221130_160126235_01](https://user-images.githubusercontent.com/68932465/204822586-fa13a127-100e-4632-a070-0b0529a35fc8.jpg)

