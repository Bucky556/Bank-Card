package code.uz.bankcard.service.Impl;

import code.uz.bankcard.config.CustomUserDetails;
import code.uz.bankcard.dto.AppResponse;
import code.uz.bankcard.dto.auth.AuthDTO;
import code.uz.bankcard.dto.auth.ProfileResponseDTO;
import code.uz.bankcard.dto.auth.RegisterDTO;
import code.uz.bankcard.entity.ProfileEntity;
import code.uz.bankcard.entity.RoleEntity;
import code.uz.bankcard.enums.Role;
import code.uz.bankcard.exception.BadException;
import code.uz.bankcard.repository.ProfileRepository;
import code.uz.bankcard.repository.RoleRepository;
import code.uz.bankcard.service.AuthService;
import code.uz.bankcard.util.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service implementation for authentication and registration.
 * <p>
 * This class handles:
 * <ul>
 *     <li>User registration with role assignment</li>
 *     <li>User login and JWT token generation</li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final ProfileRepository profileRepository;
    private final RoleServiceImpl roleService;
    private final AuthenticationManager authenticationManager;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registers a new user profile with ROLE_USER.
     *
     * @param dto RegisterDTO containing name, username, and password
     * @return AppResponse with success message
     * @throws BadException if the username already exists
     */
    public AppResponse<String> register(@Valid RegisterDTO dto) {
        Optional<ProfileEntity> byUsername = profileRepository.findByUsernameAndVisibleTrue(dto.getUsername());
        if (byUsername.isPresent()) {
            throw new BadException("Username already exists");
        }

        ProfileEntity profileEntity = new ProfileEntity();
        profileEntity.setName(dto.getName());
        profileEntity.setUsername(dto.getUsername());
        profileEntity.setPassword(passwordEncoder.encode(dto.getPassword()));
        profileEntity.setCreatedDate(LocalDateTime.now());
        profileRepository.save(profileEntity);

        // Assign default ROLE_USER to the new profile
        roleService.create(profileEntity.getId(), List.of(Role.ROLE_USER));

        return new AppResponse<>("Registered Successfully");
    }

    /**
     * Authenticates a user and returns a login response with JWT access token.
     *
     * @param dto AuthDTO containing username and password
     * @return ProfileResponseDTO containing user info and JWT token
     * @throws BadException if username or password is invalid
     */
    public ProfileResponseDTO login(@Valid AuthDTO dto) {
        try {
            Authentication authenticate = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword())
            );

            if (authenticate.isAuthenticated()) {
                CustomUserDetails principal = (CustomUserDetails) authenticate.getPrincipal();
                return getLoginResponse(principal.getId(), principal.getUsername(), principal.getName());
            }
        } catch (Exception e) {
            throw new BadException("Invalid username or password");
        }
        throw new BadException("Invalid username or password");
    }

    /**
     * Builds the ProfileResponseDTO for login response.
     *
     * @param profileId User's profile ID
     * @param username User's username
     * @param name User's display name
     * @return ProfileResponseDTO with roles and JWT token
     */
    private ProfileResponseDTO getLoginResponse(UUID profileId, String username, String name) {
        List<RoleEntity> allRoleByProfileId = roleRepository.findAllByProfileId(profileId);
        List<Role> roleList = allRoleByProfileId.stream()
                .map(RoleEntity::getRole)
                .toList();

        // build response
        ProfileResponseDTO responseDTO = new ProfileResponseDTO();
        responseDTO.setName(name);
        responseDTO.setUsername(username);
        responseDTO.setRoles(roleList);
        responseDTO.setAccessToken(JwtUtil.encode(username, profileId, roleList));

        return responseDTO;
    }
}
