package com.sportshop.api.Service;

import com.sportshop.api.Domain.Request.Order.PlaceOrderRequest;
import com.sportshop.api.Domain.Reponse.Order.DashboardStatisticsResponse;
import com.sportshop.api.Domain.Reponse.Order.MonthlyRevenueResponse;
import com.sportshop.api.Domain.Reponse.Order.OrderResponse;
import com.sportshop.api.Domain.Reponse.Order.OrderStatisticsResponse;
import com.sportshop.api.Domain.Reponse.Order.TopProductResponse;
import com.sportshop.api.Domain.*;
import com.sportshop.api.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.persistence.criteria.Predicate;

import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.Random;
import com.sportshop.api.Domain.Payments;
import com.sportshop.api.Repository.PaymentsRepository;
import java.time.format.DateTimeFormatter;
import com.sportshop.api.Domain.UserUsedDiscountCode;
import com.sportshop.api.Repository.UserUsedDiscountCodeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Map;

/**
 * Service xử lý logic đặt hàng
 */
@Service
public class OrderService {
    private final OrdersRepository ordersRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final CartService cartService;
    private final ProductService productService;
    private final DiscountsService discountsService;
    private final UserRepository userRepository;
    private final ShippingAddressRepository shippingAddressRepository;
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final PaymentsRepository paymentsRepository;
    private final ProductImageRepository productImageRepository;
    private final UserUsedDiscountCodeRepository userUsedDiscountCodeRepository;
    private final VNPayService vnPayService;

    public OrderService(OrdersRepository ordersRepository,
            OrderItemsRepository orderItemsRepository,
            CartService cartService,
            ProductService productService,
            DiscountsService discountsService,
            UserRepository userRepository,
            ShippingAddressRepository shippingAddressRepository,
            JavaMailSender mailSender,
            TemplateEngine templateEngine,
            PaymentsRepository paymentsRepository,
            ProductImageRepository productImageRepository,
            UserUsedDiscountCodeRepository userUsedDiscountCodeRepository,
            VNPayService vnPayService) {
        this.ordersRepository = ordersRepository;
        this.orderItemsRepository = orderItemsRepository;
        this.cartService = cartService;
        this.productService = productService;
        this.discountsService = discountsService;
        this.userRepository = userRepository;
        this.shippingAddressRepository = shippingAddressRepository;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.paymentsRepository = paymentsRepository;
        this.productImageRepository = productImageRepository;
        this.userUsedDiscountCodeRepository = userUsedDiscountCodeRepository;
        this.vnPayService = vnPayService;
    }

    // Lấy entity Discounts từ code
    private Discounts getDiscountEntityByCode(String code) {
        return discountsService.getDiscountEntityByCode(code);
    }

    /**
     * Xử lý logic đặt hàng
     * 
     * @param request Thông tin đặt hàng
     * @return Thông tin đơn hàng vừa tạo
     */
    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        // 1. Kiểm tra user
        Users user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng!"));
        // 2. Kiểm tra địa chỉ giao hàng
        Shipping_addresses shippingAddress = shippingAddressRepository
                .findByUserIdAndIsDefaultTrue(user.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ mặc định của người dùng!"));
        String shippingAddressFull = shippingAddress.getAddressLine() + ", " + shippingAddress.getWard() + ", "
                + shippingAddress.getDistrict() + ", " + shippingAddress.getProvince();
        // 3. Lấy giỏ hàng
        var cart = cartService.getCartByUserId(user.getId());
        var cartItems = cartService.getCartItems(user.getId());
        if (cartItems == null || cartItems.isEmpty())
            throw new RuntimeException("Giỏ hàng trống!");
        // 4. Kiểm tra tồn kho từng sản phẩm
        for (var item : cartItems) {
            if (item.getVariant() != null) {
                var variant = item.getVariant();
                if (variant.getStockQuantity() == null || variant.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + item.getProduct().getName() + " (" + variant.getSize()
                            + ", " + variant.getColor() + ") không đủ tồn kho!");
                }
            } else {
                var product = item.getProduct();
                if (product.getStockQuantity() == null || product.getStockQuantity() < item.getQuantity()) {
                    throw new RuntimeException("Sản phẩm " + product.getName() + " không đủ tồn kho!");
                }
            }
        }
        // 5. Áp dụng mã giảm giá (chỉ 1 mã giảm tiền + 1 mã freeship)
        Long discountAmount = 0L;
        Long shippingFee = 0L;
        Long shippingDiscount = 0L;
        Discounts discountMoney = null;
        Discounts discountFreeship = null;
        if (request.getDiscountCodes() != null && !request.getDiscountCodes().isEmpty()) {
            for (String code : request.getDiscountCodes()) {
                Discounts d = getDiscountEntityByCode(code);
                // Kiểm tra số lần user đã dùng mã này
                UserUsedDiscountCode usage = userUsedDiscountCodeRepository.findByUserAndDiscount(user, d).orElse(null);
                int used = usage != null ? usage.getUsedCount() : 0;
                int perUserLimit = d.getPerUserLimit() != null ? d.getPerUserLimit() : 1;
                if (used >= perUserLimit) {
                    throw new RuntimeException("Bạn không còn quyền sử dụng mã giảm giá " + code + " này nữa!");
                }
                if (d.getDiscountType() == Discounts.DiscountType.FREE_SHIPPING && discountFreeship == null) {
                    discountFreeship = d;
                } else if ((d.getDiscountType() == Discounts.DiscountType.FIXED_AMOUNT
                        || d.getDiscountType() == Discounts.DiscountType.PERCENTAGE) && discountMoney == null) {
                    discountMoney = d;
                }
            }
        }
        // 6. Tính phí ship cứng
        if (request.getShippingMethod().equals("STANDARD")) {
            shippingFee = 30000L;
        } else if (request.getShippingMethod().equals("EXPRESS")) {
            shippingFee = 50000L;
        }
        // 7. Áp dụng giảm giá tiền
        var cartTotal = cart.getTotalPrice();
        if (discountMoney != null) {
            if (discountMoney.getDiscountType() == Discounts.DiscountType.PERCENTAGE) {
                discountAmount = cartTotal.multiply(discountMoney.getDiscountValue())
                        .divide(java.math.BigDecimal.valueOf(100)).longValue();
            } else {
                discountAmount = discountMoney.getDiscountValue().longValue();
            }
            if (discountAmount > cartTotal.longValue())
                discountAmount = cartTotal.longValue();
        }
        // 8. Áp dụng giảm giá freeship
        if (discountFreeship != null) {
            shippingDiscount = shippingFee;
        }
        // 9. Tính tổng tiền
        Long totalAmount = cart.getTotalPrice().longValue() - discountAmount + shippingFee - shippingDiscount;
        if (totalAmount < 0)
            totalAmount = 0L;
        // 10. Sinh mã đơn hàng (orderCode)
        String orderCode = generateOrderCode();
        // 11. Tạo đơn hàng
        Orders order = new Orders();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(shippingAddress.getAddressLine());
        order.setStatus(Orders.OrderStatus.CONFIRMED);
        order.setPaymentStatus(Orders.PaymentStatus.UNPAID);
        order.setPaymentMethod(Orders.PaymentMethod.valueOf(request.getPaymentMethod()));
        order.setTotalPrice(cart.getTotalPrice().longValue());
        order.setTotal_amount(totalAmount);
        order.setDiscount(discountMoney);
        order.setDiscountAmount(discountAmount);
        order.setShippingMethod(Orders.ShippingMethod.valueOf(request.getShippingMethod()));
        order.setShippingFee(shippingFee);
        order.setOrderCode(orderCode);
        ordersRepository.save(order);
        // 12. Tạo order_items
        List<Order_items> orderItems = cartItems.stream().map(item -> {
            if (item.getProduct() == null) {
                throw new RuntimeException("Cart item thiếu thông tin sản phẩm (product_id)!");
            }
            // Nếu sản phẩm có variant, phải có variant
            if (item.getProduct().getProduct_variants() != null && !item.getProduct().getProduct_variants().isEmpty()) {
                if (item.getVariant() == null) {
                    throw new RuntimeException("Cart item thiếu thông tin biến thể (variant_id) cho sản phẩm: "
                            + item.getProduct().getName());
                }
            }
            Order_items oi = new Order_items();
            oi.setOrder(order);
            oi.setProduct(item.getProduct());
            oi.setProductName(item.getProduct().getName());
            oi.setVariant(item.getVariant());
            oi.setSize(item.getSize());
            oi.setColor(item.getColor());
            oi.setQuantity(item.getQuantity());
            oi.setUnit_price(item.getUnitPrice().longValue());
            oi.setSubtotal(item.getUnitPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())).longValue());
            return oi;
        }).collect(Collectors.toList());
        orderItemsRepository.saveAll(orderItems);
        // 13. Tạo payment cho đơn hàng
        Payments payment = new Payments();
        payment.setTransactionId(generateTransactionId());
        payment.setOrder(order);
        payment.setPaymentAmount(totalAmount);
        payment.setPaymentStatus(Payments.PaymentStatus.PENDING);
        // Map payment method
        Payments.PaymentMethod paymentMethod;
        if (order.getPaymentMethod() == Orders.PaymentMethod.CASH_ON_DELIVERY) {
            paymentMethod = Payments.PaymentMethod.CASH_ON_DELIVERY;
        } else if (order.getPaymentMethod() == Orders.PaymentMethod.VNPAY) {
            paymentMethod = Payments.PaymentMethod.VNPAY;
        } else {
            paymentMethod = Payments.PaymentMethod.BANK_TRANSFER;
        }
        payment.setPaymentMethod(paymentMethod);
        paymentsRepository.save(payment);
        // 14. Trừ tồn kho
        for (var item : cartItems) {
            if (item.getVariant() != null) {
                var variant = item.getVariant();
                variant.setStockQuantity(variant.getStockQuantity() - item.getQuantity());
            } else {
                var product = item.getProduct();
                product.setStockQuantity(product.getStockQuantity() - item.getQuantity());
            }
        }
        // 15. Cập nhật bảng user_used_discount_code
        if (discountMoney != null) {
            UserUsedDiscountCode usage = userUsedDiscountCodeRepository.findByUserAndDiscount(user, discountMoney)
                    .orElse(null);
            if (usage == null) {
                usage = new UserUsedDiscountCode();
                usage.setUser(user);
                usage.setDiscount(discountMoney);
                usage.setUsedCount(1);
            } else {
                usage.setUsedCount(usage.getUsedCount() + 1);
            }
            userUsedDiscountCodeRepository.save(usage);
        }
        if (discountFreeship != null) {
            UserUsedDiscountCode usage = userUsedDiscountCodeRepository.findByUserAndDiscount(user, discountFreeship)
                    .orElse(null);
            if (usage == null) {
                usage = new UserUsedDiscountCode();
                usage.setUser(user);
                usage.setDiscount(discountFreeship);
                usage.setUsedCount(1);
            } else {
                usage.setUsedCount(usage.getUsedCount() + 1);
            }
            userUsedDiscountCodeRepository.save(usage);
        }
        // 16. Gửi email xác nhận đơn hàng
        List<OrderResponse.OrderItemInfo> itemInfos = orderItems.stream().map(oi -> {
            String imageUrl = null;
            // Ưu tiên lấy ảnh theo màu
            List<Product_images> images = productImageRepository.findByProductIdAndColor(oi.getProduct().getId(),
                    oi.getColor());
            if (images != null && !images.isEmpty()) {
                imageUrl = images.get(0).getImageUrl();
            } else {
                // fallback: lấy ảnh chung
                images = productImageRepository.findByProductIdAndColor(oi.getProduct().getId(), null);
                if (images != null && !images.isEmpty()) {
                    imageUrl = images.get(0).getImageUrl();
                } else {
                    imageUrl = oi.getProduct().getImageUrl();
                }
            }
            return new OrderResponse.OrderItemInfo(
                    oi.getProduct().getId(),
                    oi.getVariant() != null ? oi.getVariant().getId() : null,
                    oi.getProductName(),
                    oi.getSize(),
                    oi.getColor(),
                    oi.getQuantity(),
                    oi.getUnit_price(),
                    oi.getSubtotal(),
                    imageUrl);
        }).collect(Collectors.toList());
        sendOrderConfirmationEmail(user, order, orderItems, shippingAddressFull, shippingFee, shippingDiscount,
                itemInfos);
        // 17. Xóa giỏ hàng
        cartService.clearCart(user.getId());
        // 18. Trả về response
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getOrderDate(),
                order.getShippingAddress(),
                order.getPaymentMethod().name(),
                order.getPaymentStatus().name(),
                order.getTotal_amount(),
                order.getDiscountAmount(),
                itemInfos,
                order.getOrderCode(),
                order.getUser().getId(),
                order.getUser().getFullName());
    }

    // Sinh mã đơn hàng dạng VNSPXxxxxxxx
    private String generateOrderCode() {
        String prefix = "VNSPX";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return prefix + sb.toString();
    }

    // Sinh transactionId cho payment
    private String generateTransactionId() {
        String prefix = "TXN";
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return prefix + sb.toString();
    }

    /**
     * Gửi email xác nhận đơn hàng cho user
     */
    private void sendOrderConfirmationEmail(Users user, Orders order, List<Order_items> orderItems,
            String shippingAddressFull, Long shippingFee, Long shippingDiscount,
            List<OrderResponse.OrderItemInfo> itemInfos) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject("Xác nhận đơn hàng - SportX");
            helper.setFrom("noreply@sportx.com");
            Context context = new Context();
            context.setVariable("fullName", user.getFullName());
            context.setVariable("orderId", order.getOrderCode());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            String orderDateStr = order.getOrderDate().format(formatter);
            context.setVariable("orderDate", orderDateStr);
            context.setVariable("shippingAddressFull", shippingAddressFull);
            context.setVariable("totalAmount", order.getTotal_amount());
            context.setVariable("discountAmount", order.getDiscountAmount());
            context.setVariable("shippingFee", shippingFee);
            context.setVariable("shippingDiscount", shippingDiscount);
            context.setVariable("items", itemInfos);
            context.setVariable("paymentMethod", order.getPaymentMethod().name());
            context.setVariable("paymentStatus", order.getPaymentStatus().name());
            String htmlContent = templateEngine.process("order-confirmation", context);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Lỗi khi render/gửi email: " + e.getMessage());
        }
    }

    // Lấy tất cả đơn hàng (admin, có phân trang, lọc)
    public Page<OrderResponse> getAllOrders(int page, int size, String status, Long userId, String orderCode,
            String dateFrom, String dateTo) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));

        var spec = (org.springframework.data.jpa.domain.Specification<Orders>) (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            if (status != null && !status.isEmpty()) {
                predicates.add(cb.equal(root.get("status"), Orders.OrderStatus.valueOf(status)));
            }
            if (userId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), userId));
            }
            if (orderCode != null && !orderCode.isEmpty()) {
                predicates.add(cb.like(root.get("orderCode"), "%" + orderCode + "%"));
            }
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (dateFrom != null && !dateFrom.isEmpty()) {
                LocalDate from = LocalDate.parse(dateFrom, fmt);
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), from.atStartOfDay()));
            }
            if (dateTo != null && !dateTo.isEmpty()) {
                LocalDate to = LocalDate.parse(dateTo, fmt);
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), to.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        Page<Orders> orderPage = ordersRepository.findAll(spec, pageable);
        // Map sang OrderResponse, trả về cả order_items
        return orderPage.map(order -> {
            List<Order_items> items = orderItemsRepository.findByOrder(order);
            List<OrderResponse.OrderItemInfo> itemInfos = items.stream().map(oi -> {
                String imageUrl = null;
                Long productId = oi.getProduct() != null ? oi.getProduct().getId() : null;
                Long variantId = oi.getVariant() != null ? oi.getVariant().getId() : null;
                String productName = oi.getProductName();
                String itemSize = oi.getSize(); // Đổi tên biến ở đây
                String color = oi.getColor();
                Integer quantity = oi.getQuantity();
                Long unitPrice = oi.getUnit_price();
                Long subtotal = oi.getSubtotal();
                if (oi.getProduct() != null) {
                    List<Product_images> images = productImageRepository
                            .findByProductIdAndColor(oi.getProduct().getId(), oi.getColor());
                    if (images != null && !images.isEmpty()) {
                        imageUrl = images.get(0).getImageUrl();
                    } else {
                        images = productImageRepository.findByProductIdAndColor(oi.getProduct().getId(), null);
                        if (images != null && !images.isEmpty()) {
                            imageUrl = images.get(0).getImageUrl();
                        } else {
                            imageUrl = oi.getProduct().getImageUrl();
                        }
                    }
                }
                return new OrderResponse.OrderItemInfo(
                        productId,
                        variantId,
                        productName,
                        itemSize,
                        color,
                        quantity,
                        unitPrice,
                        subtotal,
                        imageUrl);
            }).collect(java.util.stream.Collectors.toList());
            return new OrderResponse(
                    order.getId(),
                    order.getStatus().name(),
                    order.getOrderDate(),
                    order.getShippingAddress(),
                    order.getPaymentMethod().name(),
                    order.getPaymentStatus().name(),
                    order.getTotal_amount(),
                    order.getDiscountAmount(),
                    itemInfos,
                    order.getOrderCode(),
                    order.getUser().getId(),
                    order.getUser().getFullName());
        });
    }

    // Lấy đơn hàng theo ID
    public OrderResponse getOrderById(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        // Lấy danh sách order_items
        List<Order_items> items = orderItemsRepository.findByOrder(order);
        List<OrderResponse.OrderItemInfo> itemInfos = items.stream().map(oi -> {
            String imageUrl = null;
            Long productId = oi.getProduct() != null ? oi.getProduct().getId() : null;
            Long variantId = oi.getVariant() != null ? oi.getVariant().getId() : null;
            // Lấy ảnh nếu có product, nếu không thì để null
            if (oi.getProduct() != null) {
                List<Product_images> images = productImageRepository.findByProductIdAndColor(productId, oi.getColor());
                if (images != null && !images.isEmpty()) {
                    imageUrl = images.get(0).getImageUrl();
                } else {
                    images = productImageRepository.findByProductIdAndColor(productId, null);
                    if (images != null && !images.isEmpty()) {
                        imageUrl = images.get(0).getImageUrl();
                    } else {
                        imageUrl = oi.getProduct().getImageUrl();
                    }
                }
            }
            return new OrderResponse.OrderItemInfo(
                    productId,
                    variantId,
                    oi.getProductName(),
                    oi.getSize(),
                    oi.getColor(),
                    oi.getQuantity(),
                    oi.getUnit_price(),
                    oi.getSubtotal(),
                    imageUrl);
        }).collect(java.util.stream.Collectors.toList());
        return new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getOrderDate(),
                order.getShippingAddress(),
                order.getPaymentMethod().name(),
                order.getPaymentStatus().name(),
                order.getTotal_amount(),
                order.getDiscountAmount(),
                itemInfos,
                order.getOrderCode(),
                order.getUser().getId(),
                order.getUser().getFullName());
    }

    // Lấy đơn hàng theo userId (phân trang)
    public Page<OrderResponse> getOrdersByUserId(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "orderDate"));
        Page<Orders> orderPage = ordersRepository
                .findAll((root, query, cb) -> cb.equal(root.get("user").get("id"), userId), pageable);
        return orderPage.map(order -> new OrderResponse(
                order.getId(),
                order.getStatus().name(),
                order.getOrderDate(),
                order.getShippingAddress(),
                order.getPaymentMethod().name(),
                order.getPaymentStatus().name(),
                order.getTotal_amount(),
                order.getDiscountAmount(),
                null,
                order.getOrderCode(),
                order.getUser().getId(),
                order.getUser().getFullName()));
    }

    // Tìm kiếm đơn hàng theo mã
    public OrderResponse searchOrderByCode(String orderCode) {
        Orders order = ordersRepository.findAll((root, query, cb) -> cb.equal(root.get("orderCode"), orderCode))
                .stream().findFirst()
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + orderCode));
        return getOrderById(order.getId());
    }

    // Thống kê đơn hàng
    public OrderStatisticsResponse getOrderStatistics(String from, String to) {
        // Lọc theo ngày nếu có
        var spec = (Specification<Orders>) (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            if (from != null && !from.isEmpty()) {
                LocalDate fromDate = LocalDate.parse(from, fmt);
                predicates.add(cb.greaterThanOrEqualTo(root.get("orderDate"), fromDate.atStartOfDay()));
            }
            if (to != null && !to.isEmpty()) {
                LocalDate toDate = LocalDate.parse(to, fmt);
                predicates.add(cb.lessThanOrEqualTo(root.get("orderDate"), toDate.atTime(23, 59, 59)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        List<Orders> orders = ordersRepository.findAll(spec);
        long totalOrders = orders.size();
        long totalRevenue = orders.stream().mapToLong(o -> o.getTotal_amount() != null ? o.getTotal_amount() : 0L)
                .sum();
        java.util.Map<String, Long> orderCountByStatus = new java.util.HashMap<>();
        java.util.Map<String, Long> revenueByStatus = new java.util.HashMap<>();
        for (Orders o : orders) {
            String status = o.getStatus().name();
            orderCountByStatus.put(status, orderCountByStatus.getOrDefault(status, 0L) + 1);
            revenueByStatus.put(status, revenueByStatus.getOrDefault(status, 0L)
                    + (o.getTotal_amount() != null ? o.getTotal_amount() : 0L));
        }
        return new OrderStatisticsResponse(
                totalOrders, totalRevenue, orderCountByStatus, revenueByStatus);
    }

    // Thống kê doanh thu theo tháng/năm
    public List<MonthlyRevenueResponse> getMonthlyRevenue(int year) {
        List<Orders> orders = ordersRepository.findAll((root, query, cb) -> {
            return cb.and(
                    cb.equal(cb.function("YEAR", Integer.class, root.get("orderDate")), year),
                    // Chỉ loại bỏ đơn hàng đã hủy, tính doanh thu từ các đơn đã xác nhận trở lên
                    cb.notEqual(root.get("status"), Orders.OrderStatus.CANCELLED));
        });

        Map<Integer, List<Orders>> ordersByMonth = orders.stream()
                .collect(Collectors.groupingBy(
                        order -> order.getOrderDate().getMonthValue()));

        List<MonthlyRevenueResponse> result = new ArrayList<>();
        String[] monthNames = { "", "Tháng 1", "Tháng 2", "Tháng 3", "Tháng 4", "Tháng 5", "Tháng 6",
                "Tháng 7", "Tháng 8", "Tháng 9", "Tháng 10", "Tháng 11", "Tháng 12" };

        for (int month = 1; month <= 12; month++) {
            List<Orders> monthOrders = ordersByMonth.getOrDefault(month, new ArrayList<>());
            Long totalRevenue = monthOrders.stream()
                    .mapToLong(o -> o.getTotal_amount() != null ? o.getTotal_amount() : 0L)
                    .sum();
            Long totalOrders = (long) monthOrders.size();

            result.add(new MonthlyRevenueResponse(
                    month, year, totalRevenue, totalOrders, monthNames[month]));
        }

        return result;
    }

    // Lấy top sản phẩm bán chạy
    public List<TopProductResponse> getTopProducts(int limit) {
        List<Order_items> orderItems = orderItemsRepository.findAll();

        // Lọc các order items từ đơn hàng không bị hủy
        List<Order_items> filteredItems = orderItems.stream()
                .filter(item -> item.getOrder().getStatus() != Orders.OrderStatus.CANCELLED)
                .collect(Collectors.toList());

        // Group theo productId nếu có, nếu không thì group theo productName
        Map<String, List<Order_items>> itemsByProduct = filteredItems.stream()
                .collect(Collectors.groupingBy(item -> {
                    if (item.getProduct() != null) {
                        return "PRODUCT_" + item.getProduct().getId();
                    } else {
                        return "CUSTOM_" + item.getProductName();
                    }
                }));

        return itemsByProduct.entrySet().stream()
                .map(entry -> {
                    List<Order_items> items = entry.getValue();
                    Order_items firstItem = items.get(0);

                    Long totalSold = items.stream().mapToLong(Order_items::getQuantity).sum();
                    Long totalRevenue = items.stream().mapToLong(item -> item.getUnit_price() * item.getQuantity())
                            .sum();

                    Products product = firstItem.getProduct();
                    String productName;
                    String productImage = "";
                    Long productId = null;

                    if (product != null) {
                        productId = product.getId();
                        productName = product.getName();
                        productImage = productImageRepository.findByProductId(product.getId())
                                .stream().findFirst().map(Product_images::getImageUrl).orElse("");
                    } else {
                        productName = firstItem.getProductName();
                    }

                    return new TopProductResponse(
                            productId, productName, totalSold, totalRevenue, productImage);
                })
                .sorted((a, b) -> b.getTotalSold().compareTo(a.getTotalSold()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    // Thống kê tổng quan cho dashboard
    public DashboardStatisticsResponse getDashboardStatistics() {
        // Tổng doanh thu và đơn hàng
        List<Orders> allOrders = ordersRepository.findAll((root, query, cb) -> {
            return cb.and(
                    cb.notEqual(root.get("status"), Orders.OrderStatus.CANCELLED));
        });

        Long totalRevenue = allOrders.stream()
                .mapToLong(o -> o.getTotal_amount() != null ? o.getTotal_amount() : 0L)
                .sum();
        Long totalOrders = (long) allOrders.size();

        // Tổng số khách hàng
        Long totalCustomers = userRepository.count();

        // Tổng số sản phẩm
        Long totalProducts = (long) productService.getAllProductResponses().size();

        // Doanh thu theo tháng (năm hiện tại)
        int currentYear = java.time.LocalDateTime.now().getYear();
        List<MonthlyRevenueResponse> monthlyRevenue = getMonthlyRevenue(
                currentYear);

        // Top sản phẩm bán chạy
        List<TopProductResponse> topProducts = getTopProducts(5);

        // Thống kê tháng hiện tại
        int currentMonth = java.time.LocalDateTime.now().getMonthValue();
        Long revenueThisMonth = monthlyRevenue.stream()
                .filter(m -> m.getMonth() == currentMonth)
                .findFirst()
                .map(m -> m.getTotalRevenue())
                .orElse(0L);

        Long ordersThisMonth = monthlyRevenue.stream()
                .filter(m -> m.getMonth() == currentMonth)
                .findFirst()
                .map(m -> m.getTotalOrders())
                .orElse(0L);

        // Khách hàng mới tháng này
        Long newCustomersThisMonth = userRepository.count();

        return new DashboardStatisticsResponse(
                totalRevenue, totalOrders, totalCustomers, totalProducts,
                monthlyRevenue, topProducts, revenueThisMonth, ordersThisMonth, newCustomersThisMonth);
    }

    // Cập nhật đơn hàng (trạng thái, thông tin nhận hàng)
    public OrderResponse updateOrder(Long orderId,
            com.sportshop.api.Domain.Request.Order.OrderUpdateRequest updateRequest) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        // Chỉ cho phép cập nhật trạng thái và thông tin nhận hàng
        if (updateRequest.getStatus() != null) {
            order.setStatus(updateRequest.getStatus());
        }
        // Nếu có cập nhật địa chỉ
        if (updateRequest.getAddressLine() != null) {
            StringBuilder sb = new StringBuilder();
            sb.append(updateRequest.getAddressLine());
            if (updateRequest.getWard() != null)
                sb.append(", ").append(updateRequest.getWard());
            if (updateRequest.getDistrict() != null)
                sb.append(", ").append(updateRequest.getDistrict());
            if (updateRequest.getProvince() != null)
                sb.append(", ").append(updateRequest.getProvince());
            order.setShippingAddress(sb.toString());
        }
        ordersRepository.save(order);
        return getOrderById(orderId);
    }

    // Xoá đơn hàng (soft delete: cập nhật status = CANCELLED,
    public boolean deleteOrder(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        order.setStatus(Orders.OrderStatus.CANCELLED);
        ordersRepository.save(order);
        return true;
    }

    // Hủy đơn hàng (user): chỉ cho phép hủy trong 1 tiếng đầu
    public boolean cancelOrderByUser(Long orderId, Long userId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này!");
        }
        java.time.Duration duration = java.time.Duration.between(order.getOrderDate(), java.time.LocalDateTime.now());
        if (duration.toHours() >= 1) {
            throw new RuntimeException("Chỉ được phép hủy đơn hàng trong vòng 1 tiếng sau khi đặt!");
        }
        if (order.getStatus() == Orders.OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy trước đó!");
        }
        order.setStatus(Orders.OrderStatus.CANCELLED);
        ordersRepository.save(order);
        return true;
    }

    // Hủy đơn hàng (admin): không giới hạn thời gian
    public boolean cancelOrderByAdmin(Long orderId, Long adminId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));
        // Kiểm tra quyền admin
        if (!order.getUser().getId().equals(adminId)) {
            throw new RuntimeException("Bạn không có quyền hủy đơn hàng này!");
        }
        if (order.getStatus() == Orders.OrderStatus.CANCELLED) {
            throw new RuntimeException("Đơn hàng đã bị hủy trước đó!");
        }
        order.setStatus(Orders.OrderStatus.CANCELLED);
        ordersRepository.save(order);
        return true;
    }

    /**
     * Tạo URL thanh toán VNPay cho đơn hàng
     * 
     * @param orderId ID đơn hàng
     * @return URL thanh toán VNPay
     */
    public String createVNPayPaymentUrl(Long orderId) {
        Orders order = ordersRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng!"));

        if (order.getPaymentMethod() != Orders.PaymentMethod.VNPAY) {
            throw new RuntimeException("Đơn hàng này không sử dụng phương thức thanh toán VNPay!");
        }

        String orderInfo = "Thanh toan don hang " + order.getOrderCode();
        return vnPayService.createPaymentUrl(order.getOrderCode(), order.getTotal_amount(), orderInfo, null, "vn");
    }

    /**
     * Xử lý callback từ VNPay và cập nhật trạng thái đơn hàng
     * 
     * @param params Các tham số từ VNPay callback
     * @return true nếu xử lý thành công
     */
    @Transactional
    public boolean processVNPayCallback(Map<String, String> params) {
        // Xác thực callback
        boolean signatureValid = vnPayService.verifyPaymentResponse(params);
        if (!signatureValid) {
            throw new RuntimeException("Invalid VNPay signature");
        }

        String vnp_ResponseCode = params.get("vnp_ResponseCode");
        String vnp_TxnRef = params.get("vnp_TxnRef");
        String vnp_Amount = params.get("vnp_Amount");
        String vnp_TransactionNo = params.get("vnp_TransactionNo");

        // Tìm đơn hàng theo orderCode
        Orders order = null;
        try {
            order = ordersRepository.findAll((root, query, cb) -> cb.equal(root.get("orderCode"), vnp_TxnRef))
                    .stream().findFirst()
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với mã: " + vnp_TxnRef));
        } catch (Exception e) {
            throw e;
        }

        // Kiểm tra số tiền
        Long expectedAmount = order.getTotal_amount() * 100; // VNPay trả về số tiền đã nhân 100
        if (!expectedAmount.toString().equals(vnp_Amount)) {
            throw new RuntimeException("Số tiền không khớp!");
        }

        try {
            if ("00".equals(vnp_ResponseCode)) {
                // Thanh toán thành công
                order.setPaymentStatus(Orders.PaymentStatus.PAID);
                ordersRepository.save(order);

                // Cập nhật payment
                Payments payment = paymentsRepository.findByOrder(order)
                        .stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy payment cho đơn hàng!"));
                payment.setPaymentStatus(Payments.PaymentStatus.PAID);
                paymentsRepository.save(payment);

                return true;
            } else {
                // Thanh toán thất bại
                order.setPaymentStatus(Orders.PaymentStatus.UNPAID);
                ordersRepository.save(order);

                // Cập nhật payment
                Payments payment = paymentsRepository.findByOrder(order)
                        .stream().findFirst()
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy payment cho đơn hàng!"));
                payment.setPaymentStatus(Payments.PaymentStatus.FAILED);
                paymentsRepository.save(payment);

                return false;
            }
        } catch (Exception e) {
            throw e;
        }
    }
}