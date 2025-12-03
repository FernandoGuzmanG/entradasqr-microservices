package com.microservice.comunicaciones.service;

import com.microservice.comunicaciones.dto.EnvioEntradasRequest;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;
import java.io.ByteArrayOutputStream;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * Construye y envía el correo electrónico con las entradas adjuntas.
     * @param request Datos de la solicitud de envío.
     * @throws MessagingException Si hay un error al construir o enviar el mensaje.
     */
    public void enviarEntradas(EnvioEntradasRequest request) throws MessagingException {

        MimeMessage message = mailSender.createMimeMessage();
        // true indica multipart (necesario para adjuntos/recursos en línea)
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom("no-reply@eventos.com");
        helper.setTo(request.getCorreoDestino());
        helper.setSubject("Tus Entradas para: " + request.getNombreEvento());

        // --- GENERACIÓN DEL CONTENIDO DINÁMICO ---

        // 1. Crear el StringBuilder para los bloques HTML de los tickets y el QR
        StringBuilder ticketsHtml = new StringBuilder();
        int count = 1;

        for (EnvioEntradasRequest.TicketData ticket : request.getTickets()) {
            String qrCode = ticket.getCodigoQR();
            String contentId = "qr_imagen_" + count; // ID ÚNICO para incrustar

            try {
                // Generación y adjunto del QR
                byte[] qrImageBytes = generateQRCodeImage(qrCode, 200, 200);
                ByteArrayResource resource = new ByteArrayResource(qrImageBytes);
                helper.addInline(contentId, resource, "image/png");

                // Añadir el bloque HTML con la referencia al CID
                ticketsHtml.append("<div style='border: 1px solid #ccc; padding: 15px; margin-bottom: 20px; text-align: center;'>")
                        .append("<h4>Entrada Tipo: ").append(request.getNombreTipoEntrada()).append("</h4>")
                        .append("<p>Código de Acceso: <b>").append(qrCode).append("</b></p>")
                        .append("<img src='cid:").append(contentId).append("' alt='Código QR' style='width: 180px; height: 180px; display: block; margin: 10px auto;'>")
                        .append("</div>");

                count++;
            } catch (Exception e) {
                ticketsHtml.append("<p style='color: red;'>Error al generar QR para: ").append(qrCode).append("</p>");
            }
        }

        // 2. CONSTRUIR EL HTML FINAL CON LA ESTRUCTURA BASE
        String finalHtml = "<html><body>"
                + "<h2>¡Hola, " + request.getNombreInvitado() + "!</h2>"
                + "<p>Gracias por registrarte. Aquí están tus entradas para el evento:</p>"
                + "<h3>Evento: " + request.getNombreEvento() + "</h3>"
                + "<div style='margin-top: 20px;'>"
                + ticketsHtml.toString() // <--- Se inserta la lista generada aquí
                + "</div>"
                + "<p>¡Nos vemos pronto!</p>"
                + "</body></html>";

        helper.setText(finalHtml, true); // true para HTML

        mailSender.send(message);
    }

    // NUEVO MÉTODO AUXILIAR PARA GENERAR LA IMAGEN QR
    private byte[] generateQRCodeImage(String text, int width, int height) throws Exception {
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height);

        // Escribir la matriz de bits a un ByteArrayOutputStream
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);

        return pngOutputStream.toByteArray();
    }

    // Ajusta tu buildEmailContent para que use un PLACEHOLDER
    public String buildEmailContent(String nombreInvitado,
                                    String nombreEvento,
                                    String tipoEntrada,
                                    List<EnvioEntradasRequest.TicketData> tickets) {

        // Genera el HTML completo para el campo 'cuerpoMensaje' de la tabla notificaciones
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body><h2>¡Hola, ").append(nombreInvitado).append("!</h2>");
        sb.append("<p>Gracias por registrarte. Aquí están tus entradas para el evento: <b>").append(nombreEvento).append("</b></p>");

        for (EnvioEntradasRequest.TicketData ticket : tickets) {
            sb.append("<div style='border: 1px solid #ccc; padding: 15px; margin-bottom: 10px;'>")
                    .append("<h4>Entrada Tipo: ").append(tipoEntrada).append("</h4>")
                    .append("<p>Código de Acceso: <b>").append(ticket.getCodigoQR()).append("</b></p>")
                    .append("</div>");
        }

        sb.append("</body></html>");
        return sb.toString();
    }
}