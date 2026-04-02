package ntfur.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthViewController {

    // Trang đăng nhập
    @GetMapping("/auth/login")
    public String loginPage() {
        return "login";
    }

    // Trang đăng ký
    @GetMapping("/auth/register")
    public String registerPage() {
        return "register";
    }

    // Trang quên mật khẩu
    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    // Trang đặt lại mật khẩu
    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token) {
        return "reset-password";
    }

    // Trang xác thực email
    @GetMapping("/auth/verify")
    public String verifyEmailPage(@RequestParam(required = false) String token) {
        return "verify-email";
    }
}
