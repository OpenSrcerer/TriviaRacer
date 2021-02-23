package dracer.styling;

import dracer.racing.words.DictionaryWord;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ImageProcessor {
    public static InputStream getImage(List<DictionaryWord> wordList) throws IOException {
        StringBuilder key = new StringBuilder();
        wordList.forEach(w -> key.append(w.getWord()).append(" -> ").append(w.getFirstDefinition()).append("\n"));
        BufferedImage bufferedImage = new BufferedImage(500, 500,
                BufferedImage.TYPE_INT_ARGB);
        Graphics graphics = prepareGraphics(bufferedImage.createGraphics());
        graphics.setColor(Color.LIGHT_GRAY);
        graphics.fillRect(0, 0, 500, 500);
        graphics.setColor(Color.BLACK);
        graphics.setFont(new Font("Arial", Font.PLAIN, 20));
        drawString(graphics, key.toString(), 10, 10);

        // Convert image to an inputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private static Graphics2D prepareGraphics(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        return graphics;
    }

    private static void drawString(Graphics graphics, String text, int x, int y) {
        text = addNewlines(text);
        for (String line : text.split("\n")) {
            graphics.drawString(line, x, y += graphics.getFontMetrics().getHeight());
        }
    }

    private static String addNewlines(String string) {
        return string.replaceAll("(.{50})", "$1\n");
    }
}
