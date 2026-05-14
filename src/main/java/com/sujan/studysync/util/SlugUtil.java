package com.sujan.studysync.util;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.UUID;

@Component
public class SlugUtil {
    /**
     * "Java Champions" → "java-champions-a3f9k2"
     * Unique enough that we don't need a DB check every time.
     */
    public String generateSlug(String name){
        String base = Normalizer.normalize(name, Normalizer.Form.NFD)
                .replaceAll("[^\\p{ASCII}]", "")  // remove accented chars
                .toLowerCase()
                .trim()
                .replaceAll("[^a-z0-9\\s]", "")   // keep letters, numbers, spaces
                .replaceAll("\\s+", "-")           // spaces → hyphens
                .replaceAll("-+", "-");             // multiple hyphens → one

        // 6-char random suffix for uniqueness
        String suffix = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6);

        return base + "-" + suffix;
    }

    /**
     * Generates a 10-char uppercase invite code
     * e.g. "K3F8NQP2YA"
     */
    public String generateInviteCode() {
        return UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 10)
                .toUpperCase();
    }
}

