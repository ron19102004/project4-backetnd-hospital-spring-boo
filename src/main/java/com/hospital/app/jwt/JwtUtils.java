package com.hospital.app.jwt;

import com.hospital.app.dto.auth.TokenResponse;
import com.hospital.app.entities.account.User;
import com.hospital.app.exception.ServiceException;
import com.hospital.app.utils.VietNamTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.function.Consumer;


@Component
public class JwtUtils {
    @Autowired
    private JwtEncoder jwtAccessTokenEncoder;
    @Autowired
    @Qualifier("jwtRefreshTokenEncoder")
    private JwtEncoder jwtRefreshTokenEncoder;
    @Autowired
    private JwtDecoder jwtAccessTokenDecoder;

    public String encodeToken(final TokenDTO tokenDTO, final long amountToAdd) {

        Instant instant = VietNamTime.instantNow();
        JwtClaimsSet.Builder claimsSetBuilder = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(amountToAdd, ChronoUnit.MINUTES))
                .subject(tokenDTO.subject());
        tokenDTO.claims().forEach(claimsSetBuilder::claim);
        JwtClaimsSet claimsSet = claimsSetBuilder.build();
        return jwtAccessTokenEncoder
                .encode(JwtEncoderParameters.from(claimsSet))
                .getTokenValue();
    }

    public TokenDTO decodeToken(final String token) {
        Jwt jwt = jwtAccessTokenDecoder.decode(token);
        String subject = jwt.getSubject();
        Map<String, Object> claims = jwt.getClaims();
        return new TokenDTO(subject, claims);
    }

    private Jwt jwtAccessToken(final Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Instant instant = VietNamTime.instantNow();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(15, ChronoUnit.DAYS))
                .subject(user.getId().toString())
                .build();
        return jwtAccessTokenEncoder.encode(JwtEncoderParameters.from(claimsSet));
    }

    private Jwt jwtRefreshToken(final Authentication authentication) {
        User user = (User) authentication.getPrincipal();

        Instant instant = VietNamTime.instantNow();

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuedAt(instant)
                .expiresAt(instant.plus(30, ChronoUnit.DAYS))
                .subject(user.getId().toString())
                .build();
        return jwtRefreshTokenEncoder.encode(JwtEncoderParameters.from(claimsSet));
    }

    public JwtCreateTokenDTO createToken(final Authentication authentication) {
        if (!(authentication.getPrincipal() instanceof User user)) {
            throw new BadCredentialsException(
                    MessageFormat.format("principal {0} is not of User type",
                            authentication.getPrincipal().getClass()));
        }
        String refreshToken;
        if (authentication.getCredentials() instanceof Jwt jwt) {
            Instant instant = VietNamTime.instantNow();
            Instant expiresAt = jwt.getExpiresAt();
            Duration duration = Duration.between(instant, expiresAt);
            long daysUntilExpired = duration.toDays();
            if (daysUntilExpired < 3) {
                refreshToken = jwtRefreshToken(authentication).getTokenValue();
            } else {
                refreshToken = jwt.getTokenValue();
            }
        } else {
            refreshToken = jwtRefreshToken(authentication).getTokenValue();
        }
        Jwt jwtAccessToken = jwtAccessToken(authentication);
        return new JwtCreateTokenDTO(jwtAccessToken, refreshToken, user.getId());
    }
}
