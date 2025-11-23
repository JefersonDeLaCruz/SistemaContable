package com.ues.sic.detalle_partida;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ues.sic.cuentas.CuentaRepository;
import java.util.List;
import java.util.Set;

@Service
public class DetallePartidaService {

    @Autowired
    private DetallePartidaRepository detallePartidaRepository;

    @Autowired
    private CuentaRepository cuentaRepository;

    // Cuentas GRUPO - no deben aceptar movimientos directos
    private static final Set<String> CUENTAS_GRUPO = Set.of(
        "1", "1.1", "1.2",      // ACTIVO y subcategorías
        "2", "2.1", "2.2",      // PASIVO y subcategorías
        "3",                    // CAPITAL CONTABLE
        "4",                    // INGRESOS
        "5",                    // COSTO DE VENTAS
        "6", "6.1", "6.2",      // GASTOS DE OPERACIÓN y subcategorías
        "7"                     // GASTOS NO OPERATIVOS
    );

    /**
     * Valida que la cuenta no sea una cuenta GRUPO antes de permitir movimientos
     */
    private void validarCuentaDetalle(String idCuenta) {
        try {
            Integer id = Integer.parseInt(idCuenta);
            String codigo = cuentaRepository.findCodigoByIdCuenta(id);

            if (codigo != null && CUENTAS_GRUPO.contains(codigo)) {
                throw new IllegalArgumentException(
                    "No se pueden registrar movimientos en la cuenta GRUPO '" + codigo + "'. " +
                    "Debe usar una cuenta de detalle (subcuenta)."
                );
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("ID de cuenta inválido: " + idCuenta);
        }
    }

    public DetallePartidaModel save(DetallePartidaModel detalle) {
        // Validar que no sea una cuenta GRUPO
        validarCuentaDetalle(detalle.getIdCuenta());
        return detallePartidaRepository.save(detalle);
    }
    
    public List<DetallePartidaModel> findByPartidaId(Long partidaId) {
        return detallePartidaRepository.findByPartida_Id(partidaId);
    }
    
    public DetallePartidaModel findById(Long id) {
        return detallePartidaRepository.findById(id).orElse(null);
    }
    
    public void deleteById(Long id) {
        detallePartidaRepository.deleteById(id);
    }

    public List<DetallePartidaModel> findAll() {
        
        return detallePartidaRepository.findAll();
    }
}