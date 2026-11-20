package cl.pagoflex.autenticacion.security;

import cl.pagoflex.autenticacion.model.Credencial;
import cl.pagoflex.autenticacion.repository.CredencialRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CredencialUserDetailsService implements UserDetailsService {

    private final CredencialRepository credencialRepository;

    public CredencialUserDetailsService(CredencialRepository credencialRepository) {
        this.credencialRepository = credencialRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        Credencial credencial = credencialRepository.findByCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException("Credencial no encontrada"));
        return new User(credencial.getCorreo(), credencial.getPasswordHash(),
                credencial.isActivo(), true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}
