package org.faddistr.smsbackup;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SMSWork {
    private class MakeSMSWork extends MakeWork
    {
        private ICb mCb ;
        final int bufSize = 4096;

        MakeSMSWork(int work_type, String uri, String fPath, Context cont,
                    String descr, String title, ICb iICb)
        {
            super(work_type, uri, fPath, cont, descr, title, iICb);
            mCb = iICb;
        }

        private void saveSMSFile(JSONArray ar, String fPath) throws IOException {
            FileOutputStream F = new FileOutputStream(new File(fPath));
            byte[] buf = ar.toString().getBytes();
            int write = 0;
            int chunk;

            while (write != buf.length) {
                chunk = ((buf.length - write) >= bufSize)?bufSize:(buf.length - write);
                F.write(buf, write, chunk);
                write+=chunk;
                super.updatePB((write*50)/buf.length);
            }

            F.flush();
            F.close();

        }


        private JSONArray loadJSON(String fPath) throws IOException, JSONException {
            StringBuffer strJSON = new StringBuffer();
            FileInputStream in = new FileInputStream(fPath);
            int fSize = in.available();
            byte[] buffer=new byte[bufSize];
            int curSize;
            int read=0;

            while ((curSize = in.read(buffer)) != -1) {
                strJSON.append(new String(buffer, 0, curSize));
                read+=curSize;
                super.updatePB((read * 50) / fSize);
            }

            return new JSONArray(strJSON.toString());
        }

        @Override
        void makeDrop(SMS2JSON mSMS) throws Exception {
            int cnt = mSMS.getCount();

            for(int i=0; i<cnt; i++)
            {
                mSMS.deleteNext();
                super.updatePB((i * 100) / cnt);
            }
        }


        @Override
        void makeLoad(SMS2JSON mSMS, String fPath) throws Exception{
            JSONArray ar = loadJSON(fPath);
            int cnt = ar.length();

            for(int i=0; i<cnt; i++)
            {
                mSMS.insertNext(ar.getJSONObject(i));
                super.updatePB((i * 50) / cnt);
            }
        }

        @Override
        void makeSave(SMS2JSON mSMS, String fPath) throws Exception {
            JSONArray ar = new JSONArray();
            int cnt = mSMS.getCount();

            for(int i=0; i<cnt; i++)
            {
                ar.put(mSMS.readNext());
                super.updatePB((i*50)/cnt);
            }

            saveSMSFile(ar, fPath);
         }
    }

    private int totalW = 0;
    private String title;
    private String fPath;
    private Context cont;
    private int workType;

    private void genSMSWork(String name, int cur, ICb cb)
    {
        new MakeSMSWork(workType, "content://sms/"+name, fPath+"/"+name+".json", cont,
                title+" content://sms/"+name, title+" "+cur+"/"+totalW, cb).execute();
    }

    SMSWork(int workType, AppCompatActivity cont, String fPath, AFlags flags, ICb cb)
    {
        int cur = 1;

        this.workType = workType;
        this.fPath = fPath;
        this.cont = cont;

        switch (workType)
        {
            case MakeWork.WORK_DROP:
                title = cont.getString(R.string.titleD);
                break;

            case MakeWork.WORK_LOAD:
                title = cont.getString(R.string.titleL);
                break;

            case MakeWork.WORK_SAVE:
                title = cont.getString(R.string.titleS);
                break;

            default:
                    throw new IllegalArgumentException();
        }

        if(flags.mDraft)
            totalW++;

        if(flags.mSent)
            totalW++;

        if(flags.mInbox)
            totalW++;

        if(flags.mInbox)
        {
            genSMSWork("inbox", cur, cb);
            cur++;
        }

        if(flags.mSent)
        {
            genSMSWork("sent", cur, cb);
            cur++;
        }

        if(flags.mDraft)
            genSMSWork("draft", cur, cb);
    }

}
