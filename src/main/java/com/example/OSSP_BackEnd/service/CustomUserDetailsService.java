package com.example.OSSP_BackEnd.service;

import com.example.OSSP_BackEnd.entity.User;
import com.example.OSSP_BackEnd.repository.UserRepository;
import com.example.OSSP_BackEnd.security.CustomUserDetails; // CustomUserDetails import 추가
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security가 인증을 위해 호출하는 메소드입니다.
     * 사용자 이름(여기서는 이메일)을 기반으로 데이터베이스에서 사용자 정보를 조회합니다.
     *
     * @param email 사용자가 로그인 시 입력한 이메일
     * @return UserDetails를 구현한 CustomUserDetails 객체
     * @throws UsernameNotFoundException 해당 이메일을 가진 사용자를 찾을 수 없을 때 발생
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당하는 유저를 찾을 수 없습니다. : " + email));
        return new CustomUserDetails(user); // User 엔티티를 CustomUserDetails로 래핑하여 반환
    }
}