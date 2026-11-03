import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class ImageAverager {
    public static void main(String[] args) {
        int squareSize;
        String mode;
        File file;
        
        try {
            validateArgumentCount(args);
            squareSize = parseSquareSize(args[1]);
            mode = validateMode(args[2]);
            file = resolveImageFile(args[0]);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.exit(1);
            return;
        }

        if (mode.equals("M")) {
            System.exit(1);
            return;
        }

        BufferedImage image;
        try {
            image = ImageIO.read(file);
            if (image == null) {
                System.err.println("The file could not be read as an image.");
                System.exit(1);
                return;
            }
        } catch (IOException e) {
            System.err.println("Could not open image: " + file.getPath());
            System.exit(1);
            return;
        }

        System.out.println("Image size: " + image.getWidth() + " x " + image.getHeight());
        System.out.println("Square size: " + squareSize);
        System.out.println("Mode: " + mode);

        SwingUtilities.invokeLater(() -> showImage(image, squareSize));
    }

    private static void validateArgumentCount(String[] args) {
        if (args.length != 3) {
            throw new IllegalArgumentException(
                    "Exactly 3 arguments are required: image filename, square size, and processing mode.\n"
                    + "Usage: java -cp build ImageAverager <file.jpg> <square size> <S|M>");
        }
    }

    private static int parseSquareSize(String value) {
        int squareSize;
        try {
            squareSize = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Square size must be a positive whole number.");
        }
        
        if (squareSize <= 0) {
            throw new IllegalArgumentException("Square size must be a positive whole number.");
        }
        
        return squareSize;
    }

    private static String validateMode(String mode) {
        if (!mode.equals("S") && !mode.equals("M")) {
            throw new IllegalArgumentException("Processing mode must be S or M.");
        }
        
        return mode;
    }

    private static File resolveImageFile(String value) {
        File file = new File(value);
        if (!file.isAbsolute() && file.getParent() == null) {
            file = new File("input", value);
        }
        
        String fileName = file.getName().toLowerCase(java.util.Locale.ROOT);
        if (!fileName.endsWith(".jpg") && !fileName.endsWith(".jpeg")) {
            throw new IllegalArgumentException("Please provide a JPG image (.jpg or .jpeg).");
        }
        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("Could not open image: " + file.getPath());
        }
        
        return file;
    }

    private static void averageSquare(BufferedImage image, int startX, int startY, int squareSize) {
        int endX = startX + Math.min(squareSize, image.getWidth() - startX);
        int endY = startY + Math.min(squareSize, image.getHeight() - startY);
        long redTotal = 0;
        long greenTotal = 0;
        long blueTotal = 0;
        long pixelCount = 0;

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                Color color = new Color(image.getRGB(x, y));
                redTotal += color.getRed();
                greenTotal += color.getGreen();
                blueTotal += color.getBlue();
                pixelCount++;
            }
        }

        Color averageColor = new Color(
                (int) (redTotal / pixelCount),
                (int) (greenTotal / pixelCount),
                (int) (blueTotal / pixelCount));

        for (int y = startY; y < endY; y++) {
            for (int x = startX; x < endX; x++) {
                image.setRGB(x, y, averageColor.getRGB());
            }
        }
    }

    private static void processSingleThreaded(BufferedImage image, int squareSize, ImagePanel panel)
            throws InterruptedException {
        for (int y = 0; y < image.getHeight(); y += Math.min(squareSize, image.getHeight() - y)) {
            for (int x = 0; x < image.getWidth(); x += Math.min(squareSize, image.getWidth() - x)) {
                synchronized (image) {
                    averageSquare(image, x, y, squareSize);
                }
            }
            panel.repaint();
            Thread.sleep(20); // Keep the row-by-row progress visible.
        }
    }

    private static void saveImage(BufferedImage image) throws IOException {
        File output = new File("result.jpg");
        if (!ImageIO.write(image, "jpg", output)) {
            throw new IOException("No JPG writer is available.");
        }
        System.out.println("Saved image to result.jpg");
    }

    private static void showImage(BufferedImage image, int squareSize) {
        Rectangle screen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        int availableWidth = Math.max(1, screen.width - 100);
        int availableHeight = Math.max(1, screen.height - 100);
        double scale = Math.min(1.0, Math.min(
                (double) availableWidth / image.getWidth(),
                (double) availableHeight / image.getHeight()));

        ImagePanel panel = new ImagePanel(image);
        panel.setPreferredSize(new Dimension(
                Math.max(1, (int) (image.getWidth() * scale)),
                Math.max(1, (int) (image.getHeight() * scale))));

        JFrame window = new JFrame("Image Averager");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(panel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        Thread processingThread = new Thread(
                () -> processAndSave(image, squareSize, panel, window), "image-processor");
        processingThread.start();
    }

    private static void processAndSave(BufferedImage image, int squareSize, ImagePanel panel, JFrame window) {
        try {
            Thread.sleep(300); // Show the original image before processing starts.
            
            processSingleThreaded(image, squareSize, panel);
            saveImage(image);
            
            System.out.println("Processing complete.");
            SwingUtilities.invokeLater(() -> window.setTitle("Image Averager - Complete"));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Processing interrupted. The result was not saved.");
        } catch (IOException e) {
            System.err.println("Could not save result.jpg: " + e.getMessage());
        }
    }

    private static class ImagePanel extends JPanel {
        private final BufferedImage image;

        ImagePanel(BufferedImage image) {
            this.image = image;
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            double scale = Math.min((double) getWidth() / image.getWidth(),
                    (double) getHeight() / image.getHeight());
            int width = Math.max(1, (int) (image.getWidth() * scale));
            int height = Math.max(1, (int) (image.getHeight() * scale));
            int x = (getWidth() - width) / 2;
            int y = (getHeight() - height) / 2;
            synchronized (image) {
                graphics.drawImage(image, x, y, width, height, null);
            }
        }
    }
}
