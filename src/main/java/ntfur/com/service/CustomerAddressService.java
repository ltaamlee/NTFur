package ntfur.com.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ntfur.com.entity.Customer;
import ntfur.com.entity.CustomerAddress;
import ntfur.com.entity.CustomerAddress.AddressType;
import ntfur.com.entity.dto.CustomerAddressDTO;
import ntfur.com.repository.CustomerAddressRepository;
import ntfur.com.repository.CustomerRepository;

@Service
@RequiredArgsConstructor
public class CustomerAddressService {

    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerRepository customerRepository;

    public List<CustomerAddressDTO> getAddressesByCustomerId(Long customerId) {
        return customerAddressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customerId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public CustomerAddressDTO getAddressById(Long id, Long customerId) {
        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với id: " + id));
        return toDTO(address);
    }

    @Transactional
    public CustomerAddressDTO createAddress(Long customerId, CustomerAddressDTO dto) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng với id: " + customerId));

        CustomerAddress address = new CustomerAddress();
        address.setCustomer(customer);
        updateAddressFromDTO(address, dto);

        if (dto.isDefault() || customerAddressRepository.findByCustomerIdOrderByIsDefaultDescCreatedAtDesc(customerId).isEmpty()) {
            customerAddressRepository.clearDefaultForCustomer(customerId);
            address.setDefault(true);
        }

        CustomerAddress saved = customerAddressRepository.save(address);
        return toDTO(saved);
    }

    @Transactional
    public CustomerAddressDTO updateAddress(Long id, Long customerId, CustomerAddressDTO dto) {
        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với id: " + id));

        updateAddressFromDTO(address, dto);

        if (dto.isDefault() && !address.isDefault()) {
            customerAddressRepository.clearDefaultForCustomer(customerId);
            address.setDefault(true);
        }

        CustomerAddress saved = customerAddressRepository.save(address);
        return toDTO(saved);
    }

    @Transactional
    public void deleteAddress(Long id, Long customerId) {
        if (!customerAddressRepository.existsByIdAndCustomerId(id, customerId)) {
            throw new RuntimeException("Không tìm thấy địa chỉ với id: " + id);
        }
        customerAddressRepository.deleteByIdAndCustomerId(id, customerId);
    }

    @Transactional
    public CustomerAddressDTO setDefaultAddress(Long id, Long customerId) {
        CustomerAddress address = customerAddressRepository.findByIdAndCustomerId(id, customerId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy địa chỉ với id: " + id));

        customerAddressRepository.clearDefaultForCustomer(customerId);
        address.setDefault(true);

        CustomerAddress saved = customerAddressRepository.save(address);
        return toDTO(saved);
    }

    private void updateAddressFromDTO(CustomerAddress address, CustomerAddressDTO dto) {
        if (dto.getAddressName() != null) address.setAddressName(dto.getAddressName());
        if (dto.getRecipientName() != null) address.setRecipientName(dto.getRecipientName());
        if (dto.getPhone() != null) address.setPhone(dto.getPhone());
        if (dto.getAddressLine() != null) address.setAddressLine(dto.getAddressLine());
        if (dto.getCity() != null) address.setCity(dto.getCity());
        if (dto.getDistrict() != null) address.setDistrict(dto.getDistrict());
        if (dto.getWard() != null) address.setWard(dto.getWard());
        if (dto.getAddressType() != null) {
            try {
                address.setAddressType(AddressType.valueOf(dto.getAddressType()));
            } catch (IllegalArgumentException ignored) {
            }
        }
        if (dto.getNotes() != null) address.setNotes(dto.getNotes());
    }

    private CustomerAddressDTO toDTO(CustomerAddress address) {
        return CustomerAddressDTO.builder()
                .id(address.getId())
                .customerId(address.getCustomer() != null ? address.getCustomer().getId() : null)
                .addressName(address.getAddressName())
                .recipientName(address.getRecipientName())
                .phone(address.getPhone())
                .addressLine(address.getAddressLine())
                .city(address.getCity())
                .district(address.getDistrict())
                .ward(address.getWard())
                .fullAddress(address.getFormattedAddress())
                .isDefault(address.isDefault())
                .addressType(address.getAddressType() != null ? address.getAddressType().name() : null)
                .notes(address.getNotes())
                .createdAt(address.getCreatedAt())
                .updatedAt(address.getUpdatedAt())
                .build();
    }
}
