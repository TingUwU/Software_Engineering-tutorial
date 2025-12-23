package Team5.example.breakfast_ordering.security;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import Team5.example.breakfast_ordering.repository.UserRepository;
import Team5.example.breakfast_ordering.model.User;
import Team5.example.breakfast_ordering.model.User.AuthProvider;

import java.util.Map;
import java.util.Optional;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    // Google login API: http://localhost:8088/oauth2/authorization/google
    // Facebook login API: http://localhost:8088/oauth2/authorization/facebook

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
        String email = null;
        String name = null;
        String photo = null;
        String providerId = null;

        if("google".equals(provider)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub"); // Google 的 ID 叫 sub
            photo = oAuth2User.getAttribute("picture");
        }
        else if("facebook".equals(provider)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("id"); // Facebook 的 ID 叫 id

            if (oAuth2User.getAttributes().containsKey("picture")) {
                Map<String, Object> pictureObj = (Map<String, Object>) oAuth2User.getAttribute("picture");
                if (pictureObj.containsKey("data")) {
                    Map<String, Object> dataObj = (Map<String, Object>) pictureObj.get("data");
                    if (dataObj.containsKey("url")) {
                        photo = (String) dataObj.get("url");
                    }
                }
            }

            if(photo == null) {   // 如果上面沒抓到再自己組
                photo = "https://graph.facebook.com/" + providerId + "/picture?type=large";
            }
        }

        if(email == null || email.isEmpty()) {
            throw new RuntimeException("無法從第三方帳號取得 Email，請確認您的帳號設定。");
        }
        
        AuthProvider authProviderEnum = AuthProvider.valueOf(provider.toUpperCase());
        Optional<User> userOptional = userRepository.findByEmailAndProvider(email, authProviderEnum);

        User user;

        if(userOptional.isPresent()) {
            user = userOptional.get();
            user = updateExistingUser(user, name, photo);
        }
        else{
            user = registerNewUser(provider, email, name, photo, providerId);
        }

        return oAuth2User;
    }

    private User registerNewUser(String provider, String email, String name, String photo, String providerId) {
        User user = new User();
        user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setProviderId(providerId);
        user.setAccount(email); // 用 Email 當帳號
        user.setEmail(email);
        user.setNickname(name);
        user.setPhoto(photo);
        
        user.setRole("buyer");   // 預設為買家，待更改
        
        return userRepository.save(user);
    }

    private User updateExistingUser(User existingUser, String name, String photo) {
        existingUser.setNickname(name);
        existingUser.setPhoto(photo);
        return userRepository.save(existingUser);
    }
}