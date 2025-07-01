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

@Service
public class CartService {
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductVariantsRepository productVariantsRepository;

    public CartService(CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            ProductVariantsRepository productVariantsRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.productVariantsRepository = productVariantsRepository;
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
        return new CartResponse(
                cart.getId(),
                cart.getUser().getId(),
                itemResponses,
                cart.getTotalQuantity() != null ? cart.getTotalQuantity() : 0,
                cart.getTotalPrice() != null ? cart.getTotalPrice() : BigDecimal.ZERO);
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
     * Cập nhật sản phẩm trong giỏ hàng, nếu đã tồn tại thì cộng dồn số lượng
     * 
     * @param userId ID người dùng
     * @param req    Thông tin sản phẩm cần cập nhật
     * @return CartItemResponse đã cập nhật
     */
    @Transactional
    public CartItemResponse updateCartItem(Long userId, AddOrUpdateCartItemRequest req) {
        // Logic giống addCartItem: cộng dồn số lượng nếu đã có
        return addCartItem(userId, req);
    }

    /**
     * Cập nhật tổng số lượng và tổng tiền của giỏ hàng
     * 
     * @param cart Cart cần cập nhật
     */
    private void updateCartTotals(Cart cart) {
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            cart.setTotalQuantity(0);
            cart.setTotalPrice(BigDecimal.ZERO);
        } else {
            // Tổng số sản phẩm khác nhau (distinct productId trong cart)
            long totalQuantity = cart.getCartItems().stream()
                    .map(Cart_item::getProduct)
                    .map(Products::getId)
                    .distinct()
                    .count();
            BigDecimal totalPrice = cart.getCartItems().stream()
                    .map(i -> i.getUnitPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
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
        return new CartItemResponse(
                item.getId(),
                item.getProduct().getId(),
                item.getProduct().getName(),
                item.getVariant() != null ? item.getVariant().getId() : null,
                item.getVariant() != null ? item.getVariant().getSize().name() : item.getSize(),
                item.getVariant() != null ? item.getVariant().getColor() : item.getColor(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
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
}