package display;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.imageio.ImageIO;

/**
 * Loads and caches images for the game.
 */
public class ImageManager {

    private static final Map<String, BufferedImage> imageCache = new HashMap<>();

    /**
     * Returns a BufferedImage from cache or loads it from disk.
     * 
     * @param pathString Path to the image resource.
     * @return The loaded image, or a default image if not found.
     */
    public static BufferedImage getImage(String pathString) {
        Objects.requireNonNull(pathString, "pathString cannot be null");
        if (imageCache.containsKey(pathString)) {
            return imageCache.get(pathString);
        }
        return loadAndCache(pathString);
    }

    /**
     * Internal helper to load an image and store it in the cache.
     */
    private static BufferedImage loadAndCache(String pathString) {
        BufferedImage img = loadFromFile(pathString);
        if (img != null) {
            imageCache.put(pathString, img);
            return img;
        }
        return loadDefaultImage();
    }

    /**
     * Loads a fallback default image if the requested one is missing.
     */
    private static BufferedImage loadDefaultImage() {
        return loadFromFile("default.png");
    }

    /**
     * Loads a buffered image using the ClassLoader for JAR compatibility.
     * Use this method to avoid java.io.File and ensure resources are loaded
     * from the classpath (especially when inside a JAR).
     */
    private static BufferedImage loadFromFile(String pathString) {
        // Resources are now located in src/resources, so they are at /resources/ in the
        // classpath
        String cleanedPath = pathString.startsWith("/") ? pathString.substring(1) : pathString;
        String resourcePath = "/resources/" + cleanedPath;

        try (InputStream is = ImageManager.class.getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Resource not found: " + resourcePath);
                return null;
            }
            return ImageIO.read(is);
        } catch (IOException e) {
            System.err.println("Error reading resource: " + resourcePath);
            e.printStackTrace();
            return null;
        }
    }
}
