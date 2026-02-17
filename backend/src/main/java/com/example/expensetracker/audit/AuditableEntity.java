package com.example.expensetracker.audit;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
public abstract class AuditableEntity {

    @CreatedBy
    @Column(
            name = "created_by",
            nullable = false,
            updatable = false,
            length = 100,
            columnDefinition = "varchar(100) not null default 'system'"
    )
    private String createdBy;

    @CreatedDate
    @Column(
            name = "created_on",
            nullable = false,
            updatable = false,
            columnDefinition = "datetime(6) not null default current_timestamp(6)"
    )
    private LocalDateTime createdOn;

    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @LastModifiedDate
    @Column(name = "updated_on")
    private LocalDateTime updatedOn;

}
