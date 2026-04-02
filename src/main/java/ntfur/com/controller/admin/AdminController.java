package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.EmployeeDTO;
import ntfur.com.entity.dto.SupplierDTO;
import ntfur.com.service.EmployeeService;
import ntfur.com.service.SupplierService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {



    // ==================== PAGE ROUTES ====================
    
    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("title", "Tổng quan");
        model.addAttribute("pageTitle", "Tổng quan");
        model.addAttribute("activePage", "dashboard");
        model.addAttribute("content", "admin/pages/dashboard");
        return "admin/layout";
    }

    @GetMapping("/employees")
    public String employees(Model model) {
        model.addAttribute("title", "Quản lý nhân viên");
        model.addAttribute("pageTitle", "Quản lý nhân viên");
        model.addAttribute("activePage", "employees");
        model.addAttribute("content", "admin/pages/employees");
        return "admin/layout";
    }

    @GetMapping("/customers")
    public String customers(Model model) {
        model.addAttribute("title", "Quản lý khách hàng");
        model.addAttribute("pageTitle", "Quản lý khách hàng");
        model.addAttribute("activePage", "customers");
        model.addAttribute("content", "admin/pages/customers");
        return "admin/layout";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("title", "Quản lý danh mục");
        model.addAttribute("pageTitle", "Quản lý danh mục");
        model.addAttribute("activePage", "categories");
        model.addAttribute("content", "admin/pages/categories");
        return "admin/layout";
    }

    @GetMapping("/suppliers")
    public String suppliers(Model model) {
        model.addAttribute("title", "Quản lý nhà cung cấp");
        model.addAttribute("pageTitle", "Quản lý nhà cung cấp");
        model.addAttribute("activePage", "suppliers");
        model.addAttribute("content", "admin/pages/suppliers");
        return "admin/layout";
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("title", "Quản lý sản phẩm");
        model.addAttribute("pageTitle", "Quản lý sản phẩm");
        model.addAttribute("activePage", "products");
        model.addAttribute("content", "admin/pages/products");
        return "admin/layout";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("title", "Quản lý đơn hàng");
        model.addAttribute("pageTitle", "Quản lý đơn hàng");
        model.addAttribute("activePage", "orders");
        model.addAttribute("content", "admin/pages/orders");
        return "admin/layout";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {
        model.addAttribute("title", "Quản lý vận chuyển");
        model.addAttribute("pageTitle", "Quản lý vận chuyển & Lắp đặt");
        model.addAttribute("activePage", "shipping");
        model.addAttribute("content", "admin/pages/shipping");
        return "admin/layout";
    }

}
