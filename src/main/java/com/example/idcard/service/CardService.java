package com.example.idcard.service;

import com.example.idcard.model.UserCard;
import com.example.idcard.repository.CardRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

@Service
public class CardService {

    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_DIMENSION = 6_000;
    private static final long MAX_IMAGE_PIXELS = 20_000_000;
    private static final int MAX_GYM_ID_ATTEMPTS = 10;

    private static final DateTimeFormatter VALIDITY_FORMATTER =
            DateTimeFormatter.ofPattern("MMM uuuu", Locale.ENGLISH);

    private final CardRepository cardRepository;
    private final Path uploadDirectory;

    public CardService(
            CardRepository cardRepository,
            @Value("${app.upload.directory:uploads}") String uploadDirectory) {
        this.cardRepository = cardRepository;
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    @Transactional
    public UserCard register(
            String name,
            String bloodGroup,
            String phoneNumber,
            String validUntil,
            MultipartFile profilePic) {
        String cleanedName = requireText(name, "Name", 120);
        String cleanedBloodGroup = requireText(bloodGroup, "Blood group", 8);
        String cleanedPhoneNumber = requireText(phoneNumber, "Phone number", 24);
        String formattedValidUntil = normalizeValidityMonth(validUntil);
        String extension = validateImage(profilePic);
        String fileName = UUID.randomUUID() + "." + extension;
        Path storedImage = storeImage(profilePic, fileName);

        try {
            UserCard card = new UserCard();
            card.setName(cleanedName);
            card.setBloodGroup(cleanedBloodGroup);
            card.setPhoneNumber(cleanedPhoneNumber);
            card.setValidUntil(formattedValidUntil);
            card.setProfilePicPath("/uploads/" + fileName);

            for (int attempt = 0; attempt < MAX_GYM_ID_ATTEMPTS; attempt++) {
                String gymId = UserCard.generateGymId();
                if (!cardRepository.existsByGymId(gymId)) {
                    card.setGymId(gymId);
                    return cardRepository.save(card);
                }
            }

            throw new IllegalStateException("Could not generate a unique gym ID. Please try again.");
        } catch (RuntimeException exception) {
            try {
                Files.deleteIfExists(storedImage);
            } catch (IOException cleanupException) {
                exception.addSuppressed(cleanupException);
            }
            throw exception;
        }
    }

    static String normalizeValidityMonth(String value) {
        YearMonth validityMonth;
        try {
            validityMonth = value == null || value.isBlank()
                    ? YearMonth.now().plusMonths(1)
                    : YearMonth.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Card validity must be a valid month and year.");
        }
        if (validityMonth.isBefore(YearMonth.now())) {
            throw new IllegalArgumentException("Card validity cannot be in the past.");
        }
        return validityMonth.format(VALIDITY_FORMATTER);
    }

    @Transactional(readOnly = true)
    public UserCard getByGymId(String gymId) {
        return cardRepository.findByGymId(gymId)
                .orElseThrow(() -> new CardNotFoundException("No card found for gym ID " + gymId));
    }

    private Path storeImage(MultipartFile profilePic, String fileName) {
        try {
            Files.createDirectories(uploadDirectory);
            Path destination = uploadDirectory.resolve(fileName).normalize();
            if (!destination.startsWith(uploadDirectory)) {
                throw new IllegalStateException("Invalid generated upload path.");
            }
            try (var input = profilePic.getInputStream()) {
                Files.copy(input, destination);
                return destination;
            }
        } catch (IOException exception) {
            throw new ImageStorageException("Could not store the uploaded profile picture.", exception);
        }
    }

    private String validateImage(MultipartFile profilePic) {
        if (profilePic == null || profilePic.isEmpty()) {
            throw new IllegalArgumentException("A profile picture is required.");
        }
        if (profilePic.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("The profile picture must be 5 MB or smaller.");
        }

        try (var source = profilePic.getInputStream();
             ImageInputStream input = ImageIO.createImageInputStream(source)) {
            if (input == null) {
                throw new IllegalArgumentException("The uploaded profile picture is not a valid image.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException("Only JPEG, PNG, or GIF profile pictures are supported.");
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                String format = reader.getFormatName().toLowerCase();
                if (!format.equals("jpeg") && !format.equals("png") && !format.equals("gif")) {
                    throw new IllegalArgumentException("Only JPEG, PNG, or GIF profile pictures are supported.");
                }
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                if (width <= 0 || height <= 0 || width > MAX_IMAGE_DIMENSION
                        || height > MAX_IMAGE_DIMENSION || (long) width * height > MAX_IMAGE_PIXELS) {
                    throw new IllegalArgumentException("The profile picture dimensions are too large.");
                }
                if (reader.read(0) == null) {
                    throw new IllegalArgumentException("The uploaded profile picture could not be decoded.");
                }
                return format.equals("jpeg") ? "jpg" : format;
            } finally {
                reader.dispose();
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("The uploaded profile picture could not be read.", exception);
        }
    }

    private String requireText(String value, String fieldName, int maxLength) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        String cleaned = value.trim();
        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " must be " + maxLength + " characters or fewer.");
        }
        return cleaned;
    }

    public static class CardNotFoundException extends RuntimeException {
        public CardNotFoundException(String message) {
            super(message);
        }
    }

    public static class ImageStorageException extends RuntimeException {
        public ImageStorageException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
