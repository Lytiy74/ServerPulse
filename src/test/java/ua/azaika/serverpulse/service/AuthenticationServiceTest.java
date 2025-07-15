package ua.azaika.serverpulse.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import ua.azaika.serverpulse.dto.auth.JwtAuthenticationResponseDTO;
import ua.azaika.serverpulse.dto.auth.SignInRequestDTO;
import ua.azaika.serverpulse.dto.auth.SignUpRequestDTO;
import ua.azaika.serverpulse.dto.auth.UserResponseDTO;
import ua.azaika.serverpulse.entity.Role;
import ua.azaika.serverpulse.entity.UserEntity;
import ua.azaika.serverpulse.exception.UserAlreadyExistsException;
import ua.azaika.serverpulse.security.CustomUserDetails;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 @author Andrii Zaika
 **/
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private SignUpRequestDTO signUpRequestDTO;
    private SignInRequestDTO signInRequestDTO;
    private UserEntity userEntity;
    private CustomUserDetails userDetails;
    private UserResponseDTO userResponseDTO;

    @BeforeEach
    void setUp() {
        signUpRequestDTO = new SignUpRequestDTO("testUser", "test@example.com", "password");
        signInRequestDTO = new SignInRequestDTO("testUser", "password");
        userResponseDTO = new UserResponseDTO("test", "test@email.com", List.of(Role.USER));

        userEntity = UserEntity.builder()
                .id(UUID.randomUUID())
                .username("testUser")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(List.of(Role.USER))
                .createdAt(LocalDateTime.now())
                .build();

        userDetails = new CustomUserDetails(userEntity);
    }


    @Nested
    class SignUpTest {
        @Test
        void should_register_and_return_jwt_token_when_sign_up_is_successful() {
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            when(userService.create(any(UserEntity.class))).thenReturn(userResponseDTO);
            when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("jwtToken");

            JwtAuthenticationResponseDTO response = authenticationService.signUp(signUpRequestDTO);

            verify(passwordEncoder, times(1)).encode(signUpRequestDTO.password());
            verify(userService, times(1)).create(any(UserEntity.class));
            verify(jwtService, times(1)).generateToken(any(CustomUserDetails.class));

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("jwtToken");

        }

        @Test
        void should_throw_if_user_already_exists() {
            when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
            doThrow(new UserAlreadyExistsException("User already exists")).when(userService).create(any(UserEntity.class));

            assertThrows(UserAlreadyExistsException.class, () -> authenticationService.signUp(signUpRequestDTO));

            verify(passwordEncoder, times(1)).encode(signUpRequestDTO.password());
            verify(userService, times(1)).create(any(UserEntity.class));
            verify(jwtService, never()).generateToken(any(CustomUserDetails.class));
        }
    }

    @Nested
    class SignInTest {
        @Test
        void should_authenticate_user_and_generate_token_when_credentials_are_valid() {
            Authentication authentication = mock(Authentication.class);
            when(authentication.getPrincipal()).thenReturn(userDetails);
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

            when(jwtService.generateToken(any(CustomUserDetails.class))).thenReturn("mockedJwtToken");

            JwtAuthenticationResponseDTO response = authenticationService.signIn(signInRequestDTO);

            verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
            verify(jwtService, times(1)).generateToken(any(CustomUserDetails.class));

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("mockedJwtToken");
        }

        @Test
        void should_throw_BadCredentialsException_when_credentials_are_invalid() {
            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Invalid credentials"));

            assertThrows(BadCredentialsException.class, () -> authenticationService.signIn(signInRequestDTO));

            verify(jwtService, never()).generateToken(any(UserDetails.class));
        }
    }
}