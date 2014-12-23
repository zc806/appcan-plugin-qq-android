package org.zywx.wbpalmstar.plugin.uexqq;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

public class Utils {

    public static String copyImage(Context context, String url) {
        String resPath = url;// 鑾峰彇鐨勪负assets璺緞
        InputStream imageInputStream = null;
        OutputStream out = null;
        String imagTemPath = null;
        try {
            imageInputStream = context.getResources().getAssets()
                    .open(resPath);
            String sdPath = "";// 涓簊d鍗＄粷瀵硅矾寰�
            boolean sdCardExist = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED); // 鍒ゆ柇sd鍗℃槸鍚﹀瓨鍦�
            if (sdCardExist) {
                sdPath = Environment.getExternalStorageDirectory() + "";// 鑾峰彇鐩綍
            } else {
                Toast.makeText(context, "sd卡不存在，请查看", Toast.LENGTH_SHORT)
                        .show();
            }
            imagTemPath = sdPath + File.separator + resPath;
            File file = new File(imagTemPath);
            if(!file.getParentFile().exists()){
                file.getParentFile().mkdirs(); 
            }  
            if(file.exists()){
                file.delete();  
            } 
            out = new FileOutputStream(file);
            int count = 0;
            byte[] buff = new byte[1024];
            while ((count = imageInputStream.read(buff)) != -1) {
                out.write(buff, 0, count);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(imageInputStream != null) imageInputStream.close();
                if(out != null) out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return imagTemPath;
    }
	
}
