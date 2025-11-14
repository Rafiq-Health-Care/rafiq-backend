package com.nexaworks.rafiq.service;

import com.nexaworks.rafiq.entities.DoctorProfile;
import com.nexaworks.rafiq.entities.User;
import java.util.UUID;

public interface DoctorService {
  DoctorProfile createProfile(
      User doctor, String description, UUID specialization, String id, String logo);
}
