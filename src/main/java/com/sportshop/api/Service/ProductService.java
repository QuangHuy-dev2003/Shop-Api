package com.sportshop.api.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.sportshop.api.Domain.Products;
import com.sportshop.api.Domain.Reponse.Product.ProductResponse;
import com.sportshop.api.Domain.Category;
import com.sportshop.api.Domain.Brand;
import com.sportshop.api.Domain.Product_images;
import com.sportshop.api.Domain.Product_variants;
import com.sportshop.api.Domain.Request.Product.CreateProductRequest;
import com.sportshop.api.Repository.ProductRepository;
import com.sportshop.api.Repository.CategoryRepository;
import com.sportshop.api.Repository.BrandRepository;
import com.sportshop.api.Repository.ProductImageRepository;
import com.sportshop.api.Repository.ProductVariantsRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final CloudinaryService cloudinaryService;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            BrandRepository brandRepository, ProductImageRepository productImageRepository,
            ProductVariantsRepository productVariantsRepository, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantsRepository = productVariantsRepository;
        this.cloudinaryService = cloudinaryService;
    }

    public List<Products> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Tạo sản phẩm mới với thông tin cơ bản
     */
    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        // Tìm category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));

        // Tìm brand
        Brand brand = brandRepository.findById(request.getBrandId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + request.getBrandId()));

        // Tạo sản phẩm mới
        Products product = new Products();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSale(request.getSale());
        product.setCategory(category);
        product.setBrand(brand);
        product.setImageUrl(request.getImageUrl());
        product.setIsActive(true);

        // Tính sale price nếu có sale
        if (request.getSale() != null && request.getSale() > 0) {
            BigDecimal salePrice = request.getPrice().multiply(BigDecimal.valueOf(100 - request.getSale()))
                    .divide(BigDecimal.valueOf(100));
            product.setSalePrice(salePrice);
        }

        // Lưu sản phẩm
        Products savedProduct = productRepository.save(product);

        // Tạo product images nếu có
        if (request.getAdditionalImages() != null && !request.getAdditionalImages().isEmpty()) {
            List<Product_images> productImages = request.getAdditionalImages().stream()
                    .map(imageUrl -> {
                        Product_images image = new Product_images();
                        image.setProduct(savedProduct);
                        image.setImageUrl(imageUrl);
                        return image;
                    })
                    .collect(Collectors.toList());
            productImageRepository.saveAll(productImages);
        }

        // Tạo product variants nếu có
        if (request.getVariants() != null && !request.getVariants().isEmpty()) {
            List<Product_variants> variants = request.getVariants().stream()
                    .map(variantRequest -> {
                        Product_variants variant = new Product_variants();
                        variant.setProduct(savedProduct);
                        variant.setSize(variantRequest.getSize());
                        variant.setStockQuantity(variantRequest.getStockQuantity());
                        variant.setPrice(
                                variantRequest.getPrice() != null ? variantRequest.getPrice() : request.getPrice());
                        return variant;
                    })
                    .collect(Collectors.toList());
            productVariantsRepository.saveAll(variants);
        }

        // Chuyển đổi thành response
        return convertToProductResponse(savedProduct, request.getAdditionalImages(), request.getVariants());
    }

    /**
     * Chuyển đổi Products thành ProductResponse
     */
    private ProductResponse convertToProductResponse(Products product, List<String> additionalImages,
            List<CreateProductRequest.ProductVariantRequest> variants) {
        ProductResponse response = new ProductResponse();
        response.setId(product.getId());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setSale(product.getSale());
        response.setSalePrice(product.getSalePrice());
        response.setIsActive(product.getIsActive());
        response.setImageUrl(product.getImageUrl());
        response.setCreatedAt(product.getCreatedAt());
        response.setAdditionalImages(additionalImages);

        // Set category
        if (product.getCategory() != null) {
            ProductResponse.CategoryResponse categoryResponse = new ProductResponse.CategoryResponse();
            categoryResponse.setId(product.getCategory().getId());
            categoryResponse.setName(product.getCategory().getName());
            categoryResponse.setDescription(product.getCategory().getDescription());
            response.setCategory(categoryResponse);
        }

        // Set brand
        if (product.getBrand() != null) {
            ProductResponse.BrandResponse brandResponse = new ProductResponse.BrandResponse();
            brandResponse.setId(product.getBrand().getId());
            brandResponse.setName(product.getBrand().getName());
            brandResponse.setDescription(product.getBrand().getDescription());
            brandResponse.setLogoUrl(product.getBrand().getLogoUrl());
            response.setBrand(brandResponse);
        }

        // Set variants
        if (variants != null) {
            List<ProductResponse.ProductVariantResponse> variantResponses = variants.stream()
                    .map(variantRequest -> {
                        ProductResponse.ProductVariantResponse variantResponse = new ProductResponse.ProductVariantResponse();
                        variantResponse.setSize(variantRequest.getSize().name());
                        variantResponse.setStockQuantity(variantRequest.getStockQuantity());
                        variantResponse.setPrice(
                                variantRequest.getPrice() != null ? variantRequest.getPrice() : product.getPrice());
                        return variantResponse;
                    })
                    .collect(Collectors.toList());
            response.setVariants(variantResponses);
        }

        return response;
    }
}
