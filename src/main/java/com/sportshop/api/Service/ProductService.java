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
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            BrandRepository brandRepository, ProductImageRepository productImageRepository,
            ProductVariantsRepository productVariantsRepository, CloudinaryService cloudinaryService) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantsRepository = productVariantsRepository;
        this.cloudinaryService = cloudinaryService;
        this.objectMapper = new ObjectMapper();
    }

    // Lấy tất cả sản phẩm (trả về response chuẩn)
    public List<ProductResponse> getAllProductResponses() {
        return productRepository.findAll().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    // Lấy sản phẩm theo ID (trả về response chuẩn)
    public ProductResponse getProductResponseById(Long id) {
        Products product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        return convertToProductResponse(product);
    }

    /**
     * Tạo mới hoặc cập nhật sản phẩm (nếu id null thì tạo mới, có id thì cập nhật)
     * 
     * @param id          id sản phẩm (null nếu tạo mới)
     * @param productJson JSON string thông tin sản phẩm (bao gồm variants,
     *                    categoryId, brandId, ...)
     * @param images      Danh sách ảnh (có thể null)
     */
    @Transactional
    public ProductResponse createOrUpdateProduct(Long id, String productJson, List<MultipartFile> images) {
        try {
            // Parse JSON thành DTO
            CreateProductRequest request = objectMapper.readValue(productJson, CreateProductRequest.class);

            Products product;
            if (id == null) {
                product = new Products();
                product.setIsActive(true);
            } else {
                product = productRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
                // Xóa ảnh cũ trên Cloudinary nếu có
                deleteProductImagesOnCloudinary(product);
                // Xóa ảnh cũ trong DB
                productImageRepository.deleteByProductId(product.getId());
                // Xóa variants cũ nếu muốn (tùy logic)
                productVariantsRepository.deleteByProductId(product.getId());
            }

            // Set các trường cơ bản
            product.setName(request.getName());
            product.setDescription(request.getDescription());
            product.setPrice(request.getPrice());
            product.setSale(request.getSale());
            product.setIsActive(true);

            // Set category & brand
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new RuntimeException("Không tìm thấy danh mục với ID: " + request.getCategoryId()));
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(
                            () -> new RuntimeException("Không tìm thấy thương hiệu với ID: " + request.getBrandId()));
            product.setCategory(category);
            product.setBrand(brand);

            // Tính sale price nếu có sale
            if (request.getSale() != null && request.getSale() > 0) {
                BigDecimal salePrice = request.getPrice().multiply(BigDecimal.valueOf(100 - request.getSale()))
                        .divide(BigDecimal.valueOf(100));
                product.setSalePrice(salePrice);
            }

            // Lưu sản phẩm
            Products savedProduct = productRepository.save(product);

            // Upload ảnh lên Cloudinary nếu có
            List<String> imageUrls = new ArrayList<>();
            if (images != null && !images.isEmpty()) {
                imageUrls = cloudinaryService.uploadMultipleImages(images, "products");
            } else if (request.getAdditionalImages() != null) {
                imageUrls = request.getAdditionalImages();
            }

            // Lưu ảnh vào DB
            if (imageUrls != null && !imageUrls.isEmpty()) {
                List<Product_images> productImages = imageUrls.stream()
                        .map(url -> {
                            Product_images img = new Product_images();
                            img.setProduct(savedProduct);
                            img.setImageUrl(url);
                            return img;
                        }).collect(Collectors.toList());
                productImageRepository.saveAll(productImages);
                // Ảnh đầu tiên là ảnh đại diện
                savedProduct.setImageUrl(imageUrls.get(0));
                productRepository.save(savedProduct);
            }

            // Lưu variants nếu có
            if (request.getVariants() != null && !request.getVariants().isEmpty()) {
                List<Product_variants> variants = request.getVariants().stream()
                        .map(variantRequest -> {
                            Product_variants variant = new Product_variants();
                            variant.setProduct(savedProduct);
                            variant.setSize(variantRequest.getSize());
                            variant.setColor(variantRequest.getColor());
                            variant.setStockQuantity(variantRequest.getStockQuantity());
                            variant.setPrice(
                                    variantRequest.getPrice() != null ? variantRequest.getPrice() : request.getPrice());
                            return variant;
                        }).collect(Collectors.toList());
                productVariantsRepository.saveAll(variants);
            }

            return convertToProductResponse(savedProduct);

        } catch (Exception e) {
            throw new RuntimeException("Lỗi xử lý sản phẩm: " + e.getMessage(), e);
        }
    }

    /**
     * Xóa sản phẩm và toàn bộ ảnh trên Cloudinary
     */
    @Transactional
    public void deleteProduct(Long id) {
        Products product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + id));
        // Xóa ảnh trên Cloudinary
        deleteProductImagesOnCloudinary(product);
        // Xóa ảnh trong DB
        productImageRepository.deleteByProductId(product.getId());
        // Xóa variants
        productVariantsRepository.deleteByProductId(product.getId());
        // Xóa sản phẩm
        productRepository.delete(product);
    }

    /**
     * Xóa tất cả ảnh của sản phẩm trên Cloudinary
     */
    private void deleteProductImagesOnCloudinary(Products product) {
        List<Product_images> images = productImageRepository.findByProductId(product.getId());
        for (Product_images img : images) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(img.getImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
        }
        // Xóa ảnh đại diện nếu có
        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            String publicId = cloudinaryService.extractPublicIdFromUrl(product.getImageUrl());
            if (publicId != null) {
                cloudinaryService.deleteImage(publicId);
            }
        }
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
                        variantResponse.setColor(variantRequest.getColor());
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

    // Chuyển đổi Products thành ProductResponse
    private ProductResponse convertToProductResponse(Products product) {
        // Fetch images from DB
        List<Product_images> images = productImageRepository.findByProductId(product.getId());
        List<String> imageUrls = images.stream().map(Product_images::getImageUrl).collect(Collectors.toList());
        // Fetch variants from DB
        List<Product_variants> variants = productVariantsRepository.findByProductId(product.getId());
        List<ProductResponse.ProductVariantResponse> variantResponses = variants.stream().map(variant -> {
            ProductResponse.ProductVariantResponse resp = new ProductResponse.ProductVariantResponse();
            resp.setId(variant.getId());
            resp.setSize(variant.getSize().name());
            resp.setColor(variant.getColor());
            resp.setStockQuantity(variant.getStockQuantity());
            resp.setPrice(variant.getPrice());
            return resp;
        }).collect(Collectors.toList());
        // Build response
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
        response.setAdditionalImages(imageUrls);
        // Set category, brand
        if (product.getCategory() != null) {
            ProductResponse.CategoryResponse categoryResponse = new ProductResponse.CategoryResponse();
            categoryResponse.setId(product.getCategory().getId());
            categoryResponse.setName(product.getCategory().getName());
            categoryResponse.setDescription(product.getCategory().getDescription());
            response.setCategory(categoryResponse);
        }
        if (product.getBrand() != null) {
            ProductResponse.BrandResponse brandResponse = new ProductResponse.BrandResponse();
            brandResponse.setId(product.getBrand().getId());
            brandResponse.setName(product.getBrand().getName());
            brandResponse.setDescription(product.getBrand().getDescription());
            brandResponse.setLogoUrl(product.getBrand().getLogoUrl());
            response.setBrand(brandResponse);
        }
        response.setVariants(variantResponses);
        return response;
    }
}
