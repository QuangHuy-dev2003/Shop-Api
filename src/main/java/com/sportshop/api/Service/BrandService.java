package com.sportshop.api.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sportshop.api.Domain.Brand;
import com.sportshop.api.Repository.BrandRepository;

@Service
public class BrandService {

    private final BrandRepository brandRepository;
    private final CloudinaryService cloudinaryService;

    public BrandService(BrandRepository brandRepository, CloudinaryService cloudinaryService) {
        this.brandRepository = brandRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public Brand parseBrandJson(String brandJson) {
        try {
            return new ObjectMapper().readValue(brandJson, Brand.class);
        } catch (IOException e) {
            throw new RuntimeException("Lỗi parse brand json: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy tất cả thương hiệu
     */
    public List<Brand> getAllBrands() {
        return brandRepository.findByIsActiveTrue();
    }

    /**
     * Lấy thương hiệu theo ID
     */
    public Optional<Brand> getBrandById(Long id) {
        return brandRepository.findById(id);
    }

    /**
     * Tạo mới hoặc cập nhật thương hiệu (nếu id null thì tạo mới, có id thì cập
     * nhật)
     */
    @Transactional
    public Brand createOrUpdateBrand(Long id, Brand brandDetails, MultipartFile logoFile) {
        Brand brand;
        if (id == null) {
            // Tạo mới
            brand = new Brand();
            brand.setIsActive(brandDetails.getIsActive() != null ? brandDetails.getIsActive() : true);
        } else {
            // Cập nhật
            brand = brandRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        }
        brand.setName(brandDetails.getName());
        brand.setDescription(brandDetails.getDescription());
        brand.setIsActive(brandDetails.getIsActive() != null ? brandDetails.getIsActive() : brand.getIsActive());
        if (logoFile != null && !logoFile.isEmpty()) {
            try {
                String logoUrl = cloudinaryService.uploadImage(logoFile, "brands/logos");
                brand.setLogoUrl(logoUrl);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload logo: " + e.getMessage(), e);
            }
        }
        return brandRepository.save(brand);
    }

    /**
     * Xóa vĩnh viễn thương hiệu (hard delete)
     */
    @Transactional
    public void hardDeleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + id));
        // Nếu có logoUrl thì xóa ảnh trên Cloudinary
        if (brand.getLogoUrl() != null && !brand.getLogoUrl().isEmpty()) {
            // Lấy publicId từ URL Cloudinary
            String publicId = cloudinaryService.extractPublicIdFromUrl(brand.getLogoUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
        }
        brandRepository.delete(brand);
    }

}