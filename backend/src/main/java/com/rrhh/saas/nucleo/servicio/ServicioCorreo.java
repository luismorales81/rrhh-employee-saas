package com.rrhh.saas.nucleo.servicio;

import com.rrhh.saas.nucleo.config.PropiedadesCorreo;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ServicioCorreo {

    private static final Logger log = LoggerFactory.getLogger(ServicioCorreo.class);

    private final JavaMailSender remitenteCorreo;
    private final PropiedadesCorreo propiedades;

    public ServicioCorreo(Optional<JavaMailSender> remitenteCorreo, PropiedadesCorreo propiedades) {
        this.remitenteCorreo = remitenteCorreo.orElse(null);
        this.propiedades = propiedades;
    }

    public boolean enviarSiHabilitado(String destinatario, String asunto, String cuerpoTexto) {
        if (destinatario == null || destinatario.isBlank()) {
            log.debug("Correo omitido: sin destinatario para '{}'", asunto);
            return false;
        }
        if (!propiedades.isHabilitado() || remitenteCorreo == null) {
            log.info("Correo simulado a {} — asunto: {}", destinatario, asunto);
            return false;
        }
        try {
            MimeMessage mensaje = remitenteCorreo.createMimeMessage();
            MimeMessageHelper ayudante = new MimeMessageHelper(mensaje, false, "UTF-8");
            ayudante.setFrom(propiedades.getRemitente(), propiedades.getNombreRemitente());
            ayudante.setTo(destinatario);
            ayudante.setSubject(asunto);
            ayudante.setText(cuerpoTexto, false);
            remitenteCorreo.send(mensaje);
            log.info("Correo enviado a {} — asunto: {}", destinatario, asunto);
            return true;
        } catch (Exception ex) {
            log.warn("No se pudo enviar correo a {}: {}", destinatario, ex.getMessage());
            return false;
        }
    }
}
