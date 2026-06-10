package co.edu.upc.citasmedicas.service;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import java.util.Hashtable;
import java.util.regex.Pattern;

public final class ValidacionService {

    private static final Pattern EMAIL_FORMATO =
            Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    private static final Pattern TELEFONO_COLOMBIA =
            Pattern.compile("^(\\+57)?3\\d{9}$");

    private static final Pattern SOLO_LETRAS =
            Pattern.compile("^[a-zA-ZáéíóúÁÉÍÓÚñÑ ]+$");

    private static final Pattern SOLO_DIGITOS =
            Pattern.compile("^\\d+$");

    private ValidacionService() {
    }

    public static boolean emailValidoFormato(String email) {
        if (email == null || email.isBlank()) return false;
        return EMAIL_FORMATO.matcher(email.trim()).matches();
    }

    public static boolean dominioTieneMX(String email) {
        if (email == null || email.isBlank()) return false;
        String dominio = email.trim().substring(email.indexOf('@') + 1);
        if (dominio.isEmpty()) return false;

        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");
            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(dominio, new String[]{"MX"});
            Attribute mx = attrs.get("MX");
            return mx != null && mx.size() > 0;
        } catch (NamingException e) {
            return false;
        }
    }

    public static boolean telefonoValidoColombia(String telefono) {
        if (telefono == null || telefono.isBlank()) return false;
        return TELEFONO_COLOMBIA.matcher(telefono.trim()).matches();
    }

    public static String mensajeErrorEmail(String email) {
        if (email == null || email.isBlank()) return "El email es obligatorio";
        if (!emailValidoFormato(email)) return "Formato de email invalido";
        if (!dominioTieneMX(email)) return "El dominio del email no existe o no recibe correos";
        return null;
    }

    public static String mensajeErrorTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) return "El telefono es obligatorio";
        if (!telefonoValidoColombia(telefono)) return "Numero invalido. Debe ser 3XXXXXXXXX (10 digitos)";
        return null;
    }

    public static String mensajeErrorConfirmarTelefono(String telefono, String confirmacion) {
        if (confirmacion == null || confirmacion.isBlank()) return "Confirma tu numero de telefono";
        if (!telefono.equals(confirmacion.trim())) return "Los numeros de telefono no coinciden";
        return null;
    }

    public static String mensajeErrorNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        if (!SOLO_LETRAS.matcher(nombre.trim()).matches())
            return "Solo se permiten letras y espacios";
        return null;
    }

    public static String mensajeErrorNumeroDocumento(String doc) {
        if (doc == null || doc.isBlank()) return null;
        if (!SOLO_DIGITOS.matcher(doc.trim()).matches())
            return "Solo se permiten numeros";
        return null;
    }

    public static String mensajeErrorEps(String eps) {
        if (eps == null || eps.isBlank()) return null;
        if (!SOLO_LETRAS.matcher(eps.trim()).matches())
            return "Solo se permiten letras y espacios";
        return null;
    }
}