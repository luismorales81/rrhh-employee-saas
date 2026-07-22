package com.rrhh.saas.certificado.repositorio;



import com.rrhh.saas.certificado.dominio.DatosExtraidosCertificado;

import org.springframework.data.jpa.repository.JpaRepository;



import java.util.List;

import java.util.Optional;



public interface RepositorioDatosExtraidosCertificado extends JpaRepository<DatosExtraidosCertificado, Long> {



    Optional<DatosExtraidosCertificado> findByCertificadoIdAndEsVigenteTrueAndDeletedFalse(Long certificadoId);



    List<DatosExtraidosCertificado> findByCertificadoIdOrderByNumeroVersionDesc(Long certificadoId);

}


