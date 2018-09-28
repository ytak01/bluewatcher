package com.bluewatcher;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.lang.Thread.UncaughtExceptionHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.animation.AccelerateInterpolator;

public class CsUncaughtExceptionHandler implements java.lang.Thread.UncaughtExceptionHandler {
    private static Context sContext = null;
    private static Activity sActivity = null;
    private static final String BUG_FILE = "BugReport";
    private static final UncaughtExceptionHandler sDefaultHandler
            = Thread.getDefaultUncaughtExceptionHandler();

    /**
     * コンストラクタ
     * @param context
     */
    public CsUncaughtExceptionHandler(Activity activity, Context context){
        sActivity = activity;
        sContext = context;
    }

    /**
     * キャッチされない例外によって指定されたスレッドが終了したときに呼び出されます
     * 例外スタックトレースの内容をファイルに出力します
     */
    public void uncaughtException(Thread thread, Throwable ex) {
        PrintWriter pw = null;
        try {
            pw = new PrintWriter(sContext.openFileOutput(BUG_FILE, Context.MODE_WORLD_READABLE));
            ex.printStackTrace(pw);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            if (pw != null) pw.close();
        }
        SendBugReport(sActivity);
        sDefaultHandler.uncaughtException(thread, ex);
    }

    /**
     * バグレポートの内容をメールで送信します。
     * @param activity
     */
    public static void SendBugReport(final Activity activity) {
        //バグレポートがなければ以降の処理を行いません。
        final File bugfile = activity.getFileStreamPath(BUG_FILE);
        if (!bugfile.exists()) {
            return;
        }
        SendMail(activity,bugfile);
        //AlertDialogを表示します。
        /*
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle("ERROR");
        alert.setMessage("予期しないエラーが発生しました。開発元にエラーを送信してください。");
        alert.setPositiveButton("Post", new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SendMail(activity,bugfile);
            }});
        alert.setNegativeButton("Cancel", null);
        alert.show();
        */
    }

    /**
     * バグレポートの内容をメールで送信します。
     * @param activity
     * @param bugfile
     */
    private static void SendMail(final Activity activity,File bugfile){
        //バグレポートの内容を読み込みます。
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(bugfile));
            String str;
            while((str = br.readLine()) != null){
                sb.append(str +"\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //メールで送信します。
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:" + "bnr32@mail.cuvie.net"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "【BugReport】" + R.string.app_name );
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        activity.startActivity(intent);
        //バグレポートを削除します。
        bugfile.delete();
    }

}
