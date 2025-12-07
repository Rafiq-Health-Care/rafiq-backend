package com.nexaworks.rafiq.doctor.service;

import com.nexaworks.rafiq.shared.event.doctor.DoctorRegisterEvent;

public interface DoctorService {

    void register(DoctorRegisterEvent event);
}
