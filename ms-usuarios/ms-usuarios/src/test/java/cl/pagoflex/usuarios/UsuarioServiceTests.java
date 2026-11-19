package cl.pagoflex.usuarios;

import cl.pagoflex.usuarios.dto.UsuarioRequest;
import cl.pagoflex.usuarios.dto.UsuarioResponse;
import cl.pagoflex.usuarios.dto.UsuarioRolRequest;
import cl.pagoflex.usuarios.exception.CorreoDuplicadoException;
import cl.pagoflex.usuarios.exception.SolicitudInvalidaException;
import cl.pagoflex.usuarios.exception.UsuarioNoEncontradoException;
import cl.pagoflex.usuarios.model.RolUsuario;
import cl.pagoflex.usuarios.model.Usuario;
import cl.pagoflex.usuarios.repository.UsuarioRepository;
import cl.pagoflex.usuarios.service.UsuarioService;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTests {

	@Mock
	private UsuarioRepository usuarioRepository;

	@InjectMocks
	private UsuarioService usuarioService;

	private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

	@Test
	void registraUsuarioConRolCliente() {
		UsuarioRequest request = new UsuarioRequest("Juan Perez", "juan@demo.cl", "912345678");
		when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
			Usuario usuario = invocation.getArgument(0);
			usuario.setId(1L);
			return usuario;
		});

		UsuarioResponse response = usuarioService.crearUsuario(request);

		assertEquals(RolUsuario.CLIENTE, response.getRol());
		assertTrue(response.getActivo());
		assertEquals(1L, response.getId());
	}

	@Test
	void rechazaCorreoDuplicadoAlCrear() {
		UsuarioRequest request = new UsuarioRequest("Juan Perez", "juan@demo.cl", null);
		when(usuarioRepository.existsByCorreo(request.getCorreo())).thenReturn(true);

		assertThrows(CorreoDuplicadoException.class, () -> usuarioService.crearUsuario(request));
	}

	@Test
	void validaLosDatosDeRegistro() {
		UsuarioRequest request = new UsuarioRequest(" ", "correo-invalido", null);

		assertFalse(VALIDATOR.validate(request).isEmpty());
	}

	@Test
	void listaUsuarios() {
		when(usuarioRepository.findAll()).thenReturn(List.of(usuario(1L, "juan@demo.cl")));

		assertEquals(1, usuarioService.listarUsuarios().size());
	}

	@Test
	void buscaUsuarioPorId() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "juan@demo.cl")));

		assertEquals("juan@demo.cl", usuarioService.buscarPorId(1L).getCorreo());
	}

	@Test
	void fallaAlBuscarIdInexistente() {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.buscarPorId(99L));
	}

	@Test
	void buscaUsuarioPorCorreo() {
		when(usuarioRepository.findByCorreo("juan@demo.cl"))
				.thenReturn(Optional.of(usuario(1L, "juan@demo.cl")));

		assertEquals(1L, usuarioService.buscarPorCorreo("juan@demo.cl").getId());
	}

	@Test
	void actualizaUsuarioConSuMismoCorreo() {
		Usuario existente = usuario(1L, "juan@demo.cl");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(existente));
		when(usuarioRepository.existsByCorreoAndIdNot("juan@demo.cl", 1L)).thenReturn(false);
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UsuarioResponse response = usuarioService.actualizarUsuario(
				1L, new UsuarioRequest("Juan Actualizado", "juan@demo.cl", "912345678"));

		assertEquals("Juan Actualizado", response.getNombre());
		assertEquals("juan@demo.cl", response.getCorreo());
	}

	@Test
	void rechazaActualizacionConCorreoDeOtroUsuario() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "juan@demo.cl")));
		when(usuarioRepository.existsByCorreoAndIdNot("otra@demo.cl", 1L)).thenReturn(true);

		assertThrows(CorreoDuplicadoException.class, () -> usuarioService.actualizarUsuario(
				1L, new UsuarioRequest("Juan", "otra@demo.cl", null)));
	}

	@Test
	void eliminaUsuarioExistente() {
		Usuario usuario = usuario(1L, "juan@demo.cl");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

		usuarioService.eliminarUsuario(1L);

		verify(usuarioRepository).delete(usuario);
	}

	@Test
	void fallaAlEliminarUsuarioInexistente() {
		when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

		assertThrows(UsuarioNoEncontradoException.class, () -> usuarioService.eliminarUsuario(99L));
	}

	@Test
	void desactivaSinEliminar() {
		Usuario usuario = usuario(1L, "juan@demo.cl");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UsuarioResponse response = usuarioService.desactivarUsuario(1L);

		assertFalse(response.getActivo());
		verify(usuarioRepository).save(usuario);
	}

	@Test
	void listaUsuariosPorRol() {
		when(usuarioRepository.findByRol(RolUsuario.EMPRESA))
				.thenReturn(List.of(usuario(1L, "empresa@demo.cl")));

		assertEquals(1, usuarioService.listarPorRol(RolUsuario.EMPRESA).size());
	}

	@Test
	void cambiaRolYAsignaEmpresaId() {
		Usuario usuario = usuario(1L, "empresa@demo.cl");
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
		when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));

		UsuarioResponse response = usuarioService.cambiarRol(
				1L, new UsuarioRolRequest(RolUsuario.EMPRESA, 15L));

		assertEquals(RolUsuario.EMPRESA, response.getRol());
		assertEquals(15L, response.getEmpresaId());
	}

	@Test
	void exigeEmpresaIdAlAsignarRolEmpresa() {
		when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario(1L, "usuario@demo.cl")));

		assertThrows(SolicitudInvalidaException.class,
				() -> usuarioService.cambiarRol(1L, new UsuarioRolRequest(RolUsuario.EMPRESA, null)));
	}

	private Usuario usuario(Long id, String correo) {
		Usuario usuario = new Usuario("Juan Perez", correo, "912345678", RolUsuario.CLIENTE, null);
		usuario.setId(id);
		return usuario;
	}
}
