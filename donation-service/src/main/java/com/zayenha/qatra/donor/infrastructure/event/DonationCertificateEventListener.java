package com.zayenha.qatra.donor.infrastructure.event;

import com.zayenha.qatra._shared.domain.DateUtils;
import com.zayenha.qatra._shared.event.DonationCompletedEvent;
import com.zayenha.qatra.donor.infrastructure.persistence.entity.DonationCertificateEntity;
import com.zayenha.qatra.donor.infrastructure.persistence.repository.DonationCertificateJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DonationCertificateEventListener {

    private final DonationCertificateJpaRepository certificateRepository;

    @EventListener
    public void onDonationCompleted(DonationCompletedEvent event) {
        var certificate = new DonationCertificateEntity();
        certificate.setDonorId(event.donorId());
        certificate.setAppointmentId(event.appointmentId());
        certificate.setDonorName(event.donorName());
        certificate.setCenterId(event.centerId());
        certificate.setCenterName(event.centerName());
        certificate.setMlCollected(event.mlCollected());
        certificate.setDonationType(event.donationType());
        certificate.setDonationDate(DateUtils.toLocalDate(event.completedAt()));
        certificateRepository.save(certificate);
    }
}
