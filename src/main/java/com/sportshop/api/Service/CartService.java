package com.sportshop.api.Service;

import com.sportshop.api.Domain.*;
import com.sportshop.api.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import java.util.stream.Collectors;

import com.sportshop.api.Domain.Request.Cart.AddOrUpdateCartItemRequest;
import com.sportshop.api.Domain.Reponse.Cart.CartItemResponse;
import com.sportshop.api.Domain.Reponse.Cart.CartResponse;
import com.sportshop.api.Domain.Reponse.Discounts.DiscountResponse;
import com.sportshop.api.Repository.UserUsedDiscountCodeRepository;

@Service
public class CartService {
    private final UserUsedDiscountCodeRepository userUsedDiscountCodeRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantsRepository productVariantsRepository;
    private final ProductImageRepository productImageRepository;
    private final DiscountsService discountsService;

    public CartService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductVariantsRepository productVariantsRepository,
            ProductImageRepository productImageRepository,
            DiscountsService discountsService,
            UserUsedDiscountCodeRepository userUsedDiscountCodeRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productVariantsRepository = productVariantsRepository;
        this.productImageRepository = productImageRepository;
        this.discountsService = discountsService;
        this.userUsedDiscountCodeRepository = userUsedDiscountCodeRepository;
    }

    /**
     * Lấy giỏ hàng theo userId, nếu chưa có sẽ tạo mới
     * 
     * @param userId ID người dùng
     * @return Đối tượng Cart
     */
    public Cart getCartByUserId(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    /**
     * Lấy danh sách Cart_item trong giỏ hàng của user
     * 
     * @param userId ID người dùng
     * @return Danh sách Cart_item
     */
    public List<Cart_item> getCartItems(Long userId) {
        Cart cart = getCartByUserId(userId);
        return cartItemRepository.findByCart(cart);
    }

    /**
     * Lấy thông tin giỏ hàng trả về cho client
     * 
     * @param userId ID người dùng
     * @return CartResponse
     */
    public CartResponse getCartResponseByUserId(Long userId) {
        Cart cart = getCartByUserId(userId);
        List<Cart_item> items = cartItemRepository.findByCart(cart);
        List<CartItemResponse> itemResponses = items.stream().map(this::toCartItemResponse)
                .collect(Collectors.toList());
        BigDecimal subtotal = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;
        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                itemResponses,
                cart.getTotalQuantity() != null ? cart.getTotalQuantity() : 0,
                subtotal, // totalPrice = subtotal khi chưa áp mã
                subtotal, // subtotal = giá gốc trước khi áp mã
                null,
                null,
                null,
                false,
                null);
    }

    /**
     * Lấy danh sách CartItemResponse cho user
     * 
     * @param userId ID người dùng
     * @return Danh sách CartItemResponse
     */
    public List<CartItemResponse> getCartItemResponses(Long userId) {
        Cart cart = getCartByUserId(userId);
        return cartItemRepository.findByCart(cart).stream().map(this::toCartItemResponse).collect(Collectors.toList());
    }

    /**
     * Thêm mới sản phẩm vào giỏ hàng, nếu đã tồn tại thì cộng dồn số lượng
     * 
     * @param userId ID người dùng
     * @param req    Thông tin sản phẩm cần thêm
     * @return CartItemResponse đã thêm/cập nhật
     */
    @Transactional
    public CartItemResponse addCartItem(Long userId, AddOrUpdateCartItemRequest req) {
        Cart cart = getCartByUserId(userId);
        Cart_item item = cart.getCartItems() == null ? null
                : cart.getCartItems().stream()
                        .filter(i -> i.getProduct().getId().equals(req.getProductId()) &&
                                ((req.getVariantId() == null && i.getVariant() == null) ||
                                        (req.getVariantId() != null && i.getVariant() != null
                                                && i.getVariant().getId().equals(req.getVariantId())))
                                && (req.getSize() == null || req.getSize().equals(i.getSize()))
                                && (req.getColor() == null || req.getColor().equals(i.getColor())))
                        .findFirst().orElse(null);
        if (item != null) {
            item.setQuantity(item.getQuantity() + req.getQuantity());
        } else {
            item = new Cart_item();
            item.setCart(cart);
            Products product = productRepository.findById(req.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm"));
            item.setProduct(product);
            Product_variants variant = null;
            if (req.getVariantId() != null) {
                variant = productVariantsRepository.findById(req.getVariantId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể sản phẩm"));
                if (variant.getStockQuantity() == null || variant.getStockQuantity() <= 0) {
                    throw new RuntimeException("Biến thể sản phẩm này đã hết hàng!");
                }
                item.setVariant(variant);
                item.setSize(variant.getSize() != null ? variant.getSize().name() : req.getSize());
                item.setColor(variant.getColor() != null ? variant.getColor() : req.getColor());
            } else {
                item.setSize(req.getSize());
                item.setColor(req.getColor());
            }
            item.setQuantity(req.getQuantity());
            if (variant != null && variant.getPrice() != null) {
                item.setUnitPrice(variant.getPrice());
            } else {
                item.setUnitPrice(product.getPrice());
            }
        }
        Cart_item saved = cartItemRepository.save(item);
        updateCartTotals(cart);
        return toCartItemResponse(saved);
    }

    /**
     * Cập nhật sản phẩm trong giỏ hàng - thay thế số lượng thay vì cộng dồn
     * 
     * @param userId ID người dùng
     * @param req    Thông tin sản phẩm cần cập nhật
     * @return CartItemResponse đã cập nhật
     */
    @Transactional
    public CartItemResponse updateCartItem(Long userId, AddOrUpdateCartItemRequest req) {
        Cart cart = getCartByUserId(userId);

        // Tìm item hiện tại trong giỏ hàng
        Cart_item existingItem = cart.getCartItems() == null ? null
                : cart.getCartItems().stream()
                        .filter(i -> i.getProduct().getId().equals(req.getProductId()) &&
                                ((req.getVariantId() == null && i.getVariant() == null) ||
                                        (req.getVariantId() != null && i.getVariant() != null
                                                && i.getVariant().getId().equals(req.getVariantId())))
                                && (req.getSize() == null || req.getSize().equals(i.getSize()))
                                && (req.getColor() == null || req.getColor().equals(i.getColor())))
                        .findFirst().orElse(null);

        if (existingItem == null) {
            throw new RuntimeException("Sản phẩm không tồn tại trong giỏ hàng");
        }

        // Kiểm tra stock quantity
        if (req.getQuantity() < 1) {
            throw new RuntimeException("Số lượng không thể nhỏ hơn 1");
        }

        if (existingItem.getVariant() != null &&
                existingItem.getVariant().getStockQuantity() != null &&
                req.getQuantity() > existingItem.getVariant().getStockQuantity()) {
            throw new RuntimeException("Số lượng vượt quá tồn kho");
        }

        // Thay thế số lượng thay vì cộng dồn
        existingItem.setQuantity(req.getQuantity());

        Cart_item saved = cartItemRepository.save(existingItem);
        updateCartTotals(cart);
        return toCartItemResponse(saved);
    }

    /**
     * Cập nhật tổng số lượng và tổng tiền của giỏ hàng
     * 
     * @param cart Cart cần cập nhật
     */
    private void updateCartTotals(Cart cart) {
        List<Cart_item> items = cartItemRepository.findByCart(cart); // luôn lấy mới từ DB
        if (items == null || items.isEmpty()) {
            cart.setTotalQuantity(0);
            cart.setTotalPrice(BigDecimal.ZERO);
        } else {
            // Số sản phẩm khác nhau (distinct productId)
            long totalQuantity = items.stream()
                    .map(Cart_item::getProduct)
                    .map(Products::getId)
                    .distinct()
                    .count();

            // Tính tổng tiền dựa trên giá sale (nếu có) hoặc giá gốc
            BigDecimal totalPrice = items.stream()
                    .map(i -> {
                        Products product = i.getProduct();
                        BigDecimal finalPrice = i.getUnitPrice();

                        // Kiểm tra nếu sản phẩm có sale
                        if (product.getSale() != null && product.getSale() > 0 && product.getSalePrice() != null) {
                            if (i.getVariant() != null && i.getVariant().getPrice() != null) {
                                // Tính giá sale cho variant
                                BigDecimal discountPercentage = BigDecimal.valueOf(product.getSale())
                                        .divide(BigDecimal.valueOf(100));
                                finalPrice = i.getVariant().getPrice()
                                        .multiply(BigDecimal.ONE.subtract(discountPercentage));
                            } else {
                                // Sử dụng giá sale của sản phẩm
                                finalPrice = product.getSalePrice();
                            }
                        }

                        return finalPrice.multiply(BigDecimal.valueOf(i.getQuantity()));
                    })
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            cart.setTotalQuantity((int) totalQuantity);
            cart.setTotalPrice(totalPrice);
        }
        cartRepository.save(cart);
    }

    /**
     * Chuyển đổi Cart_item sang CartItemResponse
     * 
     * @param item Cart_item
     * @return CartItemResponse
     */
    private CartItemResponse toCartItemResponse(Cart_item item) {
        Products product = item.getProduct();

        // Xác định giá hiển thị và giá sale
        BigDecimal displayPrice = item.getUnitPrice();
        BigDecimal salePrice = null;
        Boolean isOnSale = false;

        // Kiểm tra nếu sản phẩm có sale
        if (product.getSale() != null && product.getSale() > 0 && product.getSalePrice() != null) {
            isOnSale = true;
            salePrice = product.getSalePrice();
            // Nếu có variant, tính giá sale cho variant
            if (item.getVariant() != null && item.getVariant().getPrice() != null) {
                // Tính giá sale dựa trên phần trăm giảm giá của sản phẩm
                BigDecimal discountPercentage = BigDecimal.valueOf(product.getSale()).divide(BigDecimal.valueOf(100));
                salePrice = item.getVariant().getPrice().multiply(BigDecimal.ONE.subtract(discountPercentage));
            }
        }

        // Tính totalPrice dựa trên giá sale (nếu có) hoặc giá gốc
        BigDecimal finalPrice = salePrice != null ? salePrice : displayPrice;
        BigDecimal totalPrice = finalPrice.multiply(BigDecimal.valueOf(item.getQuantity()));

        // Lấy ảnh của biến thể màu
        String imageUrl = null;
        Integer stockQuantity = null;

        if (item.getVariant() != null) {
            stockQuantity = item.getVariant().getStockQuantity();
            // Lấy ảnh theo màu của variant
            List<Product_images> images = productImageRepository.findByProductIdAndColor(product.getId(),
                    item.getVariant().getColor());
            if (!images.isEmpty()) {
                imageUrl = images.get(0).getImageUrl(); // Lấy ảnh đầu tiên
            }
        } else {
            // Nếu không có variant, lấy ảnh đầu tiên của sản phẩm
            List<Product_images> images = productImageRepository.findByProductId(product.getId());
            if (!images.isEmpty()) {
                imageUrl = images.get(0).getImageUrl();
            }
        }

        return new CartItemResponse(
                item.getId(),
                product.getId(),
                product.getName(),
                item.getVariant() != null ? item.getVariant().getId() : null,
                item.getVariant() != null ? item.getVariant().getSize().name() : item.getSize(),
                item.getVariant() != null ? item.getVariant().getColor() : item.getColor(),
                item.getQuantity(),
                displayPrice,
                salePrice,
                totalPrice,
                imageUrl,
                stockQuantity,
                isOnSale);
    }

    /**
     * Xóa một sản phẩm khỏi giỏ hàng
     * 
     * @param userId    ID người dùng
     * @param productId ID sản phẩm
     * @param variantId ID biến thể (nếu có)
     */
    @Transactional
    public void removeCartItem(Long userId, Long productId, Long variantId) {
        Cart cart = getCartByUserId(userId);
        Products product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        if (variantId == null) {
            cartItemRepository.deleteByCartAndProduct(cart, product);
        } else {
            Product_variants variant = productVariantsRepository.findById(variantId)
                    .orElseThrow(() -> new RuntimeException("Variant not found"));
            cartItemRepository.deleteByCartAndProductAndVariant(cart, product, variant);
        }
        // Cập nhật lại tổng số lượng và tổng tiền sau khi xóa
        updateCartTotals(cart);
    }

    /**
     * Xóa toàn bộ sản phẩm trong giỏ hàng
     * 
     * @param userId ID người dùng
     */
    @Transactional
    public void clearCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        cartItemRepository.deleteByCart(cart);
        // Cập nhật lại tổng số lượng và tổng tiền sau khi xóa
        updateCartTotals(cart);
    }

    /**
     * Xóa giỏ hàng của người dùng
     * 
     * @param userId ID người dùng
     */
    @Transactional
    public void deleteCart(Long userId) {
        Users user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        cartRepository.deleteByUser(user);
    }

    // Lấy danh sách mã giảm giá có thể áp dụng cho giỏ hàng của user
    public List<DiscountResponse> getAvailableDiscountsForCart(Long userId) {
        Cart cart = getCartByUserId(userId);
        BigDecimal orderAmount = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;
        List<DiscountResponse> allDiscounts = discountsService.getAllDiscounts();
        Users user = userRepository.findById(userId).orElse(null);

        return allDiscounts.stream()
                .map(d -> {
                    DiscountResponse resp = discountsService.validateDiscountCode(d.getCode(), orderAmount);
                    // Kiểm tra số lần user đã dùng mã này
                    if (user != null && d.getPerUserLimit() != null) {
                        Discounts discountEntity = discountsService.getDiscountEntityByCode(d.getCode());
                        int used = userUsedDiscountCodeRepository.findByUserAndDiscount(user, discountEntity)
                                .map(UserUsedDiscountCode::getUsedCount).orElse(0);
                        if (used >= d.getPerUserLimit()) {
                            resp.setIsValid(false);
                            resp.setStatusMessage("Bạn đã hết lượt sử dụng mã này");
                        }
                    }
                    return resp;
                })
                .filter(DiscountResponse::getIsValid)
                .collect(Collectors.toList());
    }

    // Áp dụng mã giảm giá cho giỏ hàng
    public CartResponse applyDiscountToCart(Long userId, List<String> discountCodes) {
        Cart cart = getCartByUserId(userId);
        BigDecimal orderAmount = cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO;
        BigDecimal totalDiscountAmount = BigDecimal.ZERO;
        List<String> appliedCodes = new java.util.ArrayList<>();
        Boolean hasFreeShipping = false;
        BigDecimal freeShippingAmount = BigDecimal.ZERO;
        for (String code : discountCodes) {
            DiscountResponse discount = discountsService.validateDiscountCode(code,
                    orderAmount.subtract(totalDiscountAmount));
            if (!discount.getIsValid()) {
                continue; // bỏ qua mã không hợp lệ
            }
            BigDecimal discountAmount = BigDecimal.ZERO;
            if (discount.getDiscountType() == com.sportshop.api.Domain.Discounts.DiscountType.PERCENTAGE) {
                discountAmount = (orderAmount.subtract(totalDiscountAmount)).multiply(discount.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));
            } else if (discount.getDiscountType() == com.sportshop.api.Domain.Discounts.DiscountType.FIXED_AMOUNT) {
                discountAmount = discount.getDiscountValue();
            } else if (discount.getDiscountType() == com.sportshop.api.Domain.Discounts.DiscountType.FREE_SHIPPING) {
                hasFreeShipping = true;
                if (discount.getDiscountValue() != null
                        && discount.getDiscountValue().compareTo(freeShippingAmount) > 0) {
                    freeShippingAmount = discount.getDiscountValue();
                }
            }
            if (discountAmount.compareTo(orderAmount.subtract(totalDiscountAmount)) > 0) {
                discountAmount = orderAmount.subtract(totalDiscountAmount);
            }
            totalDiscountAmount = totalDiscountAmount.add(discountAmount);
            appliedCodes.add(code);
        }
        BigDecimal newTotal = orderAmount.subtract(totalDiscountAmount);
        CartResponse response = getCartResponseByUserId(userId);
        response.setSubtotal(orderAmount); // Giữ nguyên giá gốc
        response.setTotalPrice(newTotal); // Cập nhật giá sau khi áp mã
        response.setDiscountCodes(appliedCodes);
        response.setDiscountAmount(totalDiscountAmount);
        response.setFreeShipping(hasFreeShipping);
        response.setFreeShippingAmount(hasFreeShipping ? freeShippingAmount : null);
        return response;
    }
}