package code.uz.bankcard.config;

import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.entity.RoleEntity;
import code.uz.bankcard.repository.ProfileRepository;
import code.uz.bankcard.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final ProfileRepository profileRepository;
    private final RoleRepository roleRepository;


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ProfileEntity profileEntity = profileRepository.findByUsernameAndVisibleTrue(username)
                .orElseThrow(() -> new UsernameNotFoundException("Username not found"));
        List<RoleEntity> roleList = roleRepository.findAllByProfileId(profileEntity.getId());

        return new CustomUserDetails(profileEntity, roleList);
    }
}
