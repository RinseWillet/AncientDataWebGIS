package com.webgis.ancientdata.utils;

import io.jsonwebtoken.Jwts;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JwtKeyGenerator {
    private static final Logger logger = Logger.getLogger(JwtKeyGenerator.class.getName());

    public static void main(String[] args) {
        SecretKey key = Jwts.SIG.HS256.key().build();
        String secret = Base64.getEncoder().encodeToString(key.getEncoded());
        logger.log(Level.INFO, "Generated Secret Key: {0}", secret);
    }
}
