package com.zhou.goldtask.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;

@Service
public class OcrService {

    public String getUrlText(String url) {
        String uuid = UUID.randomUUID().toString();
        String fileName = "D:\\zhouwx\\img\\" + uuid + ".jpg";
        byte[] bytes = HttpUtil.createGet(url).execute().bodyBytes();
        FileUtil.writeBytes(bytes, fileName);
        System.gc();
        System.setProperty("TESSDATA_PREFIX", "D:\\Program Files\\Tesseract-OCR\\");
        File imageFile = new File(fileName); // 替换为你的图片路径
        Tesseract tesseract = new Tesseract();

        try {
            // 只识别数字
            tesseract.setDatapath("D:\\Program Files\\Tesseract-OCR\\tessdata");
            tesseract.setTessVariable("tessedit_char_whitelist", "0123456789."); // 只允许识别数字
            tesseract.setLanguage("eng"); // 语言设置
            return tesseract.doOCR(imageFile);
        } catch (TesseractException e) {
            e.printStackTrace();
        } finally {
            imageFile.delete();
        }
        return null;
    }
}