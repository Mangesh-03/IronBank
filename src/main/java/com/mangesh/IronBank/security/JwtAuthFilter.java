package com.mangesh.IronBank.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter
{
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException
    {

        // Step 1: Get "Authorization" header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check if header exists and starts with "Bearer "
        //         if not → skip everything, call filterChain.doFilter() and return
        if( !(authHeader != null && authHeader.startsWith("Bearer")) )
        {
            filterChain.doFilter(request, response);
            return;
        }

        // Step 3: Extract token (remove "Bearer " prefix - 7 characters)

        authHeader = authHeader.replace("Bearer ","");

        // Step 4: Extract username from token using jwtTokenProvider
        String userName = jwtTokenProvider.extractName(authHeader);

        // Step 5: Check if username exists AND user not already authenticated
        //         (SecurityContextHolder.getContext().getAuthentication() == null)
        if(userName != null && SecurityContextHolder.getContext().getAuthentication() == null )
        {
            // Step 6: Load user details from database using UserDetailsService
            UserDetails userDetails = userDetailsService.loadUserByUsername(userName);

            // Step 7: Validate token using jwtTokenProvider
//            boolean isValid = jwtTokenProvider.validateToken(authHeader);

            // Step 8: If valid → create UsernamePasswordAuthenticationToken
            //         and set it in SecurityContextHolder

            if(jwtTokenProvider.validateToken(authHeader))
            {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        // Step 9: filterChain.doFilter(request, response)
        filterChain.doFilter(request, response);
    }
}
