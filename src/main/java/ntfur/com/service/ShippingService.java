package ntfur.com.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Order;
import ntfur.com.entity.Shipping;
import ntfur.com.entity.Shipping.ShippingMethod;
import ntfur.com.entity.Shipping.ShippingStatus;
import ntfur.com.entity.dto.ShippingDTO;
import ntfur.com.entity.dto.UpdateShippingRequest;
import ntfur.com.repository.OrderRepository;
import ntfur.com.repository.ShippingRepository;

@Service
@RequiredArgsConstructor
public class ShippingService {

    private final ShippingRepository shippingRepository;
    private final OrderRepository orderRepository;

    public List<ShippingDTO> getAllShippings() {
        return shippingRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ShippingDTO getShippingById(Long id) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vận chuyển với id: " + id));
        return toDTO(shipping);
    }

    public ShippingDTO getShippingByOrderId(Long orderId) {
        Shipping shipping = shippingRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vận chuyển cho đơn hàng: " + orderId));
        return toDTO(shipping);
    }

    public ShippingDTO getShippingByTrackingNumber(String trackingNumber) {
        Shipping shipping = shippingRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vận chuyển với mã: " + trackingNumber));
        return toDTO(shipping);
    }

    public List<ShippingDTO> getShippingsByStatus(String status) {
        return shippingRepository.findByStatusOrderByCreatedAtDesc(ShippingStatus.valueOf(status)).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ShippingDTO> getPendingInstallations() {
        return shippingRepository.findPendingInstallations().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ShippingDTO> getDeliveredPendingInstallations() {
        return shippingRepository.findDeliveredPendingInstallations().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<ShippingDTO> getFailedDeliveries() {
        return shippingRepository.findFailedDeliveries(3).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public ShippingDTO createShipping(Long orderId, ShippingDTO request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng với id: " + orderId));

        Shipping shipping = new Shipping();
        shipping.setOrder(order);
        shipping.setTrackingNumber(request.getTrackingNumber());
        shipping.setCarrier(request.getCarrier());
        if (request.getShippingMethod() != null) {
            shipping.setShippingMethod(ShippingMethod.valueOf(request.getShippingMethod()));
        }
        shipping.setShippingFee(request.getShippingFee());
        shipping.setEstimatedDays(request.getEstimatedDays());
        if (request.getStatus() != null) {
            shipping.setStatus(ShippingStatus.valueOf(request.getStatus()));
        } else {
            shipping.setStatus(ShippingStatus.PENDING);
        }
        shipping.setReceiverName(request.getReceiverName());
        shipping.setReceiverPhone(request.getReceiverPhone());
        shipping.setDeliveryAddress(request.getDeliveryAddress());
        shipping.setDeliveryNotes(request.getDeliveryNotes());
        shipping.setInstallationRequired(request.isInstallationRequired());
        shipping.setInstallationFee(request.getInstallationFee());

        Shipping savedShipping = shippingRepository.save(shipping);
        return toDTO(savedShipping);
    }

    @Transactional
    public ShippingDTO updateShipping(Long id, UpdateShippingRequest request) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vận chuyển với id: " + id));

        if (request.getTrackingNumber() != null) {
            shipping.setTrackingNumber(request.getTrackingNumber());
        }
        if (request.getCarrier() != null) {
            shipping.setCarrier(request.getCarrier());
        }
        if (request.getShippingMethod() != null) {
            shipping.setShippingMethod(ShippingMethod.valueOf(request.getShippingMethod()));
        }
        if (request.getShippingFee() != null) {
            shipping.setShippingFee(request.getShippingFee());
        }
        if (request.getEstimatedDays() != null) {
            shipping.setEstimatedDays(request.getEstimatedDays());
        }
        if (request.getStatus() != null) {
            shipping.setStatus(ShippingStatus.valueOf(request.getStatus()));
        }
        if (request.getReceiverName() != null) {
            shipping.setReceiverName(request.getReceiverName());
        }
        if (request.getReceiverPhone() != null) {
            shipping.setReceiverPhone(request.getReceiverPhone());
        }
        if (request.getDeliveryAddress() != null) {
            shipping.setDeliveryAddress(request.getDeliveryAddress());
        }
        if (request.getDeliveryNotes() != null) {
            shipping.setDeliveryNotes(request.getDeliveryNotes());
        }
        if (request.getInstallationRequired() != null) {
            shipping.setInstallationRequired(request.getInstallationRequired());
        }
        if (request.getInstallationFee() != null) {
            shipping.setInstallationFee(request.getInstallationFee());
        }
        if (request.getInstallationNotes() != null) {
            shipping.setInstallationNotes(request.getInstallationNotes());
        }

        Shipping updatedShipping = shippingRepository.save(shipping);
        return toDTO(updatedShipping);
    }

    @Transactional
    public ShippingDTO updateShippingStatus(Long id, String status) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vận chuyển với id: " + id));

        shipping.setStatus(ShippingStatus.valueOf(status));

        if (status.equals("DELIVERED")) {
            shipping.setDeliveryDate(java.time.LocalDateTime.now());
        }

        Shipping updatedShipping = shippingRepository.save(shipping);
        return toDTO(updatedShipping);
    }

    @Transactional
    public ShippingDTO markInstallationComplete(Long id, String notes) {
        Shipping shipping = shippingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy vận chuyển với id: " + id));

        shipping.setInstallationCompletedAt(java.time.LocalDateTime.now());
        if (notes != null) {
            shipping.setInstallationNotes(notes);
        }

        Shipping updatedShipping = shippingRepository.save(shipping);
        return toDTO(updatedShipping);
    }

    @Transactional
    public void deleteShipping(Long id) {
        if (!shippingRepository.existsById(id)) {
            throw new RuntimeException("Không tìm thấy vận chuyển với id: " + id);
        }
        shippingRepository.deleteById(id);
    }

    public long countShippingsByStatus(String status) {
        return shippingRepository.countByStatus(ShippingStatus.valueOf(status));
    }

    private ShippingDTO toDTO(Shipping shipping) {
        return ShippingDTO.builder()
                .id(shipping.getId())
                .orderId(shipping.getOrder() != null ? shipping.getOrder().getId() : null)
                .orderNumber(shipping.getOrder() != null ? shipping.getOrder().getOrderNumber() : null)
                .trackingNumber(shipping.getTrackingNumber())
                .carrier(shipping.getCarrier())
                .shippingMethod(shipping.getShippingMethod() != null ? shipping.getShippingMethod().name() : null)
                .shippingFee(shipping.getShippingFee())
                .estimatedDays(shipping.getEstimatedDays())
                .status(shipping.getStatus() != null ? shipping.getStatus().name() : null)
                .pickupDate(shipping.getPickupDate())
                .pickupTime(shipping.getPickupTime())
                .deliveryDate(shipping.getDeliveryDate())
                .deliveryTime(shipping.getDeliveryTime())
                .receiverName(shipping.getReceiverName())
                .receiverPhone(shipping.getReceiverPhone())
                .deliveryAddress(shipping.getDeliveryAddress())
                .deliveryNotes(shipping.getDeliveryNotes())
                .installationRequired(shipping.isInstallationRequired())
                .installationFee(shipping.getInstallationFee())
                .installationCompletedAt(shipping.getInstallationCompletedAt())
                .installationNotes(shipping.getInstallationNotes())
                .attemptCount(shipping.getAttemptCount())
                .lastAttemptDate(shipping.getLastAttemptDate())
                .failureReason(shipping.getFailureReason())
                .signedBy(shipping.getSignedBy())
                .signatureUrl(shipping.getSignatureUrl())
                .photoUrl(shipping.getPhotoUrl())
                .totalFee(shipping.getTotalFee())
                .createdAt(shipping.getCreatedAt())
                .updatedAt(shipping.getUpdatedAt())
                .build();
    }
}
