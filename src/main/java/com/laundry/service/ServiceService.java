package com.laundry.service;
import com.laundry.dto.ServiceCreateDTO; // DTO me name, description, pricePerUnit, unitType, estimatedTime
import com.laundry.dto.ServiceUpdateDTO; // DTO me fushat opsionale për update
import com.laundry.entity.Services; // Vetëm një import për entity Service
import com.laundry.entity.UnitType;
import com.laundry.repository.ServiceRepository; // Asumo ekziston
import com.laundry.service.serviceInterface.IServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service  // Kjo është annotation e saktë (nuk ka nevojë për tjetër)
public class ServiceService extends BaseService implements IServiceService {  // Asumo se interface ekziston; nëse jo, hiq 'implements IServiceService'

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> createService(ServiceCreateDTO request) {
        try {
            // Validime bazë
            if (request.getName() == null || request.getName().trim().isEmpty()) {
                return createErrorResponse("Service name is missing or empty", HttpStatus.BAD_REQUEST);
            }
            if (request.getPricePerUnit() == null || request.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0) {
                return createErrorResponse("Invalid price per unit", HttpStatus.BAD_REQUEST);
            }

            // Kontrollo duplicate name (opsionale, nëse duhet uniqueness)
            Optional<Services> existingService = serviceRepository.findByName(request.getName());
            if (existingService.isPresent()) {
                return createErrorResponse("Service with this name already exists", HttpStatus.BAD_REQUEST);
            }

            getAuthenticatedUser();
            // Krijo entity të re nga DTO DHE set-o timestamps KËTU (nëse entity ka createdAt/updatedAt, shto ato)
            Services newService = new Services();
            newService.setName(request.getName());
            newService.setDescription(request.getDescription());
            newService.setPricePerUnit(request.getPricePerUnit());
            newService.setUnitType(request.getUnitType() != null ? request.getUnitType() : UnitType.ITEM);
            newService.setEstimatedTime(request.getEstimatedTime());

            // Ruaj entity-n
            Services savedService = serviceRepository.save(newService);

            return createSuccessResponse(savedService, "Service created successfully", HttpStatus.CREATED);
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> updateService(Integer id, ServiceUpdateDTO updateRequest) {
        try {
            // Validime...
            getAuthenticatedUser();
            Optional<Services> serviceOptional = serviceRepository.findById(id);
            if (serviceOptional.isPresent()) {
                Services service = serviceOptional.get();
                if (updateRequest.getName() != null && !updateRequest.getName().trim().isEmpty()) {
                    service.setName(updateRequest.getName());
                }
                if (updateRequest.getDescription() != null) {
                    service.setDescription(updateRequest.getDescription());
                }
                if (updateRequest.getPricePerUnit() != null && updateRequest.getPricePerUnit().compareTo(BigDecimal.ZERO) > 0) {
                    service.setPricePerUnit(updateRequest.getPricePerUnit());
                }
                if (updateRequest.getUnitType() != null) {
                    service.setUnitType(updateRequest.getUnitType());
                }
                if (updateRequest.getEstimatedTime() != null) {
                    service.setEstimatedTime(updateRequest.getEstimatedTime());
                }
                // Hiqur setUpdatedAt pasi entity nuk e ka fushën updatedAt

                return createSuccessResponse(serviceRepository.save(service), "Service updated successfully", HttpStatus.OK);
            } else {
                return createErrorResponse("Service not found", HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional(readOnly = true)  // Bërë readOnly = true për get methods
    public Optional<Services> getServiceById(Integer id) {
        return serviceRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Iterable<Services> getAllServices() {
        return serviceRepository.findAll();
    }

    @Transactional
    public void deleteServiceById(Integer id) {
        getAuthenticatedUser();
        if (!serviceRepository.existsById(id)) {
            throw new RuntimeException("Service with ID " + id + " not found.");
        }
        serviceRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Services> findByName(String name) {
        return serviceRepository.findByName(name);
    }

    @Transactional(readOnly = true)
    public List<Services> getServicesByUnitType(UnitType unitType) {
        // Ndryshuar në .name() pasi repository pret String (nga error-i)
        return serviceRepository.findByUnitType(unitType.name());
    }
}