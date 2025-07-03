package com.sportshop.api.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.api.Domain.Discounts;
import com.sportshop.api.Domain.Request.Discounts.CreateDiscountRequest;
import com.sportshop.api.Domain.Reponse.Discounts.DiscountResponse;
import com.sportshop.api.Repository.DiscountsRepository;

@Service
public class DiscountsService {

    private final DiscountsRepository discountsRepository;

    public DiscountsService(DiscountsRepository discountsRepository) {
        this.discountsRepository = discountsRepository;
    }

    /**
     * Lấy tất cả mã giảm giá
     */
    public List<DiscountResponse> getAllDiscounts() {
        return discountsRepository.findAll().stream()
                .map(this::convertToDiscountResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy mã giảm giá theo ID
     */
    public DiscountResponse getDiscountById(Long id) {
        Discounts discount = discountsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));
        return convertToDiscountResponse(discount);
    }

    /**
     * Lấy mã giảm giá theo code
     */
    public DiscountResponse getDiscountByCode(String code) {
        Discounts discount = discountsRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với code: " + code));
        return convertToDiscountResponse(discount);
    }

    /**
     * Lấy entity Discounts từ code
     */
    public Discounts getDiscountEntityByCode(String code) {
        return discountsRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với code: " + code));
    }

    /**
     * Tạo mới mã giảm giá
     */
    @Transactional
    public DiscountResponse createDiscount(CreateDiscountRequest request) {
        // Kiểm tra code đã tồn tại chưa
        if (discountsRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Mã giảm giá đã tồn tại: " + request.getCode());
        }

        // Validate discount value theo loại
        validateDiscountValue(request.getDiscountType(), request.getDiscountValue());

        // Validate thời gian
        validateDiscountTime(request.getStartDate(), request.getEndDate());

        Discounts discount = new Discounts();
        discount.setCode(request.getCode());
        discount.setName(request.getName());
        discount.setDescription(request.getDescription());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMinimumOrderAmount(request.getMinimumOrderAmount());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setPerUserLimit(request.getPerUserLimit());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setIsActive(request.getIsActive());

        Discounts savedDiscount = discountsRepository.save(discount);
        return convertToDiscountResponse(savedDiscount);
    }

    /**
     * Cập nhật mã giảm giá
     */
    @Transactional
    public DiscountResponse updateDiscount(Long id, CreateDiscountRequest request) {
        Discounts discount = discountsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));

        // Kiểm tra code đã tồn tại chưa (trừ chính nó)
        if (!discount.getCode().equals(request.getCode()) &&
                discountsRepository.findByCode(request.getCode()).isPresent()) {
            throw new RuntimeException("Mã giảm giá đã tồn tại: " + request.getCode());
        }

        // Validate discount value theo loại
        validateDiscountValue(request.getDiscountType(), request.getDiscountValue());

        // Validate thời gian
        validateDiscountTime(request.getStartDate(), request.getEndDate());

        discount.setCode(request.getCode());
        discount.setName(request.getName());
        discount.setDescription(request.getDescription());
        discount.setDiscountType(request.getDiscountType());
        discount.setDiscountValue(request.getDiscountValue());
        discount.setMinimumOrderAmount(request.getMinimumOrderAmount());
        discount.setUsageLimit(request.getUsageLimit());
        discount.setPerUserLimit(request.getPerUserLimit());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        discount.setIsActive(request.getIsActive());

        Discounts updatedDiscount = discountsRepository.save(discount);
        return convertToDiscountResponse(updatedDiscount);
    }

    /**
     * Xóa mã giảm giá (soft delete)
     */
    @Transactional
    public void deleteDiscount(Long id) {
        Discounts discount = discountsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));

        discountsRepository.delete(discount);
    }

    /**
     * Kích hoạt/hủy kích hoạt mã giảm giá
     */
    @Transactional
    public DiscountResponse toggleDiscountStatus(Long id) {
        Discounts discount = discountsRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã giảm giá với ID: " + id));

        discount.setIsActive(!discount.getIsActive());
        Discounts updatedDiscount = discountsRepository.save(discount);
        return convertToDiscountResponse(updatedDiscount);
    }

    /**
     * Validate mã giảm giá (dùng khi user nhập mã)
     */
    public DiscountResponse validateDiscountCode(String code, BigDecimal orderAmount) {
        Discounts discount = discountsRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Mã giảm giá không tồn tại"));

        DiscountResponse response = convertToDiscountResponse(discount);

        // Kiểm tra các điều kiện
        if (!discount.getIsActive()) {
            response.setIsValid(false);
            response.setStatusMessage("Mã giảm giá đã bị vô hiệu hóa");
            return response;
        }

        if (discount.getStartDate() != null && LocalDateTime.now().isBefore(discount.getStartDate())) {
            response.setIsValid(false);
            response.setStatusMessage("Mã giảm giá chưa có hiệu lực");
            return response;
        }

        if (discount.getEndDate() != null && LocalDateTime.now().isAfter(discount.getEndDate())) {
            response.setIsValid(false);
            response.setStatusMessage("Mã giảm giá đã hết hạn");
            return response;
        }

        if (discount.getUsageLimit() != null &&
                discount.getUsedCount() >= discount.getUsageLimit()) {
            response.setIsValid(false);
            response.setStatusMessage("Mã giảm giá đã hết lượt sử dụng");
            return response;
        }

        if (discount.getMinimumOrderAmount() != null &&
                orderAmount.compareTo(discount.getMinimumOrderAmount()) < 0) {
            response.setIsValid(false);
            response.setStatusMessage("Đơn hàng tối thiểu: " + discount.getMinimumOrderAmount());
            return response;
        }

        response.setIsValid(true);
        response.setStatusMessage("Mã giảm giá hợp lệ");
        return response;
    }

    /**
     * Validate giá trị giảm giá theo loại
     */
    private void validateDiscountValue(Discounts.DiscountType discountType, BigDecimal discountValue) {
        if (discountType == Discounts.DiscountType.PERCENTAGE) {
            if (discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new RuntimeException("Phần trăm giảm giá không được vượt quá 100%");
            }
        } else if (discountType == Discounts.DiscountType.FIXED_AMOUNT) {
            if (discountValue.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException("Giá trị giảm giá cố định phải lớn hơn 0");
            }
        }
    }

    /**
     * Validate thời gian mã giảm giá
     */
    private void validateDiscountTime(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            throw new RuntimeException("Thời gian bắt đầu phải trước thời gian kết thúc");
        }
    }

    /**
     * Chuyển đổi Discounts thành DiscountResponse
     */
    private DiscountResponse convertToDiscountResponse(Discounts discount) {
        DiscountResponse response = new DiscountResponse();
        response.setId(discount.getId());
        response.setCode(discount.getCode());
        response.setName(discount.getName());
        response.setDescription(discount.getDescription());
        response.setDiscountType(discount.getDiscountType());
        response.setDiscountValue(discount.getDiscountValue());
        response.setMinimumOrderAmount(discount.getMinimumOrderAmount());
        response.setUsageLimit(discount.getUsageLimit());
        response.setUsedCount(discount.getUsedCount());
        response.setPerUserLimit(discount.getPerUserLimit());
        response.setStartDate(discount.getStartDate());
        response.setEndDate(discount.getEndDate());
        response.setIsActive(discount.getIsActive());
        response.setCreatedAt(discount.getCreatedAt());
        response.setUpdatedAt(discount.getUpdatedAt());

        // Tính toán trạng thái hợp lệ
        boolean isValid = discount.getIsActive();
        String statusMessage = "Mã giảm giá hợp lệ";

        if (discount.getStartDate() != null && LocalDateTime.now().isBefore(discount.getStartDate())) {
            isValid = false;
            statusMessage = "Mã giảm giá chưa có hiệu lực";
        } else if (discount.getEndDate() != null && LocalDateTime.now().isAfter(discount.getEndDate())) {
            isValid = false;
            statusMessage = "Mã giảm giá đã hết hạn";
        } else if (discount.getUsageLimit() != null &&
                discount.getUsedCount() >= discount.getUsageLimit()) {
            isValid = false;
            statusMessage = "Mã giảm giá đã hết lượt sử dụng";
        }

        response.setIsValid(isValid);
        response.setStatusMessage(statusMessage);

        return response;
    }
}
