package company.bankingsoftware.paymenttracker.model;

/**
 * Represents couple of payment event type and payment for inputs.
 */
public class PaymentEvent {

    public enum PaymentEventType { ADD, OUTPUT, SHUTDOWN }

    private final PaymentEventType paymentEventType;
    private final Payment payment;

    private PaymentEvent(Builder b) {
        this.paymentEventType = b.paymentEventType;
        this.payment = b.payment;
    }

    public PaymentEventType getPaymentEventType() {
        return paymentEventType;
    }

    public Payment getPayment() {
        return payment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private PaymentEventType paymentEventType;
        private Payment payment;

        private Builder() {}

        public Builder withPaymentEventType(PaymentEventType paymentEventType) {
            this.paymentEventType = paymentEventType;
            return this;
        }

        public Builder withPayment(Payment payment) {
            this.payment = payment;
            return this;
        }

        public PaymentEvent build() {
            return new PaymentEvent(this);
        }
    }}
