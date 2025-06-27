package com.sportshop.api.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.IOException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

@Service
public class CloudinaryService {
    private final Cloudinary cloudinary;

    // Giới hạn kích thước file (5MB = 5 * 1024 * 1024 bytes)
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    // Các định dạng ảnh được phép
    private static final List<String> ALLOWED_IMAGE_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp");

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Upload một ảnh lên Cloudinary
     * 
     * @param file   File ảnh cần upload
     * @param folder Thư mục lưu trữ trên Cloudinary (ví dụ: "products",
     *               "categories")
     * @return URL của ảnh đã upload
     * @throws IOException              Nếu có lỗi khi upload
     * @throws IllegalArgumentException Nếu file không hợp lệ
     */
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        // Validate file
        validateImageFile(file);

        // Tạo options cho Cloudinary
        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("resource_type", "image");
        options.put("transformation", "f_auto,q_auto"); // Tự động tối ưu format và quality

        // Upload lên Cloudinary
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

        // Trả về URL của ảnh
        return (String) result.get("secure_url");
    }

    /**
     * Upload nhiều ảnh lên Cloudinary
     * 
     * @param files  Danh sách file ảnh
     * @param folder Thư mục lưu trữ
     * @return Danh sách URL của các ảnh đã upload
     * @throws IOException Nếu có lỗi khi upload
     */
    public List<String> uploadMultipleImages(List<MultipartFile> files, String folder) throws IOException {
        List<String> imageUrls = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                String imageUrl = uploadImage(file, folder);
                imageUrls.add(imageUrl);
            } catch (Exception e) {
                // Log lỗi và tiếp tục với file tiếp theo
                System.err.println("Lỗi upload file " + file.getOriginalFilename() + ": " + e.getMessage());
                throw new IOException("Lỗi upload file " + file.getOriginalFilename(), e);
            }
        }

        return imageUrls;
    }

    /**
     * Upload ảnh với custom transformation (resize, crop, etc.)
     * 
     * @param file   File ảnh
     * @param folder Thư mục lưu trữ
     * @param width  Chiều rộng mong muốn
     * @param height Chiều cao mong muốn
     * @param crop   Loại crop (fill, scale, fit, etc.)
     * @return URL của ảnh đã xử lý
     * @throws IOException Nếu có lỗi khi upload
     */
    public String uploadImageWithTransformation(MultipartFile file, String folder,
            Integer width, Integer height, String crop) throws IOException {
        // Validate file
        validateImageFile(file);

        // Tạo transformation string
        StringBuilder transformation = new StringBuilder();
        if (width != null && height != null) {
            transformation.append("w_").append(width).append(",h_").append(height);
            if (crop != null) {
                transformation.append(",c_").append(crop);
            }
        }
        transformation.append(",f_auto,q_auto");

        // Tạo options
        Map<String, Object> options = new HashMap<>();
        options.put("folder", folder);
        options.put("resource_type", "image");
        options.put("transformation", transformation.toString());

        // Upload lên Cloudinary
        Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), options);

        return (String) result.get("secure_url");
    }

    /**
     * Trích xuất publicId từ URL Cloudinary
     */
    public String extractPublicIdFromUrl(String url) {
        try {
            int uploadIndex = url.indexOf("/upload/");
            if (uploadIndex == -1)
                return null;
            String afterUpload = url.substring(uploadIndex + 8);
            // Loại bỏ version nếu có (bắt đầu bằng v + số + /)
            if (afterUpload.matches("^v\\d+/.+")) {
                afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
            }
            int dotIndex = afterUpload.lastIndexOf('.');
            if (dotIndex != -1) {
                afterUpload = afterUpload.substring(0, dotIndex);
            }
            return afterUpload;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Xóa ảnh khỏi Cloudinary
     * 
     * @param publicId Public ID của ảnh trên Cloudinary
     * @return true nếu xóa thành công
     */
    public boolean deleteImage(String publicId) {
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equals(result.get("result"));
        } catch (IOException e) {
            System.err.println("Lỗi xóa ảnh: " + e.getMessage());
            return false;
        }
    }

    /**
     * Validate file ảnh trước khi upload
     * 
     * @param file File cần validate
     * @throws IllegalArgumentException Nếu file không hợp lệ
     */
    private void validateImageFile(MultipartFile file) {
        // Kiểm tra file có tồn tại không
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        // Kiểm tra kích thước file
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Kích thước file không được vượt quá 5MB");
        }

        // Kiểm tra content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Chỉ chấp nhận file ảnh (JPEG, PNG, GIF, WebP)");
        }

        // Kiểm tra tên file
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }
    }

    /**
     * Tạo URL với transformation cho ảnh đã có
     * 
     * @param originalUrl URL gốc của ảnh
     * @param width       Chiều rộng
     * @param height      Chiều cao
     * @param crop        Loại crop
     * @return URL đã được transform
     */
    public String generateTransformedUrl(String originalUrl, Integer width, Integer height, String crop) {
        if (originalUrl == null || originalUrl.isEmpty()) {
            return originalUrl;
        }

        // Tạo transformation string
        StringBuilder transformation = new StringBuilder();
        if (width != null && height != null) {
            transformation.append("w_").append(width).append(",h_").append(height);
            if (crop != null) {
                transformation.append(",c_").append(crop);
            }
        }

        // Thêm transformation vào URL
        if (transformation.length() > 0) {
            return originalUrl.replace("/upload/", "/upload/" + transformation.toString() + "/");
        }

        return originalUrl;
    }
}
