package ntfur.com.entity.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShippingRequest {

    private String trackingNumber;

    private String carrier;

    private String shippingMethod;

    @DecimalMin(value = "0.0", message = "Phí vận chuyển không được nhỏ hơn 0")
    private BigDecimal shippingFee;

    @DecimalMin(value = "0", message = "Số ngày ước tính không được nhỏ hơn 0")
    private Integer estimatedDays;

    private String status;

    private String pickupDate;

    private String pickupTime;

    private String deliveryDate;

    private String deliveryTime;

    private String receiverName;

    private String receiverPhone;

    private String deliveryAddress;

    private String deliveryNotes;

    private Boolean installationRequired;

    @DecimalMin(value = "0.0", message = "Phí lắp đặt không được nhỏ hơn 0")
    private BigDecimal installationFee;

    private String installationNotes;
}
