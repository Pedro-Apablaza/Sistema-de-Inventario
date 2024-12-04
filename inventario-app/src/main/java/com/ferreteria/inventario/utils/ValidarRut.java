package com.ferreteria.inventario.utils;

public class ValidarRut {

    public static boolean validarRut(String rut) {
        if (rut == null || rut.isEmpty()) {
            return false;
        }

        // Eliminar puntos y guión
        rut = rut.replaceAll("[^0-9kK]", "");

        if (rut.length() < 2) {
            return false;
        }

        char dv = rut.charAt(rut.length() - 1);
        String cuerpo = rut.substring(0, rut.length() - 1);

        int suma = 0;
        int multiplicador = 2;

        for (int i = cuerpo.length() - 1; i >= 0; i--) {
            suma += Integer.parseInt(String.valueOf(cuerpo.charAt(i))) * multiplicador;
            multiplicador = (multiplicador == 7) ? 2 : multiplicador + 1;
        }

        int resto = suma % 11;
        int dvCalculado = 11 - resto;

        if (dvCalculado == 11) {
            dvCalculado = 0;
        } else if (dvCalculado == 10) {
            return (dv == 'K' || dv == 'k');
        }

        return (dv == Character.forDigit(dvCalculado, 10));
    }
}
