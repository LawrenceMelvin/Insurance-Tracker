package com.project.lawrence.insurance_tracker.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "family_group")
public class FamilyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int familyGroupId;

    @Column(nullable = false)
    private String groupName;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    public FamilyGroup() {
        this.createdAt = LocalDate.now();
    }

    public int getFamilyGroupId() {
        return familyGroupId;
    }

    public void setFamilyGroupId(int familyGroupId) {
        this.familyGroupId = familyGroupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }
}
