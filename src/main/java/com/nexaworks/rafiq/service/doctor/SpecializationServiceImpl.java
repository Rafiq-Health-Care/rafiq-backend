package com.nexaworks.rafiq.service.doctor;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.nexaworks.rafiq.constant.CacheNames;
import com.nexaworks.rafiq.entities.enums.Specialization;

@Service
public class SpecializationServiceImpl implements SpecializationService {

    @Override
    @Cacheable(cacheNames = CacheNames.SPECIALIZATIONS, key = "'all'")
    public List<Specialization> getSpecializations() {
        return List.of(Specialization.values());
    }
}
