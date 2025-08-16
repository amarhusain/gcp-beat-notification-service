package com.beat.gcp_notification_service.email.pubsub;

import com.beat.gcp_notification_service.email.dto.EmailCommand;
import com.beat.gcp_notification_service.email.service.IEmailSender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.PubsubMessage;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.stereotype.Component;

@Component
public class PubSubEmailReceiver implements SmartLifecycle {

    private static final Logger log = LoggerFactory.getLogger(PubSubEmailReceiver.class);

    private final String projectId;
    private final String subscriptionId;
    private final IEmailSender sender;
    private final ObjectMapper mapper = new ObjectMapper();

    private Subscriber subscriber;
    private volatile boolean running = false;

    public PubSubEmailReceiver(
            @Value("${gcp.project-id}") String projectId,
            @Value("${gcp.pubsub.subscription}") String subscriptionId,
            IEmailSender sender) {
        this.projectId = projectId;
        this.subscriptionId = subscriptionId;
        this.sender = sender;
    }

    @PostConstruct
    public void init() {
        ProjectSubscriptionName name = ProjectSubscriptionName.of(projectId, subscriptionId);
        subscriber = Subscriber.newBuilder(name, this::onMessage).build();
    }

    private void onMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String payload = message.getData().toStringUtf8();
            EmailCommand cmd = mapper.readValue(payload, EmailCommand.class);
            sender.sendEmailAPI(cmd);
            consumer.ack();
            log.info("Processed messageId={} to={}", message.getMessageId(), cmd.to());
        } catch (Exception ex) {
            log.error("Failed processing messageId={}, will NACK", message.getMessageId(), ex);
            consumer.nack(); // Let Pub/Sub retry with backoff / DLQ if configured
        }
    }

    @Override public void start() {
        if (!running) {
            subscriber.startAsync().awaitRunning();
            running = true;
        }
    }

    @Override public void stop() {
        if (subscriber != null) {
            subscriber.stopAsync().awaitTerminated();
        }
        running = false;
    }

    @Override public boolean isRunning() { return running; }

    @PreDestroy
    public void shutdown() { stop(); }
}

