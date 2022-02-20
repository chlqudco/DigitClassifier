package com.chlqudco.develop.digitclassifier.draw;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.util.Pair;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

//TFLite 모델을 ByteBuffer 클래스로 불러오고 옵션을 추가해서 Interpreter 객체 생성
public class Classifier {
    //모델 입력 정보, 가로, 세로, 채널
    int modelInputWidth, modelInputHeight, modelInputChannel;

    //모델의 분류 클래스 개수
    int modelOutputClasses;

    //cnn없는거 : 다층 퍼셉트론 모델 (MLP)
    //cnn있는거 : 합성곱 신경망 모델 (CNN)
    private static final String MODEL_NAME = "keras_model_cnn.tflite";

    //모델 작동 하는 애, 모델에 데이터를 입력하고 추론 결과를 전달받을 수 있는 클래스
   Interpreter interpreter = null;

    //assets 폴더를 참조할때 context가 필요함, 생성자에서 받아옴
    Context context;
    public Classifier(Context context) {
        this.context = context;
    }

    //tflite파일을 읽어오는 함수
    private ByteBuffer loadModelFile(String modelName) throws IOException{
        //에셋매니저 생성 (aseets폴더에 저장된 리소스에 접근하기 위한 기능 제공)
        AssetManager am  =context.getAssets();
        //tflite파일의 파일디스크립터 획득
        AssetFileDescriptor afd = am.openFd(modelName);
        //파일 스트림 얻은 뒤
        FileInputStream fis = new FileInputStream(afd.getFileDescriptor());
        //성능을 위해 파일 채널로 읽을 거임
        FileChannel fc = fis.getChannel();

        //모델 파일의 길이와 오프셋 정보 획득
        long startOffset = afd.getStartOffset();
        long declaredLength = afd.getDeclaredLength();

        //파일채널의 map 함수에 길이와 오프셋을 전달하면 tflite 파일을 ByteBuffer형으로 변환
        return fc.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    public void init() throws IOException{
        //파일명으로 모델 읽어오기, IO예외 발생 가능
        ByteBuffer model = loadModelFile(MODEL_NAME);

        //시스템의 byteOrder값과 동일하게 설정...? 먼소리? 대문자 소문자 구분..?
        model.order(ByteOrder.nativeOrder());

        //읽어온 모델을 이용해서 인터프리터 생성
        interpreter = new Interpreter(model);

        //비트맵 이미지 전처리
        initModelShape();
    }

    //기기마다 생성하는 비트맵 크기가 제각각이라 모델의 입력크기에 맞도록 전처리 해야함, 비트맵의 포맷은 ARGB8888 (4채널)
    //모델의 입력은 28 X 28, 그레이스케일(1채널) 을 원함
   private void initModelShape() {
        //모델이 어떤 크기를 원하는지 확인해야함

       //입력 텐서 하나 가져와서
        Tensor inputTensor = interpreter.getInputTensor(0);
        //텐서의 모양 가져온 후 변수 값 저장
        int[] inputShape = inputTensor.shape();
        modelInputChannel = inputShape[0];
        modelInputWidth = inputShape[1];
        modelInputHeight = inputShape[2];

        //아웃풋 클래스 개수를 조사하기 위해 또 지랄
        Tensor outputTensor = interpreter.getOutputTensor(0);
        int[] outputShape = outputTensor.shape();
        modelOutputClasses = outputShape[1];
    }

    //내가 그린 그림의 크기 변환
    private Bitmap resizeBitmap(Bitmap bitmap){
        //메소드 개꿀^^, 걍 넣어주면 알아서 변환해줌^^, 마지막인자는 어떤 보간법 쓸건지
        return Bitmap.createScaledBitmap(bitmap, modelInputWidth, modelInputHeight, false);
    }

    //4채널을 1채널로, Bitmap 형식을 ByteBuffer 형식으로 바꿔주는 함수
    private ByteBuffer convertBitmapToGrayByteBuffer(Bitmap bitmap){
        //계산 결과를 담을 바이트버퍼 선언,,, 이미지의 바이트 크기만큼만 ByteBuffer 메모리 할당, 성능 증가
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(bitmap.getByteCount());
        //얜 아까부터 뭐냐
        byteBuffer.order(ByteOrder.nativeOrder());

        //비트맵의 픽셀값을 담을 변수 선언
        int[] pixels = new int[bitmap.getWidth() * bitmap.getHeight()];
        //담아!
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        //모든 픽셀에 대하여 RGB값의 평균으로 흑백영상 생성
        for (int pixel: pixels){
            int r = pixel >> 16 & 0xFF;
            int g = pixel >> 8 & 0xFF;
            int b = pixel & 0xFF;

            float avgPixelValue = (r+g+b)/3.0f;

            //평균값을 0~1의 값으로 정규화 하는 작업
            float normalizedPixelValue = avgPixelValue / 255.0f;

            //버퍼에 차곡차곡 담아
            byteBuffer.putFloat(normalizedPixelValue);
        }

        return byteBuffer;
    }

    //젤 중요한 함수, 여기서 모든게 이뤄짐
    public Pair<Integer, Float> classify(Bitmap image){
        //우리가 그린 그림을 적절히 변환
        ByteBuffer buffer = convertBitmapToGrayByteBuffer(resizeBitmap(image));

        //추론 결과를 담을 버퍼 선언, 모델의 결과 클래스 갯수만큼, 첫번째 인자가 1인 이유는 하나의 입력만 받아서 하나의 출력만 반환하므로 1
        float[][] result = new float[1][modelOutputClasses];

        //추론 시작!!
        interpreter.run(buffer,result);

        //결과 값 중 젤 의미있는 값만 반환
        return argmax(result[0]);
    }

    //제일 큰 확률 값 고르는 함수, array에는 인덱스와 확률이 들어있음
    private Pair<Integer, Float> argmax(float[] array){
        int argmax = 0;
        float max = array[0];
        for (int i = 1; i< array.length; i++){
            float f=  array[i];
            if (f>max){
                argmax = i;
                max = f;
            }
        }
        return new Pair<>(argmax,max);
    }

    public void finish(){
        if (interpreter != null){
            interpreter.close();
        }
    }
}
