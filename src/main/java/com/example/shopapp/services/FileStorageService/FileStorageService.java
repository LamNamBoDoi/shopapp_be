package com.example.shopapp.services.FileStorageService;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads");

    public FileStorageService() throws IOException {
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        if (!isImageFile(file) || file.getOriginalFilename() == null) {
            throw new IOException("Invalid image format");
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = UUID.randomUUID().toString() + "_" + filename;
        Path destination = uploadDir.resolve(uniqueFilename);
        //sao chép nội dung của file vào file đích
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return uniqueFilename;
    }

    public List<String> storeFiles(List<MultipartFile> files) throws IOException {
        List<String> filenames = new ArrayList<>();
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                filenames.add(storeFile(file));
            }
        }
        return filenames;
    }

    public void deleteFile(String filename) throws IOException {
        if (filename == null || filename.isEmpty()) {
            return;
        }

        Path filePath = uploadDir.resolve(filename);
        if (Files.exists(filePath)) {
            Files.delete(filePath);
            System.out.println("Đã xóa file: " + filename);
        } else {
            System.out.println("Không tìm thấy file để xóa: " + filename);
        }
    }

    private boolean isImageFile(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }
}
