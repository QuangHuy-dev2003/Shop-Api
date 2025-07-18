package com.sportshop.api.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.time.LocalDateTime;
import java.util.Date;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.ss.util.CellRangeAddress;

import com.sportshop.api.Domain.Products;
import com.sportshop.api.Domain.Reponse.Product.ProductResponse;
import com.sportshop.api.Domain.Category;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportshop.api.Domain.Brand;
import com.sportshop.api.Domain.Product_images;
import com.sportshop.api.Domain.Product_variants;
import com.sportshop.api.Domain.Request.Product.CreateProductRequest;
import com.sportshop.api.Domain.Request.Product.UpdateProductRequest;

import com.sportshop.api.Repository.ProductRepository;
import com.sportshop.api.Repository.CategoryRepository;
import com.sportshop.api.Repository.BrandRepository;
import com.sportshop.api.Repository.ProductImageRepository;
import com.sportshop.api.Repository.ProductVariantsRepository;
import com.sportshop.api.Repository.ProductReviewsRepository;
import com.sportshop.api.Repository.CartItemRepository;
import com.sportshop.api.Repository.FavoritesRepository;
import com.sportshop.api.Repository.OrderItemsRepository;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final CloudinaryService cloudinaryService;
    private final ObjectMapper objectMapper;

    private final ProductReviewsRepository productReviewsRepository;
    private final CartItemRepository cartItemRepository;
    private final FavoritesRepository favoritesRepository;
    private final OrderItemsRepository orderItemsRepository;

    public ProductService(ProductRepository productRepository, CategoryRepository categoryRepository,
            BrandRepository brandRepository, ProductImageRepository productImageRepository,
            ProductVariantsRepository productVariantsRepository, CloudinaryService cloudinaryService,
            ProductReviewsRepository productReviewsRepository, CartItemRepository cartItemRepository,
            FavoritesRepository favoritesRepository, OrderItemsRepository orderItemsRepository) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantsRepository = productVariantsRepository;
        this.cloudinaryService = cloudinaryService;
        this.objectMapper = new ObjectMapper();
        this.productReviewsRepository = productReviewsRepository;
        this.cartItemRepository = cartItemRepository;
        this.favoritesRepository = favoritesRepository;
        this.orderItemsRepository = orderItemsRepository;
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

    // Lấy tất cả sản phẩm sale
    public List<ProductResponse> getAllSaleProducts() {
        return productRepository.findAllSaleProducts().stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    // Lấy top 10 sản phẩm sale theo id giảm dần
    public List<ProductResponse> getTop10SaleProducts() {
        return productRepository.findTop10SaleProducts().stream()
                .limit(10)
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    // Lấy top 10 sản phẩm mới theo category name không thuộc sale
    public List<ProductResponse> getTop10NewProductsByCategory(String categoryName) {
        return productRepository.findTop10NewProductsByCategory(categoryName).stream()
                .limit(10)
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tạo mới sản phẩm
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
            product.setProductCode(request.getProductCode());
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

            // Kiểm tra trùng product_code
            if (productRepository.findByProductCode(request.getProductCode()).isPresent()) {
                throw new RuntimeException("Mã sản phẩm đã tồn tại");
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

                List<String> imageColors = request.getImageColors(); // List<String> cùng size với imageUrls, có thể
                                                                     // null
                List<Product_images> productImages = new ArrayList<>();
                for (int i = 0; i < imageUrls.size(); i++) {
                    Product_images img = new Product_images();
                    img.setProduct(savedProduct);
                    img.setImageUrl(imageUrls.get(i));
                    if (imageColors != null && imageColors.size() > i) {
                        img.setColor(imageColors.get(i)); // gán màu cho ảnh nếu có
                    }
                    productImages.add(img);
                }
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
                // Cập nhật stock_quantity của sản phẩm
                updateProductStockQuantity(savedProduct.getId());
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
        // Xóa ảnh trên Cloudinary và DB
        deleteProductImagesOnCloudinary(product);
        productImageRepository.deleteByProductId(product.getId());

        List<Long> variantIds = productVariantsRepository.findByProductId(product.getId()).stream()
                .map(Product_variants::getId)
                .collect(Collectors.toList());
        for (Long variantId : variantIds) {
            orderItemsRepository.setVariantIdNull(variantId);
        }
        // Xóa biến thể
        productVariantsRepository.deleteByProductId(product.getId());

        // Xóa đánh giá
        productReviewsRepository.deleteByProductId(product.getId());

        // Xóa sản phẩm khỏi giỏ hàng
        cartItemRepository.deleteByProductId(product.getId());

        // Xóa sản phẩm khỏi yêu thích
        favoritesRepository.deleteByProductId(product.getId());

        // Giữ lịch sử đơn hàng: set product_id = null ở order_items
        orderItemsRepository.setProductIdNull(product.getId());

        // Cuối cùng xóa sản phẩm
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

    // Chuyển đổi Products thành ProductResponse
    private ProductResponse convertToProductResponse(Products product) {
        // Fetch images from DB
        List<Product_images> images = productImageRepository.findByProductId(product.getId());
        List<String> imageUrls = images.stream().map(Product_images::getImageUrl).collect(Collectors.toList());
        List<String> imageColors = images.stream().map(Product_images::getColor).collect(Collectors.toList());
        List<Long> imageIds = images.stream().map(Product_images::getId).collect(Collectors.toList());
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
        response.setProductCode(product.getProductCode());
        response.setName(product.getName());
        response.setDescription(product.getDescription());
        response.setPrice(product.getPrice());
        response.setSale(product.getSale());
        response.setSalePrice(product.getSalePrice());
        response.setIsActive(product.getIsActive());
        response.setStockQuantity(product.getStockQuantity());
        response.setImageUrl(product.getImageUrl());
        response.setCreatedAt(product.getCreatedAt());
        response.setAdditionalImages(imageUrls);
        response.setImageColors(imageColors);
        response.setImageIds(imageIds);
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

    // Import sản phẩm từ file Excel
    @Transactional
    public Map<String, Object> bulkImportFromExcel(MultipartFile file) throws IOException {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Map<String, Object>> errorRows = new ArrayList<>();
        int successCount = 0;
        int errorCount = 0;

        // Map để nhóm ảnh theo product và màu
        Map<String, Map<String, List<String>>> productColorImages = new HashMap<>();
        // Set để track đã lưu ảnh cho màu nào
        Set<String> processedColorKeys = new HashSet<>();

        try (InputStream is = file.getInputStream();
                Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0); // Chỉ đọc sheet đầu tiên

            // First pass: Collect all images grouped by product and color
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row))
                    continue;

                try {
                    String productCode = getCellValueAsString(row.getCell(0));
                    String color = getCellValueAsString(row.getCell(5));
                    String imageUrls = getCellValueAsString(row.getCell(9));

                    if (productCode != null && color != null && imageUrls != null && !imageUrls.trim().isEmpty()) {
                        productColorImages.computeIfAbsent(productCode, k -> new HashMap<>())
                                .computeIfAbsent(color.trim(), k -> new ArrayList<>());

                        String[] urls = imageUrls.split(",");
                        for (String url : urls) {
                            String trimmedUrl = url.trim();
                            if (!trimmedUrl.isEmpty()) {
                                productColorImages.get(productCode).get(color.trim()).add(trimmedUrl);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Continue collecting images even if other data is invalid
                }
            }

            // Second pass: Process products and variants
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null || isEmptyRow(row))
                    continue;

                try {
                    Map<String, Object> rowResult = processProductRow(row, i + 1, productColorImages,
                            processedColorKeys);
                    if (rowResult.containsKey("error")) {
                        errors.add(rowResult);
                        errorRows.add(Map.of("row", i + 1, "data", getRowData(row)));
                        errorCount++;
                    } else {
                        successCount++;
                    }
                } catch (Exception e) {
                    Map<String, Object> error = new HashMap<>();
                    error.put("row", i + 1);
                    error.put("error", "Lỗi xử lý dòng: " + e.getMessage());
                    errors.add(error);
                    errorRows.add(Map.of("row", i + 1, "data", getRowData(row)));
                    errorCount++;
                }
            }
        }

        result.put("successCount", successCount);
        result.put("errorCount", errorCount);
        result.put("errors", errors);

        // Tạo file lỗi nếu có lỗi
        if (errorCount > 0) {
            try {
                Resource errorFile = generateErrorExcelFile(errorRows, errors);
                String errorFileName = "import_error_" + java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
                // Upload file lên Cloudinary (resource_type=raw)
                String errorFileUrl = cloudinaryService.uploadRawFile(((ByteArrayResource) errorFile).getByteArray(),
                        errorFileName, "import_errors_excel");
                result.put("errorFileUrl", errorFileUrl);
            } catch (Exception e) {
                result.put("errorFile", "Không thể tạo file lỗi: " + e.getMessage());
            }
        }

        return result;
    }

    private Map<String, Object> processProductRow(Row row, int rowNumber,
            Map<String, Map<String, List<String>>> productColorImages, Set<String> processedColorKeys) {
        Map<String, Object> result = new HashMap<>();

        try {
            // Read data from Excel row
            String productCode = getCellValueAsString(row.getCell(0));
            String productName = getCellValueAsString(row.getCell(1));
            String description = getCellValueAsString(row.getCell(2));
            String categoryName = getCellValueAsString(row.getCell(3));
            String brandName = getCellValueAsString(row.getCell(4));
            String color = getCellValueAsString(row.getCell(5));
            String sizeStr = getCellValueAsString(row.getCell(6));
            Double price = getCellValueAsDouble(row.getCell(7));
            Integer stockQuantity = getCellValueAsInteger(row.getCell(8));

            // Validation
            if (productCode == null || productCode.trim().isEmpty()) {
                result.put("row", rowNumber);
                result.put("error", "Product code không được để trống");
                result.put("action", "error");
                return result;
            }

            if (productName == null || productName.trim().isEmpty()) {
                result.put("row", rowNumber);
                result.put("error", "Product name không được để trống");
                result.put("action", "error");
                return result;
            }

            if (price == null || price <= 0) {
                result.put("row", rowNumber);
                result.put("error", "Price phải lớn hơn 0");
                result.put("action", "error");
                return result;
            }

            if (stockQuantity == null || stockQuantity < 0) {
                result.put("row", rowNumber);
                result.put("error", "Stock quantity phải >= 0");
                result.put("action", "error");
                return result;
            }

            // Find or create category
            final Category category = categoryName != null && !categoryName.trim().isEmpty()
                    ? categoryRepository.findByName(categoryName.trim())
                            .orElseGet(() -> {
                                Category newCategory = new Category();
                                newCategory.setName(categoryName.trim());
                                return categoryRepository.save(newCategory);
                            })
                    : null;

            // Find or create brand
            final Brand brand = brandName != null && !brandName.trim().isEmpty()
                    ? brandRepository.findByName(brandName.trim())
                            .orElseGet(() -> {
                                Brand newBrand = new Brand();
                                newBrand.setName(brandName.trim());
                                return brandRepository.save(newBrand);
                            })
                    : null;

            // Find existing product or create new one
            Products product = productRepository.findByProductCode(productCode.trim())
                    .orElseGet(() -> {
                        Products newProduct = new Products();
                        newProduct.setProductCode(productCode.trim());
                        newProduct.setName(productName.trim());
                        newProduct.setDescription(description != null ? description.trim() : "");
                        newProduct.setPrice(BigDecimal.valueOf(price));
                        newProduct.setCategory(category);
                        newProduct.setBrand(brand);
                        newProduct.setIsActive(true);
                        newProduct.setStockQuantity(0); // Will be updated after variants
                        newProduct.setCreatedAt(LocalDateTime.now());
                        return productRepository.save(newProduct);
                    });

            // Parse size
            Product_variants.Size sizeEnum = null;
            if (sizeStr != null && !sizeStr.trim().isEmpty()) {
                try {
                    sizeEnum = Product_variants.Size.valueOf(sizeStr.trim().toUpperCase());
                } catch (IllegalArgumentException e) {
                    result.put("row", rowNumber);
                    result.put("error", "Size không hợp lệ: " + sizeStr
                            + ". Các size hợp lệ: XS, S, M, L, XL, XXL, XXXL, SIZE_36-SIZE_46, WAIST_28-WAIST_50");
                    result.put("action", "error");
                    return result;
                }
            }
            String colorKey = color != null ? color.trim() : "";

            // Kiểm tra variant đã tồn tại chưa
            Product_variants existingVariant = productVariantsRepository.findByProductIdAndColorAndSize(
                    product.getId(), colorKey, sizeEnum);
            if (existingVariant != null) {
                // Cập nhật variant
                existingVariant.setPrice(BigDecimal.valueOf(price));
                existingVariant.setStockQuantity(stockQuantity);
                productVariantsRepository.save(existingVariant);
                result.put("action", "updated");
            } else {
                // Tạo mới variant
                Product_variants variant = new Product_variants();
                variant.setProduct(product);
                variant.setColor(colorKey);
                variant.setSize(sizeEnum);
                variant.setPrice(BigDecimal.valueOf(price));
                variant.setStockQuantity(stockQuantity);
                productVariantsRepository.save(variant);
                result.put("action", "created");
            }

            // Xử lý ảnh cho màu
            if (productColorImages.containsKey(productCode) &&
                    productColorImages.get(productCode).containsKey(colorKey)) {
                List<String> colorImages = productColorImages.get(productCode).get(colorKey);
                String colorKeyForTracking = productCode + "_" + colorKey;
                if (!processedColorKeys.contains(colorKeyForTracking)) {
                    // Kiểm tra ảnh cũ
                    List<Product_images> oldImages = productImageRepository.findByProductIdAndColor(product.getId(),
                            colorKey);
                    List<String> oldUrls = oldImages.stream().map(Product_images::getImageUrl).toList();
                    boolean needUpdate = !new HashSet<>(oldUrls).equals(new HashSet<>(colorImages));
                    if (needUpdate) {
                        // Xóa ảnh cũ
                        for (Product_images img : oldImages) {
                            String publicId = cloudinaryService.extractPublicIdFromUrl(img.getImageUrl());
                            if (publicId != null) {
                                cloudinaryService.deleteImage(publicId);
                            }
                            productImageRepository.delete(img);
                        }
                        // Lưu ảnh mới
                        for (String imageUrl : colorImages) {
                            Product_images image = new Product_images();
                            image.setProduct(product);
                            image.setImageUrl(imageUrl);
                            image.setColor(colorKey);
                            productImageRepository.save(image);
                        }
                        // Set main image nếu chưa có
                        if (product.getImageUrl() == null || product.getImageUrl().isEmpty()) {
                            product.setImageUrl(colorImages.get(0));
                            productRepository.save(product);
                        }
                    }
                    processedColorKeys.add(colorKeyForTracking);
                }
            }

            // Update product stock quantity
            updateProductStockQuantity(product.getId());
            result.put("row", rowNumber);
        } catch (Exception e) {
            result.put("row", rowNumber);
            result.put("error", "Lỗi: " + e.getMessage());
            result.put("action", "error");
        }

        return result;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                return String.valueOf((long) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf((long) cell.getNumericCellValue());
                    } catch (Exception ex) {
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    private Double getCellValueAsDouble(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return cell.getNumericCellValue();
            case STRING:
                try {
                    return Double.parseDouble(cell.getStringCellValue().replace(",", ""));
                } catch (NumberFormatException e) {
                    return null;
                }
            case FORMULA:
                try {
                    return cell.getNumericCellValue();
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private Integer getCellValueAsInteger(Cell cell) {
        if (cell == null)
            return null;

        switch (cell.getCellType()) {
            case NUMERIC:
                return (int) cell.getNumericCellValue();
            case STRING:
                try {
                    return Integer.parseInt(cell.getStringCellValue().replace(",", ""));
                } catch (NumberFormatException e) {
                    return null;
                }
            case FORMULA:
                try {
                    return (int) cell.getNumericCellValue();
                } catch (Exception e) {
                    return null;
                }
            default:
                return null;
        }
    }

    private List<String> getRowData(Row row) {
        List<String> data = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            data.add(getCellValueAsString(row.getCell(i)));
        }
        return data;
    }

    /**
     * Kiểm tra xem một dòng có trống hay không
     */
    private boolean isEmptyRow(Row row) {
        if (row == null)
            return true;

        // Kiểm tra các cột quan trọng (productCode, productName, price, stockQuantity)
        String productCode = getCellValueAsString(row.getCell(0));
        String productName = getCellValueAsString(row.getCell(1));
        Double price = getCellValueAsDouble(row.getCell(7));
        Integer stockQuantity = getCellValueAsInteger(row.getCell(8));

        // Nếu tất cả các trường bắt buộc đều trống thì coi như dòng trống
        return (productCode == null || productCode.trim().isEmpty()) &&
                (productName == null || productName.trim().isEmpty()) &&
                (price == null || price <= 0) &&
                (stockQuantity == null || stockQuantity <= 0);
    }

    // Tạo file Excel mẫu
    public Resource generateExcelTemplate() throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {

            // Sheet 1: Template dữ liệu
            Sheet dataSheet = workbook.createSheet("Dữ liệu sản phẩm");

            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Tạo headers
            String[] headers = {
                    "Product Code*", "Product Name*", "Description", "Category Name", "Brand Name",
                    "Color", "Size", "Price*", "Stock Quantity*", "Image URLs"
            };

            Row headerRow = dataSheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                dataSheet.setColumnWidth(i, 20 * 256); // Set column width
            }

            // Tạo style cho dữ liệu mẫu
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);

            // Thêm dữ liệu mẫu
            String[][] sampleData = {
                    { "SP001", "Nike Air Max 270", "Giày chạy bộ thoải mái", "Running Shoes", "Nike", "Red", "SIZE_42",
                            "1500000", "10", "https://example.com/image1.jpg,https://example.com/image2.jpg" },
                    { "SP001", "Nike Air Max 270", "Giày chạy bộ thoải mái", "Running Shoes", "Nike", "Blue", "SIZE_42",
                            "1500000", "5", "https://example.com/image3.jpg" },
                    { "SP002", "Adidas T-Shirt", "Áo thun thể thao", "T-Shirts", "Adidas", "Black", "M", "500000", "15",
                            "https://example.com/image4.jpg" }
            };

            for (int i = 0; i < sampleData.length; i++) {
                Row row = dataSheet.createRow(i + 1);
                for (int j = 0; j < sampleData[i].length; j++) {
                    Cell cell = row.createCell(j);
                    cell.setCellValue(sampleData[i][j]);
                    cell.setCellStyle(dataStyle);
                }
            }

            // Sheet 2: Hướng dẫn
            Sheet guideSheet = workbook.createSheet("Hướng dẫn");

            // Tạo style cho tiêu đề
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);

            // Tạo style cho nội dung
            CellStyle contentStyle = workbook.createCellStyle();
            contentStyle.setWrapText(true);

            int rowNum = 0;

            // Tiêu đề
            Row titleRow = guideSheet.createRow(rowNum++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("HƯỚNG DẪN NHẬP DỮ LIỆU SẢN PHẨM");
            titleCell.setCellStyle(titleStyle);

            rowNum++; // Dòng trống

            // Quy tắc chung
            Row ruleTitleRow = guideSheet.createRow(rowNum++);
            Cell ruleTitleCell = ruleTitleRow.createCell(0);
            ruleTitleCell.setCellValue("QUY TẮC CHUNG:");
            ruleTitleCell.setCellStyle(titleStyle);

            String[] rules = {
                    "1. Chỉ nhập dữ liệu vào Sheet 'Dữ liệu sản phẩm'",
                    "2. Không xóa hoặc thay đổi dòng header (dòng 1)",
                    "3. Các cột có dấu * là bắt buộc",
                    "4. Mỗi dòng là một variant của sản phẩm",
                    "5. Upload ảnh lên Cloudinary trước khi nhập URL"
            };

            for (String rule : rules) {
                Row ruleRow = guideSheet.createRow(rowNum++);
                Cell ruleCell = ruleRow.createCell(0);
                ruleCell.setCellValue(rule);
                ruleCell.setCellStyle(contentStyle);
            }

            rowNum++; // Dòng trống

            // Chi tiết từng cột
            Row detailTitleRow = guideSheet.createRow(rowNum++);
            Cell detailTitleCell = detailTitleRow.createCell(0);
            detailTitleCell.setCellValue("CHI TIẾT TỪNG CỘT:");
            detailTitleCell.setCellStyle(titleStyle);

            String[] columnDetails = {
                    "A - Product Code*: Mã sản phẩm duy nhất (bắt buộc, dùng để nhóm variants)",
                    "B - Product Name*: Tên sản phẩm (bắt buộc)",
                    "C - Description: Mô tả sản phẩm (tùy chọn)",
                    "D - Category Name: Tên danh mục (tùy chọn, sẽ tạo mới nếu chưa có)",
                    "E - Brand Name: Tên thương hiệu (tùy chọn, sẽ tạo mới nếu chưa có)",
                    "F - Color: Màu sắc variant (tùy chọn)",
                    "G - Size: Kích thước (tùy chọn, sử dụng các giá trị: XS, S, M, L, XL, XXL, XXXL, SIZE_36-SIZE_46, WAIST_28-WAIST_50)",
                    "H - Price*: Giá sản phẩm (bắt buộc, > 0, đơn vị VND)",
                    "I - Stock Quantity*: Số lượng tồn kho (bắt buộc, >= 0)",
                    "J - Image URLs: URL ảnh (tùy chọn, phân cách bằng dấu phẩy)"
            };

            for (String detail : columnDetails) {
                Row detailRow = guideSheet.createRow(rowNum++);
                Cell detailCell = detailRow.createCell(0);
                detailCell.setCellValue(detail);
                detailCell.setCellStyle(contentStyle);
            }

            rowNum++; // Dòng trống

            // Ví dụ
            Row exampleTitleRow = guideSheet.createRow(rowNum++);
            Cell exampleTitleCell = exampleTitleRow.createCell(0);
            exampleTitleCell.setCellValue("VÍ DỤ:");
            exampleTitleCell.setCellStyle(titleStyle);

            String[] examples = {
                    "Sản phẩm giày Nike với 2 màu:",
                    "Dòng 1: SP001 | Nike Air Max 270 | Giày chạy bộ | Running Shoes | Nike | Red | SIZE_42 | 1500000 | 10 | https://example.com/red.jpg",
                    "Dòng 2: SP001 | Nike Air Max 270 | Giày chạy bộ | Running Shoes | Nike | Blue | SIZE_42 | 1500000 | 5 | https://example.com/blue.jpg",
                    "",
                    "Sản phẩm áo với nhiều size:",
                    "Dòng 1: SP002 | Adidas T-Shirt | Áo thun | T-Shirts | Adidas | Black | S | 500000 | 15 | https://example.com/black.jpg",
                    "Dòng 2: SP002 | Adidas T-Shirt | Áo thun | T-Shirts | Adidas | Black | M | 500000 | 20 | https://example.com/black.jpg",
                    "Dòng 3: SP002 | Adidas T-Shirt | Áo thun | T-Shirts | Adidas | Black | L | 500000 | 12 | https://example.com/black.jpg"
            };

            for (String example : examples) {
                Row exampleRow = guideSheet.createRow(rowNum++);
                Cell exampleCell = exampleRow.createCell(0);
                exampleCell.setCellValue(example);
                exampleCell.setCellStyle(contentStyle);
            }

            // Set column width cho sheet hướng dẫn
            guideSheet.setColumnWidth(0, 80 * 256);

            // Ghi workbook vào ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    // Tạo file Excel lỗi
    private Resource generateErrorExcelFile(List<Map<String, Object>> errorRows, List<Map<String, Object>> errors)
            throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dữ liệu lỗi");

            // Tạo style cho header
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);

            // Tạo style cho dữ liệu lỗi
            CellStyle errorStyle = workbook.createCellStyle();
            Font errorFont = workbook.createFont();
            errorFont.setColor(IndexedColors.RED.getIndex());
            errorStyle.setFont(errorFont);
            errorStyle.setFillForegroundColor(IndexedColors.ROSE.getIndex());
            errorStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            errorStyle.setBorderTop(BorderStyle.THIN);
            errorStyle.setBorderBottom(BorderStyle.THIN);
            errorStyle.setBorderLeft(BorderStyle.THIN);
            errorStyle.setBorderRight(BorderStyle.THIN);

            // Headers
            String[] headers = {
                    "Row", "Product Code", "Product Name", "Description", "Category Name", "Brand Name",
                    "Color", "Size", "Price", "Stock Quantity", "Image URLs", "Lỗi", "Action"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // Dữ liệu lỗi
            for (int i = 0; i < errorRows.size(); i++) {
                Row row = sheet.createRow(i + 1);

                Map<String, Object> errorRow = errorRows.get(i);
                Map<String, Object> error = errors.get(i);

                List<String> data = (List<String>) errorRow.get("data");

                // Row number
                Cell rowCell = row.createCell(0);
                rowCell.setCellValue((Integer) errorRow.get("row"));
                rowCell.setCellStyle(errorStyle);

                // Data
                for (int j = 0; j < data.size(); j++) {
                    Cell cell = row.createCell(j + 1);
                    cell.setCellValue(data.get(j) != null ? data.get(j) : "");
                    cell.setCellStyle(errorStyle);
                }

                // Error message
                Cell errorCell = row.createCell(11);
                errorCell.setCellValue((String) error.get("error"));
                errorCell.setCellStyle(errorStyle);

                // Action
                Cell actionCell = row.createCell(12);
                actionCell.setCellValue(error.get("action") != null ? error.get("action").toString() : "");
                actionCell.setCellStyle(errorStyle);
            }

            // Ghi workbook vào ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);

            return new ByteArrayResource(outputStream.toByteArray());
        }
    }

    /**
     * Cập nhật stock_quantity của sản phẩm bằng tổng stock của tất cả variants
     */
    private void updateProductStockQuantity(Long productId) {
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm với ID: " + productId));

        List<Product_variants> variants = productVariantsRepository.findByProductId(productId);
        int totalStock = variants.stream().mapToInt(Product_variants::getStockQuantity).sum();

        product.setStockQuantity(totalStock);
        productRepository.save(product);
    }

    /**
     * Upload ảnh lên Cloudinary (không liên kết với sản phẩm)
     * Dùng để upload ảnh trước khi import Excel
     * 
     * @param images Danh sách ảnh cần upload
     * @return Danh sách URL ảnh đã upload
     */
    public List<String> uploadImagesToCloudinary(List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ảnh không được để trống");
        }

        try {
            // Upload ảnh lên Cloudinary với folder "products"
            List<String> imageUrls = cloudinaryService.uploadMultipleImages(images, "products");
            return imageUrls;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage(), e);
        }
    }

    /**
     * Upload ảnh lên Cloudinary với transformation (không liên kết với sản phẩm)
     * 
     * @param images Danh sách ảnh cần upload
     * @param width  Chiều rộng mong muốn (có thể null)
     * @param height Chiều cao mong muốn (có thể null)
     * @param crop   Loại crop (có thể null)
     * @return Danh sách URL ảnh đã upload
     */
    public List<String> uploadImagesToCloudinaryWithTransformation(List<MultipartFile> images,
            Integer width, Integer height, String crop) {
        if (images == null || images.isEmpty()) {
            throw new IllegalArgumentException("Danh sách ảnh không được để trống");
        }

        try {
            List<String> imageUrls = new ArrayList<>();

            // Upload từng ảnh với transformation
            for (MultipartFile image : images) {
                String imageUrl = cloudinaryService.uploadImageWithTransformation(image, "products", width, height,
                        crop);
                imageUrls.add(imageUrl);
            }

            return imageUrls;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi upload ảnh: " + e.getMessage(), e);
        }
    }

    @Transactional
    public ProductResponse smartUpdateProduct(Long id, String productJson, List<MultipartFile> images) {
        try {
            com.sportshop.api.Domain.Request.Product.UpdateProductRequest request = objectMapper.readValue(productJson,
                    com.sportshop.api.Domain.Request.Product.UpdateProductRequest.class);

            Products product = productRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

            // Cập nhật các trường cơ bản nếu có
            if (request.getName() != null)
                product.setName(request.getName());
            if (request.getProductCode() != null)
                product.setProductCode(request.getProductCode());
            if (request.getDescription() != null)
                product.setDescription(request.getDescription());
            if (request.getPrice() != null)
                product.setPrice(request.getPrice());
            if (request.getSale() != null)
                product.setSale(request.getSale());
            if (request.getIsActive() != null)
                product.setIsActive(request.getIsActive());
            if (request.getCategoryId() != null) {
                Category category = categoryRepository.findById(request.getCategoryId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục"));
                product.setCategory(category);
            }
            if (request.getBrandId() != null) {
                Brand brand = brandRepository.findById(request.getBrandId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy thương hiệu"));
                product.setBrand(brand);
            }

            // Tính và lưu salePrice nếu có sale và price
            if (product.getSale() != null && product.getSale() > 0 && product.getPrice() != null) {
                java.math.BigDecimal salePrice = product.getPrice()
                        .multiply(java.math.BigDecimal.valueOf(100 - product.getSale()))
                        .divide(java.math.BigDecimal.valueOf(100), java.math.RoundingMode.HALF_UP);
                product.setSalePrice(salePrice);
            } else {
                product.setSalePrice(null);
            }

            // Xử lý ảnh mới (nếu có)
            if (images != null && !images.isEmpty()) {
                List<String> imageUrls = cloudinaryService.uploadMultipleImages(images, "products");
                List<String> imageColors = request.getImageColors();
                for (int i = 0; i < imageUrls.size(); i++) {
                    Product_images img = new Product_images();
                    img.setProduct(product);
                    img.setImageUrl(imageUrls.get(i));
                    if (imageColors != null && imageColors.size() > i) {
                        img.setColor(imageColors.get(i));
                    }
                    productImageRepository.save(img);
                }
                // Nếu chưa có ảnh đại diện thì set ảnh đầu tiên
                if (product.getImageUrl() == null && !imageUrls.isEmpty()) {
                    product.setImageUrl(imageUrls.get(0));
                }
            }

            // Xử lý xóa ảnh (nếu có)
            if (request.getImageIdsToDelete() != null && !request.getImageIdsToDelete().isEmpty()) {
                for (Long imageId : request.getImageIdsToDelete()) {
                    Product_images img = productImageRepository.findById(imageId)
                            .orElseThrow(() -> new RuntimeException("Không tìm thấy ảnh"));
                    // TODO: Kiểm tra liên quan biến thể nếu cần
                    cloudinaryService.deleteImage(img.getImageUrl());
                    productImageRepository.delete(img);
                }
            }

            // Xử lý biến thể (thêm mới/cập nhật/xóa)
            if (request.getVariants() != null) {
                for (com.sportshop.api.Domain.Request.Product.UpdateProductRequest.VariantDTO v : request
                        .getVariants()) {
                    if (v.getId() == null) {
                        // Thêm mới
                        Product_variants variant = new Product_variants();
                        variant.setProduct(product);
                        variant.setSize(v.getSize());
                        variant.setColor(v.getColor());
                        variant.setStockQuantity(v.getStockQuantity());
                        variant.setPrice(v.getPrice());
                        productVariantsRepository.save(variant);
                    } else {
                        // Cập nhật
                        Product_variants variant = productVariantsRepository.findById(v.getId())
                                .orElseThrow(() -> new RuntimeException("Không tìm thấy biến thể"));
                        if (v.getSize() != null)
                            variant.setSize(v.getSize());
                        if (v.getColor() != null)
                            variant.setColor(v.getColor());
                        if (v.getStockQuantity() != null)
                            variant.setStockQuantity(v.getStockQuantity());
                        if (v.getPrice() != null)
                            variant.setPrice(v.getPrice());
                        productVariantsRepository.save(variant);
                    }
                }
                // Xử lý xóa biến thể nếu có
                if (request.getVariantIdsToDelete() != null) {
                    for (Long variantId : request.getVariantIdsToDelete()) {
                        productVariantsRepository.deleteById(variantId);
                    }
                }
            }

            productRepository.save(product);
            return convertToProductResponse(product);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi cập nhật sản phẩm: " + e.getMessage(), e);
        }
    }

    // Search sản phẩm theo tên (LIKE, ignore case, chỉ lấy sản phẩm active, limit
    // 5)
    public List<ProductResponse> searchProductsByName(String keyword) {
        return productRepository.searchActiveProductsByName(keyword).stream()
                .map(product -> {
                    ProductResponse resp = new ProductResponse();
                    resp.setId(product.getId());
                    resp.setName(product.getName());
                    resp.setImageUrl(product.getImageUrl());
                    return resp;
                })
                .toList();
    }
}
