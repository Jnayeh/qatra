package com.zayenha.qatra.donor.infrastructure.persistence.repository;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonationCertificateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationCertificateJpaRepository extends JpaRepository<DonationCertificateEntity, Long> {
    List<DonationCertificateEntity> findByDonorIdOrderByDonationDateDesc(Long donorId);
}
