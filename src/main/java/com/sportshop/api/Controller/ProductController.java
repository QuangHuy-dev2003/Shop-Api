package com.sportshop.api.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.sportshop.api.Service.ProductService;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Domain.Reponse.Product.ProductResponse;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Lấy tất cả sản phẩm
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        List<ProductResponse> products = productService.getAllProductResponses();
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách sản phẩm thành công"));
    }

    // Lấy sản phẩm theo ID
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable("id") Long id) {
        ProductResponse product = productService.getProductResponseById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy sản phẩm thành công"));
    }

    // Lấy tất cả sản phẩm sale
    @GetMapping("/products/sale")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllSaleProducts() {
        List<ProductResponse> products = productService.getAllSaleProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy danh sách sản phẩm sale thành công"));
    }

    // Lấy top 10 sản phẩm sale theo id giảm dần
    @GetMapping("/products/sale/top10")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTop10SaleProducts() {
        List<ProductResponse> products = productService.getTop10SaleProducts();
        return ResponseEntity.ok(ApiResponse.success(products, "Lấy top 10 sản phẩm sale thành công"));
    }

    // Lấy top 10 sản phẩm mới theo category name không thuộc sale
    @GetMapping("/products/category/{categoryName}/new/top10")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getTop10NewProductsByCategory(
            @PathVariable("categoryName") String categoryName) {
        List<ProductResponse> products = productService.getTop10NewProductsByCategory(categoryName);
        return ResponseEntity
                .ok(ApiResponse.success(products, "Lấy top 10 sản phẩm " + categoryName + " mới thành công"));
    }

    // Tạo mới sản phẩm (có thể có hoặc không có ảnh)
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductResponse created = productService.createOrUpdateProduct(null, productJson, images);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(created, "Tạo sản phẩm thành công"));
    }

    // Cập nhật sản phẩm
    @PutMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable("id") Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductResponse updated = productService.smartUpdateProduct(id, productJson, images);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật sản phẩm thành công"));
    }

    // Xóa sản phẩm (xóa luôn ảnh trên Cloudinary)
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công"));
    }

    // Import sản phẩm từ file Excel
    @PostMapping("/import-products-from-excel")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkImportProducts(
            @RequestParam("file") MultipartFile file) {
        try {
            Map<String, Object> result = productService.bulkImportFromExcel(file);
            return ResponseEntity.ok(ApiResponse.success(result, "Import thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi import: " + e.getMessage()));
        }
    }

    // Tạo file Excel mẫu
    @GetMapping("/download-excel-template")
    public ResponseEntity<Resource> downloadExcelTemplate() {
        try {
            Resource resource = productService.generateExcelTemplate();
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"product_import_template.xlsx\"")
                    .header(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS, "Content-Disposition")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo file template: " + e.getMessage());
        }
    }

    // Upload ảnh lên Cloudinary (không liên kết sản phẩm)
    @PostMapping("/upload-images")
    public ResponseEntity<ApiResponse<List<String>>> uploadImagesToCloudinary(
            @RequestParam("images") List<MultipartFile> images) {
        try {
            List<String> imageUrls = productService.uploadImagesToCloudinary(images);
            return ResponseEntity.ok(ApiResponse.success(imageUrls, "Upload ảnh lên Cloudinary thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi upload ảnh: " + e.getMessage()));
        }
    }

    // Upload ảnh lên Cloudinary với transformation (không liên kết sản phẩm)
    @PostMapping("/upload-images-with-transformation")
    public ResponseEntity<ApiResponse<List<String>>> uploadImagesToCloudinaryWithTransformation(
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam(value = "width", required = false) Integer width,
            @RequestParam(value = "height", required = false) Integer height,
            @RequestParam(value = "crop", required = false) String crop) {
        try {
            List<String> imageUrls = productService.uploadImagesToCloudinaryWithTransformation(
                    images, width, height, crop);
            return ResponseEntity.ok(ApiResponse.success(imageUrls, "Upload ảnh với transformation thành công"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Lỗi upload ảnh: " + e.getMessage()));
        }
    }

    // Search sản phẩm theo tên (LIKE, ignore case, chỉ lấy sản phẩm active, limit
    // 5)
    @GetMapping("/products/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProductsByName(
            @RequestParam("keyword") String keyword) {
        List<ProductResponse> products = productService.searchProductsByName(keyword);
        return ResponseEntity.ok(ApiResponse.success(products, "Tìm kiếm sản phẩm thành công"));
    }

}
