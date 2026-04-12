package ntfur.com.controller.employee;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/employee")
@RequiredArgsConstructor
public class EmployeeController {

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "Tổng quan");
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("content", "employee/pages/dashboard");
        return "employee/layout";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "Quản lý sản phẩm");
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        model.addAttribute("activePage", "products");
        model.addAttribute("content", "employee/pages/products");
        return "employee/layout";
    }

    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("title", "Quản lý nhà cung cấp");
        model.addAttribute("pageTitle", "Quản lý nhà cung cấp");
        model.addAttribute("activePage", "suppliers");
        model.addAttribute("content", "employee/pages/suppliers");
        return "employee/layout";
    }

    @GetMapping("/import")
    public String importPage(Model model) {
        model.addAttribute("title", "Nhập hàng");
        model.addAttribute("pageTitle", "Nhập hàng");
        model.addAttribute("activePage", "import");
        model.addAttribute("content", "employee/pages/import");
        return "employee/layout";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("title", "Quản lý đơn hàng");
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        model.addAttribute("activePage", "orders");
        model.addAttribute("content", "employee/pages/orders");
        return "employee/layout";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {
        model.addAttribute("title", "Quản lý vận chuyển");
        model.addAttribute("pageTitle", "Quản lý vận chuyển & Lắp đặt");
        model.addAttribute("activePage", "shipping");
        model.addAttribute("content", "employee/pages/shipping");
        return "employee/layout";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("title", "Quản lý khách hàng");
        model.addAttribute("pageTitle", "Quản lý khách hàng");
        model.addAttribute("activePage", "customers");
        model.addAttribute("content", "employee/pages/customers");
        return "employee/layout";
    }
}
