package com.example.pytorchandroid;

import android.graphics.Bitmap;
import android.util.Log;

import org.pytorch.Tensor;
import org.pytorch.Module;
import org.pytorch.IValue;
import org.pytorch.torchvision.TensorImageUtils;

public class Classifier {


    Module model;
    float[] mean = {0.485f, 0.456f, 0.406f};
    float[] std = {0.229f, 0.224f, 0.225f};
    public String labelScore;

    public Classifier(String modelPath){

        model = Module.load(modelPath);

    }

    public void setMeanAndStd(float[] mean, float[] std){

        this.mean = mean;
        this.std = std;
    }

    public Tensor preprocess(Bitmap bitmap, int size){

        bitmap = Bitmap.createScaledBitmap(bitmap,size,size,false);
        return TensorImageUtils.bitmapToFloat32Tensor(bitmap,this.mean,this.std);

    }

    public int argMax(float[] inputs){

        int maxIndex = -1;
        float maxvalue = 0.0f;

        for (int i = 0; i < inputs.length; i++){

            if(inputs[i] > maxvalue) {

                maxIndex = i;
                maxvalue = inputs[i];
            }

        }


        return maxIndex;
    }

    public String predict(Bitmap bitmap){

        Tensor tensor = preprocess(bitmap,224);

        IValue inputs = IValue.from(tensor);
        Tensor outputs = model.forward(inputs).toTensor();
        float[] scores = outputs.getDataAsFloatArray();

        float max = scores[0];
        for (int i = 0; i < scores.length; i++) {
            if (scores[i] > max) {
                max = scores[i];
            }
        }


        int classIndex = argMax(scores);

        float scorePercentage = max * 100;
        int integerScorePercentage = Math.round(scorePercentage);

        //interp the confidence level and then add a label
        String fullDetail = Constants.IMAGENET_CLASSES[classIndex] + " " + String.format( "%d", integerScorePercentage ) + "% Match ";;
        if(integerScorePercentage>85){
            fullDetail += "(High)";
        }else if(integerScorePercentage>70){
            fullDetail += "(Med)";
        }else if(integerScorePercentage>50){
            fullDetail += "(Low)";
        }else{
            fullDetail += "(very low)";
        }


        return fullDetail;

    }


}
