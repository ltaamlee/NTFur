package ntfur.com.controller.customer;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/customer")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('CUSTOMER')")
public class CustomerController {

    @GetMapping("/profile")
    public String profile(Model model) {
        model.addAttribute("title", "Hồ sơ của tôi");
        model.addAttribute("pageTitle", "Hồ sơ của tôi");
        model.addAttribute("activePage", "profile");
        model.addAttribute("content", "customer/pages/profile");
        return "customer/layout";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("title", "Đơn hàng");
        model.addAttribute("pageTitle", "Đơn hàng của tôi");
        model.addAttribute("activePage", "orders");
        model.addAttribute("content", "customer/pages/orders");
        return "customer/layout";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        model.addAttribute("title", "Giỏ hàng");
        model.addAttribute("pageTitle", "Giỏ hàng");
        model.addAttribute("activePage", "cart");
        model.addAttribute("content", "customer/pages/cart");
        return "customer/layout";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        model.addAttribute("title", "Thanh toán");
        model.addAttribute("pageTitle", "Thanh toán");
        model.addAttribute("activePage", "checkout");
        model.addAttribute("content", "customer/pages/checkout");
        return "customer/layout";
    }
}
