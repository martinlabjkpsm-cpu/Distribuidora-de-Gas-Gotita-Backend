package cl.duoc.backend.seguridad;

import cl.duoc.backend.modelo.Usuario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

/** Emite el JWT de acceso. Claims: sub (RUT), iss, aud, iat, exp y rol. Sin contraseñas ni datos sensibles. */
@Service
public class TokenService {

    private final JwtEncoder encoder;
    private final String emisor;
    private final String destinatario;
    private final long minutos;

    public TokenService(JwtEncoder encoder,
                        @Value("${gotita.jwt.emisor}") String emisor,
                        @Value("${gotita.jwt.destinatario}") String destinatario,
                        @Value("${gotita.jwt.minutos}") long minutos) {
        this.encoder = encoder;
        this.emisor = emisor;
        this.destinatario = destinatario;
        this.minutos = minutos;
    }

    public String emitir(Usuario usuario) {
        Instant ahora = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(emisor)
                .audience(List.of(destinatario))
                .subject(usuario.getRut())
                .issuedAt(ahora)
                .expiresAt(ahora.plus(minutos, ChronoUnit.MINUTES))
                .claim("rol", usuario.getRol().name())
                .build();
        JwsHeader cabecera = JwsHeader.with(MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(cabecera, claims)).getTokenValue();
    }

    public long vigenciaEnSegundos() {
        return minutos * 60;
    }
}
