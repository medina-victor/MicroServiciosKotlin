package cl.pagoflex.usuarios.exception;

public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(Long id) {
        super("Usuario no encontrado con ID " + id);
    }

    public UsuarioNoEncontradoException(String correo) {
        super("Usuario no encontrado con correo " + correo);
    }
}
