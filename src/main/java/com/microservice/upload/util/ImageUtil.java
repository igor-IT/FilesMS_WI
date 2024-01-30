package com.microservice.upload.util;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ImageUtil implements Runnable {

    private final MultipartFile file;
    private final String format;
    private final Path filePath;
    private static final float quality = 0.3f;

    public ImageUtil(MultipartFile file, String format, Path filePath) {
        this.file = file;
        this.format = format;
        this.filePath = filePath;
    }

    @Override
    public void run() {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            BufferedImage originalImage = ImageIO.read(file.getInputStream());

            ImageWriter imageWriter = ImageIO.getImageWritersByFormatName(format).next();
            ImageWriteParam imageWriteParam = imageWriter.getDefaultWriteParam();
            imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            imageWriteParam.setCompressionQuality(quality);

            ImageOutputStream imageOutputStream = ImageIO.createImageOutputStream(byteArrayOutputStream);
            imageWriter.setOutput(imageOutputStream);
            imageWriter.write(null, new IIOImage(originalImage, null, null), imageWriteParam);

            Files.write(filePath, byteArrayOutputStream.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}