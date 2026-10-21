package com.his.outpatient.service;


public interface DoctorStatusCacheService {

    void setStatus(Long doctorId, int status);

    int getStatus(Long doctorId);

    void clearStatus(Long doctorId);

    void startConsulting(Long doctorId);

    void pauseConsulting(Long doctorId);

    void finishConsulting(Long doctorId);
}
