package com.webcon.nh.newbreath.utils;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CrashHandler implements UncaughtExceptionHandler {
    private static CrashHandler crashHandler;
    // 程序的 Context 对象
    private Context mContext;

    // 系统默认的 UncaughtException 处理类
    private UncaughtExceptionHandler mDefaultHandler;

    public static CrashHandler getInstance() {
        if (crashHandler == null) {
            synchronized (CrashHandler.class) {
                if (crashHandler == null) {
                    crashHandler = new CrashHandler();
                }
            }
        }
        return crashHandler;
    }
    // 用来存储设备信息和异常信息
    private Map<String, String> infos = new HashMap<String, String>();

    // 用于格式化日期,作为日志文件名的一部分
    private DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

    private CrashHandler() {

    }


    // 保存文件的根目录
    private static final String BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "webcon" + File.separator + "wp" + File.separator;
    // 保存临时错误信息文件的路径
    private static final String TEMP_LOG_PATH = BASE_PATH + "temp" + File.separator + "Log" + File.separator;



    /**
     * 初始化
     */
    public void init(Context context) {
        mContext = context;

        // 获取系统默认的 UncaughtException 处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该 CrashHandler 为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        ex.printStackTrace();// 这句只是为了在控制台显示
        if (!handleException(ex) && mDefaultHandler != null)
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);

    }

    /**
     * 自定义错误处理，收集错误信息，发送错误报告等操作均在此完成
     *
     * @param ex
     * @return true：如果处理了该异常信息；否则返回 false
     */
    private boolean handleException(final Throwable ex) {
        if (ex == null) {
            return false;
        }

        // 使用 Toast 来显示异常信息
        new Thread() {
            @Override
            public void run() {
                Looper.prepare();
                Builder builder = new Builder(mContext);
                builder.setTitle("提示");

                //TODO change to NanHong tele-info(builder.setMessage(R.string.dialog_logoutexit_message_crash);)
//                builder.setMessage(ex.getMessage());
                builder.setMessage("很抱歉！，程序出现异常，即将退出。");

                Log.e("unCaught", Log.getStackTraceString(ex));


                builder.setNegativeButton(
                        "退出",
                        new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                Log.e("unCaught", "click-tui chu");
                                dialog.dismiss();
                                System.exit(0);
                            }
                        });
                builder.setPositiveButton("----发送错误报告----", new OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.e("unCaught", "click-cuo wu bao gao");
                        // 收集设备参数信息
                        collectDeviceInfo(mContext);
                        // 保存日志文件
                        saveCatchInfo2File(ex);
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.getWindow().setType(
                        WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setCancelable(false);
                dialog.show();
                Looper.loop();
            }
        }.start();

        return true;
    }

    /**
     * 收集设备参数信息
     *
     * @param ctx
     */
    public void collectDeviceInfo(Context ctx) {
        try {
            PackageManager pm = ctx.getPackageManager();
            PackageInfo pi = pm.getPackageInfo(ctx.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                String versionName = pi.versionName == null ? "null"
                        : pi.versionName;
                String versionCode = pi.versionCode + "";
                infos.put("versionName", versionName);
                infos.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                infos.put(field.getName(), field.get(null).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 保存错误信息到文件中
     *
     * @param ex
     * @return 返回文件名称, 便于将文件传送到服务器
     */
    private String saveCatchInfo2File(Throwable ex) {

        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, String> entry : infos.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\n");
        }

        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        ex.printStackTrace(printWriter);
        Throwable cause = ex.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        String result = writer.toString();
        sb.append(result);
        try {
            long timestamp = System.currentTimeMillis();
            String time = formatter.format(new Date());
            String fileName = "crash-" + time + "-" + timestamp + ".log";
            if (Environment.getExternalStorageState().equals(
                    Environment.MEDIA_MOUNTED)) {
                File dir = new File(TEMP_LOG_PATH);
                if (!dir.exists()) {
                    dir.mkdirs();
                }
                File saveFile = new File(TEMP_LOG_PATH, fileName);
                saveFile.createNewFile();
                FileOutputStream outStream = new FileOutputStream(saveFile);
                outStream.write(sb.toString().getBytes());
//                sendCrashLog2PM(fileName);
                outStream.close();
                // 发送给开发人员

                // fos.close();
            }
            return fileName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * 将捕获的导致崩溃的错误信息发送给开发人员
//     * <p/>
//     * 目前只将log日志保存在sdcard 和输出到LogCat中，并未发送给后台。
//     */
//    private void sendCrashLog2PM(String fileName) {
//        if (!new File(TEMP_LOG_PATH + fileName).exists()) {
//            Toast.makeText(mContext, "日志文件不存在！", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        sendEmail(fileName);
//    }
//
//    /**
//     * 发送邮件的方法
//     *
//     * @return
//     */
//    private boolean sendEmail(final String filename) {
//        new Thread() {
//            public void run() {
//                Properties props = new Properties();
//                props.put("mail.smtp.protocol", "smtp");
//                props.put("mail.smtp.auth", "true"); // 设置要验证
//                props.put("mail.smtp.host", "smtp.163.com"); // 设置host
//                props.put("mail.smtp.port", "25"); // 设置端口
//                PassAuthenticator pass = new PassAuthenticator(); // 获取帐号密码
//                Session session = Session.getInstance(props, pass); // 获取验证会话
//                try {
//                    // 配置发送及接收邮箱
//                    InternetAddress fromAddress, toAddress;
//                    /* TODO ##这个地方需要改成自己的邮箱 */
//                    fromAddress = new InternetAddress("webcon_log@163.com");
//                    toAddress = new InternetAddress("webcon_log@163.com");
//
//                    MimeMultipart allMultipart = new MimeMultipart("mixed"); // 附件
//                    // 设置文件到MimeMultipart
//                    MimeBodyPart contentPart = createContentPart("错误日志", filename);
//                    allMultipart.addBodyPart(contentPart);
//                    // 配置发送信息
//                    MimeMessage message = new MimeMessage(session);
//                    message.setContent("Hello", "text/plain");
//                    message.setContent(allMultipart); // 发邮件时添加附件
//                    message.setSubject("错误日志");
//                    message.setFrom(fromAddress);
//                    message.addRecipient(javax.mail.Message.RecipientType.TO, toAddress);
//                    message.saveChanges();
//                    // 连接邮箱并发送
//                    Transport transport = session.getTransport("smtp");
//                    /**
//                     * 这个地方需要改称自己的账号和密码
//                     */
//                    transport.connect("smtp.163.com", "webcon_log@163.com", "webcon-log");
//                    transport.send(message);
//                    transport.close();
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    // Log.i("Exception_sendEmail", e.getMessage());
//                }
//                File saveFile = new File(TEMP_LOG_PATH, filename);
//                saveFile.delete();
//                System.exit(0);
//            }
//
//            ;
//        }.start();
//
//        return false;
//    }
//
//    /**
//     * 发送带附件的邮件
//     */
//    private MimeBodyPart createContentPart(String bodyStr, String filename)
//            throws Exception {
//        // 用于保存最终正文部分
//        MimeBodyPart contentBody = new MimeBodyPart();
//        // 用于组合文本和图片，"related"型的MimeMultipart对象
//        MimeMultipart contentMulti = new MimeMultipart("related");
//
//        // 正文的文本部分
//        MimeBodyPart textBody = new MimeBodyPart();
//        textBody.setContent(bodyStr, "text/html;charset=utf-8");
//        contentMulti.addBodyPart(textBody);
//
//        /**
//         * 一下内容是：发送邮件时添加附件
//         */
//        MimeBodyPart attachPart = new MimeBodyPart();
//        FileDataSource fds = new FileDataSource(TEMP_LOG_PATH
//                + filename); // 打开要发送的文件
//        if (fds.getFile().length() > 0) {
//            attachPart.setDataHandler(new DataHandler(fds));
//            attachPart.setFileName(fds.getName());
//            contentMulti.addBodyPart(attachPart);
//        }
//
//        // 将上面"related"型的 MimeMultipart 对象作为邮件的正文
//        contentBody.setContent(contentMulti);
//        return contentBody;
//    }
}
