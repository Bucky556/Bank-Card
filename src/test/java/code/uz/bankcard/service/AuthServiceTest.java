package code.uz.bankcard.service;

import code.uz.bankcard.config.CustomUserDetails;
import code.uz.bankcard.dto.auth.AuthDTO;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.dto.auth.RegisterDTO;
import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.entity.RoleEntity;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.repository.ProfileRepository;
import code.uz.bankcard.repository.RoleRepository;
import code.uz.bankcard.service.Impl.AuthServiceImpl;
import code.uz.bankcard.service.Impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private RoleServiceImpl roleService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void register_success() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");
        dto.setPassword("1234");
        dto.setName("Test Name");

        when(profileRepository.findByUsernameAndVisibleTrue("testuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234")).thenReturn("encoded1234");

        // save chaqirilganda ID set qilamiz
        when(profileRepository.save(any(ProfileEntity.class)))
                .thenAnswer(invocation -> {
                    ProfileEntity p = invocation.getArgument(0);
                    p.setId(UUID.randomUUID());
                    return p;
                });

        var response = authService.register(dto);

        ArgumentCaptor<ProfileEntity> captor = ArgumentCaptor.forClass(ProfileEntity.class);
        verify(profileRepository).save(captor.capture());
        ProfileEntity saved = captor.getValue();

        assertEquals("Test Name", saved.getName());
        assertEquals("testuser", saved.getUsername());
        assertEquals("encoded1234", saved.getPassword());
        assertNotNull(saved.getCreatedDate());

        verify(roleService).create(any(UUID.class), eq(List.of(Role.ROLE_USER)));

        assertEquals("Registered Successfully", response.getMessage());
    }


    @Test
    void register_existingUsername_throws() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("testuser");

        when(profileRepository.findByUsernameAndVisibleTrue("testuser"))
                .thenReturn(Optional.of(new ProfileEntity()));

        assertThrows(BadException.class, () -> authService.register(dto));
    }

    @Test
    void login_success() {
        AuthDTO dto = new AuthDTO();
        dto.setUsername("testuser");
        dto.setPassword("1234");

        UUID profileId = UUID.randomUUID();
        String name = "Test Name";

        Authentication authMock = mock(Authentication.class);
        when(authMock.isAuthenticated()).thenReturn(true);

        var userDetails = mock(CustomUserDetails.class);
        when(userDetails.getId()).thenReturn(profileId);
        when(userDetails.getUsername()).thenReturn("testuser");
        when(userDetails.getName()).thenReturn(name);
        when(authMock.getPrincipal()).thenReturn(userDetails);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);

        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRole(Role.ROLE_USER);
        when(roleRepository.findAllByProfileId(profileId))
                .thenReturn(List.of(roleEntity));

        ProfileResponseDTO response = authService.login(dto);

        assertEquals("testuser", response.getUsername());
        assertEquals("Test Name", response.getName());
        assertTrue(response.getRoles().contains(Role.ROLE_USER));
        assertNotNull(response.getAccessToken());
    }

    @Test
    void login_invalidUsernameOrPassword_throws() {
        AuthDTO dto = new AuthDTO();
        dto.setUsername("wronguser");
        dto.setPassword("wrongpass");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThrows(BadException.class, () -> authService.login(dto));
    }
}
