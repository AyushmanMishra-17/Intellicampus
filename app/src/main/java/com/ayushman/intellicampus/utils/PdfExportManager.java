package com.ayushman.intellicampus.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.text.TextUtils;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Creates a clean, multi-page PDF from AI Tutor response text and opens
 * the device's public Downloads folder.
 */
public final class PdfExportManager {

    private static final int PAGE_WIDTH = 595;   // A4 at approximately 72 dpi
    private static final int PAGE_HEIGHT = 842;
    private static final float LEFT = 48f;
    private static final float RIGHT = 48f;
    private static final float TOP = 58f;
    private static final float BOTTOM = 54f;

    private PdfExportManager() {
    }

    /**
     * Generates the same formatted PDF and saves it directly to the public
     * Downloads/IntelliCampus folder using MediaStore. Android 10+ does not
     * require storage permission for this operation.
     */
    public static void exportToDownloads(
            Context context,
            String documentTitle,
            String content
    ) {
        if (context == null || TextUtils.isEmpty(content)) {
            return;
        }

        PdfDocument document = new PdfDocument();
        Uri savedUri = null;

        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                throw new IOException(
                        "Direct Downloads export requires Android 10 or later."
                );
            }

            String fileName = makeFileName(documentTitle);
            writeDocument(document, documentTitle, content);

            ContentValues values = new ContentValues();
            values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
            values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
            values.put(
                    MediaStore.Downloads.RELATIVE_PATH,
                    Environment.DIRECTORY_DOWNLOADS + "/IntelliCampus"
            );
            values.put(MediaStore.Downloads.IS_PENDING, 1);

            Uri collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            savedUri = context.getContentResolver().insert(collection, values);

            if (savedUri == null) {
                throw new IOException("Android could not create the PDF in Downloads.");
            }

            try (java.io.OutputStream output =
                         context.getContentResolver().openOutputStream(savedUri)) {
                if (output == null) {
                    throw new IOException("Could not open the downloaded PDF.");
                }
                document.writeTo(output);
                output.flush();
            }

            ContentValues completed = new ContentValues();
            completed.put(MediaStore.Downloads.IS_PENDING, 0);
            context.getContentResolver().update(
                    savedUri, completed, null, null
            );

            Toast.makeText(
                    context,
                    "PDF saved to Downloads/IntelliCampus",
                    Toast.LENGTH_LONG
            ).show();

        } catch (Exception e) {
            if (savedUri != null) {
                try {
                    context.getContentResolver().delete(savedUri, null, null);
                } catch (Exception ignored) {
                }
            }

            Toast.makeText(
                    context,
                    "Could not download PDF: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        } finally {
            document.close();
        }
    }

    private static void writeDocument(
            PdfDocument document,
            String title,
            String content
    ) {
        Paint titlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        titlePaint.setColor(0xFF263F8F);
        titlePaint.setTextSize(21f);
        titlePaint.setFakeBoldText(true);

        Paint bodyPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        bodyPaint.setColor(0xFF20242D);
        bodyPaint.setTextSize(10.5f);

        Paint headingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headingPaint.setColor(0xFF263F8F);
        headingPaint.setTextSize(13f);
        headingPaint.setFakeBoldText(true);

        Paint metaPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        metaPaint.setColor(0xFF6B7280);
        metaPaint.setTextSize(8f);

        Paint rulePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        rulePaint.setColor(0xFFD8DEEA);
        rulePaint.setStrokeWidth(1f);

        PageWriter writer = new PageWriter(
                document, bodyPaint, headingPaint, metaPaint, rulePaint
        );

        writer.startPage();
        writer.canvas.drawText(
                safeTitle(title), LEFT, writer.y, titlePaint
        );
        writer.y += 22f;
        writer.canvas.drawText(
                "INTELLICAMPUS  •  AI TUTOR",
                LEFT, writer.y, metaPaint
        );
        writer.y += 12f;
        writer.canvas.drawLine(
                LEFT, writer.y, PAGE_WIDTH - RIGHT, writer.y, rulePaint
        );
        writer.y += 22f;

        String[] paragraphs = content.replace("\r", "").split("\n", -1);
        for (String raw : paragraphs) {
            String line = raw.trim();

            if (line.isEmpty()) {
                writer.y += 7f;
                writer.ensureSpace(18f);
                continue;
            }

            boolean heading = isHeading(line);
            Paint selected = heading ? headingPaint : bodyPaint;
            String cleaned = cleanMarkdown(line);

            if (heading) {
                writer.y += 7f;
                writer.ensureSpace(22f);
            }

            writer.drawWrapped(cleaned, selected, heading ? 18f : 14f);
        }

        writer.finishPage();
    }

    private static boolean isHeading(String line) {
        return line.startsWith("#")
                || line.matches("^(\\d+[.)]|[A-Z][A-Z0-9 ,:&()/-]{3,})$");
    }

    private static String cleanMarkdown(String value) {
        String result = value;
        while (result.startsWith("#")) result = result.substring(1).trim();
        result = result.replace("**", "")
                .replace("__", "")
                .replace("`", "");
        if (result.startsWith("- ") || result.startsWith("* ")) {
            result = "•  " + result.substring(2);
        }
        return result;
    }

    private static String safeTitle(String title) {
        return TextUtils.isEmpty(title) ? "Study Notes" : title.trim();
    }

    private static String makeFileName(String title) {
        String base = safeTitle(title).toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        if (base.isEmpty()) base = "study_notes";
        String stamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
                .format(new Date());
        return base + "_" + stamp + ".pdf";
    }

    private static final class PageWriter {
        final PdfDocument document;
        final Paint body;
        final Paint heading;
        final Paint meta;
        final Paint rule;
        PdfDocument.Page page;
        Canvas canvas;
        float y;
        int pageNumber = 0;

        PageWriter(
                PdfDocument document,
                Paint body,
                Paint heading,
                Paint meta,
                Paint rule
        ) {
            this.document = document;
            this.body = body;
            this.heading = heading;
            this.meta = meta;
            this.rule = rule;
        }

        void startPage() {
            pageNumber++;
            PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(
                    PAGE_WIDTH, PAGE_HEIGHT, pageNumber
            ).create();
            page = document.startPage(info);
            canvas = page.getCanvas();
            y = TOP;
        }

        void ensureSpace(float needed) {
            if (y + needed > PAGE_HEIGHT - BOTTOM) {
                finishPage();
                startPage();
            }
        }

        void drawWrapped(String text, Paint paint, float lineHeight) {
            String[] words = text.split("\\s+");
            StringBuilder line = new StringBuilder();

            for (String word : words) {
                String candidate = line.length() == 0
                        ? word : line + " " + word;

                if (paint.measureText(candidate) > PAGE_WIDTH - LEFT - RIGHT
                        && line.length() > 0) {
                    ensureSpace(lineHeight);
                    canvas.drawText(line.toString(), LEFT, y, paint);
                    y += lineHeight;
                    line.setLength(0);
                    line.append(word);
                } else {
                    if (line.length() > 0) line.append(' ');
                    line.append(word);
                }
            }

            if (line.length() > 0) {
                ensureSpace(lineHeight);
                canvas.drawText(line.toString(), LEFT, y, paint);
                y += lineHeight;
            }
        }

        void finishPage() {
            if (page == null) return;

            canvas.drawLine(
                    LEFT, PAGE_HEIGHT - 36,
                    PAGE_WIDTH - RIGHT, PAGE_HEIGHT - 36,
                    rule
            );
            canvas.drawText(
                    "IntelliCampus • AI-generated learning material",
                    LEFT, PAGE_HEIGHT - 22, meta
            );
            String pageLabel = "Page " + pageNumber;
            canvas.drawText(
                    pageLabel,
                    PAGE_WIDTH - RIGHT - meta.measureText(pageLabel),
                    PAGE_HEIGHT - 22,
                    meta
            );

            document.finishPage(page);
            page = null;
            canvas = null;
        }
    }
}
