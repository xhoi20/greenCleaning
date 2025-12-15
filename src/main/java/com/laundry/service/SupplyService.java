package com.laundry.service;

import com.laundry.entity.Supply;
import com.laundry.repository.SupplyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SupplyService {

    private final SupplyRepository supplyRepository;

    public List<Supply> getAllSupplies() {
        return supplyRepository.findAll();
    }

    public Supply getSupplyById(int id) {
        return supplyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supply not found with id: " + id));
    }

    public Supply createSupply(Supply supply) {
        // mund të shtosh validime këtu
        return supplyRepository.save(supply);
    }

    public Supply updateSupply(int id, Supply updated) {
        Supply existing = getSupplyById(id);

        existing.setName(updated.getName());
        existing.setDescription(updated.getDescription());
        existing.setUnit(updated.getUnit());
        existing.setReorderLevel(updated.getReorderLevel());
        existing.setCurrentStock(updated.getCurrentStock());

        return supplyRepository.save(existing);
    }

    public void deleteSupply(int id) {
        Supply existing = getSupplyById(id);
        supplyRepository.delete(existing);
    }
}
