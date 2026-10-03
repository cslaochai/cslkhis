package com.his.appoint.service;


public interface DoctorStatusCacheService {

    public static final int STATUS_IDLE = 0;

    public static final int STATUS_CONSULTING = 1;

    public static final int STATUS_PAUSED = 2;

    void setStatus(Long doctorId, int status);

    int getStatus(Long doctorId);

    void clearStatus(Long doctorId);

    void startConsulting(Long doctorId);

    void pauseConsulting(Long doctorId);

    void finishConsulting(Long doctorId);
}
