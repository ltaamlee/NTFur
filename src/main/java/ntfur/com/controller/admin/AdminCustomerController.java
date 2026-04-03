package ntfur.com.controller.admin;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ntfur.com.entity.dto.ApiResponse;
import ntfur.com.entity.dto.customer.CustomerDTO;
import ntfur.com.entity.dto.customer.UpdateCustomerRequest;
import ntfur.com.service.CustomerService;

@RestController
@RequestMapping("/api/admin/customers")
@RequiredArgsConstructor
public class AdminCustomerController {

    private final CustomerService customerService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getAllCustomers() {
        List<CustomerDTO> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerById(@PathVariable Long id) {
        try {
            CustomerDTO customer = customerService.getCustomerById(id);
            return ResponseEntity.ok(ApiResponse.success(customer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<CustomerDTO>> getCustomerByUserId(@PathVariable Long userId) {
        try {
            CustomerDTO customer = customerService.getCustomerByUserId(userId);
            return ResponseEntity.ok(ApiResponse.success(customer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> searchCustomers(@RequestParam String keyword) {
        List<CustomerDTO> customers = customerService.searchCustomers(keyword);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/top/spent")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getTopCustomersBySpent() {
        List<CustomerDTO> customers = customerService.getTopCustomersBySpent();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/top/orders")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getTopCustomersByOrders() {
        List<CustomerDTO> customers = customerService.getTopCustomersByOrders();
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @GetMapping("/recent")
    public ResponseEntity<ApiResponse<List<CustomerDTO>>> getRecentCustomers(@RequestParam(defaultValue = "5") int limit) {
        List<CustomerDTO> customers = customerService.getRecentCustomers(limit);
        return ResponseEntity.ok(ApiResponse.success(customers));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CustomerDTO>> updateCustomer(@PathVariable Long id, @Valid @RequestBody UpdateCustomerRequest request) {
        try {
            CustomerDTO customer = customerService.updateCustomer(id, request);
            return ResponseEntity.ok(ApiResponse.success("Cập nhật khách hàng thành công", customer));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        try {
            customerService.deleteCustomer(id);
            return ResponseEntity.ok(ApiResponse.success("Xóa khách hàng thành công", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> countCustomers() {
        long count = customerService.countCustomers();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
