package org.faddistr.smsbackup;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Looper;
import android.os.Handler;

public abstract  class MakeWork extends AsyncTask<Void, Void, Void>
{
    public static final int WORK_DROP = 0;
    public static final int WORK_LOAD = 1;
    public static final int WORK_SAVE = 2;

    private int             mWorkId;
    private String          mPath;
    private SMS2JSON        mSMS;
    private ProgressDialog  mPDial;
    private static boolean   mIsCancel;
    private Handler         mHandler;
    private ICb             mCb;
    private Exception emsg;

    public MakeWork(int work_type,String uri, String fPath, Context cont,
                    String descr, String title, ICb cb) {

        if( ((work_type != WORK_DROP) && (work_type != WORK_LOAD) && (work_type != WORK_SAVE))
                || (cont == null) || (uri == null) || (cb == null))
        {
            throw new IllegalArgumentException();
        }

        mWorkId = work_type;
        mSMS    = new SMS2JSON(cont, uri);
        mPath   = fPath;
        mPDial = new ProgressDialog(cont);
        mHandler = new Handler(Looper.getMainLooper());
        mCb     =   cb;

        if(title!=null)
            mPDial.setTitle(title);

        if(descr!=null)
            mPDial.setMessage(descr);

        mPDial.setMax(100);
        mPDial.setCancelable(true);
        mPDial.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mPDial.setCanceledOnTouchOutside(false);
        mPDial.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mIsCancel = true;
            }
        });

    }

    protected void updatePB(final int progress)
    {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPDial.setProgress(progress);
             }
        });
    }

    protected boolean getCancel()
    {
        return mIsCancel;
    }

    abstract  void makeDrop(SMS2JSON mSMS) throws Exception;
    abstract  void makeLoad(SMS2JSON mSMS, String fPath) throws Exception;
    abstract  void makeSave(SMS2JSON mSMS, String fPath) throws Exception;

    @Override
    protected Void doInBackground(Void... params) {

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mPDial.show();
                updatePB(0);
            }
        });

        mIsCancel = false;
        emsg = null;
        try {
            switch (mWorkId) {
                case WORK_DROP:
                    makeDrop(mSMS);
                    break;

                case WORK_LOAD:
                    makeLoad(mSMS, mPath);
                    break;

                case WORK_SAVE:
                    makeSave(mSMS, mPath);
                    break;

                default:
                    break;
            }
        }catch (Exception e)
        {
            emsg = e;
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {

                if(emsg!=null)
                    mCb.onErrF(emsg);

                mPDial.hide();
                mCb.onFinF();
            }
        });

        return null;
    }
}
