package com.healthsync.healthsync.service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmergencySmsService {

    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    @Value("${twilio.whatsapp.number:whatsapp:+14155238886}")
    private String twilioWhatsappNumber;

    /** Full emergency alert — sent immediately on scan */
    public void sendEmergencySMS(String to, String patientName, String location) {

        String locationText = (location != null && !location.isBlank())
                ? "📍 Location: " + location
                : "📍 Location: Being retrieved...";

        String text =
                "🚨 *HEALTHSYNC EMERGENCY ALERT* 🚨\n\n" +
                "Patient *" + patientName + "* needs immediate help!\n\n" +
                locationText + "\n\n" +
                "Please respond immediately.";

        send(to, text);
    }

    /** Raw message — used for location follow-up */
    public void sendRawMessage(String to, String message) {
        send(to, message);
    }

    private void send(String to, String text) {
        String formattedTo = formatPhone(to);
        if (formattedTo == null) {
            System.out.println("❌ Invalid phone: " + to);
            return;
        }

        try {
            Twilio.init(accountSid, authToken);

            Message message = Message.creator(
                    new PhoneNumber("whatsapp:" + formattedTo),
                    new PhoneNumber(twilioWhatsappNumber),
                    text
            ).create();

            System.out.println("✅ WhatsApp sent to: " + formattedTo + " | SID: " + message.getSid());

        } catch (Exception e) {
            System.out.println("❌ Failed to send to: " + formattedTo);
            e.printStackTrace();
        }
    }

    /**
     * Normalises Indian numbers to E.164 (+91XXXXXXXXXX)
     */
    private String formatPhone(String raw) {
        if (raw == null || raw.isBlank()) return null;
        String c = raw.trim().replaceAll("[\\s\\-()]", "");
        if (c.startsWith("+91") && c.length() == 13) return c;
        if (c.startsWith("91")  && c.length() == 12) return "+" + c;
        if (!c.startsWith("+")  && c.length() == 10) return "+91" + c;
        if (c.startsWith("+")   && c.length() >= 10) return c;
        System.out.println("⚠️ Unrecognised phone format: " + c);
        return c;
    }
}