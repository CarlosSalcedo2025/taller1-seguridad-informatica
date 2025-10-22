import com.example.taller1.auth.security.JwtUtil;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/auth")
@Validated
public class AuthController {
    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final JwtUtil jwtUtil; // ✅ nuevo

    public AuthController(UserRepository repo, PasswordEncoder encoder, JwtUtil jwtUtil) {
        this.repo = repo;
        this.encoder = encoder;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        var userOpt = repo.findByEmail(body.get("email"));
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(new Msg("Usuario no encontrado"));
        }

        var user = userOpt.get();
        if (!encoder.matches(body.get("password"), user.getPasswordHash())) {
            return ResponseEntity.status(401).body(new Msg("Contraseña incorrecta"));
        }

        var claims = Map.of("roles", user.getRoles());
        String token = jwtUtil.generateToken(user.getEmail(), claims);

        ResponseCookie cookie = ResponseCookie.from("JWT", token)
                .httpOnly(true)
                .path("/")
                .maxAge(3600)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new Msg("Login exitoso"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("JWT", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header("Set-Cookie", cookie.toString())
                .body(new Msg("Logout exitoso"));
    }
}
