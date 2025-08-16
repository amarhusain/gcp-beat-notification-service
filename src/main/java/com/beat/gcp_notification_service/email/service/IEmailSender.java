package com.beat.gcp_notification_service.email.service;

import com.beat.gcp_notification_service.email.dto.EmailCommand;

public interface IEmailSender {
    void sendEmailSMTP(EmailCommand cmd);
    void sendEmailAPI(EmailCommand cmd);
}
