package com.qr_vehicle.QRvehicle.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;

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

        // ================= Background =================

        InputStream is = StickerPdfRenderer.class.getResourceAsStream("/static/tag.png");

        if (is != null) {

            Image bg = new Image(ImageDataFactory.create(is.readAllBytes()));

            bg.scaleAbsolute(area.getWidth(), area.getHeight());

            bg.setFixedPosition(
                    area.getLeft(),
                    area.getBottom());

            canvas.add(bg);
        }

        // ================= QR URL =================

        // Production
        String qrUrl = "https://owntag.in/v/" + uniqueCode;

        // Local
        // String qrUrl = "http://localhost:3000/v/" + uniqueCode;

        QRCodeWriter writer = new QRCodeWriter();

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.MARGIN, 0);

        BitMatrix matrix = writer.encode(
                qrUrl,
                BarcodeFormat.QR_CODE,
                350,
                350,
                hints);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        MatrixToImageWriter.writeToStream(matrix, "PNG", out);

        Image qr = new Image(ImageDataFactory.create(out.toByteArray()));

        // QR Size
        float qrSize = cm(2.50f);   // smaller than 2.95

        qr.scaleAbsolute(qrSize, qrSize);

        // QR Position
        qr.setFixedPosition(
                area.getLeft() + area.getWidth() - cm(3.10f),
                area.getBottom() + cm(1.75f));

        canvas.add(qr);

        // ================= Tag ID =================

        canvas.showTextAligned(
        new Paragraph(tagId)
                .setFontSize(8)
                .setFontColor(ColorConstants.BLACK),
        area.getLeft() + area.getWidth() - cm(1.90f),  //moved left
        area.getBottom() + cm(0.50f),  // moved up
        TextAlignment.CENTER);

        canvas.close();
    }

}