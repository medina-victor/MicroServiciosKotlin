package cl.pagoflex.compromisos.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;

public record AccesoPropietario(boolean privilegiado, String rol, Long usuarioId, Long empresaId) {
    public static AccesoPropietario desde(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccesoNoAutorizadoException();
        }
        if (authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")
                        || authority.getAuthority().equals("ROLE_SERVICE"))) {
            return new AccesoPropietario(true, null, null, null);
        }
        if (!(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new AccesoNoAutorizadoException();
        }

        String rol = jwt.getClaimAsString("rol");
        Long usuarioId = numeroPositivo(jwt.getClaims().get("usuarioId"));
        Long empresaId = numeroPositivo(jwt.getClaims().get("empresaId"));
        if (usuarioId == null || ("EMPRESA".equals(rol) && empresaId == null)
                || (!"CLIENTE".equals(rol) && !"EMPRESA".equals(rol))) {
            throw new AccesoNoAutorizadoException();
        }
        return new AccesoPropietario(false, rol, usuarioId, empresaId);
    }

    public static AccesoPropietario admin() {
        return new AccesoPropietario(true, null, null, null);
    }

    public boolean esCliente() {
        return "CLIENTE".equals(rol);
    }

    public boolean esEmpresa() {
        return "EMPRESA".equals(rol);
    }

    public boolean permite(Long propietarioUsuarioId, Long propietarioEmpresaId) {
        return privilegiado
                || (esCliente() && usuarioId.equals(propietarioUsuarioId))
                || (esEmpresa() && empresaId.equals(propietarioEmpresaId));
    }

    public void exigirPropietario(Long propietarioUsuarioId, Long propietarioEmpresaId) {
        if (!permite(propietarioUsuarioId, propietarioEmpresaId)) {
            throw new AccesoNoAutorizadoException();
        }
    }

    private static Long numeroPositivo(Object valor) {
        if (valor instanceof Number number && number.longValue() > 0) {
            return number.longValue();
        }
        return null;
    }
}
