package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.dto.InsuranceDTO;
import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class InsuranceReminderService {

    private static final Logger logger = LoggerFactory.getLogger(InsuranceReminderService.class);

    @Autowired
    private Insurancerepo insuranceRepository;

    @Autowired
    private EmailService emailService;

    public void findExpiry() {
        LocalDate today = LocalDate.now();

        // 7 days reminder
        LocalDate sevenDaysAhead = today.plusDays(7);
        List<Insurance> sevenDaysExpiring = insuranceRepository.findByInsuranceToDate(sevenDaysAhead);
        sendReminders(sevenDaysExpiring, "7 days");

        // 5 days reminder
        LocalDate fiveDaysAhead = today.plusDays(5);
        List<Insurance> fiveDaysExpiring = insuranceRepository.findByInsuranceToDate(fiveDaysAhead);
        sendReminders(fiveDaysExpiring, "5 days");

        // 2 days reminder
        LocalDate twoDaysAhead = today.plusDays(2);
        List<Insurance> twoDaysExpiring = insuranceRepository.findByInsuranceToDate(twoDaysAhead);
        sendReminders(twoDaysExpiring, "2 days");
    }

     // Runs every day at 9 AM
    public void sendReminders(List<Insurance> expiryinsurances, String reminderType) {
        logger.info("Starting insurance reminder service...");
        for (Insurance insurance : expiryinsurances) {
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
            logger.info("Sent reminder email to {} for insurance {}", email, insurance.getInsuranceName());
        }
    }
}
