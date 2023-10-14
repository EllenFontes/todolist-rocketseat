package com.rocketseat.todolist.filter;


import at.favre.lib.crypto.bcrypt.BCrypt;
import com.rocketseat.todolist.user.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Base64;

//Faz um filtro na requisição antes de ir para o controller

@Component
public class FilterTaskAuth extends OncePerRequestFilter {


    @Autowired
    private UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var serveletPath = request.getServletPath();

        if (serveletPath.startsWith("/tasks/")) {

            //Pega o header criptografado da requisição (login e senha recebidos)
            var authorization = request.getHeader("Authorization");
            //Remove a palavra basic que vem junto com o header
            var authEncoded = authorization.substring("Basic".length()).trim();

            //Descriptografa os dados e os coloca em uma variável, em seguida dividindo email e senha em um array
            byte[] authDecode = Base64.getDecoder().decode(authEncoded);
            var authDecodeString = new String(authDecode);

            String[] credentials = authDecodeString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            System.out.println("Authorization");
            System.out.println(username);
            System.out.println(password);


            //Validar se o usuário existe
            var user = userRepository.findByUsername(username);

            if (user == null) {
                response.sendError(401, "Usuário não cadastrado");
            } else {

                //validar senha

                var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());
                if (passwordVerify.verified == false) {
                    response.sendError(401, "Senha incorreta");
                }

                //Setta o ID do usuário para passar na request para o controller
                request.setAttribute("idUser", user.getIdUser());
                filterChain.doFilter(request, response);
            }

        } else {

            filterChain.doFilter(request, response);

        }


    }
}
