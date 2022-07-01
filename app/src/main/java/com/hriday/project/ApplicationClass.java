package com.hriday.project;

public class ApplicationClass extends android.app.Application{
    private DBHelper DB=new DBHelper(this);
    private String database_sentence="";
    public DBHelper getDB(){
        return(this.DB);
    }
    public String getDatabase_sentence(){return (this.database_sentence);}
    public void setDatabase_sentence(String new_sentence){
        database_sentence=new_sentence;
    }
}
