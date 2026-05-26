package com.nexaworks.rafiq.entities.enums;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Getter;
@Getter
public enum SubSpecialization {

    // ─── INTERNAL MEDICINE ────────────────────────────────────────
    GENERAL_INTERNAL_MEDICINE(Specialization.INTERNAL_MEDICINE), HOSPITAL_MEDICINE(
            Specialization.INTERNAL_MEDICINE), CLINICAL_PHARMACOLOGY(
                    Specialization.INTERNAL_MEDICINE),

    // CARDIOLOGY
    INTERVENTIONAL_CARDIOLOGY(Specialization.CARDIOLOGY), ELECTROPHYSIOLOGY(
            Specialization.CARDIOLOGY), HEART_FAILURE(Specialization.CARDIOLOGY), CARDIAC_IMAGING(
                    Specialization.CARDIOLOGY), PREVENTIVE_CARDIOLOGY(
                            Specialization.CARDIOLOGY), CONGENITAL_HEART_DISEASE(
                                    Specialization.CARDIOLOGY), CARDIAC_REHABILITATION(
                                            Specialization.CARDIOLOGY),

    // ENDOCRINOLOGY
    DIABETES_METABOLISM(Specialization.ENDOCRINOLOGY), THYROID_DISEASE(
            Specialization.ENDOCRINOLOGY), PITUITARY_DISEASE(
                    Specialization.ENDOCRINOLOGY), ADRENAL_DISEASE(
                            Specialization.ENDOCRINOLOGY), BONE_METABOLISM(
                                    Specialization.ENDOCRINOLOGY), OBESITY_MEDICINE(
                                            Specialization.ENDOCRINOLOGY),

    // GASTROENTEROLOGY
    HEPATOLOGY(Specialization.GASTROENTEROLOGY), INFLAMMATORY_BOWEL_DISEASE(
            Specialization.GASTROENTEROLOGY), ADVANCED_ENDOSCOPY(
                    Specialization.GASTROENTEROLOGY), MOTILITY(
                            Specialization.GASTROENTEROLOGY), PANCREATIC_DISEASE(
                                    Specialization.GASTROENTEROLOGY), TRANSPLANT_HEPATOLOGY(
                                            Specialization.GASTROENTEROLOGY),

    // HEMATOLOGY
    BENIGN_HEMATOLOGY(Specialization.HEMATOLOGY), MALIGNANT_HEMATOLOGY(
            Specialization.HEMATOLOGY), BONE_MARROW_TRANSPLANT(
                    Specialization.HEMATOLOGY), COAGULATION_DISORDERS(
                            Specialization.HEMATOLOGY), TRANSFUSION_MEDICINE(
                                    Specialization.HEMATOLOGY),

    // INFECTIOUS_DISEASE
    HIV_MEDICINE(Specialization.INFECTIOUS_DISEASE), TRANSPLANT_INFECTIOUS_DISEASE(
            Specialization.INFECTIOUS_DISEASE), TROPICAL_INFECTIOUS_DISEASE(
                    Specialization.INFECTIOUS_DISEASE), ANTIMICROBIAL_STEWARDSHIP(
                            Specialization.INFECTIOUS_DISEASE), TRAVEL_MEDICINE(
                                    Specialization.INFECTIOUS_DISEASE),

    // NEPHROLOGY
    DIALYSIS(Specialization.NEPHROLOGY), KIDNEY_TRANSPLANT(
            Specialization.NEPHROLOGY), GLOMERULAR_DISEASE(
                    Specialization.NEPHROLOGY), HYPERTENSION_NEPHROLOGY(
                            Specialization.NEPHROLOGY), ONCO_NEPHROLOGY(Specialization.NEPHROLOGY),

    // ONCOLOGY
    MEDICAL_ONCOLOGY(Specialization.ONCOLOGY), RADIATION_ONCOLOGY(
            Specialization.ONCOLOGY), HEMATOLOGIC_ONCOLOGY(
                    Specialization.ONCOLOGY), GYNECOLOGIC_ONCOLOGY(
                            Specialization.ONCOLOGY), PEDIATRIC_ONCOLOGY(
                                    Specialization.ONCOLOGY), NEURO_ONCOLOGY(
                                            Specialization.ONCOLOGY), PSYCHO_ONCOLOGY(
                                                    Specialization.ONCOLOGY),

    // PULMONOLOGY
    CRITICAL_CARE_PULM(Specialization.PULMONOLOGY), SLEEP_MEDICINE(
            Specialization.PULMONOLOGY), INTERVENTIONAL_PULMONOLOGY(
                    Specialization.PULMONOLOGY), LUNG_TRANSPLANT(
                            Specialization.PULMONOLOGY), PULMONARY_HYPERTENSION(
                                    Specialization.PULMONOLOGY), CYSTIC_FIBROSIS(
                                            Specialization.PULMONOLOGY),

    // RHEUMATOLOGY
    INFLAMMATORY_ARTHRITIS(Specialization.RHEUMATOLOGY), CONNECTIVE_TISSUE_DISEASE(
            Specialization.RHEUMATOLOGY), VASCULITIS(
                    Specialization.RHEUMATOLOGY), SPONDYLOARTHROPATHY(
                            Specialization.RHEUMATOLOGY), MYOSITIS(Specialization.RHEUMATOLOGY),

    // ─── SURGERY ──────────────────────────────────────────────────
    LAPAROSCOPIC_SURGERY(Specialization.GENERAL_SURGERY), TRAUMA_SURGERY(
            Specialization.GENERAL_SURGERY), BARIATRIC_SURGERY(
                    Specialization.GENERAL_SURGERY), ENDOCRINE_SURGERY(
                            Specialization.GENERAL_SURGERY), SURGICAL_ONCOLOGY(
                                    Specialization.GENERAL_SURGERY),

    CORONARY_ARTERY_BYPASS(Specialization.CARDIAC_SURGERY), VALVE_SURGERY(
            Specialization.CARDIAC_SURGERY), AORTIC_SURGERY(
                    Specialization.CARDIAC_SURGERY), HEART_TRANSPLANT(
                            Specialization.CARDIAC_SURGERY), MECHANICAL_CIRCULATORY_SUPPORT(
                                    Specialization.CARDIAC_SURGERY),

    COLORECTAL_ONCOLOGY(Specialization.COLORECTAL_SURGERY), INFLAMMATORY_BOWEL_SURGERY(
            Specialization.COLORECTAL_SURGERY), PELVIC_FLOOR_SURGERY(
                    Specialization.COLORECTAL_SURGERY), ANORECTAL_SURGERY(
                            Specialization.COLORECTAL_SURGERY),

    BRAIN_TUMOR_SURGERY(Specialization.NEUROSURGERY), SPINE_NEUROSURGERY(
            Specialization.NEUROSURGERY), FUNCTIONAL_NEUROSURGERY(
                    Specialization.NEUROSURGERY), CEREBROVASCULAR_SURGERY(
                            Specialization.NEUROSURGERY), PEDIATRIC_NEUROSURGERY(
                                    Specialization.NEUROSURGERY),

    SPINE_SURGERY(Specialization.ORTHOPEDIC_SURGERY), JOINT_REPLACEMENT(
            Specialization.ORTHOPEDIC_SURGERY), HAND_SURGERY(
                    Specialization.ORTHOPEDIC_SURGERY), FOOT_ANKLE_SURGERY(
                            Specialization.ORTHOPEDIC_SURGERY), PEDIATRIC_ORTHOPEDIC_SURGERY(
                                    Specialization.ORTHOPEDIC_SURGERY), ONCOLOGIC_ORTHOPEDICS(
                                            Specialization.ORTHOPEDIC_SURGERY), SPORTS_ORTHOPEDICS(
                                                    Specialization.ORTHOPEDIC_SURGERY),

    RECONSTRUCTIVE_SURGERY(Specialization.PLASTIC_SURGERY), COSMETIC_SURGERY(
            Specialization.PLASTIC_SURGERY), BURN_SURGERY(
                    Specialization.PLASTIC_SURGERY), CRANIOFACIAL_SURGERY(
                            Specialization.PLASTIC_SURGERY), MICROSURGERY(
                                    Specialization.PLASTIC_SURGERY),

    LUNG_SURGERY(Specialization.THORACIC_SURGERY), ESOPHAGEAL_SURGERY(
            Specialization.THORACIC_SURGERY), MEDIASTINAL_SURGERY(
                    Specialization.THORACIC_SURGERY), MINIMALLY_INVASIVE_THORACIC(
                            Specialization.THORACIC_SURGERY),

    LIVER_TRANSPLANT(Specialization.TRANSPLANT_SURGERY), KIDNEY_TRANSPLANT_SURGERY(
            Specialization.TRANSPLANT_SURGERY), PANCREAS_TRANSPLANT(
                    Specialization.TRANSPLANT_SURGERY), MULTIORGAN_TRANSPLANT(
                            Specialization.TRANSPLANT_SURGERY),

    AORTIC_ANEURYSM(Specialization.VASCULAR_SURGERY), PERIPHERAL_ARTERIAL_DISEASE(
            Specialization.VASCULAR_SURGERY), VENOUS_DISEASE(
                    Specialization.VASCULAR_SURGERY), CAROTID_SURGERY(
                            Specialization.VASCULAR_SURGERY), ENDOVASCULAR_SURGERY(
                                    Specialization.VASCULAR_SURGERY),

    UROLOGIC_ONCOLOGY(Specialization.UROLOGICAL_SURGERY), ENDOUROLOGY(
            Specialization.UROLOGICAL_SURGERY), NEUROUROLOGY(
                    Specialization.UROLOGICAL_SURGERY), PEDIATRIC_UROLOGY(
                            Specialization.UROLOGICAL_SURGERY), MALE_INFERTILITY(
                                    Specialization.UROLOGICAL_SURGERY), FEMALE_UROLOGY(
                                            Specialization.UROLOGICAL_SURGERY),

    // ─── HEAD & NECK ──────────────────────────────────────────────
    RETINA_VITREOUS(Specialization.OPHTHALMOLOGY), CORNEA_EXTERNAL_DISEASE(
            Specialization.OPHTHALMOLOGY), GLAUCOMA(
                    Specialization.OPHTHALMOLOGY), PEDIATRIC_OPHTHALMOLOGY(
                            Specialization.OPHTHALMOLOGY), OCULOPLASTICS(
                                    Specialization.OPHTHALMOLOGY), NEURO_OPHTHALMOLOGY(
                                            Specialization.OPHTHALMOLOGY),

    HEAD_NECK_SURGERY(Specialization.OTOLARYNGOLOGY), RHINOLOGY(
            Specialization.OTOLARYNGOLOGY), OTOLOGY_NEUROTOLOGY(
                    Specialization.OTOLARYNGOLOGY), LARYNGOLOGY(
                            Specialization.OTOLARYNGOLOGY), PEDIATRIC_ENT(
                                    Specialization.OTOLARYNGOLOGY),

    ORTHODONTICS(Specialization.DENTISTRY), ENDODONTICS(Specialization.DENTISTRY), PERIODONTICS(
            Specialization.DENTISTRY), PROSTHODONTICS(
                    Specialization.DENTISTRY), PEDIATRIC_DENTISTRY(
                            Specialization.DENTISTRY), ORAL_SURGERY(Specialization.DENTISTRY),

    FACIAL_TRAUMA(Specialization.ORAL_MAXILLOFACIAL_SURGERY), ORTHOGNATHIC_SURGERY(
            Specialization.ORAL_MAXILLOFACIAL_SURGERY), ORAL_ONCOLOGY(
                    Specialization.ORAL_MAXILLOFACIAL_SURGERY), TEMPOROMANDIBULAR_JOINT(
                            Specialization.ORAL_MAXILLOFACIAL_SURGERY),

    // ─── NERVOUS SYSTEM ───────────────────────────────────────────
    STROKE_NEUROLOGY(Specialization.NEUROLOGY), EPILEPSY(
            Specialization.NEUROLOGY), MOVEMENT_DISORDERS(
                    Specialization.NEUROLOGY), MULTIPLE_SCLEROSIS(
                            Specialization.NEUROLOGY), NEUROMUSCULAR_DISEASE(
                                    Specialization.NEUROLOGY), HEADACHE_MEDICINE(
                                            Specialization.NEUROLOGY), COGNITIVE_NEUROLOGY(
                                                    Specialization.NEUROLOGY),

    CHILD_ADOLESCENT_PSYCHIATRY(Specialization.PSYCHIATRY), GERIATRIC_PSYCHIATRY(
            Specialization.PSYCHIATRY), FORENSIC_PSYCHIATRY(
                    Specialization.PSYCHIATRY), ADDICTION_PSYCHIATRY(
                            Specialization.PSYCHIATRY), CONSULTATION_LIAISON_PSYCHIATRY(
                                    Specialization.PSYCHIATRY), MOOD_DISORDERS(
                                            Specialization.PSYCHIATRY), SCHIZOPHRENIA_PSYCHOSIS(
                                                    Specialization.PSYCHIATRY),

    CLINICAL_PSYCHOLOGY(Specialization.PSYCHOLOGY), NEUROPSYCHOLOGY(
            Specialization.PSYCHOLOGY), HEALTH_PSYCHOLOGY(
                    Specialization.PSYCHOLOGY), BEHAVIORAL_THERAPY(Specialization.PSYCHOLOGY),

    // ─── WOMEN & CHILDREN ─────────────────────────────────────────
    MATERNAL_FETAL_MEDICINE(Specialization.OBSTETRICS), HIGH_RISK_OBSTETRICS(
            Specialization.OBSTETRICS), FETAL_MEDICINE(Specialization.OBSTETRICS),

    UROGYNECOLOGY(Specialization.GYNECOLOGY), MINIMALLY_INVASIVE_GYNECOLOGY(
            Specialization.GYNECOLOGY), GYNECOLOGIC_ONCOLOGY_SUB(Specialization.GYNECOLOGY),

    REPRODUCTIVE_ENDOCRINOLOGY_INFERTILITY(Specialization.OBSTETRICS_GYNECOLOGY), GENERAL_OBGYN(
            Specialization.OBSTETRICS_GYNECOLOGY),

    PEDIATRIC_CARDIOLOGY(Specialization.PEDIATRICS), PEDIATRIC_NEUROLOGY(
            Specialization.PEDIATRICS), PEDIATRIC_INFECTIOUS_DISEASE(
                    Specialization.PEDIATRICS), PEDIATRIC_ENDOCRINOLOGY(
                            Specialization.PEDIATRICS), PEDIATRIC_RHEUMATOLOGY(
                                    Specialization.PEDIATRICS), DEVELOPMENTAL_PEDIATRICS(
                                            Specialization.PEDIATRICS), PEDIATRIC_GASTROENTEROLOGY(
                                                    Specialization.PEDIATRICS), PEDIATRIC_PULMONOLOGY(
                                                            Specialization.PEDIATRICS),

    PEDIATRIC_GENERAL_SURGERY(Specialization.PEDIATRIC_SURGERY), PEDIATRIC_UROLOGY_SUB(
            Specialization.PEDIATRIC_SURGERY), PEDIATRIC_ORTHOPEDICS(
                    Specialization.PEDIATRIC_SURGERY), PEDIATRIC_PLASTIC_SURGERY(
                            Specialization.PEDIATRIC_SURGERY),

    PREMATURE_INFANT_CARE(Specialization.NEONATOLOGY), NEONATAL_INTENSIVE_CARE(
            Specialization.NEONATOLOGY), NEONATAL_TRANSPORT(Specialization.NEONATOLOGY),

    IVF_ART(Specialization.REPRODUCTIVE_MEDICINE), FERTILITY_PRESERVATION(
            Specialization.REPRODUCTIVE_MEDICINE), RECURRENT_PREGNANCY_LOSS(
                    Specialization.REPRODUCTIVE_MEDICINE),

    // ─── SKIN & MUSCULOSKELETAL ───────────────────────────────────
    MOHS_SURGERY(Specialization.DERMATOLOGY), COSMETIC_DERMATOLOGY(
            Specialization.DERMATOLOGY), PEDIATRIC_DERMATOLOGY(
                    Specialization.DERMATOLOGY), DERMATO_PATHOLOGY(
                            Specialization.DERMATOLOGY), INFLAMMATORY_SKIN_DISEASE(
                                    Specialization.DERMATOLOGY), DERMATOLOGIC_ONCOLOGY(
                                            Specialization.DERMATOLOGY),

    GENERAL_ORTHOPEDICS(Specialization.ORTHOPEDICS), SPORTS_MEDICINE_ORTHO(
            Specialization.ORTHOPEDICS), TRAUMA_ORTHOPEDICS(Specialization.ORTHOPEDICS),

    AUTOIMMUNE_DISEASE(Specialization.RHEUMATOLOGY_IMMUNOLOGY), CLINICAL_IMMUNOLOGY(
            Specialization.RHEUMATOLOGY_IMMUNOLOGY), BIOLOGICS_THERAPY(
                    Specialization.RHEUMATOLOGY_IMMUNOLOGY),

    // ─── IMAGING & DIAGNOSTICS ───────────────────────────────────
    NEURORADIOLOGY(Specialization.RADIOLOGY), INTERVENTIONAL_RADIOLOGY(
            Specialization.RADIOLOGY), MUSCULOSKELETAL_RADIOLOGY(
                    Specialization.RADIOLOGY), BREAST_IMAGING(
                            Specialization.RADIOLOGY), CARDIOVASCULAR_RADIOLOGY(
                                    Specialization.RADIOLOGY), PEDIATRIC_RADIOLOGY(
                                            Specialization.RADIOLOGY), BODY_IMAGING(
                                                    Specialization.RADIOLOGY),

    PET_IMAGING(Specialization.NUCLEAR_MEDICINE), THYROID_NUCLEAR(
            Specialization.NUCLEAR_MEDICINE), THERANOSTICS(
                    Specialization.NUCLEAR_MEDICINE), BONE_SCINTIGRAPHY(
                            Specialization.NUCLEAR_MEDICINE),

    SURGICAL_PATHOLOGY(Specialization.PATHOLOGY), CYTOPATHOLOGY(
            Specialization.PATHOLOGY), MOLECULAR_PATHOLOGY(
                    Specialization.PATHOLOGY), NEUROPATHOLOGY(
                            Specialization.PATHOLOGY), HEMATOPATHOLOGY(Specialization.PATHOLOGY),

    CLINICAL_CHEMISTRY(Specialization.CLINICAL_LABORATORY), MICROBIOLOGY(
            Specialization.CLINICAL_LABORATORY), CLINICAL_GENETICS(
                    Specialization.CLINICAL_LABORATORY), IMMUNOASSAY(
                            Specialization.CLINICAL_LABORATORY),

    // ─── EMERGENCY & CRITICAL CARE ───────────────────────────────
    TOXICOLOGY(Specialization.EMERGENCY_MEDICINE), DISASTER_MEDICINE(
            Specialization.EMERGENCY_MEDICINE), WILDERNESS_MEDICINE(
                    Specialization.EMERGENCY_MEDICINE), PEDIATRIC_EMERGENCY(
                            Specialization.EMERGENCY_MEDICINE),

    MEDICAL_ICU(Specialization.CRITICAL_CARE), SURGICAL_ICU(
            Specialization.CRITICAL_CARE), CARDIAC_ICU(
                    Specialization.CRITICAL_CARE), NEURO_ICU(Specialization.CRITICAL_CARE),

    CARDIAC_ANESTHESIA(Specialization.ANESTHESIOLOGY), PEDIATRIC_ANESTHESIA(
            Specialization.ANESTHESIOLOGY), REGIONAL_ANESTHESIA(
                    Specialization.ANESTHESIOLOGY), NEURO_ANESTHESIA(
                            Specialization.ANESTHESIOLOGY), OBSTETRIC_ANESTHESIA(
                                    Specialization.ANESTHESIOLOGY),

    INTERVENTIONAL_PAIN(Specialization.PAIN_MANAGEMENT), CHRONIC_PAIN(
            Specialization.PAIN_MANAGEMENT), CANCER_PAIN(
                    Specialization.PAIN_MANAGEMENT), PALLIATIVE_PAIN(
                            Specialization.PAIN_MANAGEMENT),

    // ─── OTHER SPECIALIZATIONS ───────────────────────────────────
    RURAL_MEDICINE(Specialization.FAMILY_MEDICINE), PREVENTIVE_MEDICINE(
            Specialization.FAMILY_MEDICINE), CHRONIC_DISEASE_MANAGEMENT(
                    Specialization.FAMILY_MEDICINE),

    GENERAL_PRACTICE_MEDICINE(Specialization.GENERAL_PRACTICE), COMMUNITY_MEDICINE(
            Specialization.GENERAL_PRACTICE),

    DEMENTIA_CARE(Specialization.GERIATRICS), FALLS_BALANCE(
            Specialization.GERIATRICS), GERIATRIC_REHABILITATION(Specialization.GERIATRICS),

    ALLERGIC_DISEASE(Specialization.ALLERGY), ASTHMA(Specialization.ALLERGY), IMMUNODEFICIENCY(
            Specialization.IMMUNOLOGY), ANAPHYLAXIS(Specialization.ALLERGY),

    WORKPLACE_INJURY(Specialization.OCCUPATIONAL_MEDICINE), OCCUPATIONAL_LUNG_DISEASE(
            Specialization.OCCUPATIONAL_MEDICINE), TOXICOLOGY_OCCUPATIONAL(
                    Specialization.OCCUPATIONAL_MEDICINE),

    ATHLETIC_INJURY(Specialization.SPORTS_MEDICINE), EXERCISE_PHYSIOLOGY(
            Specialization.SPORTS_MEDICINE), CONCUSSION_MANAGEMENT(Specialization.SPORTS_MEDICINE),

    HOSPICE_CARE(Specialization.PALLIATIVE_CARE), SYMPTOM_MANAGEMENT(
            Specialization.PALLIATIVE_CARE), END_OF_LIFE_CARE(Specialization.PALLIATIVE_CARE),

    STROKE_REHABILITATION(Specialization.REHABILITATION), SPINAL_CORD_REHABILITATION(
            Specialization.REHABILITATION), AMPUTEE_REHABILITATION(
                    Specialization.REHABILITATION), BRAIN_INJURY_REHABILITATION(
                            Specialization.REHABILITATION),

    MUSCULOSKELETAL_REHAB(Specialization.PHYSICAL_MEDICINE), ELECTRODIAGNOSTIC_MEDICINE(
            Specialization.PHYSICAL_MEDICINE),

    FORENSIC_PATHOLOGY(Specialization.FORENSIC_MEDICINE), CLINICAL_FORENSICS(
            Specialization.FORENSIC_MEDICINE), MEDICAL_JURISPRUDENCE(
                    Specialization.FORENSIC_MEDICINE),

    AEROMEDICAL_CERTIFICATION(Specialization.AVIATION_MEDICINE), HYPOXIA_PHYSIOLOGY(
            Specialization.AVIATION_MEDICINE), SPATIAL_DISORIENTATION(
                    Specialization.AVIATION_MEDICINE),

    SEXUALLY_TRANSMITTED_INFECTIONS(Specialization.VENEREOLOGY), MALARIA(
            Specialization.TROPICAL_MEDICINE), PARASITIC_DISEASE(
                    Specialization.TROPICAL_MEDICINE), NEGLECTED_TROPICAL_DISEASE(
                            Specialization.TROPICAL_MEDICINE);

    // ─── Field & Constructor ──────────────────────────────────────

    private final Specialization specialization;

    SubSpecialization(Specialization specialization) {
        this.specialization = specialization;
    }

    public Specialization getSpecialization() {
        return specialization;
    }

    public boolean belongsTo(Specialization specialization) {
        return this.specialization == specialization;
    }

    public static List<SubSpecialization> getBySpecialization(Specialization specialization) {
        return Arrays.stream(values()).filter(k -> k.specialization == specialization)
                .collect(Collectors.toList());
    }

    public static void validate(SubSpecialization keyword, Specialization specialization) {
        if (!keyword.belongsTo(specialization)) {
            throw new IllegalArgumentException("Keyword [" + keyword.name()
                    + "] does not belong to specialization [" + specialization.name() + "]. "
                    + "Expected specialization: [" + keyword.specialization.name() + "]");
        }
    }
}