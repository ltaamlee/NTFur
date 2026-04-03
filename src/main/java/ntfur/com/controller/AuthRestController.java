package ntfur.com.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.auth.ForgotPasswordRequest;
import ntfur.com.entity.dto.auth.LoginRequest;
import ntfur.com.entity.dto.auth.RefreshTokenRequest;
import ntfur.com.entity.dto.auth.RegisterRequest;
import ntfur.com.entity.dto.auth.ResetPasswordRequest;
import ntfur.com.entity.dto.*;
import ntfur.com.service.AuthService;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthRestController {

    private final AuthService authService;

    /** Trả về thông tin user khi gửi kèm Bearer token; dùng để hiển thị tên trên header. */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserDTO>> me() {
        ApiResponse<UserDTO> res = authService.getCurrentUserProfile();
        if (res.isSuccess()) {
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.status(401).body(res);
    }

    // Đăng nhập
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody LoginRequest request) {
        ApiResponse<AuthResponse> response = authService.login(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Đăng ký
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody RegisterRequest request) {
        ApiResponse<AuthResponse> response = authService.register(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Xác thực tài khoản
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyAccount(@RequestParam String token) {
        ApiResponse<String> response = authService.verifyAccount(token);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Gửi lại email xác thực
    @PostMapping("/resend-verification")
    public ResponseEntity<ApiResponse<String>> resendVerification(@RequestParam String email) {
        ApiResponse<String> response = authService.resendVerificationEmail(email);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Quên mật khẩu
    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        ApiResponse<String> response = authService.forgotPassword(request);
        return ResponseEntity.ok(response);
    }

    // Đặt lại mật khẩu
    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(@RequestBody ResetPasswordRequest request) {
        ApiResponse<String> response = authService.resetPassword(request);
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Xác thực token reset (để kiểm tra token có hợp lệ không)
    @GetMapping("/verify-reset-token")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> verifyResetToken(@RequestParam String token) {
        boolean valid = authService.isValidResetToken(token);
        return ResponseEntity.ok(ApiResponse.success(Map.of("valid", valid)));
    }

    // Refresh token
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestBody RefreshTokenRequest request) {
        ApiResponse<AuthResponse> response = authService.refreshToken(request.getRefreshToken());
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.badRequest().body(response);
    }

    // Đăng xuất
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout() {
        return ResponseEntity.ok(ApiResponse.success("Đăng xuất thành công!"));
    }
}
