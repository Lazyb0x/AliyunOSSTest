package cn.beanbang.aliyunoss;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadTest {
    @Test
    void downloadTest() {
        String url = "https://blog.beanbang.cn/css/atom.css";
        String url2 = "https://mirrors.tuna.tsinghua.edu.cn/ubuntu-releases/18.04/ubuntu-18.04.4-desktop-amd64.iso.torrent";
        downloadFromUrl(url2, getFileName(url2), "./test");
    }

    public void downloadFromUrl(String urlString, String fileName, String savePath) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:77.0) Gecko/20100101 Firefox/77.0");

            InputStream inputStream = conn.getInputStream();
//            byte[] data = readInputStream(inputStream);

            File saveDir = new File(savePath);
            if (!saveDir.exists()) {
                saveDir.mkdir();
            }
            File file = new File(saveDir + File.separator + fileName);
            FileOutputStream fos = new FileOutputStream(file);

            transfer(inputStream, fos);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] readInputStream(InputStream inputStream) throws IOException {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int len;

        while ((len = inputStream.read(buffer)) != -1) {
            bos.write(buffer, 0, len);
        }

        bos.close();
        return bos.toByteArray();
    }

    public void transfer(InputStream inputStream, OutputStream outputStream) throws IOException{
        byte[] buffer = new byte[1024*1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, len);
            System.out.println("transfered " + len + " bytes");
        }
        outputStream.close();
    }

    /**
     * 从 url 路径获取文件名
     * @return 文件名
     */
    private String getFileName(String urlPath){
        String[] s = urlPath.split("/");
        return s[s.length-1];
    }
}
