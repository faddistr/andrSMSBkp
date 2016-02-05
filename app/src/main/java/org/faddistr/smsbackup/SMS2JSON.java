package org.faddistr.smsbackup;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import org.json.JSONObject;
import java.util.ArrayList;


public class SMS2JSON extends Content2JSON {

    private ArrayList<String> ignList;


    public SMS2JSON(Context iCon, String uri)
    {
        super(iCon, uri);
        ignList = new ArrayList<>();
        ignList.add("_id");
        ignList.add("thread_id");
    }

    public void deleteNext()
    {
        ContentResolver res = super.getResolver();
        Cursor cur = super.getCursor();

        if(cur == null)
        {
            throw new ArrayIndexOutOfBoundsException();
        }
        int id =  cur.getColumnIndex("_id");
        if(id!=-1)
        {
            res.delete(Uri.parse("content://sms/" + cur.getString(id)), null, null);
        }
    }

    @Override
    public void insertNext(JSONObject obj) throws Exception
    {
        super.insertNext(obj, this.ignList);
    }
}
