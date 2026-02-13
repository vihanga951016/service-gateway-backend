package com.flex.api.job;

import com.flex.job_module.api.http.requests.PrepareJob;
import com.flex.job_module.api.services.JobService;
import com.flex.service_module.api.http.requests.AddCluster;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/12/2026
 */
@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @PostMapping("/prepare")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> prepareJob(@RequestBody PrepareJob job, HttpServletRequest request) {
        return jobService.prepareJob(job, request);
    }

    @DeleteMapping("/{jobId}/remove/customer/{customerId}")
    @PreAuthorize("@securityService.hasAnyAccess(T(com.flex.user_module.constants.PermissionConstant).PT)")
    public ResponseEntity<?> cancelPrepare(@PathVariable Integer jobId, @PathVariable Integer customerId, HttpServletRequest request) {
        return jobService.removeDummyJob(jobId, customerId, request);
    }
}
