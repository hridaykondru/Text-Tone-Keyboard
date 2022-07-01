package com.hriday.project;

import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.AsyncTask;
import android.widget.Toast;

import org.jsoup.helper.StringUtil;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LivePrediction extends AsyncTask<Void, Void, String> {
    private Context context;
    private String final_str;
    private BertNLClassifier classifier;
    private KeyboardView kv;
    private Map<String,String> emoji_map;

    public LivePrediction(Context context, String final_str, BertNLClassifier classifier, Map<String,String> emoji_map, KeyboardView kv){
        this.context=context;
        this.final_str=final_str;
        this.classifier=classifier;
        this.emoji_map=emoji_map;
        this.kv=kv;
    }



    @Override
    protected String doInBackground(Void... error) {
        String predicted_mood;
        List<Category> results =  classifier.classify(final_str);
        Collections.sort(results,(a1, a2)->Float.compare(a1.getScore(), a2.getScore()));
        Collections.reverse(results);
        //ArrayList<Category>resultsAl=new ArrayList<>(results);
        predicted_mood = results.get(0).getLabel();
        return predicted_mood;
    }



    @Override
    protected void onPostExecute(String predicted_mood){
        //Toast.makeText(context, predicted_mood, Toast.LENGTH_SHORT).show();
        Keyboard currentKeyboard=kv.getKeyboard();
        List<Keyboard.Key> keys = currentKeyboard.getKeys();
        Keyboard.Key emoji_key=keys.get(keys.size()-1);
        if(StringUtil.isBlank(final_str)){
            emoji_key.icon = context.getDrawable(android.R.drawable.presence_invisible);
            emoji_key.label=null;
        }
        else{
            emoji_key.icon = null;
            emoji_key.label=emoji_map.get(predicted_mood);
        }

        //emoji_key.pre="Mood";
        kv.invalidateAllKeys();
        /*for (int iii = 40; iii <= keys.size() - 1; iii++) {
            Keyboard.Key currentKey = keys.get(iii);
            Toast.makeText(context, Integer.toString(currentKey.codes[0]), Toast.LENGTH_SHORT).show();
            //If your Key contains more than one code, then you will have to check if the codes array contains the primary code
            if (currentKey.codes[0] == -115) {
                //Toast.makeText(context, "ok", Toast.LENGTH_SHORT).show();
                currentKey.icon = null;
                currentKey.label=emoji_map.get(predicted_mood);
                kv.invalidateAllKeys();
                break;

            }
        }*/

    }

}


