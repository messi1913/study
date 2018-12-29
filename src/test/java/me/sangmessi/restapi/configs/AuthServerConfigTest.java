package me.sangmessi.restapi.configs;

import me.sangmessi.restapi.accounts.Account;
import me.sangmessi.restapi.accounts.AccountRole;
import me.sangmessi.restapi.accounts.AccountService;
import me.sangmessi.restapi.common.BaseControllerTest;
import me.sangmessi.restapi.commons.TestDescription;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthServerConfigTest extends BaseControllerTest {

    @Autowired
    AccountService accountService;

    @Test
    @TestDescription("인증 토큰을 발급 받는 서비스 ")
    public void authToken() throws Exception {
        String clientId = "MyApp";
        String password = "pass";

        String username = "sangmessi@test.com";
        String userPassword = "sangmessi";
        Account sangmessi = Account.builder()
                .email(username)
                .password(userPassword)
                .roles(Set.of(AccountRole.ADMIN, AccountRole.USER))
                .build();
        this.accountService.saveAccount(sangmessi);

        this.mockMvc.perform(post("/oauth/token")
                .with(httpBasic(clientId, password))
                .param("username", username)
                .param("password", userPassword)
                .param("grant_type", "password")
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("access_token").exists())
                ;
    }

}