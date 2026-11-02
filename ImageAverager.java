import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
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

        try {
            BufferedImage image = ImageIO.read(file);
            if (image == null) {
                System.err.println("The file could not be read as an image.");
                System.exit(1);
                return;
            }

            System.out.println("Image size: " + image.getWidth() + " x " + image.getHeight());
            System.out.println("Square size: " + squareSize);
            System.out.println("Mode: " + mode);
            SwingUtilities.invokeLater(() -> showImage(image));
        } catch (IOException e) {
            System.err.println("Could not open image: " + file.getPath());
            System.exit(1);
        }
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

    private static void showImage(BufferedImage image) {
        JLabel imageLabel = new JLabel(new ImageIcon(image));
        JFrame window = new JFrame("Image Averager");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.add(imageLabel);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}
