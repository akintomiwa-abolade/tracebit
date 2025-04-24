package dev.io.tracebit.util;

import java.net.InetAddress;
import java.util.regex.Pattern;

public class ValidatorUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");
    private static final Pattern USER_ID_PATTERN = Pattern.compile("user_\\d+");
    private static final Pattern IPV4_PATTERN = Pattern.compile(
            "^((25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(\\.|$)){4}$"
    );

    private ValidatorUtil() {}

    public static void validateUserId(String userId) {
        if (userId == null || (!EMAIL_PATTERN.matcher(userId).matches() && !USER_ID_PATTERN.matcher(userId).matches())) {
            throw new IllegalArgumentException("Invalid user ID format: must be email or user_123");
        }
    }

    public static void validateIp(String ip) {
        if (ip == null || !IPV4_PATTERN.matcher(ip).matches()) {
            throw new IllegalArgumentException("Invalid IP address format");
        }

        try {
            InetAddress inet = InetAddress.getByName(ip);
            if (inet.isAnyLocalAddress() || inet.isLoopbackAddress() || inet.isSiteLocalAddress()) {
                throw new IllegalArgumentException("Private or local IPs are not allowed");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid IP address");
        }
    }

    public static void validateDevice(String device) {
        if (device == null || device.length() < 5) {
            throw new IllegalArgumentException("Invalid or incomplete device info");
        }
    }
}

