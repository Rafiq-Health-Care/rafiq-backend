package com.nexaworks.rafiq.rabbit.constant;

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

    public static final String EMAIL_RETRY_QUEUE = "notification.email.retry";
    public static final String SMS_RETRY_QUEUE = "notification.sms.retry";
    public static final String PUSH_RETRY_QUEUE = "notification.push.retry";
    public static final String EMAIL_RETRY_ROUTING_KEY = "notification.email.retry";
    public static final String SMS_RETRY_ROUTING_KEY = "notification.sms.retry";
    public static final String PUSH_RETRY_ROUTING_KEY = "notification.push.retry";
    public static final String NOTIFICATION_RETRY_EXCHANGE = "notification.retry.exchange";

    public static final String NOTIFICATION_DLQ_EXCHANGE = "notification.dlq.exchange";

    public static final String REFUND_REQUEST_EXCHANGE = "refund.request.exchange";
    public static final String REFUND_REQUEST_QUEUE = "refund.request.queue";
    public static final String REFUND_REQUEST_ROUTING_KEY = "refund.request";
    public static final String REFUND_REQUEST_DLQ_ROUTING_KEY = "refund.request.dlq";
    public static final String REFUND_REQUEST_DLQ_QUEUE = "refund.request.dlq.queue";
    public static final String REFUND_REQUEST_DLQ_EXCHANGE = "refund.request.dlq.exchange";

    public static final String OTP_QUEUE = "notification.otp";
    public static final String OTP_ROUTING_KEY = "notification.otp";
}
