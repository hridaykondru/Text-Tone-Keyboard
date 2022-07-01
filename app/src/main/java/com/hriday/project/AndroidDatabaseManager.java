package com.hriday.project;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.text.SpannableString;
import android.text.format.DateFormat;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class AndroidDatabaseManager extends Fragment {




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    static class indexInfo
    {
        public static int index = 10;
        public static int numberofpages = 0;
        public static int currentpage = 0;
        public static String table_name="";
        public static Cursor maincursor;
        public static int cursorpostion=0;
        public static ArrayList<String> value_string;
        public static ArrayList<String> tableheadernames;
        public static ArrayList<String> emptytablecolumnnames;
        public static boolean isEmpty;
        public static boolean isCustomQuery;
    }

// all global variables

    //in the below line Change the text 'yourCustomSqlHelper' with your custom sqlitehelper class name.
    //Do not change the variable name dbm
    DBHelper dbm;
    TableLayout tableLayout;
    TableRow.LayoutParams tableRowParams;
    HorizontalScrollView hsv;
    ScrollView mainscrollview;
    LinearLayout mainLayout;
    TextView tvmessage;
    Button previous;
    Button next;
    Button exportData;
    Spinner select_table;
    TextView tv;
    File audio_file = null;

    AndroidDatabaseManager.indexInfo info = new AndroidDatabaseManager.indexInfo();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        //in the below line Change the text 'yourCustomSqlHelper' with your custom sqlitehelper class name
        dbm = ((ApplicationClass)this.getActivity().getApplication()).getDB();

        mainscrollview = new ScrollView(getContext());

        //the main linear layout to which all tables spinners etc will be added.In this activity every element is created dynamically  to avoid using xml file
        mainLayout = new LinearLayout(this.getContext());
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setBackgroundColor(Color.WHITE);
        mainLayout.setScrollContainer(true);
        mainscrollview.addView(mainLayout);

        //all required layouts are created dynamically and added to the main scrollview
        //setContentView(mainscrollview);
       // View rootView=inflater.inflate((XmlPullParser) mainscrollview, container, false);
        View rootView = mainscrollview;
       // View rootView=inflater.inflate(R.layout.fragment_mood_prediction, container, false);
        //the first row of layout which has a text view and spinner
        final LinearLayout firstrow = new LinearLayout(this.getContext());
        firstrow.setPadding(0,10,0,20);
        LinearLayout.LayoutParams firstrowlp = new LinearLayout.LayoutParams(0, 150);
        firstrowlp.weight = 1;

        TextView maintext = new TextView(this.getContext());
        maintext.setText("Select Table");
        maintext.setTextSize(18);
        maintext.setLayoutParams(firstrowlp);
        select_table=new Spinner(this.getContext());
        select_table.setLayoutParams(firstrowlp);

        firstrow.addView(maintext);
        firstrow.addView(select_table);
        mainLayout.addView(firstrow);

        ArrayList<Cursor> alc ;

        //the horizontal scroll view for table if the table content doesnot fit into screen
        hsv = new HorizontalScrollView(this.getContext());

        //the main table layout where the content of the sql tables will be displayed when user selects a table
        tableLayout = new TableLayout(this.getContext());
        tableLayout.setHorizontalScrollBarEnabled(true);
        hsv.addView(tableLayout);

        //the second row of the layout which shows number of records in the table selected by user
        final LinearLayout secondrow = new LinearLayout(this.getContext());
        secondrow.setPadding(0,20,0,10);
        LinearLayout.LayoutParams secondrowlp = new LinearLayout.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT);
        secondrowlp.weight = 1;
        TextView secondrowtext = new TextView(this.getContext());
        secondrowtext.setText("No. Of Records : ");
        secondrowtext.setTextSize(15);
        //secondrowtext.setLayoutParams(secondrowlp);
        tv =new TextView(this.getContext());
        tv.setTextSize(18);
        tv.setTextColor(Color.parseColor("#000000"));
        //tv.setLayoutParams(secondrowlp);
        secondrow.addView(secondrowtext);
        secondrow.addView(tv);
        mainLayout.addView(secondrow);

        hsv.setPadding(0,10,0,10);
        hsv.setScrollbarFadingEnabled(false);
        hsv.setScrollBarStyle(View.SCROLLBARS_OUTSIDE_INSET);
        mainLayout.addView(hsv);
        //the third layout which has buttons for the pagination of content from database
        final LinearLayout thirdrow = new LinearLayout(this.getContext());
        previous = new Button(this.getContext());
        previous.setText("Previous");

        previous.setBackgroundColor(Color.parseColor("#BAE7F6"));
        previous.setLayoutParams(secondrowlp);
        next = new Button(this.getContext());
        next.setText("Next");
        next.setBackgroundColor(Color.parseColor("#BAE7F6"));
        next.setLayoutParams(secondrowlp);
        TextView tvblank = new TextView(getContext());
        tvblank.setLayoutParams(secondrowlp);
        thirdrow.setPadding(0,10,0,10);
        thirdrow.addView(previous);
        thirdrow.addView(tvblank);
        thirdrow.addView(next);
        mainLayout.addView(thirdrow);

        //the text view at the bottom of the screen which displays error or success messages after a query is executed
        tvmessage =new TextView(this.getContext());

        tvmessage.setText("Error Messages will be displayed here");
        String Query = "SELECT name _id FROM sqlite_master WHERE type ='table'";
        tvmessage.setTextSize(12);
        mainLayout.addView(tvmessage);

        exportData = new Button(this.getContext());
        exportData.setText("exportData");

        exportData.setBackgroundColor(Color.parseColor("#BAE7F6"));
        exportData.setLayoutParams(secondrowlp);
        mainLayout.addView(exportData);


        tableRowParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        tableRowParams.setMargins(3, 1, 3, 1);

        // a query which returns a cursor with the list of tables in the database.We use this cursor to populate spinner in the first row
        alc = dbm.getData(Query);
        //the first cursor has reults of the query
        final Cursor c=alc.get(0);


        //the second cursor has error messages
        Cursor Message =alc.get(1);

        Message.moveToLast();
        String msg = Message.getString(0);
        Log.d("Message from sql = ",msg);

        ArrayList<String> tablenames = new ArrayList<String>();

        if(c!=null)
        {

            c.moveToFirst();
            tablenames.add("click here");
            do{
                //add names of the table to tablenames array list
                tablenames.add(c.getString(0));
            }while(c.moveToNext());
        }
        //an array adapter with above created arraylist
        ArrayAdapter<String> tablenamesadapter = new ArrayAdapter<String>(this.getContext(),
                android.R.layout.simple_spinner_item, tablenames) {

            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);

                v.setBackgroundColor(Color.WHITE);
                TextView adap =(TextView)v;
                adap.setTextSize(20);

                return adap;
            }


            public View getDropDownView(int position,  View convertView,  ViewGroup parent) {
                View v =super.getDropDownView(position, convertView, parent);

                v.setBackgroundColor(Color.WHITE);

                return v;
            }
        };

        tablenamesadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        if(tablenamesadapter!=null)
        {
            //set the adpater to select_table spinner
            select_table.setAdapter(tablenamesadapter);
        }

        // when a table names is selecte display the table contents
        select_table.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent,
                                       View view, int pos, long id) {
                if(pos==0&&!AndroidDatabaseManager.indexInfo.isCustomQuery)
                {
                    secondrow.setVisibility(View.GONE);
                    hsv.setVisibility(View.GONE);
                    thirdrow.setVisibility(View.GONE);
                    tvmessage.setVisibility(View.GONE);
                }
                if(pos!=0){
                    secondrow.setVisibility(View.VISIBLE);
                    hsv.setVisibility(View.VISIBLE);

                    tvmessage.setVisibility(View.VISIBLE);

                    thirdrow.setVisibility(View.VISIBLE);
                    c.moveToPosition(pos-1);
                    AndroidDatabaseManager.indexInfo.cursorpostion=pos-1;
                    //displaying the content of the table which is selected in the select_table spinner
                    Log.d("selected table name is",""+c.getString(0));
                    AndroidDatabaseManager.indexInfo.table_name=c.getString(0);
                    tvmessage.setText("Error Messages will be displayed here");
                    tvmessage.setBackgroundColor(Color.WHITE);

                    //removes any data if present in the table layout
                    tableLayout.removeAllViews();

                    String Query2 ="select * from "+c.getString(0);
                    Log.d("",""+Query2);

                    //getting contents of the table which user selected from the select_table spinner
                    ArrayList<Cursor> alc2=dbm.getData(Query2);
                    final Cursor c2=alc2.get(0);


                    //saving cursor to the static indexinfo class which can be resued by the other functions
                    AndroidDatabaseManager.indexInfo.maincursor=c2;

                    // if the cursor returned form the database is not null we display the data in table layout
                    if(c2!=null)
                    {
                        int counts = c2.getCount();
                        AndroidDatabaseManager.indexInfo.isEmpty=false;
                        Log.d("counts",""+counts);
                        tv.setText(""+counts);



                        //display the first row of the table with column names of the table selected by the user
                        TableRow tableheader = new TableRow(getActivity().getApplication().getApplicationContext());

                        tableheader.setBackgroundColor(Color.BLACK);
                        tableheader.setPadding(3, 3, 3, 3);
                        for(int k=0;k<c2.getColumnCount();k++)
                        {
                            LinearLayout cell = new LinearLayout(getContext());
                            cell.setBackgroundColor(Color.WHITE);
                            cell.setLayoutParams(tableRowParams);
                            final TextView tableheadercolums = new TextView(getActivity().getApplication().getApplicationContext());
                            // tableheadercolums.setBackgroundDrawable(gd);
                            tableheadercolums.setPadding(3, 1, 3, 1);
                            tableheadercolums.setText(""+c2.getColumnName(k));
                            tableheadercolums.setTextColor(Color.parseColor("#000000"));
                            tableheadercolums.setTypeface(null, Typeface.BOLD);

                            //columsView.setLayoutParams(tableRowParams);
                            cell.addView(tableheadercolums);
                            tableheader.addView(cell);

                        }
                        tableLayout.addView(tableheader);
                        c2.moveToFirst();


                        paginatetable(c2.getCount());

                    }
                    else{

                        tableLayout.removeAllViews();
                        getcolumnnames();
                        TableRow tableheader2 = new TableRow(getActivity().getApplication().getApplicationContext());
                        tableheader2.setBackgroundColor(Color.BLACK);
                        tableheader2.setPadding(0, 2, 0, 2);

                        LinearLayout cell = new LinearLayout(getContext());
                        cell.setBackgroundColor(Color.WHITE);
                        cell.setLayoutParams(tableRowParams);
                        final TextView tableheadercolums = new TextView(getActivity().getApplication().getApplicationContext());

                        tableheadercolums.setPadding(0, 0, 4, 3);
                        tableheadercolums.setText("   Table   Is   Empty   ");
                        tableheadercolums.setTextSize(30);
                        tableheadercolums.setTextColor(Color.RED);

                        cell.addView(tableheadercolums);
                        tableheader2.addView(cell);


                        tableLayout.addView(tableheader2);

                        tv.setText(""+0);
                    }
                }}
            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        return rootView;
    }

    //get columnnames of the empty tables and save them in a array list
    public void getcolumnnames()
    {
        ArrayList<Cursor> alc3=dbm.getData("PRAGMA table_info("+ AndroidDatabaseManager.indexInfo.table_name+")");
        Cursor c5=alc3.get(0);



        AndroidDatabaseManager.indexInfo.isEmpty=true;
        if(c5!=null)
        {
            AndroidDatabaseManager.indexInfo.isEmpty=true;

            ArrayList<String> emptytablecolumnnames= new ArrayList<String>();
            c5.moveToFirst();
            do
            {
                emptytablecolumnnames.add(c5.getString(1));
            }while(c5.moveToNext());
            AndroidDatabaseManager.indexInfo.emptytablecolumnnames=emptytablecolumnnames;
        }



    }






    //the function which displays tuples from database in a table layout
    public void paginatetable(final int number)
    {


        final Cursor c3 = AndroidDatabaseManager.indexInfo.maincursor;


        AndroidDatabaseManager.indexInfo.numberofpages=(c3.getCount()/20)+1;
        if(c3.getCount()%20 == 0){
            AndroidDatabaseManager.indexInfo.numberofpages = c3.getCount()/20;}


        AndroidDatabaseManager.indexInfo.currentpage=1;
        c3.moveToFirst();
        int currentrow=0;

        //display the first 10 tuples of the table selected by user
        do
        {

            final TableRow tableRow = new TableRow(getActivity().getApplication().getApplicationContext());

            tableRow.setBackgroundColor(Color.BLACK);
            tableRow.setPadding(3, 1, 3, 1);
            String type="";
            for(int j=0;j<c3.getColumnCount();j++)
            {
                LinearLayout cell = new LinearLayout(getContext());
                cell.setBackgroundColor(Color.WHITE);
                cell.setLayoutParams(tableRowParams);
                //Button columsView = null;
                //TextView columsView2 = null;
                final TextView columsView2 = new TextView(getActivity().getApplication().getApplicationContext());




                String column_data = "";
                int xy;
                xy=0;
                try{

                    if(c3.getString(j)==null)
                    {
                        column_data="NULL";
                        xy=1;
                    }
                    else {
                        column_data = c3.getString(j); }
                }catch(Exception e){
                    // Column data is not a string , do not display it
                    column_data="Play Audio";
                    xy=2;

                }



                if(xy==0) {
                    columsView2.setText(column_data);
                    columsView2.setTextColor(Color.parseColor("#000000"));
                    columsView2.setTypeface(null, Typeface.NORMAL);}
                if(xy==1) {
                    columsView2.setText(column_data);
                    columsView2.setTextColor(Color.parseColor("#808080"));
                    columsView2.setTypeface(null, Typeface.ITALIC); }
                if(xy==2)
                {
                    String splay = "Play Audio";
                    SpannableString content = new SpannableString(splay);
                    content.setSpan(new UnderlineSpan(), 0, splay.length(), 0);
                    columsView2.setText(content);
                    columsView2.setTextColor(Color.parseColor("#000000"));
                    columsView2.setTypeface(null, Typeface.NORMAL);
                }
                columsView2.setPadding(0, 0, 7, 3);
                cell.addView(columsView2);
                tableRow.addView(cell);





            }



            tableRow.setVisibility(View.VISIBLE);

            //we create listener for each table row when clicked a alert dialog will be displayed
            //from where user can update or delete the row
            //int finalCurrentrow = currentrow;
            int finalCurrentrow1 = currentrow;
            currentrow=currentrow+1;

            tableRow.setOnClickListener(v -> {

                final ArrayList<String> value_string = new ArrayList<String>();
                byte[] blob;

                String datatype = null;

                for(int i=0;i<c3.getColumnCount();i++)
                {
                    LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(i);
                    TextView tc =(TextView)llcolumn.getChildAt(0);

                    String cv =tc.getText().toString();
                    value_string.add(cv);

                    if(i==1)
                    {
                        datatype = tc.getText().toString();
                    }

                    //Log.i("tt",datatype+"hi");

                    if(i == 3) {
                        assert datatype != null;
                        if (datatype.equals("Image"))
                        {
                            tc.setTextColor(Color.parseColor("#000080"));
                            tc.setTypeface(null, Typeface.BOLD);
                            final Handler handler = new Handler(Looper.getMainLooper());
                            handler.postDelayed(() -> {
                                tc.setTextColor(Color.parseColor("#000000"));
                                tc.setTypeface(null, Typeface.NORMAL);
                            }, 275);

                            /*Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri uri = Uri.parse("content:/" + cv);
                            intent.setDataAndType(uri, "image/jpg");
                            startActivity(intent);*/

                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            File imagePath = new File(cv);
                            Uri contentUri = FileProvider.getUriForFile(getActivity().getApplication().getApplicationContext(), this.getActivity().getApplication().getApplicationContext().getPackageName() + ".provider", imagePath);
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                            intent.setDataAndType(contentUri,"image/*");
                            getActivity().getApplication().getApplicationContext().startActivity(intent);
                        }
                    }
                    //String tc_str = tc.getText().toString();

                    if(i==4 && cv=="Play Audio")
                    {
                        //TextView col2 = new TextView(getActivity().getApplication().getApplicationContext());
                        tc.setTextColor(Color.parseColor("#000080"));
                        tc.setTypeface(null, Typeface.BOLD);
                        final Handler handler = new Handler(Looper.getMainLooper());
                        handler.postDelayed(() -> {
                            tc.setTextColor(Color.parseColor("#000000"));
                            tc.setTypeface(null, Typeface.NORMAL);
                        }, 275);

                        blob = dbm.getCell(finalCurrentrow1);

                        //blob = c3.getBlob(c3.getColumnIndex("AudioData"));
                        if (blob != null) {convertByyeToMP3(blob);}
                        else
                        {
                            Log.i("TAG3", String.valueOf(finalCurrentrow1));
                        }
                    }

                }

                AndroidDatabaseManager.indexInfo.value_string=value_string;
                //the below function will display the alert dialog
                /// updateDeletePopup(0);
            });

            tableLayout.addView(tableRow);


        }while(c3.moveToNext()&&currentrow<20);

        AndroidDatabaseManager.indexInfo.index=currentrow;



        // when user clicks on the previous button update the table with the previous 10 tuples from the database
        previous.setOnClickListener(v -> {
            int tobestartindex=(AndroidDatabaseManager.indexInfo.currentpage-2)*20;

            //if the tbale layout has the first 10 tuples then toast that this is the first page
            if(AndroidDatabaseManager.indexInfo.currentpage==1)
            {
                Toast.makeText(getActivity().getApplication().getApplicationContext(), "This is the first page", Toast.LENGTH_LONG).show();
            }
            else
            {
                AndroidDatabaseManager.indexInfo.currentpage= AndroidDatabaseManager.indexInfo.currentpage-1;
                c3.moveToPosition(tobestartindex);

                boolean decider=true;
                for(int i=1;i<tableLayout.getChildCount();i++)
                {
                    TableRow tableRow = (TableRow) tableLayout.getChildAt(i);


                    if(decider)
                    {
                        tableRow.setVisibility(View.VISIBLE);
                        String type="";
                        for(int j=0;j<tableRow.getChildCount();j++)
                        {
                            LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(j);
                            TextView columsView = (TextView) llcolumn.getChildAt(0);



                            try{


                                if(c3.getString(j)==null)
                                {
                                    columsView.setText(""+"NULL");
                                    columsView.setTextColor(Color.parseColor("#808080"));
                                    columsView.setTypeface(null, Typeface.ITALIC);
                                }
                                else {

                                    columsView.setText(""+c3.getString(j));
                                    columsView.setTextColor(Color.parseColor("#000000"));
                                    columsView.setTypeface(null, Typeface.NORMAL);}
                            }catch(Exception e){
                                // Column data is not a string , do not display it
                               /* byte[] ablob = c3.getBlob(j);
                                String str = new String(ablob);
                                columsView.setText(""+str); */
                                String splay = "Play Audio";
                                SpannableString content = new SpannableString(splay);
                                content.setSpan(new UnderlineSpan(), 0, splay.length(), 0);
                                columsView.setText(content);
                                columsView.setTextColor(Color.parseColor("#000000"));
                                columsView.setTypeface(null, Typeface.NORMAL);

                            }



                        }
                        decider=!c3.isLast();
                        if(!c3.isLast()){c3.moveToNext();}
                    }
                    else
                    {
                        tableRow.setVisibility(View.GONE);
                    }

                }

                AndroidDatabaseManager.indexInfo.index=tobestartindex;

                Log.d("index =",""+ AndroidDatabaseManager.indexInfo.index);
            }
        });

        exportData.setOnClickListener(v -> {

      //      List<DataTyped> myData = dbm.getAllData();
      //      Log.i("DATA ", myData.get(1).getData());

                //    ("select * from User_Data");


            final Cursor c4 = AndroidDatabaseManager.indexInfo.maincursor;
            c4.moveToFirst();
            String s = "";
            String myS = "";
            String tempStr = "";
            do{
                for (int j = 0; j < c4.getColumnCount(); j++) {
                    tempStr = c4.getString(j);
                    s+=tempStr;
                    if (j == 3) {
                        myS += tempStr;
                        myS += "\n";
                    }
                    s+="|";
                }
                s+="\n";

            }while(c4.moveToNext());
//Log.i("HRIDAY DATA", myS);
            String h;


            try{
                h = DateFormat.format("MM-dd-yyyyy-h-mmssaa", System.currentTimeMillis()).toString();
                // this will create a new name everytime and unique
                File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "Keyboard");
                root.mkdirs(); // this will create folder.
                File filepath = new File(root, h + ".txt"); // file path to save
                FileWriter writer = new FileWriter(filepath);
                writer.append(s.toString());
                writer.flush();
                writer.close();
                //File txtFile=filepath.getAbsoluteFile();
                File txtFile = File.createTempFile(h,".txt",root);
                FileWriter sendwriter = new FileWriter(txtFile);
                sendwriter.append(myS);
                sendwriter.flush();
                sendwriter.close();
                if (!txtFile.exists()){
                    Toast.makeText(getActivity().getApplication().getApplicationContext(), "File doesn't exists", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent intentShare = new Intent(Intent.ACTION_SEND);
                intentShare.setType("text/*");
                //intentShare.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+txtFile));
                intentShare.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getActivity().getApplication().getApplicationContext(), getActivity().getApplication().getPackageName()+".provider", txtFile));
                startActivity(Intent.createChooser(intentShare, "Share the file ..."));
                txtFile.deleteOnExit();
                Toast.makeText(getActivity().getApplication().getApplicationContext(), "File generated with name " + h + ".txt", Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Toast.makeText(getActivity().getApplication().getApplicationContext(), ""+e, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        });
        // when user clicks on the next button update the table with the next 10 tuples from the database
        next.setOnClickListener(v -> {

            //if there are no tuples to be shown toast that this the last page
            if(AndroidDatabaseManager.indexInfo.currentpage>= AndroidDatabaseManager.indexInfo.numberofpages)
            {
                Toast.makeText(getActivity().getApplication().getApplicationContext(), "This is the last page", Toast.LENGTH_LONG).show();
            }
            else
            {
                AndroidDatabaseManager.indexInfo.currentpage= AndroidDatabaseManager.indexInfo.currentpage+1;
                boolean decider=true;


                for(int i=1;i<tableLayout.getChildCount();i++)
                {
                    TableRow tableRow = (TableRow) tableLayout.getChildAt(i);


                    if(decider)
                    {
                        tableRow.setVisibility(View.VISIBLE);
                        for(int j=0;j<tableRow.getChildCount();j++)
                        {
                            LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(j);
                            TextView columsView =(TextView)llcolumn.getChildAt(0);

                            try{

                                if(c3.getString(j)==null)
                                {
                                    columsView.setText(""+"NULL");
                                    columsView.setTextColor(Color.parseColor("#808080"));
                                    columsView.setTypeface(null, Typeface.ITALIC);
                                }
                                else {
                                    columsView.setText(""+c3.getString(j));
                                    columsView.setTextColor(Color.parseColor("#000000"));
                                    columsView.setTypeface(null, Typeface.NORMAL);}
                            }catch(Exception e){
                                // Column data is not a string , do not display it
                              /*  byte[] ablob = c3.getBlob(j);
                                String str = new String(ablob);
                                columsView.setText(""+str); */
                                String splay = "Play Audio";
                                SpannableString content = new SpannableString(splay);
                                content.setSpan(new UnderlineSpan(), 0, splay.length(), 0);
                                columsView.setText(content);
                                columsView.setTextColor(Color.parseColor("#000000"));
                                columsView.setTypeface(null, Typeface.NORMAL);

                            }
                            //columsView.setText(""+c3.getString(j));

                        }

                        int finalI = i;
                        tableRow.setOnClickListener(v1 -> {
                                    byte[] blob;
                                    int finalCurrentrow1=((AndroidDatabaseManager.indexInfo.currentpage-1)*20) +  finalI - 1;
                                    String datatype="";

                                    for(int ii=0;ii<c3.getColumnCount();ii++)
                                    {
                                        LinearLayout llcolumn = (LinearLayout) tableRow.getChildAt(ii);
                                        TextView tc =(TextView)llcolumn.getChildAt(0);
                                        String tc_str = tc.getText().toString();

                                        if(ii==1)
                                        {
                                            datatype = tc.getText().toString();
                                        }

                                        //Log.i("tt",datatype+"hi");

                                        if(ii == 3) {
                                            assert datatype != null;
                                            if (datatype.equals("Image"))
                                            {
                                                tc.setTextColor(Color.parseColor("#000080"));
                                                tc.setTypeface(null, Typeface.BOLD);
                                                final Handler handler = new Handler(Looper.getMainLooper());
                                                handler.postDelayed(() -> {
                                                    tc.setTextColor(Color.parseColor("#000000"));
                                                    tc.setTypeface(null, Typeface.NORMAL);
                                                }, 275);

                                                Intent intent = new Intent();
                                                intent.setAction(Intent.ACTION_VIEW);
                                                File imagePath = new File(tc_str);
                                                Uri contentUri = FileProvider.getUriForFile(getActivity().getApplication().getApplicationContext(), this.getActivity().getApplication().getApplicationContext().getPackageName() + ".provider", imagePath);
                                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                intent.setDataAndType(contentUri,"image/*");
                                                getActivity().getApplication().getApplicationContext().startActivity(intent);
                                            }
                                        }

                                        if(ii==4 && tc_str=="Play Audio")
                                        {
                                            tc.setTextColor(Color.parseColor("#000080"));
                                            tc.setTypeface(null, Typeface.BOLD);

                                            final Handler handler = new Handler(Looper.getMainLooper());
                                            handler.postDelayed(() -> {
                                                tc.setTextColor(Color.parseColor("#000000"));
                                                tc.setTypeface(null, Typeface.NORMAL);
                                            }, 275);
                                            blob = dbm.getCell(finalCurrentrow1);

                                            //blob = c3.getBlob(c3.getColumnIndex("AudioData"));
                                            if (blob != null) {convertByyeToMP3(blob);}
                                            else
                                            {
                                                Log.i("TAG3", String.valueOf(finalCurrentrow1));
                                            }
                                        }
                                    }

                                }
                        );



                        decider=!c3.isLast();
                        if(!c3.isLast()){c3.moveToNext();}
                    }
                    else
                    {
                        tableRow.setVisibility(View.GONE);
                    }
                }
            }
        });

    }

    private void convertByyeToMP3(byte[] blob) {
        /*try {
            //ContextWrapper c = new ContextWrapper(getActivity().getApplication().getApplicationContext());
            //File directory = new File(c.getFilesDir().getAbsolutePath());

            audio_file = new File(getFilesDir(),"Audio_btp.mp3");
            FileOutputStream fos2 = new FileOutputStream(audio_file);
            fos2.write(blob);
            fos2.close();

            MediaPlayer mp = new MediaPlayer();

            try {
                mp.setDataSource(getFilesDir() + File.separator + "Audio_btp.mp3");
                mp.prepare();
                mp.start();
            } catch (Exception e) {
                e.printStackTrace();
            }


            Log.i("TAG2","SUCCESS");

            //Log.i("Byte array to mp3 conversion: ", "successfull");
        } catch (Exception ex) {
            Log.i("TAG2","FAILED");
            //Log.d("In convertToByteToMp3 Function:", ex.toString());
        }*/


    }




}