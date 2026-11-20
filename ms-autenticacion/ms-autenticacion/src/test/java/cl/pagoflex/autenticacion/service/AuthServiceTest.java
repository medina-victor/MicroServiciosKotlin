package cl.pagoflex.autenticacion.service;

import cl.pagoflex.autenticacion.client.UsuarioCliente;
import cl.pagoflex.autenticacion.dto.LoginRequestDTO;
import cl.pagoflex.autenticacion.dto.RegistroRequestDTO;
import cl.pagoflex.autenticacion.exception.CredencialDuplicadaException;
import cl.pagoflex.autenticacion.exception.CredencialesInvalidasException;
import cl.pagoflex.autenticacion.exception.UsuarioInactivoException;
import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.repository.CredencialRepository;
import cl.pagoflex.autenticacion.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private CredencialRepository repository;
    @Mock
    private UsuarioCliente usuarioCliente;
    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder;
    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        service = new AuthServiceImpl(repository, usuarioCliente, passwordEncoder, jwtService);
    }

    @Test
    void registraUsuarioActivoCifrandoPassword() {
        RegistroRequestDTO request = registro();
        when(repository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(repository.existsByUsuarioId(1L)).thenReturn(false);
        when(usuarioCliente.obtenerPorId(1L))
                .thenReturn(new UsuarioCliente.UsuarioExterno(1L, "cliente@pagoflex.cl", "CLIENTE", true));
        when(repository.save(any(Credencial.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Credencial credencial = service.registrar(request);

        assertThat(credencial.getPasswordHash()).startsWith("$2");
        assertThat(passwordEncoder.matches("Secreto123", credencial.getPasswordHash())).isTrue();
        assertThat(credencial.isActivo()).isTrue();
    }

    @Test
    void rechazaDuplicados() {
        RegistroRequestDTO request = registro();
        when(repository.existsByCorreo(request.getCorreo())).thenReturn(true);

        assertThatThrownBy(() -> service.registrar(request)).isInstanceOf(CredencialDuplicadaException.class);
    }

    @Test
    void rechazaUsuarioInactivo() {
        RegistroRequestDTO request = registro();
        when(repository.existsByCorreo(request.getCorreo())).thenReturn(false);
        when(repository.existsByUsuarioId(1L)).thenReturn(false);
        when(usuarioCliente.obtenerPorId(1L))
                .thenReturn(new UsuarioCliente.UsuarioExterno(1L, "cliente@pagoflex.cl", "CLIENTE", false));

        assertThatThrownBy(() -> service.registrar(request)).isInstanceOf(UsuarioInactivoException.class);
    }

    @Test
    void loginCorrectoGeneraJWT() {
        Credencial credencial = credencial();
        LoginRequestDTO request = new LoginRequestDTO();
        request.setCorreo("cliente@pagoflex.cl");
        request.setPassword("Secreto123");
        when(repository.findByCorreo(request.getCorreo())).thenReturn(Optional.of(credencial));
        when(usuarioCliente.obtenerPorId(1L))
                .thenReturn(new UsuarioCliente.UsuarioExterno(1L, "cliente@pagoflex.cl", "CLIENTE", true));
        when(jwtService.generarToken(1L, "cliente@pagoflex.cl", "CLIENTE")).thenReturn("jwt-prueba");
        when(jwtService.getExpirationMs()).thenReturn(3600000L);

        var response = service.login(request);

        assertThat(response.getAccessToken()).isEqualTo("jwt-prueba");
        assertThat(response.getRol()).isEqualTo("CLIENTE");
    }

    @Test
    void loginIncorrectoRechazaPassword() {
        Credencial credencial = credencial();
        LoginRequestDTO request = new LoginRequestDTO();
        request.setCorreo("cliente@pagoflex.cl");
        request.setPassword("Incorrecta123");
        when(repository.findByCorreo(request.getCorreo())).thenReturn(Optional.of(credencial));

        assertThatThrownBy(() -> service.login(request)).isInstanceOf(CredencialesInvalidasException.class);
    }

    private Credencial credencial() {
        Credencial credencial = new Credencial();
        credencial.setUsuarioId(1L);
        credencial.setCorreo("cliente@pagoflex.cl");
        credencial.setPasswordHash(passwordEncoder.encode("Secreto123"));
        credencial.setActivo(true);
        return credencial;
    }

    private RegistroRequestDTO registro() {
        RegistroRequestDTO request = new RegistroRequestDTO();
        request.setUsuarioId(1L);
        request.setCorreo("cliente@pagoflex.cl");
        request.setPassword("Secreto123");
        return request;
    }
}
