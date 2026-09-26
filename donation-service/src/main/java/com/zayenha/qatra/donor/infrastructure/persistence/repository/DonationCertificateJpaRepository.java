package com.zayenha.qatra.donor.infrastructure.persistence.repository;

import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonationCertificateEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationCertificateJpaRepository extends JpaRepository<DonationCertificateEntity, Long> {
    List<DonationCertificateEntity> findByDonorIdOrderByDonationDateDesc(Long donorId);

    Page<DonationCertificateEntity> findByDonorIdOrderByDonationDateDesc(Long donorId, Pageable pageable);
}
