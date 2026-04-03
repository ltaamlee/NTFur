package ntfur.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthViewController {

    // Trang đăng nhập
    @GetMapping("/auth/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/login")
    public String loginAliasPage() {
        return "auth/login";
    }

    // Trang đăng ký
    @GetMapping("/auth/register")
    public String registerPage() {
        return "auth/register";
    }

    @GetMapping("/register")
    public String registerAliasPage() {
        return "auth/register";
    }

    // Trang quên mật khẩu
    @GetMapping("/auth/forgot-password")
    public String forgotPasswordPage() {
        return "auth/forgot-password";
    }

    // Trang đặt lại mật khẩu
    @GetMapping("/auth/reset-password")
    public String resetPasswordPage(@RequestParam(required = false) String token) {
        return "auth/reset-password";
    }

    // Trang xác thực email
    @GetMapping("/auth/verify")
    public String verifyEmailPage(@RequestParam(required = false) String token) {
        return "auth/verify-email";
    }
}
