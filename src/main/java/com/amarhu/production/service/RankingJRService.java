package com.amarhu.production.service;

import com.amarhu.production.dto.RankingJRDTO;
import com.amarhu.production.dto.PersonalProductionDTO;
import com.amarhu.user.entity.Role;
import com.amarhu.user.entity.User;
import com.amarhu.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RankingJRService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PersonalProductionService personalProductionService;

    public List<RankingJRDTO> getRankingJRs() {
        // Obtener usuarios con rol Jefe de Redacción y código "JR"
        List<User> jrs = userRepository.findByRoleAndCodigo(Role.JEFE_REDACCION, "JR");

        // Crear el ranking basado en la producción personal obtenida por cada JR
        return jrs.stream()
                .map(jr -> {
                    // Obtener la producción del JR usando el servicio de producción personal
                    PersonalProductionDTO productionDTO = personalProductionService.getPersonalProduction(jr.getId());

                    return new RankingJRDTO(
                            jr.getId(),
                            jr.getName(),
                            productionDTO.getVideosTotales(),
                            productionDTO.getVideosCaidos(),
                            productionDTO.getGananciaTotal(),
                            productionDTO.getComisionDolares(), // Comisiones netas en dólares
                            BigDecimal.ZERO, // Ajustable si se necesita otro cálculo
                            BigDecimal.ZERO  // Ajustable si se necesita otro cálculo
                    );
                })
                .sorted((a, b) -> Integer.compare(b.getTotalVideos(), a.getTotalVideos())) // Ordenar por total de videos descendente
                .collect(Collectors.toList());
    }
}
