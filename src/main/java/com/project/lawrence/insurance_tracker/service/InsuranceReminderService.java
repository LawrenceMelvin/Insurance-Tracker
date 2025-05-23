package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InsuranceReminderService {

    @Autowired
    private Insurancerepo insuranceRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 9 * * ?") // Runs every day at 9 AM
    public void sendInsuranceReminders() {
        LocalDate today = LocalDate.now();
        LocalDate reminderDate = today.plusDays(7); // 7 days before expiry

        List<Insurance> expiringInsurances = insuranceRepository.findByInsuranceToDate(reminderDate);

        for (Insurance insurance : expiringInsurances) {
            String email = insurance.getUser().getUserEmail(); // Replace with the user's email
            String subject = "Insurance Expiry Reminder";
            String message = """
                    Dear User,
                    
                    Your insurance "%s" (Type: %s) is expiring on %s.
                    Please take necessary action.
                    
                    Regards,
                    Insurance Tracker Team
                    """.formatted(insurance.getInsuranceName(), insurance.getInsuranceType(), insurance.getInsuranceToDate());

            emailService.sendReminderEmail(email, subject, message);
        }
    }
}
