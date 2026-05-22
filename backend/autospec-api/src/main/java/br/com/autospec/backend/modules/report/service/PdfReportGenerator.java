package br.com.autospec.backend.modules.report.service;

import br.com.autospec.backend.modules.vehicle.dto.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class PdfReportGenerator {

    private static final Color ORANGE       = new Color(0xE8, 0x6D, 0x13);
    private static final Color ORANGE_LIGHT = new Color(0xFF, 0xA0, 0x50);
    private static final Color DARK_NAVY    = new Color(0x0D, 0x0D, 0x1A);
    private static final Color DARK2        = new Color(0x1A, 0x1A, 0x2E);
    private static final Color GRAY_BG      = new Color(0xF4, 0xF4, 0xF6);
    private static final Color GRAY_MID     = new Color(0xDD, 0xDD, 0xE5);
    private static final Color TEXT_DARK    = new Color(0x1A, 0x1A, 0x2E);
    private static final Color TEXT_MID     = new Color(0x55, 0x55, 0x70);
    private static final Color GREEN_WIN    = new Color(0x22, 0xC5, 0x5E);
    private static final Color RED_LOSE     = new Color(0xEF, 0x44, 0x44);
    private static final Color BLUE_A       = new Color(0x26, 0x8B, 0xD2);
    private static final Color AMBER_B      = new Color(0xE8, 0x6D, 0x13);

    private static final Font F_HERO        = new Font(Font.HELVETICA, 22, Font.BOLD,   Color.WHITE);
    private static final Font F_HERO_SUB    = new Font(Font.HELVETICA, 11, Font.NORMAL, new Color(0xFF, 0xCC, 0x99));
    private static final Font F_SECTION     = new Font(Font.HELVETICA, 12, Font.BOLD,   ORANGE);
    private static final Font F_CARD_NUM    = new Font(Font.HELVETICA, 18, Font.BOLD,   TEXT_DARK);
    private static final Font F_CARD_LBL    = new Font(Font.HELVETICA,  8, Font.NORMAL, TEXT_MID);
    private static final Font F_TH          = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.WHITE);
    private static final Font F_TD          = new Font(Font.HELVETICA,  9, Font.NORMAL, TEXT_DARK);
    private static final Font F_TD_WIN      = new Font(Font.HELVETICA,  9, Font.BOLD,   GREEN_WIN);
    private static final Font F_SMALL       = new Font(Font.HELVETICA,  8, Font.NORMAL, TEXT_MID);
    private static final Font F_BADGE       = new Font(Font.HELVETICA,  9, Font.BOLD,   Color.WHITE);
    private static final Font F_INSIGHT_LBL = new Font(Font.HELVETICA,  9, Font.BOLD,   ORANGE);
    private static final Font F_INSIGHT     = new Font(Font.HELVETICA,  9, Font.NORMAL, TEXT_DARK);
    private static final Font F_SCORE       = new Font(Font.HELVETICA, 28, Font.BOLD,   Color.WHITE);
    private static final Font F_SCORE_LBL   = new Font(Font.HELVETICA,  9, Font.NORMAL, new Color(0xFF, 0xCC, 0x99));
    private static final Font F_DOSSIER_VAL = new Font(Font.HELVETICA, 10, Font.BOLD,   TEXT_DARK);
    private static final Font F_DOSSIER_LBL = new Font(Font.HELVETICA,  9, Font.NORMAL, TEXT_MID);

    private static final float PAGE_W = PageSize.A4.getWidth();


    public byte[] generateComparisonPdf(VehicleCompareResponseDTO cmp) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 48);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PageFooter());
            doc.open();

            drawComparisonHeroBanner(writer, cmp);
            doc.add(spacer(70));

            addScoreBar(writer, doc, cmp);
            doc.add(spacer(8));

            addSectionTitle(doc, "Performance at a Glance");
            addPerformanceCards(writer, doc, cmp);
            doc.add(spacer(6));

            addSectionTitle(doc, "Head-to-Head Specification");
            addComparisonTable(doc, cmp);
            doc.add(spacer(6));

            addSectionTitle(doc, "Performance Bar Chart");
            addBarChart(writer, doc, cmp);
            doc.add(spacer(6));

            addSectionTitle(doc, "Key Insights & Recommendation");
            addInsights(doc, cmp);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar PDF de comparação", e);
            throw new RuntimeException("Falha na geração do PDF", e);
        }
    }

    public byte[] generateDossierPdf(VehicleResponseDTO v) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document doc = new Document(PageSize.A4, 36, 36, 36, 48);
            PdfWriter writer = PdfWriter.getInstance(doc, baos);
            writer.setPageEvent(new PageFooter());
            doc.open();

            drawDossierHeroBanner(writer, v);
            doc.add(spacer(60));

            addSectionTitle(doc, "Technical Specifications");
            addDossierSpecGrid(writer, doc, v);
            doc.add(spacer(8));

            addSectionTitle(doc, "Performance Overview");
            addDossierPerformanceBars(writer, doc, v);
            doc.add(spacer(8));

            addSectionTitle(doc, "Vehicle Summary");
            addDossierSummary(doc, v);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Erro ao gerar PDF de dossier", e);
            throw new RuntimeException("Falha na geração do PDF", e);
        }
    }


    private void drawComparisonHeroBanner(PdfWriter w, VehicleCompareResponseDTO cmp) {
        PdfContentByte cb = w.getDirectContent();
        float left = 36, top = PageSize.A4.getHeight() - 36;
        float bw = PAGE_W - 72, bh = 62;


        cb.setColorFill(DARK_NAVY);
        cb.rectangle(left, top - bh, bw, bh);
        cb.fill();

        cb.setColorFill(DARK2);
        cb.rectangle(left, top - bh, bw * 0.5f, bh);
        cb.fill();


        cb.setColorFill(ORANGE);
        cb.rectangle(left, top - bh, 4, bh);
        cb.fill();


        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Comprehensive Comparison Report", F_HERO),
                left + 14, top - 22, 0);

        String sub = cmp.vehicleA().brand() + " " + cmp.vehicleA().model() + " " + cmp.vehicleA().year()
                + "   vs   "
                + cmp.vehicleB().brand() + " " + cmp.vehicleB().model() + " " + cmp.vehicleB().year();
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(sub, F_HERO_SUB),
                left + 14, top - 40, 0);


        String date = "Generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm"));
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase(date, F_SMALL),
                left + bw - 8, top - 52, 0);


        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("AutoSpec AI", new Font(Font.HELVETICA, 10, Font.BOLD, ORANGE_LIGHT)),
                left + bw - 8, top - 38, 0);
    }

    private void addScoreBar(PdfWriter w, Document doc, VehicleCompareResponseDTO cmp) throws DocumentException {
        PdfContentByte cb = w.getDirectContent();
        float[] pos = getCurrentY(doc, w);
        float y = pos[0];
        float left = 36, bw = PAGE_W - 72, bh = 48;


        cb.setColorFill(BLUE_A);
        cb.rectangle(left, y - bh, bw * 0.35f, bh);
        cb.fill();


        cb.setColorFill(DARK_NAVY);
        cb.rectangle(left + bw * 0.35f, y - bh, bw * 0.30f, bh);
        cb.fill();


        cb.setColorFill(AMBER_B);
        cb.rectangle(left + bw * 0.65f, y - bh, bw * 0.35f, bh);
        cb.fill();


        String labelA = cmp.vehicleA().brand() + " " + cmp.vehicleA().model();
        String labelB = cmp.vehicleB().brand() + " " + cmp.vehicleB().model();

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase(labelA, F_SCORE_LBL),
                left + bw * 0.175f, y - 14, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase(String.valueOf(cmp.comparison().scoreA()), F_SCORE),
                left + bw * 0.175f, y - 38, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase("VS", new Font(Font.HELVETICA, 13, Font.BOLD, Color.WHITE)),
                left + bw * 0.50f, y - 28, 0);

        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase(labelB, F_SCORE_LBL),
                left + bw * 0.825f, y - 14, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                new Phrase(String.valueOf(cmp.comparison().scoreB()), F_SCORE),
                left + bw * 0.825f, y - 38, 0);

        doc.add(spacer(bh + 4));
    }

    private void addPerformanceCards(PdfWriter w, Document doc, VehicleCompareResponseDTO cmp) throws DocumentException {
        VehicleResponseDTO a = cmp.vehicleA(), b = cmp.vehicleB();
        PdfContentByte cb = w.getDirectContent();
        float[] pos = getCurrentY(doc, w);
        float y = pos[0];
        float left = 36, bw = PAGE_W - 72;
        float cw = (bw - 12) / 4f, ch = 52;

        String[][] cards = {
                { "Horsepower", str(a.horsepower()) + " hp", str(b.horsepower()) + " hp" },
                { "Top Speed",  str(a.topSpeed())   + " km/h", str(b.topSpeed())  + " km/h" },
                { "0–100 km/h", str(a.acceleration()) + " s",  str(b.acceleration()) + " s" },
                { "Torque",     str(a.torque())      + " Nm",  str(b.torque())     + " Nm" },
        };

        for (int i = 0; i < 4; i++) {
            float cx = left + i * (cw + 4);
            cb.setColorFill(GRAY_BG);
            cb.rectangle(cx, y - ch, cw, ch);
            cb.fill();
            cb.setColorStroke(GRAY_MID);
            cb.setLineWidth(0.5f);
            cb.rectangle(cx, y - ch, cw, ch);
            cb.stroke();

            // orange top accent
            cb.setColorFill(ORANGE);
            cb.rectangle(cx, y - 3, cw, 3);
            cb.fill();

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(cards[i][0], F_CARD_LBL),
                    cx + cw / 2, y - 14, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(cards[i][1], new Font(Font.HELVETICA, 11, Font.BOLD, BLUE_A)),
                    cx + cw / 2, y - 28, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase(cards[i][2], new Font(Font.HELVETICA, 11, Font.BOLD, AMBER_B)),
                    cx + cw / 2, y - 42, 0);
        }
        doc.add(spacer(ch + 6));
    }

    private void addComparisonTable(Document doc, VehicleCompareResponseDTO cmp) throws DocumentException {
        VehicleResponseDTO a = cmp.vehicleA(), b = cmp.vehicleB();
        CompareResultDTO r = cmp.comparison();

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);
        table.setWidths(new float[]{30f, 26f, 10f, 26f});


        addTH(table, "Specification",                           DARK_NAVY);
        addTH(table, a.brand() + " " + a.model(),              BLUE_A);
        addTH(table, "WIN",                                     DARK2);
        addTH(table, b.brand() + " " + b.model(),              AMBER_B);


        addCmpRow(table, "Engine",           a.engine(),              null,           b.engine(),              null);
        addCmpRow(table, "Horsepower (hp)",  str(a.horsepower()),     r.horsepower(), str(b.horsepower()),     r.horsepower());
        addCmpRow(table, "Torque (Nm)",      str(a.torque()),         r.torque(),     str(b.torque()),         r.torque());
        addCmpRow(table, "Top Speed (km/h)", str(a.topSpeed()),       r.topSpeed(),   str(b.topSpeed()),       r.topSpeed());
        addCmpRow(table, "0–100 km/h (s)",   str(a.acceleration()),   r.acceleration(),str(b.acceleration()), r.acceleration());
        addCmpRow(table, "Drivetrain",       a.drivetrain(),          null,           b.drivetrain(),          null);
        addCmpRow(table, "Weight (kg)",      str(a.weight()),         r.weight(),     str(b.weight()),         r.weight());
        addCmpRow(table, "Price (USD)",      fmtPrice(a.price()),     r.price(),      fmtPrice(b.price()),     r.price());
        addCmpRow(table, "Electric Range",   str(a.electricRange()) + " km", r.electricRange(), str(b.electricRange()) + " km", r.electricRange());
        addCmpRow(table, "Dimensions L×W×H",
                fmtDim(a.length(), a.width(), a.height()), null,
                fmtDim(b.length(), b.width(), b.height()), null);

        doc.add(table);
    }

    private void addBarChart(PdfWriter w, Document doc, VehicleCompareResponseDTO cmp) throws DocumentException {
        VehicleResponseDTO a = cmp.vehicleA(), b = cmp.vehicleB();
        PdfContentByte cb = w.getDirectContent();
        float[] pos = getCurrentY(doc, w);
        float y = pos[0];
        float left = 36, bw = PAGE_W - 72;
        float barH = 14, gap = 8, labelW = 90, maxBarW = (bw - labelW - 60) / 2f;

        String[][] metrics = {
                { "Horsepower (hp)", str(a.horsepower()), str(b.horsepower()) },
                { "Torque (Nm)",     str(a.torque()),     str(b.torque())     },
                { "Top Speed (km/h)",str(a.topSpeed()),   str(b.topSpeed())   },
                { "Weight (kg)",     str(a.weight()),     str(b.weight())     },
        };

        float totalH = metrics.length * (barH * 2 + gap + 4) + 10;

        for (int i = 0; i < metrics.length; i++) {
            float rowY = y - i * (barH * 2 + gap + 6) - 10;
            String label = metrics[i][0];
            double valA = parseDouble(metrics[i][1]);
            double valB = parseDouble(metrics[i][2]);
            double maxV = Math.max(valA, valB);
            if (maxV == 0) continue;

            float wA = (float) (valA / maxV * maxBarW);
            float wB = (float) (valB / maxV * maxBarW);
            float centerX = left + labelW + maxBarW;


            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(label, F_CARD_LBL), left, rowY, 0);

            cb.setColorFill(new Color(0x26, 0x8B, 0xD2, 180));
            cb.rectangle(centerX - wA, rowY - barH - 2, wA, barH);
            cb.fill();


            cb.setColorFill(new Color(0xE8, 0x6D, 0x13, 180));
            cb.rectangle(centerX + 2, rowY - barH - 2, wB, barH);
            cb.fill();


            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(metrics[i][1], new Font(Font.HELVETICA, 8, Font.BOLD, BLUE_A)),
                    centerX - wA - 3, rowY - barH + 1, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(metrics[i][2], new Font(Font.HELVETICA, 8, Font.BOLD, AMBER_B)),
                    centerX + wB + 6, rowY - barH + 1, 0);
        }

        float legendY = y - totalH;
        cb.setColorFill(BLUE_A);
        cb.rectangle(left, legendY - 8, 12, 8);
        cb.fill();
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(a.brand() + " " + a.model(), F_SMALL), left + 15, legendY - 1, 0);

        cb.setColorFill(AMBER_B);
        cb.rectangle(left + 120, legendY - 8, 12, 8);
        cb.fill();
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(b.brand() + " " + b.model(), F_SMALL), left + 135, legendY - 1, 0);

        doc.add(spacer(totalH + 18));
    }

    private void addInsights(Document doc, VehicleCompareResponseDTO cmp) throws DocumentException {
        VehicleResponseDTO a = cmp.vehicleA(), b = cmp.vehicleB();
        CompareResultDTO r = cmp.comparison();

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(100);
        table.setSpacingBefore(4f);

        if (cmp.summary() != null && !cmp.summary().isBlank()) {
            PdfPCell sc = new PdfPCell();
            sc.setBorderColor(ORANGE);
            sc.setBorderWidth(0f);
            sc.setBorderWidthLeft(3f);
            sc.setBackgroundColor(new Color(0xFF, 0xF0, 0xE0));
            sc.setPadding(8f);
            Paragraph sp = new Paragraph(cmp.summary(), F_TD);
            sp.setLeading(13f);
            sc.addElement(sp);
            table.addCell(sc);
        }

        doc.add(table);
        doc.add(spacer(6));

        PdfPTable bullets = new PdfPTable(2);
        bullets.setWidthPercentage(100);
        bullets.setWidths(new float[]{50f, 50f});
        bullets.setSpacingBefore(4f);

        if (a.horsepower() != null && b.horsepower() != null) {
            int diff = Math.abs(a.horsepower() - b.horsepower());
            String more = a.horsepower() > b.horsepower() ? a.model() : b.model();
            addInsightCell(bullets, "⚡ Power", more + " leads by " + diff + " hp");
        }
        if (a.topSpeed() != null && b.topSpeed() != null) {
            int diff = Math.abs(a.topSpeed() - b.topSpeed());
            String faster = a.topSpeed() > b.topSpeed() ? a.model() : b.model();
            addInsightCell(bullets, "🏁 Top Speed", faster + " is " + diff + " km/h faster");
        }
        if (a.acceleration() != null && b.acceleration() != null) {
            BigDecimal diff = a.acceleration().subtract(b.acceleration()).abs().setScale(2, RoundingMode.HALF_UP);
            String quicker = a.acceleration().compareTo(b.acceleration()) < 0 ? a.model() : b.model();
            addInsightCell(bullets, "⏱ Acceleration", quicker + " hits 100 km/h " + diff + "s sooner");
        }
        if (a.price() != null && b.price() != null) {
            BigDecimal diff = a.price().subtract(b.price()).abs().setScale(0, RoundingMode.HALF_UP);
            String cheaper = a.price().compareTo(b.price()) < 0 ? a.model() : b.model();
            addInsightCell(bullets, "💰 Value", cheaper + " is $" + diff.toPlainString() + " cheaper");
        }
        if (a.weight() != null && b.weight() != null) {
            BigDecimal diff = a.weight().subtract(b.weight()).abs().setScale(0, RoundingMode.HALF_UP);
            String lighter = a.weight().compareTo(b.weight()) < 0 ? a.model() : b.model();
            addInsightCell(bullets, "⚖ Weight", lighter + " is " + diff + " kg lighter");
        }

        String winnerName = switch (r.winner()) {
            case VEHICLE_A -> a.brand() + " " + a.model();
            case VEHICLE_B -> b.brand() + " " + b.model();
            default -> "Draw";
        };
        addInsightCell(bullets, "🏆 Overall Winner", winnerName + " (" + r.scoreA() + " vs " + r.scoreB() + " pts)");

        doc.add(bullets);
    }


    private void drawDossierHeroBanner(PdfWriter w, VehicleResponseDTO v) {
        PdfContentByte cb = w.getDirectContent();
        float left = 36, top = PageSize.A4.getHeight() - 36;
        float bw = PAGE_W - 72, bh = 54;

        cb.setColorFill(DARK_NAVY);
        cb.rectangle(left, top - bh, bw, bh);
        cb.fill();

        cb.setColorFill(ORANGE);
        cb.rectangle(left, top - bh, 4, bh);
        cb.fill();

        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase("Vehicle History Dossier", F_HERO),
                left + 14, top - 20, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                new Phrase(v.brand() + " " + v.model() + " " + v.version() + " (" + v.year() + ")", F_HERO_SUB),
                left + 14, top - 38, 0);

        String date = "Generated " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase(date, F_SMALL), left + bw - 8, top - 44, 0);
        ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                new Phrase("AutoSpec AI", new Font(Font.HELVETICA, 10, Font.BOLD, ORANGE_LIGHT)),
                left + bw - 8, top - 30, 0);
    }

    private void addDossierSpecGrid(PdfWriter w, Document doc, VehicleResponseDTO v) throws DocumentException {
        PdfContentByte cb = w.getDirectContent();
        float[] pos = getCurrentY(doc, w);
        float y = pos[0];
        float left = 36, bw = PAGE_W - 72;
        float cw = (bw - 8) / 3f, ch = 44;

        Object[][] specs = {
                { "Brand",            v.brand()                        },
                { "Model",            v.model()                        },
                { "Version",          v.version()                      },
                { "Year",             str(v.year())                    },
                { "Engine",           v.engine()                       },
                { "Drivetrain",       v.drivetrain()                   },
                { "Horsepower",       str(v.horsepower()) + " hp"      },
                { "Torque",           str(v.torque()) + " Nm"          },
                { "Top Speed",        str(v.topSpeed()) + " km/h"      },
                { "0–100 km/h",       str(v.acceleration()) + " s"     },
                { "Weight",           str(v.weight()) + " kg"          },
                { "Price",            fmtPrice(v.price())              },
        };

        int cols = 3;
        for (int i = 0; i < specs.length; i++) {
            int col = i % cols;
            int row = i / cols;
            float cx = left + col * (cw + 4);
            float cy = y - row * (ch + 4);

            cb.setColorFill(GRAY_BG);
            cb.rectangle(cx, cy - ch, cw, ch);
            cb.fill();
            cb.setColorStroke(GRAY_MID);
            cb.setLineWidth(0.5f);
            cb.rectangle(cx, cy - ch, cw, ch);
            cb.stroke();

            cb.setColorFill(ORANGE);
            cb.rectangle(cx, cy - 3, cw, 3);
            cb.fill();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase((String) specs[i][0], F_DOSSIER_LBL),
                    cx + 8, cy - 14, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase((String) specs[i][1], F_DOSSIER_VAL),
                    cx + 8, cy - 30, 0);
        }

        int rows = (int) Math.ceil((double) specs.length / cols);
        doc.add(spacer(rows * (ch + 4) + 4));
    }

    private void addDossierPerformanceBars(PdfWriter w, Document doc, VehicleResponseDTO v) throws DocumentException {
        PdfContentByte cb = w.getDirectContent();
        float[] pos = getCurrentY(doc, w);
        float y = pos[0];
        float left = 36, bw = PAGE_W - 72;
        float barH = 14, labelW = 110, maxBarW = bw - labelW - 60;

        Object[][] metrics = {
                { "Horsepower (hp)",  v.horsepower(),    760  },  // ref: sports car
                { "Top Speed (km/h)", v.topSpeed(),       330  },
                { "Torque (Nm)",      v.torque(),         900  },
                { "Weight (kg)",      v.weight(),        2500  },
        };

        for (int i = 0; i < metrics.length; i++) {
            float rowY = y - i * (barH + 14);
            String label = (String) metrics[i][0];
            double val = parseDouble(str(metrics[i][1]));
            double ref = parseDouble(str(metrics[i][2]));
            float pct = (float) Math.min(val / ref, 1.0);
            float barW = pct * maxBarW;

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(label, F_CARD_LBL), left, rowY, 0);


            cb.setColorFill(GRAY_MID);
            cb.rectangle(left + labelW, rowY - barH - 2, maxBarW, barH);
            cb.fill();


            Color barColor = pct > 0.75f ? GREEN_WIN : pct > 0.4f ? ORANGE : RED_LOSE;
            cb.setColorFill(barColor);
            cb.rectangle(left + labelW, rowY - barH - 2, barW, barH);
            cb.fill();

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(str(metrics[i][1]), new Font(Font.HELVETICA, 8, Font.BOLD, TEXT_DARK)),
                    left + labelW + barW + 5, rowY - barH + 2, 0);
        }

        doc.add(spacer(metrics.length * (barH + 14) + 10));
    }

    private void addDossierSummary(Document doc, VehicleResponseDTO v) throws DocumentException {
        PdfPTable t = new PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(4f);

        StringBuilder sb = new StringBuilder();
        sb.append("The ").append(v.brand()).append(" ").append(v.model()).append(" ").append(v.version())
                .append(" (").append(v.year()).append(") ");
        if (v.engine() != null) sb.append("is powered by a ").append(v.engine()).append(", ");
        if (v.horsepower() != null) sb.append("producing ").append(v.horsepower()).append(" hp");
        if (v.torque() != null) sb.append(" and ").append(v.torque()).append(" Nm of torque. ");
        if (v.topSpeed() != null) sb.append("It reaches a top speed of ").append(v.topSpeed()).append(" km/h");
        if (v.acceleration() != null) sb.append(" and sprints from 0 to 100 km/h in ").append(v.acceleration()).append("s. ");
        if (v.price() != null) sb.append("Priced at ").append(fmtPrice(v.price())).append(".");

        PdfPCell cell = new PdfPCell();
        cell.setBorderWidthLeft(3f);
        cell.setBorderColor(ORANGE);
        cell.setBorderWidth(0f);
        cell.setBorderWidthLeft(3f);
        cell.setBackgroundColor(new Color(0xFF, 0xF0, 0xE0));
        cell.setPadding(10f);
        Paragraph p = new Paragraph(sb.toString(), F_TD);
        p.setLeading(14f);
        cell.addElement(p);
        t.addCell(cell);

        doc.add(t);
    }


    private void addTH(PdfPTable t, String text, Color bg) {
        PdfPCell c = new PdfPCell(new Phrase(text, F_TH));
        c.setBackgroundColor(bg);
        c.setPadding(6f);
        c.setHorizontalAlignment(Element.ALIGN_CENTER);
        c.setBorderColor(Color.WHITE);
        c.setBorderWidth(1f);
        t.addCell(c);
    }

    private void addCmpRow(PdfPTable t, String label,
                           String valA, AttributeComparisonDTO attr,
                           String valB, AttributeComparisonDTO attrB) {

        PdfPCell lc = new PdfPCell(new Phrase(label, F_TD));
        lc.setPadding(5f);
        lc.setBackgroundColor(GRAY_BG);
        lc.setBorderColor(GRAY_MID);
        t.addCell(lc);

        boolean aWins = attr != null && attr.winner() == ComparisonWinner.VEHICLE_A;
        boolean bWins = attr != null && attr.winner() == ComparisonWinner.VEHICLE_B;

        PdfPCell ca = new PdfPCell(new Phrase(valA != null ? valA : "—", aWins ? F_TD_WIN : F_TD));
        ca.setPadding(5f);
        ca.setHorizontalAlignment(Element.ALIGN_CENTER);
        ca.setBorderColor(GRAY_MID);
        if (aWins) ca.setBackgroundColor(new Color(0xD1, 0xFA, 0xE5));
        t.addCell(ca);


        String indicator = attr == null ? "—"
                : switch (attr.winner()) {
            case VEHICLE_A -> "◀";
            case VEHICLE_B -> "▶";
            default        -> "=";
        };
        Color indColor = attr == null ? TEXT_MID
                : switch (attr.winner()) {
            case VEHICLE_A -> BLUE_A;
            case VEHICLE_B -> AMBER_B;
            default        -> TEXT_MID;
        };
        PdfPCell wi = new PdfPCell(new Phrase(indicator, new Font(Font.HELVETICA, 10, Font.BOLD, indColor)));
        wi.setPadding(5f);
        wi.setHorizontalAlignment(Element.ALIGN_CENTER);
        wi.setBackgroundColor(DARK_NAVY);
        wi.setBorderColor(Color.WHITE);
        t.addCell(wi);

        PdfPCell cb2 = new PdfPCell(new Phrase(valB != null ? valB : "—", bWins ? F_TD_WIN : F_TD));
        cb2.setPadding(5f);
        cb2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cb2.setBorderColor(GRAY_MID);
        if (bWins) cb2.setBackgroundColor(new Color(0xD1, 0xFA, 0xE5));
        t.addCell(cb2);
    }

    private void addInsightCell(PdfPTable t, String label, String text) {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(GRAY_MID);
        c.setBorderWidth(0.5f);
        c.setBackgroundColor(GRAY_BG);
        c.setPadding(7f);
        Paragraph p = new Paragraph();
        p.add(new Chunk(label + "  ", F_INSIGHT_LBL));
        p.add(new Chunk(text, F_INSIGHT));
        c.addElement(p);
        t.addCell(c);
    }



    private void addSectionTitle(Document doc, String title) throws DocumentException {
        Paragraph p = new Paragraph(title, F_SECTION);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(3f);
        doc.add(p);


        PdfPTable line = new PdfPTable(1);
        line.setWidthPercentage(100);
        PdfPCell lc = new PdfPCell(new Phrase(""));
        lc.setFixedHeight(1.5f);
        lc.setBackgroundColor(ORANGE);
        lc.setBorder(Rectangle.NO_BORDER);
        line.addCell(lc);
        doc.add(line);
        doc.add(spacer(3));
    }


    private Paragraph spacer(float h) {
        Paragraph p = new Paragraph(" ");
        p.setLeading(h);
        return p;
    }

    private float[] getCurrentY(Document doc, PdfWriter w) {
        return new float[]{ w.getVerticalPosition(true) };
    }

    private String str(Object v) {
        if (v == null) return "N/A";
        if (v instanceof BigDecimal bd) return bd.setScale(2, RoundingMode.HALF_UP).toPlainString();
        return v.toString();
    }

    private String fmtPrice(BigDecimal price) {
        if (price == null) return "N/A";
        return "$ " + String.format("%,.2f", price);
    }

    private String fmtDim(BigDecimal l, BigDecimal w, BigDecimal h) {
        return str(l) + " × " + str(w) + " × " + str(h) + " m";
    }

    private double parseDouble(String s) {
        if (s == null || s.equals("N/A")) return 0;
        try { return Double.parseDouble(s.replaceAll("[^0-9.]", "")); }
        catch (NumberFormatException e) { return 0; }
    }
    

    private static class PageFooter extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            float y = document.bottom() - 14;
            float left = document.left(), right = document.right();

            cb.setColorFill(new Color(0xE8, 0x6D, 0x13));
            cb.rectangle(left, y - 1, right - left, 1.5f);
            cb.fill();

            Phrase footer = new Phrase(
                    "© " + java.time.Year.now().getValue() + " AutoSpec Intelligence Platform  ·  Advanced Vehicle Intelligence",
                    new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(0x88, 0x88, 0x99)));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, footer, left, y - 10, 0);

            Phrase page = new Phrase("Page " + writer.getPageNumber(),
                    new Font(Font.HELVETICA, 7, Font.NORMAL, new Color(0x88, 0x88, 0x99)));
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, page, right, y - 10, 0);
        }
    }
}