# Shop API

## Giới thiệu

**Shop API** là dự án backend xây dựng bằng Java Spring Boot, cung cấp các API phục vụ cho website bán hàng. Dự án này chịu trách nhiệm xử lý toàn bộ logic nghiệp vụ, quản lý dữ liệu và cung cấp các endpoint RESTful cho frontend hoặc các dịch vụ khác.

## Chức năng chính

- **Quản lý người dùng:** Đăng ký, đăng nhập, phân quyền, xác thực OTP qua email, quản lý địa chỉ giao hàng.
- **Quản lý sản phẩm:** Thêm, sửa, xóa, xem chi tiết sản phẩm, quản lý biến thể (size, màu), hình ảnh sản phẩm, đánh giá sản phẩm.
- **Quản lý danh mục:** Phân loại sản phẩm theo danh mục.
- **Quản lý giỏ hàng:** Thêm, sửa, xóa sản phẩm trong giỏ hàng, cập nhật số lượng, lưu giỏ hàng cho từng người dùng.
- **Quản lý đơn hàng:** Tạo đơn hàng, xem lịch sử đơn hàng, chi tiết đơn hàng, trạng thái đơn hàng, theo dõi vận chuyển.
- **Quản lý thanh toán:** Xử lý thông tin thanh toán, trạng thái thanh toán, phương thức thanh toán.
- **Quản lý mã giảm giá (voucher):** Tạo, áp dụng, kiểm tra điều kiện sử dụng mã giảm giá cho đơn hàng.
- **Yêu thích sản phẩm:** Người dùng có thể thêm sản phẩm vào danh sách yêu thích.
- **Phân quyền & bảo mật:** Hệ thống phân quyền theo vai trò (admin, user, ...), bảo mật thông tin người dùng.

## Công nghệ sử dụng

- **Java 17+**
- **Spring Boot**
- **JPA/Hibernate**
- **MySQL**
- **Lombok**
- **Gradle**

## Cấu trúc dự án

- `Domain/`: Chứa các entity (bảng dữ liệu) chính như Users, Products, Orders, Cart, v.v.
- `Controller/`: Các REST API endpoint.
- `Service/`: Xử lý logic nghiệp vụ.
- `Repository/`: Tầng truy xuất dữ liệu.
- `Config/`: Cấu hình bảo mật, cấu hình ứng dụng.
- `Util/`: Các tiện ích dùng chung.

## Hướng dẫn cài đặt

1. Clone repository về máy:
   ```bash
   git clone https://github.com/QuangHuy-dev2003/Shop-Api.git
   ```
2. Cấu hình database trong file `application.properties`.
3. Chạy lệnh build và start ứng dụng:
   ```bash
   ./gradlew bootRun
   ```
4. Truy cập các endpoint API qua Postman hoặc frontend.

## Đóng góp

Mọi đóng góp, báo lỗi hoặc ý tưởng phát triển thêm đều được hoan nghênh! Hãy tạo issue hoặc pull request trên GitHub.

---

**Dự án này chỉ là backend, không bao gồm giao diện người dùng.**
