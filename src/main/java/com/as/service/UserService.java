package com.as.service;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.as.entity.User;
import com.as.entity.UserDto;
import com.as.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    @Autowired // Springが自動的にUserRepositoryの実装を注入します
    private UserRepository userRepository;

    @Autowired // Springが自動的にPasswordEncoderの実装を注入します
    private PasswordEncoder passwordEncoder;
    
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(userName); // ユーザー名でユーザーを検索します
        if (user == null) {
            throw new UsernameNotFoundException("User not found"); // ユーザーが見つからない場合、例外をスローします
        }
        return new UserPrincipal(user); // ユーザーが見つかった場合、UserPrincipalを作成し返します

    }

    //新たにメソッドを追加します
    public User findByUsername(String userName) {
        return userRepository.findByUserName(userName); // ユーザー名でユーザーを検索し返します
    }
    
    @Transactional // トランザクションを開始します。メソッドが終了したらトランザクションがコミットされます。
    public void save(UserDto userDto) {
        // UserDtoからUserへの変換
        User user = new User();
        user.setUserName(userDto.getUserName());
        // パスワードをハッシュ化してから保存
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));

        // データベースへの保存
        userRepository.save(user); // UserRepositoryを使ってユーザーをデータベースに保存します
    }
}
