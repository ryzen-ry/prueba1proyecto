package com.redsolidaria.enjambre.ws;

import com.redsolidaria.enjambre.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;

import java.util.Map;

public class SessionUserHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) return true;

        HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
        HttpSession session = httpServletRequest.getSession(false);
        if (session == null) return true;

        // Primero intentamos con valores simples guardados en sesión al login.
        Object usuarioId = session.getAttribute("usuarioId");
        Object usuarioRol = session.getAttribute("usuarioRol");
        if (usuarioId instanceof Number idNum && usuarioRol instanceof String rolStr) {
            attributes.put(AyudaWebSocketHandler.ATTR_USUARIO_ID, idNum.longValue());
            attributes.put(AyudaWebSocketHandler.ATTR_USUARIO_ROL, rolStr);
            return true;
        }

        Object usuarioObj = session.getAttribute("usuario");
        if (usuarioObj instanceof Usuario usuario) {
            attributes.put(AyudaWebSocketHandler.ATTR_USUARIO_ID, usuario.getId());
            attributes.put(AyudaWebSocketHandler.ATTR_USUARIO_ROL, usuario.getRol());
        }

        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}

