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

    @Autowired
    private ProductService productService;

    /**
     * Tạo sản phẩm mới với thông tin cơ bản (không upload ảnh)
     */
    @PostMapping("/products")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductResponse product = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Tạo sản phẩm thành công"));
    }

    /**
     * Tạo sản phẩm mới với upload ảnh lên Cloudinary
     */
    @PostMapping("/products/with-images")
    public ResponseEntity<ApiResponse<ProductResponse>> createProductWithImages(
            @RequestPart("product") @Valid CreateProductRequest request,
            @RequestPart(value = "mainImage", required = false) MultipartFile mainImage,
            @RequestPart(value = "additionalImages", required = false) List<MultipartFile> additionalImages) {

        ProductResponse product = productService.createProductWithImageUpload(request, mainImage, additionalImages);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(product, "Tạo sản phẩm với ảnh thành công"));
    }
}
