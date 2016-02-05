package org.faddistr.smsbackup;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private String m_chosenDir = "";
    private boolean is_saving = true;
    CCB cb;
    SMSWork w;

    private void unCheckAll(){
        ((CheckBox)findViewById(R.id.chkInbox)).setChecked(false);
        ((CheckBox)findViewById(R.id.chkSent)).setChecked(false);
        ((CheckBox)findViewById(R.id.chkDraft)).setChecked(false);
    }

    private AFlags setCheck()
    {
        AFlags ret = new AFlags();
        ret.mDraft = ((CheckBox)findViewById(R.id.chkDraft)).isChecked();
        ret.mInbox = ((CheckBox)findViewById(R.id.chkInbox)).isChecked();
        ret.mSent = ((CheckBox)findViewById(R.id.chkSent)).isChecked();
        return ret;
    }

    private void printMessage(String msg)
    {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    private DirectoryChooserDialog directoryChooserDialog =
            new DirectoryChooserDialog(MainActivity.this,
                    new DirectoryChooserDialog.ChosenDirectoryListener() {
                        @Override
                        public void onChosenDir(String chosenDir) {
                            w = new SMSWork(is_saving?MakeWork.WORK_SAVE:MakeWork.WORK_LOAD,
                                    MainActivity.this, chosenDir, setCheck(), cb);
                        }
                    });

    private class CCB implements ICb
    {
        @Override
        public void onErrF(Exception e) {
            printMessage(e.getMessage());
        }

        @Override
        public void onFinF() {
            unCheckAll();
            printMessage(getString(R.string.opDone));
        }
    }


    private void dropSMS()
    {
        w = new SMSWork(MakeWork.WORK_DROP,
                MainActivity.this, null, setCheck(), cb);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cb = new CCB();

        directoryChooserDialog.setNewFolderEnabled(true);

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_saving = true;
                directoryChooserDialog.chooseDirectory(m_chosenDir);
            }
        });

        findViewById(R.id.btnLoad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_saving = false;
                directoryChooserDialog.chooseDirectory(m_chosenDir);
            }
        });

        findViewById(R.id.btnDrop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dropSMS();
            }
        });
    }
}

