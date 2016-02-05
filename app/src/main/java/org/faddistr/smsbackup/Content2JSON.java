package org.faddistr.smsbackup;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Iterator;

public class Content2JSON {
    private Uri iUri;
    private ContentResolver iResolver = null;
    private Cursor iCursor = null;

    public Content2JSON(Context iCon, String uri)
    {
        if((iCon == null) || (uri == null))
        {
            throw new IllegalArgumentException();
        }

        iResolver = iCon.getContentResolver();
        iUri = Uri.parse(uri);
    }

    private void MakeCursor(){
        if(iCursor == null) {
            iCursor = iResolver.query(iUri, null, null, null, null);
            if (!iCursor.moveToFirst() || (iCursor.getCount() == 0)) {
                iCursor = null;
            }
        }else
        {
            if(!iCursor.moveToNext())
            {
                iCursor = null;
            }
        }
    }

    protected Cursor getCursor()
    {
        MakeCursor();
        return iCursor;
    }

    protected ContentResolver getResolver()
    {
        return iResolver;
    }

    public int getCount()
    {
        int cnt=0;
        MakeCursor();
        if(iCursor!=null) {
            cnt=iCursor.getCount();
        }
        this.iCursor = null;
        return cnt;
    }

    public JSONObject readNext()
    {
        MakeCursor();
        JSONObject row=new JSONObject();

        for(int idx=0;idx<iCursor.getColumnCount();idx++)
        {
            try{
                switch(iCursor.getType(idx)){
                    case Cursor.FIELD_TYPE_INTEGER:
                        row.put(iCursor.getColumnName(idx), iCursor.getLong(idx));
                        break;
                    case Cursor.FIELD_TYPE_FLOAT:
                        row.put(iCursor.getColumnName(idx), iCursor.getDouble(idx));
                        break;
                    case Cursor.FIELD_TYPE_STRING:
                        row.put(iCursor.getColumnName(idx), iCursor.getString(idx));
                        break;
                    default:
                        break;
                }
                row.put(iCursor.getColumnName(idx), iCursor.getString(idx));
            }catch (Exception e){

            }
        }

        return row;
    }

    public void insertNext(JSONObject obj) throws Exception
    {
        insertNext(obj, null);
    }

    public void insertNext(JSONObject obj, ArrayList<String> ignFields) throws Exception
    {
        ContentValues val= new ContentValues();
        Iterator<String> itr = obj.keys();
        while(itr.hasNext())
        {
            String kStr = itr.next();
            if(ignFields!=null) {
                if (ignFields.contains(kStr)) {
                    continue;
                }
            }

            try {
                Object m = obj.get(kStr);

                if(m instanceof Long){
                    val.put(kStr, (Long)m);
                    continue;
                }

                if(m instanceof String){
                    val.put(kStr, (String)m);
                    continue;
                }

                if(m instanceof Float){
                    val.put(kStr, (Float)m);
                    continue;
                }

            }catch (Exception e)
            {
                throw new Exception("Bad format. Error: "+e.getMessage());
            }
        }

        iResolver.insert(iUri, val);
        this.iCursor = null;
    }
}
