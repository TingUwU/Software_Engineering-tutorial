package Team5.example.breakfast_ordering.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import Team5.example.breakfast_ordering.repository.UserRepository;
import Team5.example.breakfast_ordering.model.User;
import Team5.example.breakfast_ordering.model.User.AuthProvider;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // Google login API: http://localhost:8088/oauth2/authorization/google

    @Autowired
    private UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 判斷是 Google 還是 FB
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        
        return processOAuth2User(registrationId, oAuth2User);
    }

    private OAuth2User processOAuth2User(String provider, OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        
        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            AuthProvider currentProvider = user.getProvider();
            if (currentProvider == null) {
                currentProvider = AuthProvider.LOCAL; // 舊資料預設視為一般帳號
            }

            if (!currentProvider.equals(AuthProvider.valueOf(provider.toUpperCase()))) {
                throw new RuntimeException("帳號已存在，請使用原本的方式登入");
            }
            user = updateExistingUser(user, oAuth2User);
        } else {
            user = registerNewUser(provider, oAuth2User);
        }

        return oAuth2User;
    }

    private User registerNewUser(String provider, OAuth2User oAuth2User) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setProviderId(oAuth2User.getAttribute("sub")); // Google 的 ID
        user.setAccount(oAuth2User.getAttribute("email"));  // 用 Email 當帳號
        user.setEmail(oAuth2User.getAttribute("email"));
        user.setNickname(oAuth2User.getAttribute("name"));
        user.setPhoto(oAuth2User.getAttribute("picture"));
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, OAuth2User oAuth2User) {
        existingUser.setNickname(oAuth2User.getAttribute("name"));
        existingUser.setPhoto(oAuth2User.getAttribute("picture"));
        return userRepository.save(existingUser);
    }
}