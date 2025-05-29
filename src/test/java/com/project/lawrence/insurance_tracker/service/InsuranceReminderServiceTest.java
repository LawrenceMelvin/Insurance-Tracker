package com.project.lawrence.insurance_tracker.service;

import com.project.lawrence.insurance_tracker.model.Insurance;
import com.project.lawrence.insurance_tracker.model.User;
import com.project.lawrence.insurance_tracker.repository.Insurancerepo;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class InsuranceReminderServiceTest {

    @Mock
    private Insurancerepo insuranceRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private InsuranceReminderService insuranceReminderService;

    @Test
    void testSendInsuranceReminders() {
        MockitoAnnotations.openMocks(this);

        // Arrange
        LocalDate reminderDate = LocalDate.now().plusDays(7);
        Insurance mockInsurance = new Insurance();
        mockInsurance.setInsuranceName("Health Insurance");
        mockInsurance.setInsuranceType("Health");
        mockInsurance.setInsuranceToDate(reminderDate);
        mockInsurance.setUser(new User());
        mockInsurance.getUser().setUserEmail("lawrencemelvinstandly@gmail.com");
        // Mock user with email

        when(insuranceRepository.findByInsuranceToDate(reminderDate))
                .thenReturn(List.of(mockInsurance));

        // Act
        insuranceReminderService.sendInsuranceReminders();

        // Assert
        verify(emailService, times(1)).sendReminderEmail(
                eq("lawrencemelvinstandly@gmail.com"),
                eq("Insurance Expiry Reminder"),
                contains("Your insurance \"Health Insurance\" (Type: Health) is expiring on " + reminderDate)
        );

        System.out.println("Mail sent successfully for insurance reminder.");
    }
}
