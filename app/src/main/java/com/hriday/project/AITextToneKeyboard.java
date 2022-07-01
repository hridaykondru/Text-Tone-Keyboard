package com.hriday.project;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import android.annotation.SuppressLint;
import android.app.AppOpsManager;
import android.app.admin.DevicePolicyManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.inputmethodservice.InputMethodService;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.VibratorManager;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputConnection;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import org.jsoup.helper.StringUtil;
import org.tensorflow.lite.task.core.BaseOptions;
import org.tensorflow.lite.task.text.nlclassifier.BertNLClassifier;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;

public class AITextToneKeyboard extends InputMethodService implements KeyboardView.OnKeyboardActionListener {



    private KeyboardView kv;
    private Keyboard keyboard;
    private Keyboard symbol_keyboard;
    EditText ed_txt;
    private Context context;
    private BertNLClassifier classifier;
    private Map<String,String> emoji_map;
    private String live_final_str;
    private boolean not_entered;
    private String database_word;
    //private String database_sentence;

    private boolean isCaps = false;

    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    MediaRecorder recorder;
    File audiofile = null;
    static final String TAG = "MediaRecording";

    String str="";
    String str2="";

    String appname="";

    String del_txt="";
    int I=0;
    int del_flag=0;
    int del_aft_txt=0;

    DBHelper DB;
    //TService Tser;

   // @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @SuppressLint("InflateParams")
    @Override
    public View onCreateInputView() {
        kv = (KeyboardView)getLayoutInflater().inflate(R.layout.keyboard,null);
        keyboard = new Keyboard(this,R.xml.qwerty);
        symbol_keyboard = new Keyboard(this,R.xml.symbol_keys);
        live_final_str="";
        kv.setKeyboard(keyboard);
        kv.setOnKeyboardActionListener(this);
        database_word="";
        //database_sentence="";
        ((ApplicationClass) this.getApplication()).setDatabase_sentence("");
        //DB = new DBHelper(this);
        DB=((ApplicationClass) this.getApplication()).getDB();
        context=this;
        BertNLClassifier.BertNLClassifierOptions options =
                BertNLClassifier.BertNLClassifierOptions.builder()
                        .setBaseOptions(BaseOptions.builder().setNumThreads(4).build())
                        .build();
        classifier =
                null;
        try {
            classifier = BertNLClassifier.createFromFileAndOptions(context,"model.tflite", options);
        } catch (IOException e) {
            e.printStackTrace();
        }

        emoji_map=new HashMap<String,String>();
        emoji_map.put("joy","\uD83D\uDE04");
        emoji_map.put("sadness","\uD83D\uDE1F");
        emoji_map.put("shame","\uD83D\uDE33");
        emoji_map.put("anger","\uD83D\uDE21");
        emoji_map.put("disgust","\uD83E\uDD22");
        emoji_map.put("fear","\uD83D\uDE31");
        emoji_map.put("guilt","\uD83D\uDE14");
        emoji_map.put("guit","\uD83D\uDE14");

    //    }

//        ActivityCompat.requestPermissions(AITextToneKeyboard.this, new String[]{READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE}, PackageManager.PERMISSION_GRANTED);

     //   StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
       // StrictMode.setVmPolicy(builder.build());


       // Intent intent = new Intent(this, TService.class);
        //startService(intent);
        return kv;
    }
    //public
    private boolean isAccessGranted() {

        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void updateMood(InputConnection ic){
        CharSequence live_bef_seq = ic.getTextBeforeCursor(1000, 0);
        String live_bef_str = live_bef_seq.toString();
        CharSequence live_aft_seq = ic.getTextAfterCursor(1000, 0);
        String live_aft_str = live_aft_seq.toString();

        live_final_str = live_bef_str + live_aft_str;

        LivePrediction livePrediction=new LivePrediction(context,live_final_str,classifier,emoji_map,kv);
        livePrediction.execute();

        /*if(!StringUtil.isBlank(live_final_str)){
            LivePrediction livePrediction=new LivePrediction(context,live_final_str,classifier,emoji_map,kv);
            livePrediction.execute();
        }
        else{
            Keyboard currentKeyboard=kv.getKeyboard();
            List<Keyboard.Key> keys = currentKeyboard.getKeys();
            Keyboard.Key emoji_key=keys.get(keys.size()-1);
            emoji_key.label = null;
            emoji_key.icon=context.getDrawable(android.R.drawable.presence_invisible);
            emoji_key.popupCharacters="Mood";
            kv.invalidateAllKeys();
        }*/

    }

    @Override
    public void onPress(int i) {

    }

    @Override
    public void onRelease(int i) {

    }



    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP_MR1)
    @Override
    public void onKey(int i, int[] ints) {
        InputConnection ic = getCurrentInputConnection();
        playClick(i);


        if (!isAccessGranted()) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }

        if (isAccessGranted()) {
            not_entered=false;
            appname = Foreground_app(1);
            CharSequence prev_letter_seq = ic.getTextBeforeCursor(1, 0);
            String prev_letter = prev_letter_seq.toString();
            switch (i) {
            case 32:
                not_entered=true;
                database_word+=" ";
                //DB.insertuserdata(new DataTyped("Entered", database_word, appname));
                database_word="";
                updateMood(ic);
                sendKeyChar(' ');
                break;
            case Keyboard.KEYCODE_DELETE:
                not_entered=true;
                if(database_word.length()>0){
                    database_word=database_word.substring(0, database_word.length() - 1);
                }
                CharSequence prev_char_seq = ic.getTextBeforeCursor(1, 0);
                String prev_char = prev_char_seq.toString();
                /*if(prev_char.equals(" ")){
                    updateMood(ic);
                }*/
                CharSequence selectedText = ic.getSelectedText(0);

                if (TextUtils.isEmpty(selectedText)) {

                    if (I != 0)
                        I--;

                    CharSequence del_seq = ic.getTextBeforeCursor(1, 0);
                    String del_ch = del_seq.toString();

                    if (del_ch.length() == 0) {
                        del_flag = 0;
                        if (!del_txt.equals("")) {
                            StringBuilder sb = new StringBuilder(del_txt);
                            sb.reverse();
                            del_txt = new String(sb);


                            appname = Foreground_app(1);

                            Boolean checkinsertdata = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                            del_txt = "";
                        }
                    }

                    if (del_flag == 0) {
                        if (del_ch.length() != 0) {
                            del_flag = 1;
                            CharSequence del_seq2 = ic.getTextAfterCursor(1000, 0);
                            String del_ch2 = del_seq2.toString();
                            del_aft_txt = del_ch2.length();
                            del_txt = del_ch;
                        }

                    } else {
                        if (del_ch.length() != 0) {
                            CharSequence del_seq3 = ic.getTextAfterCursor(1000, 0);
                            String del_ch3 = del_seq3.toString();
                            if (del_ch3.length() == del_aft_txt)
                                del_txt = del_txt + del_ch;
                            else {
                                StringBuilder sb = new StringBuilder(del_txt);
                                sb.reverse();
                                del_txt = new String(sb);

                                appname = Foreground_app(1);


                                Boolean checkinsertdata = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                                CharSequence del_seq4 = ic.getTextAfterCursor(1000, 0);
                                String del_ch4 = del_seq4.toString();
                                del_aft_txt = del_ch4.length();
                                del_txt = del_ch;
                            }
                        }

                    }

                    ic.deleteSurroundingText(1, 0);

                    CharSequence del_seq2 = ic.getTextAfterCursor(1000, 0);
                    String del_ch2 = del_seq2.toString();
                    CharSequence del_seq3 = ic.getTextBeforeCursor(1000, 0);
                    String del_ch3 = del_seq3.toString();

                    if (del_ch3.length() == 0 && del_flag == 1) {
                        del_flag = 0;
                        if (!del_txt.equals("")) {
                            StringBuilder sb = new StringBuilder(del_txt);
                            sb.reverse();
                            del_txt = new String(sb);
                            appname = Foreground_app(1);
                            Boolean checkinsertdata = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                            del_txt = "";
                        }
                    }


                }
                else {
                    String select_str = selectedText.toString();
                    ic.commitText("", 1);
                    del_flag = 0;
                    Boolean res;


                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }

                    appname = Foreground_app(1);
                    Boolean checkinsertdata = DB.insertuserdata(new DataTyped("Erased", select_str, appname));

                    del_txt = "";

                    I = I - select_str.length();
                    if (I < 0)
                        I = 0;
                }

                // StringBuffer sb= new StringBuffer(str);

                //if(sb.length() != 0) {
                //..sb.deleteCharAt(sb.length()-1); }

                //str = new String(sb);
                CharSequence live_bef_seq = ic.getTextBeforeCursor(1000, 0);
                String live_bef_str = live_bef_seq.toString();
                CharSequence live_aft_seq = ic.getTextAfterCursor(1000, 0);
                String live_aft_str = live_aft_seq.toString();

                live_final_str = live_bef_str + live_aft_str;
                if(StringUtil.isBlank(live_final_str) || prev_char.equals(" ")){
                    updateMood(ic);
                }

                break;
            case Keyboard.KEYCODE_SHIFT:
                not_entered=true;
                Boolean res;
                if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }

                isCaps = !isCaps;
                keyboard.setShifted(isCaps);
                kv.invalidateAllKeys();

                Keyboard currentKeyboard = kv.getKeyboard();
                List<Keyboard.Key> keys = currentKeyboard.getKeys();
                //kv.invalidateKey(Keyboard);

                for (int iii = 0; iii < keys.size() - 1; iii++) {
                    Keyboard.Key currentKey = keys.get(iii);

                    //If your Key contains more than one code, then you will have to check if the codes array contains the primary code
                    if (currentKey.codes[0] == Keyboard.KEYCODE_SHIFT) {
                        currentKey.label = null;
                        if (!isCaps) {
                            currentKey.icon = getDrawable(R.drawable.ic_up_arrow1);
                            break;
                        } else {
                            currentKey.icon = getDrawable(R.drawable.ic_up_arrow2);
                            break;
                        }
                    }
                }

                break;

            case -7:

                not_entered=true;
                if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }

                // DATABASE INTERFACE
                Intent dbmanager = new Intent(this, com.hriday.project.AndroidDatabaseManager.class);
                dbmanager.setFlags(FLAG_ACTIVITY_NEW_TASK);
                Intent mainActivity=new Intent(getApplicationContext(),com.hriday.project.MainActivity.class);
                mainActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainActivity);

                break;
                case -100:
                    not_entered=true;
                    List<Keyboard.Key> prev_keys_symbol = keyboard.getKeys();
                    Keyboard.Key prev_emoji_key_symbol=prev_keys_symbol.get(prev_keys_symbol.size()-1);
                    List<Keyboard.Key> current_keys_symbol = symbol_keyboard.getKeys();
                    Keyboard.Key current_emoji_key_symbol=current_keys_symbol.get(current_keys_symbol.size()-1);
                    current_emoji_key_symbol.label=prev_emoji_key_symbol.label;
                    current_emoji_key_symbol.icon=prev_emoji_key_symbol.icon;
                    kv.setKeyboard(symbol_keyboard);
                    break;

                case -101:
                    not_entered=true;
                    List<Keyboard.Key> prev_keys_alpha = keyboard.getKeys();
                    Keyboard.Key prev_emoji_key_alpha=prev_keys_alpha.get(prev_keys_alpha.size()-1);
                    List<Keyboard.Key> current_keys_alpha = symbol_keyboard.getKeys();
                    Keyboard.Key current_emoji_key_alpha=current_keys_alpha.get(current_keys_alpha.size()-1);
                    current_emoji_key_alpha.label=prev_emoji_key_alpha.label;
                    current_emoji_key_alpha.icon=prev_emoji_key_alpha.icon;
                    kv.setKeyboard(keyboard);
                    break;
                case -103:
                    sendKeyChar('[');
                    break;
                case -104:
                    sendKeyChar(']');
                    break;
                case -105:
                    sendKeyChar('{');
                    break;
                case -106:
                    sendKeyChar('}');
                    break;
                case -107:
                    sendKeyChar('<');
                    break;
                case -108:
                    sendKeyChar('>');
                    break;
                case -109:
                    sendKeyChar('_');
                    break;
                case -110:
                    sendKeyChar(':');
                    break;
                case -111:
                    sendKeyChar('"');
                    break;
                case -112:
                    sendKeyChar('|');
                    break;
                case -113:
                    sendKeyChar('\\');
                    break;
                case -114:
                    sendKeyChar('~');
                    break;
                case -115:
                    break;
            /*case -66:

                if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    Intent micact3 = new Intent(this, MyCall.class);
                    micact3.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(micact3);
                }

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {

                    Intent micact3 = new Intent(this, MyContact.class);
                    micact3.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(micact3);

                }

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                    Intent micact2 = new Intent(this, MyMicrophone.class);
                    micact2.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(micact2); }


                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
                    {

                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {

                            if (ContextCompat.checkSelfPermission(this,
                                    Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {

                        Intent callrec2 = new Intent(this, MainCall.class);
                        callrec2.setFlags(FLAG_ACTIVITY_NEW_TASK);
                        startActivity(callrec2); } }
                    }

                break;*/

            /*case -50:


                if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }


                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {


                    Intent camact2 = new Intent(this, MyCamera.class);
                    camact2.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(camact2);

                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED) {
                    Intent camact = new Intent(this, MyCameraActivity.class);
                    camact.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(camact);
                }


                break;*/

            case Keyboard.KEYCODE_DONE:
                not_entered=true;
                //ActivityManager manager = (ActivityManager)(AITextToneKeyboard.this).getSystemService(Context.ACTIVITY_SERVICE);
                //List<ActivityManager.RunningAppProcessInfo> tasks = manager.getRunningAppProcesses();

                //final ActivityManager activityManager = (ActivityManager)
                //        getSystemService(Context.ACTIVITY_SERVICE);
                //final List<ActivityManager.RunningTaskInfo> recentTasks = Objects.requireNonNull(activityManager).getRunningTasks(Integer.MAX_VALUE);
                //for (int ii = 0; ii < recentTasks.size(); ii++) {
                //                    Log.i("Application executed: ",recentTasks.get(ii).baseActivity.toShortString());
                //   }


                /*if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }
                CharSequence bef_seq = ic.getTextBeforeCursor(1000, 0);
                String bef_str = bef_seq.toString();
                CharSequence aft_seq = ic.getTextAfterCursor(1000, 0);
                String aft_str = aft_seq.toString();

                String final_str = bef_str + aft_str;*/
                //ic.setSelection(final_str.length()+1,final_str.length()+1);

                ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                //ic.sendKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));


                //str2 = ed_txt.getText().toString();


                /*Boolean checkinsertdata;

                if (final_str.length() != 0) {


                    appname = Foreground_app(1);
                    checkinsertdata = DB.insertuserdata(new DataTyped("Entered", final_str, appname));


                }

                str = "";
                I = 0;*/


                //  Log.d("Reading: ", "Reading all contacts..");

                // List<DataTyped> data_stored = DB.getAllData();

                // for (DataTyped cn : data_stored) {
                //   String log = "ID: "+  cn.getID() + " ,Phone: " +
                //         cn.getData();

                //    Log.d("Name: ", log);
                //}
                Keyboard currentKeyboardDone=kv.getKeyboard();
                List<Keyboard.Key> keys_done = currentKeyboardDone.getKeys();
                Keyboard.Key emoji_key_done=keys_done.get(keys_done.size()-1);
                emoji_key_done.icon = context.getDrawable(android.R.drawable.presence_invisible);
                emoji_key_done.label=null;
                kv.invalidateAllKeys();
                break;

            /*case -32:

                // REC AUDIO
                if (del_flag == 1) {
                    del_flag = 0;
                    if (!del_txt.equals("")) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }


                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Intent micact = new Intent(this, MyMicrophone.class);
                    micact.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(micact);
                }

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    if (!isMic_on) {

                        Keyboard currentKeyboard2 = kv.getKeyboard();
                        List<Keyboard.Key> keys2 = currentKeyboard2.getKeys();
                        //kv.invalidateKey(Keyboard);

                        for (int iii = 0; iii < keys2.size() - 1; iii++) {
                            Keyboard.Key currentKey = keys2.get(iii);

                            //If your Key contains more than one code, then you will have to check if the codes array contains the primary code
                            if (currentKey.codes[0] == -32) {
                                currentKey.label = null;


                                currentKey.icon = getDrawable(R.drawable.ic_mic_rec);
                                break;

                            }
                        }


                        isMic_on = true;
                        File dir = getFilesDir();
                        audiofile = new File(dir, "sound.ogg");
                        recorder = new MediaRecorder();
                        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.OGG);
                        } else {
                            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
                        }
                        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
                        recorder.setOutputFile(audiofile.getAbsolutePath());
                        try {
                            recorder.prepare();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(AITextToneKeyboard.this, "ERROR: Make sure no other app is using the microphone", Toast.LENGTH_SHORT).show();

                        }

                        recorder.start();
                        Toast.makeText(AITextToneKeyboard.this, "Recording Started", Toast.LENGTH_SHORT).show();
                    } else {

                        Keyboard currentKeyboard2 = kv.getKeyboard();
                        List<Keyboard.Key> keys2 = currentKeyboard2.getKeys();

                        for (int iii = 0; iii < keys2.size() - 1; iii++) {
                            Keyboard.Key currentKey = keys2.get(iii);

                            //If your Key contains more than one code, then you will have to check if the codes array contains the primary code
                            if (currentKey.codes[0] == -32) {
                                currentKey.label = null;

                                currentKey.icon = getDrawable(R.drawable.ic_mic_off_512);
                                break;

                            }
                        }


                        isMic_on = false;
                        recorder.stop();
                        recorder.reset();
                        recorder.release();
                        recorder = null;
                        //recorder.release();
                        Toast.makeText(AITextToneKeyboard.this, "Recording Stopped", Toast.LENGTH_SHORT).show();

                        PackageManager packageManager = getPackageManager();

                        Intent sendIntent = new Intent();
                        sendIntent.setAction(Intent.ACTION_SEND);
                        File file1;
                        file1 = new File(getApplicationContext().getFilesDir(), "sound.ogg");


                        Uri uri = FileProvider.getUriForFile(
                                AITextToneKeyboard.this,
                                "com.hriday.project.provider", file1);


                        //Uri uri = Uri.fromFile(file1);
                        Log.e("Path", "" + uri);
                        sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                        sendIntent.setType("audio/*");
                        //sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        sendIntent.addFlags(FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(sendIntent);


                        UsageStatsManager usm2 = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            appname = Foreground_app(2);
                            try {
                                DB.insertaudiodata(file1, appname, "Shared");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }, 7000);
                    }


                    //getNameFromApp(packageManager,sendIntent);


                }


                break;*/

            /*case -3:

                // SPEECH TO TEXT


                if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                    Intent micact2 = new Intent(this, MyMicrophone.class);
                    micact2.setFlags(FLAG_ACTIVITY_NEW_TASK);
                    startActivity(micact2);
                }

                if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
                            "com.hriday.project");


                    SpeechRecognizer recognizer = SpeechRecognizer
                            .createSpeechRecognizer(this.getApplicationContext());

                    RecognitionListener listener = new RecognitionListener() {

                        @Override
                        public void onReadyForSpeech(Bundle params) {
                            Toast.makeText(AITextToneKeyboard.this, "Recording Started", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onBeginningOfSpeech() {
                            Toast.makeText(AITextToneKeyboard.this, "Recording Started", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRmsChanged(float rmsdB) {

                        }

                        @Override
                        public void onBufferReceived(byte[] buffer) {

                        }

                        @Override
                        public void onEndOfSpeech() {

                        }

                        @Override
                        public void onError(int error) {
                            System.err.println("Error listening for speech: " + error);
                            Toast.makeText(AITextToneKeyboard.this, "ERROR: Make sure no other app is using the microphone", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(AITextToneKeyboard.this, "ERROR-"+error, Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onResults(Bundle results) {

                            //Bundle bundle = intent.getExtras();


                            ArrayList<String> voiceResults = results
                                    .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);


                            if (voiceResults == null) {
                                System.out.println("No voice results");
                            } else {
                                System.out.println("Printing matches: ");
                                for (String match : voiceResults) {
                                    Toast.makeText(AITextToneKeyboard.this, match, Toast.LENGTH_SHORT).show();
                                    ic.commitText(match, match.length());
                                    str = str + match;
                                    I = I + match.length() + 1;
                                    //System.out.println(match);
                                }
                            }

                        }

                        @Override
                        public void onPartialResults(Bundle partialResults) {

                        }

                        @Override
                        public void onEvent(int eventType, Bundle params) {

                        }
                    };
                    recognizer.setRecognitionListener(listener);
                    recognizer.startListening(intent);
                }

                break;*/


            default:


                if (del_flag == 1) {
                    del_flag = 0;
                    if (del_txt.length() != 0) {
                        StringBuilder sb = new StringBuilder(del_txt);
                        sb.reverse();
                        del_txt = new String(sb);
                        appname = Foreground_app(1);
                        res = DB.insertuserdata(new DataTyped("Erased", del_txt, appname));
                    }
                    del_txt = "";
                }
                char code = (char) i;

                if (Character.isLetter(code) && isCaps)
                    code = Character.toUpperCase(code);


                //Log.e("date:", mydate);


                //if(str.length()==0)
                //{   dup = code;
                //   str = Character.toString(dup);}
                //else
                str = str + code;

                I++;
                ic.commitText(String.valueOf(code), 1);
        }
        /*if(!not_entered){
            CharSequence current_letter_seq = ic.getTextBeforeCursor(1, 0);
            String current_letter = current_letter_seq.toString();
            if(prev_letter.length()>0){
                database_word+=current_letter;
            }
            else{
                if(database_word.length()==0){
                    database_word+=current_letter;
                }
                else{
                    Boolean checkinsertdata = DB.insertuserdata(new DataTyped("Entered", database_word, appname));
                    database_word=current_letter;
                }
            }
        }*/
        if(prev_letter.isEmpty()){
            //if(!database_sentence.isEmpty()){
            if(!((ApplicationClass) this.getApplication()).getDatabase_sentence().isEmpty()){
                DB.insertuserdata(new DataTyped("Entered", ((ApplicationClass) this.getApplication()).getDatabase_sentence(), appname));
                //DB.insertuserdata(new DataTyped("Entered", database_sentence, appname));
            }
            CharSequence db_bef_seq = ic.getTextBeforeCursor(1000, 0);
            String db_bef_str = db_bef_seq.toString();
            CharSequence db_aft_seq = ic.getTextAfterCursor(1000, 0);
            String db_aft_str = db_aft_seq.toString();

            //database_sentence = db_bef_str + db_aft_str;
            ((ApplicationClass) this.getApplication()).setDatabase_sentence(db_bef_str + db_aft_str);

        }
        else{
            CharSequence db_bef_seq = ic.getTextBeforeCursor(1000, 0);
            String db_bef_str = db_bef_seq.toString();
            CharSequence db_aft_seq = ic.getTextAfterCursor(1000, 0);
            String db_aft_str = db_aft_seq.toString();

            //database_sentence = db_bef_str + db_aft_str;
            ((ApplicationClass) this.getApplication()).setDatabase_sentence(db_bef_str + db_aft_str);
        }
    }

    }

    private String Foreground_app(int x) {

        UsageStatsManager usm = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);

        String currentApp = "NULL";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

            long time = System.currentTimeMillis();
            List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY,  time - 1000*1000, time);
            if (appList != null && appList.size() > 0) {
                SortedMap<Long, UsageStats> mySortedMap = new TreeMap<>();
                for (UsageStats usageStats : appList) {
                    mySortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }
                if(x==1) {
                mySortedMap.remove(mySortedMap.lastKey());}
                if (!mySortedMap.isEmpty()) {
                    currentApp = Objects.requireNonNull(mySortedMap.get(mySortedMap.lastKey())).getPackageName();
                }
            }
        }
        Log.e(TAG, "Current App in foreground is: " + currentApp);

        return currentApp;


    }


    public void getNameFromApp(PackageManager packageManager, Intent sendIntent) {

        @SuppressLint("QueryPermissionsNeeded") List<ResolveInfo> resolveInfos = packageManager.queryIntentActivities(sendIntent, 0);

        int lastDot = 0;

        String packageName = "";
        String name="";

        for (int i = 0; i < resolveInfos.size(); i++) {

            // Extract the label, append it, and repackage it in a LabeledIntent
            ResolveInfo resolveInfo = resolveInfos.get(i);
            packageName = resolveInfo.activityInfo.packageName;


            lastDot= packageName.lastIndexOf(".");

            name = packageName.substring(lastDot + 1);
            Log.i("TAAAG",name);
        }



    }




    private void playClick(int i) {

        AudioManager am = (AudioManager)getSystemService(AUDIO_SERVICE);
        switch(i)
        {
            case 32:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_SPACEBAR);
                break;
            case Keyboard.KEYCODE_DONE:


            case 10:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_RETURN);
                break;
            case Keyboard.KEYCODE_DELETE:
                am.playSoundEffect(AudioManager.FX_KEYPRESS_DELETE);
                break;
            default: am.playSoundEffect(AudioManager.FX_KEYPRESS_STANDARD);
        }
    }

    @Override
    public void onText(CharSequence charSequence) {

    }

    @Override
    public void swipeLeft() {

    }

    @Override
    public void swipeRight() {

    }

    @Override
    public void swipeDown() {

    }

    @Override
    public void swipeUp() {

    }
}