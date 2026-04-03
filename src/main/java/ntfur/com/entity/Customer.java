package ntfur.com.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false, columnDefinition = "nvarchar(150)")
    private String fullName;

    @Column(name = "phone", columnDefinition = "varchar(20)")
    private String phone;

    @Column(name = "address", columnDefinition = "nvarchar(MAX)")
    private String address;

    @Column(name = "city", columnDefinition = "nvarchar(100)")
    private String city;

    @Column(name = "district", columnDefinition = "nvarchar(100)")
    private String district;

    @Column(name = "ward", columnDefinition = "nvarchar(100)")
    private String ward;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Gender gender;

    @Column(name = "total_orders")
    private int totalOrders = 0;

    @Column(name = "total_spent", precision = 18, scale = 2)
    private java.math.BigDecimal totalSpent = java.math.BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "nvarchar(MAX)")
    private String notes;

    public enum Gender {
        MALE, FEMALE, OTHER
    }

    @PrePersist
    protected void onCreate() {
        if (user != null && fullName == null) {
            fullName = user.getFullName();
        }
        if (user != null && phone == null) {
            phone = user.getPhone();
        }
    }

    @PreUpdate
    protected void onUpdate() {
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Customer other = (Customer) obj;
        return id != null && id.equals(other.id);
    }

    public String getFormattedAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null) sb.append(address);
        if (ward != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(ward);
        }
        if (district != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(district);
        }
        if (city != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        return sb.toString();
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getWard() {
		return ward;
	}

	public void setWard(String ward) {
		this.ward = ward;
	}

	public LocalDate getDateOfBirth() {
		return dateOfBirth;
	}

	public void setDateOfBirth(LocalDate localDate) {
		this.dateOfBirth = localDate;
	}

	public Gender getGender() {
		return gender;
	}

	public void setGender(Gender gender) {
		this.gender = gender;
	}

	public int getTotalOrders() {
		return totalOrders;
	}

	public void setTotalOrders(int totalOrders) {
		this.totalOrders = totalOrders;
	}

	public java.math.BigDecimal getTotalSpent() {
		return totalSpent;
	}

	public void setTotalSpent(java.math.BigDecimal totalSpent) {
		this.totalSpent = totalSpent;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
    
}
