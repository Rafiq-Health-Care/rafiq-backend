package com.nexaworks.rafiq.constant;

public class RabbitMQConstant {
    public static final String EMAIL_NOTIFICATION_QUEUE = "notification.email";
    public static final String SMS_NOTIFICATION_QUEUE = "notification.sms";
    public static final String PUSH_NOTIFICATION_QUEUE = "notification.push";

    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    public static final String ROUTING_KEY_EMAIL = "notification.email";
    public static final String ROUTING_KEY_SMS = "notification.sms";
    public static final String ROUTING_KEY_PUSH = "notification.push";

    public static final String EMAIL_DLQ = "notification.email.dlq";
    public static final String SMS_DLQ = "notification.sms.dlq";
    public static final String PUSH_DLQ = "notification.push.dlq";

    public static final String NOTIFICATION_DLQ_EXCHANGE = "notification.dlq.exchange";

    public static final String CONSULTATION_EXPIRATION_QUEUE = "consultation.expiration";
    public static final String CONSULTATION_EXPIRATION_EXCHANGE = "consultation.expiration.exchange";
    public static final String CONSULTATION_EXPIRATION_ROUTING_KEY = "consultation.expiration";

    public static final String CONSULTATION_EXPIRATION_DLQ = "consultation.expiration.dlq";
    public static final String CONSULTATION_EXPIRATION_DLQ_ROUTING_KEY = "consultation.expiration.dlq";

    public static final String CONSULTATION_EXPIRATION_RETRY_QUEUE = "consultation.expiration.retry";
    public static final String CONSULTATION_EXPIRATION_RETRY_ROUTING_KEY = "consultation.expiration.retry";

    public static final String CONSULTATION_PREPARATION_EXCHANGE = "consultation.preparation.exchange";
    public static final String CONSULTATION_PREPARATION_RETRY_EXCHANGE = "consultation.preparation.retry.exchange";

    public static final String CONSULTATION_PREPARATION_QUEUE = "consultation.preparation.queue";
    public static final String CONSULTATION_PREPARATION_RETRY_QUEUE = "consultation.preparation.retry.queue";
    public static final String CONSULTATION_PREPARATION_DLQ_QUEUE = "consultation.preparation.dlq.queue";

    public static final String CONSULTATION_PREPARATION_ROUTING_KEY = "consultation.preparation";
}
