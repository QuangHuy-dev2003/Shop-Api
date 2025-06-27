package com.sportshop.api.Controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import com.sportshop.api.Service.ProductService;
import com.sportshop.api.Domain.Request.Product.CreateProductRequest;
import com.sportshop.api.Domain.Reponse.ApiResponse;
import com.sportshop.api.Domain.Reponse.Product.ProductResponse;

import java.util.List;

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
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        ProductResponse product = productService.getProductResponseById(id);
        return ResponseEntity.ok(ApiResponse.success(product, "Lấy sản phẩm thành công"));
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
            @PathVariable Long id,
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        ProductResponse updated = productService.createOrUpdateProduct(id, productJson, images);
        return ResponseEntity.ok(ApiResponse.success(updated, "Cập nhật sản phẩm thành công"));
    }

    // Xóa sản phẩm (xóa luôn ảnh trên Cloudinary)
    @DeleteMapping("/products/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm thành công"));
    }

}
