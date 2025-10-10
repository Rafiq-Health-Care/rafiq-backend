package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.User;

public interface DoctorService {
    DoctorProfile createProfile(User doctor);
}
