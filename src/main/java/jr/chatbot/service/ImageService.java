package jr.chatbot.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Service
public class ImageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Value("${app.upload.allowed-extensions}")
    private String allowedExtensions;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Map<String, Integer> IMAGE_SIZES = Map.of("small", 64, "medium", 256, "large", 512);

    public Map<String, String> uploadAndProcessImage(MultipartFile file, UUID characterId) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = getFileExtension(originalFilename);

        Map<String, String> imageUrls = new HashMap<>();

        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            for (Map.Entry<String, Integer> entry : IMAGE_SIZES.entrySet()) {
                String size = entry.getKey();
                int dimension = entry.getValue();

                String filename = String.format("%s-%s.%s", characterId, size, extension);
                Path filePath = uploadPath.resolve(filename);

                Thumbnails.of(file.getInputStream()).size(dimension, dimension).outputFormat(extension).toFile(filePath.toFile());

                imageUrls.put(size, "/uploads/characters/" + filename);
            }

            return imageUrls;

        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload image: " + e.getMessage());
        }
    }

    public void deleteCharacterImages(UUID characterId) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                return;
            }

            for (String size : IMAGE_SIZES.keySet()) {
                for (String ext : allowedExtensions.split(",")) {
                    String filename = String.format("%s-%s.%s", characterId, size, ext);
                    Path filePath = uploadPath.resolve(filename);
                    Files.deleteIfExists(filePath);
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to delete images for character " + characterId + ": " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File size exceeds maximum limit of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid filename");
        }

        String extension = getFileExtension(originalFilename);
        if (!isValidExtension(extension)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid file type. Allowed types: " + allowedExtensions);
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File must be an image");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidExtension(String extension) {
        List<String> allowed = Arrays.asList(allowedExtensions.split(","));
        return allowed.contains(extension.toLowerCase());
    }
}
