package com.zayenha.qatra.center.infrastructure.persistence.entity;

import com.zayenha.qatra.center.domain.model.CenterStatus;
import com.zayenha.qatra.center.domain.model.FacilityType;
import com.zayenha.qatra.center.domain.model.OperatingHours;
import com.zayenha.qatra.user.infrastructure.persistence.entity.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "donation_centers")
@Getter
@Setter
@NoArgsConstructor
@NamedEntityGraph(
    name = "CenterEntity.withSlots",
    attributeNodes = @NamedAttributeNode("slots")
)
public class CenterEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String country;

    private String postalCode;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String email;

    private Double latitude;
    private Double longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FacilityType facilityType;

    @JdbcTypeCode(SqlTypes.JSON)
    private OperatingHours operatingHours;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserEntity createdBy;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CenterStatus status;

    private Integer totalCapacity;
    private Integer maxRegular;
    private Integer slotPeriod;

    @OneToMany(mappedBy = "center", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SlotEntity> slots;

    @OneToMany(mappedBy = "center", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CenterStaffProfileEntity> staff;

    @OneToMany(mappedBy = "center", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CenterAdminProfileEntity> admins;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
