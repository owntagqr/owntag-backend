package com.qr_vehicle.QRvehicle.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.InputStream;
import java.io.IOException;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayOutputStream;

public class StickerPdfRenderer {

    private static float cm(float cm) {
        return cm * 28.3465f;
    }

    public static void drawSticker(
            PdfCanvas pdfCanvas,
            Rectangle area,
            String uniqueCode,
            String tagId,
            String sheetCode
    ) throws Exception {

        Canvas canvas = new Canvas(pdfCanvas, area);
        InputStream is =StickerPdfRenderer.class.getResourceAsStream("/static/tag.png");

if (is != null) {

    byte[] imageBytes = is.readAllBytes();

    Image bg = new Image(ImageDataFactory.create(imageBytes));

    bg.scaleAbsolute(area.getWidth(), area.getHeight());

    bg.setFixedPosition(
            area.getLeft(),
            area.getBottom());

    canvas.add(bg);
}

        

        // QR URL
        // String qrUrl = "https://owntag.in/v/" + uniqueCode;
        String qrUrl = "http://localhost:3000/v/" + uniqueCode;

        QRCodeWriter writer = new QRCodeWriter();

        BitMatrix matrix = writer.encode(
                qrUrl,
                BarcodeFormat.QR_CODE,
                300,
                300);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        Image qr = new Image(ImageDataFactory.create(out.toByteArray()));

        qr.scaleAbsolute(cm(2.9f), cm(2.9f));

        qr.setFixedPosition(
            area.getLeft() + area.getWidth() - cm(3.35f),
            area.getBottom() + cm(1.80f));   // move QR higher

        canvas.add(qr);

        

        canvas.showTextAligned(
    new Paragraph(tagId)
        .setBold()
        .setFontSize(8),
    area.getLeft() + area.getWidth() - cm(1.9f),
    area.getBottom() + cm(0.18f),   // lower
    TextAlignment.CENTER);

        

        canvas.close();
    }
}