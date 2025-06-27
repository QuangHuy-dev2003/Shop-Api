package com.sportshop.api.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import com.sportshop.api.Service.BrandService;
import com.sportshop.api.Domain.Brand;
import com.sportshop.api.Domain.Reponse.ApiResponse;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
public class BrandController {

    @Autowired
    private BrandService brandService;

    /**
     * Lấy tất cả thương hiệu đang active
     */
    @GetMapping("/brands")
    public ResponseEntity<ApiResponse<List<Brand>>> getAllBrands() {
        List<Brand> brands = brandService.getAllBrands();
        return ResponseEntity.ok(ApiResponse.success(brands, "Lấy danh sách thương hiệu thành công"));
    }

    /**
     * Lấy thương hiệu theo ID
     */
    @GetMapping("/brands/{id}")
    public ResponseEntity<ApiResponse<Brand>> getBrandById(@PathVariable Long id) {
        Optional<Brand> brand = brandService.getBrandById(id);
        if (brand.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(brand.get(), "Lấy thông tin thương hiệu thành công"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Không tìm thấy thương hiệu với ID: " + id));
        }
    }

    /**
     * Tạo mới thương hiệu (có thể có hoặc không có logo)
     */
    @PostMapping("/brands")
    public ResponseEntity<ApiResponse<Brand>> createBrand(
            @RequestPart("brand") String brandJson,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {
        Brand brand = brandService.parseBrandJson(brandJson);
        Brand createdBrand = brandService.createOrUpdateBrand(null, brand, logoFile);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(createdBrand, "Tạo thương hiệu thành công"));
    }

    /**
     * Cập nhật thương hiệu (có thể có hoặc không có logo, cập nhật mọi trường)
     */
    @PutMapping("/brands/{id}")
    public ResponseEntity<ApiResponse<Brand>> updateBrand(
            @PathVariable Long id,
            @RequestPart("brand") String brandJson,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile) {
        Brand brand = brandService.parseBrandJson(brandJson);
        Brand updatedBrand = brandService.createOrUpdateBrand(id, brand, logoFile);
        return ResponseEntity.ok(ApiResponse.success(updatedBrand, "Cập nhật thương hiệu thành công"));
    }

    /**
     * Xóa thương hiệu (hard delete)
     */
    @DeleteMapping("/brands/{id}")
    public ResponseEntity<ApiResponse<String>> deleteBrand(@PathVariable Long id) {
        try {
            brandService.hardDeleteBrand(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa thương hiệu vĩnh viễn thành công"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}
