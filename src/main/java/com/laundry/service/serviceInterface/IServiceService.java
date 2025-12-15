package com.laundry.service.serviceInterface;



import com.laundry.dto.ServiceCreateDTO;
import com.laundry.dto.ServiceUpdateDTO;
import com.laundry.entity.Services;
import com.laundry.entity.UnitType;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IServiceService {
    ResponseEntity<Map<String, Object>> createService(ServiceCreateDTO request);
    ResponseEntity<Map<String, Object>> updateService(Integer id, ServiceUpdateDTO updateRequest);
    Optional<Services> getServiceById(Integer id);
    Iterable<Services> getAllServices();
    void deleteServiceById(Integer id);
    Optional<Services> findByName(String name);
    List<Services> getServicesByUnitType(UnitType unitType);
}