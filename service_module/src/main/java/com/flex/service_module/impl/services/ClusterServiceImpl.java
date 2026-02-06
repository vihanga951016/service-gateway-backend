package com.flex.service_module.impl.services;

import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.api.http.requests.AssignCluster;
import com.flex.service_module.api.http.responses.ClusterData;
import com.flex.service_module.api.http.responses.ClusterServiceData;
import com.flex.service_module.api.services.CSService;
import com.flex.service_module.impl.entities.*;
import com.flex.service_module.impl.repositories.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static com.flex.common_module.http.ReturnResponse.*;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 2/1/2026
 */
@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("Duplicates")
public class ClusterServiceImpl implements CSService {

    private final ClusterRepository clusterRepository;
    private final ServicesRepository servicesRepository;
    private final ClusterServiceRepository clusterServiceRepository;
    private final ServiceProviderRepository serviceProviderRepository;
    private final ServiceCenterRepository serviceCenterRepository;
    private final CenterClusterRepository centerClusterRepository;

    @Override
    public ResponseEntity<?> addCluster(AddCluster addCluster, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        if (clusterRepository.existsByNameAndDeletedIsFalse(addCluster.getName())) {
            return CONFLICT("Cluster already exists");
        }

        List<com.flex.service_module.impl.entities.Service> serviceList = servicesRepository
                .getServicesByIds(addCluster.getServiceIds());

        if (serviceList.isEmpty()) {
            return CONFLICT("No services found");
        }

        Cluster cluster = Cluster.builder()
                .name(addCluster.getName())
                .serviceProvider(provider)
                .build();

        List<ClusterService> clusterServices =
                IntStream.range(0, serviceList.size())
                        .mapToObj(i -> ClusterService.builder()
                                .cluster(cluster)
                                .orderNumber(i + 1)
                                .service(serviceList.get(i))
                                .build()
                        )
                        .toList();


        clusterRepository.save(cluster);
        clusterServiceRepository.saveAll(clusterServices);

        return SUCCESS("Cluster added");
    }

    @Override
    public ResponseEntity<?> updateCluster(AddCluster addCluster, HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (addCluster.getId() == null) {
            return CONFLICT("Cluster id is null");
        }

        Cluster cluster = clusterRepository.findByIdAndDeletedIsFalse(addCluster.getId());

        if (cluster == null) {
            return CONFLICT("Cluster not found");
        }

        boolean hasUpdate = false;

        if (addCluster.getName() != null && !addCluster.getName().isEmpty() && !addCluster.getName().equals(cluster.getName())) {
            if (clusterRepository.existsByNameAndIdNotAndDeletedIsFalse(addCluster.getName(), addCluster.getId())) {
                return CONFLICT("Cluster already exists");
            }
            hasUpdate = true;
            cluster.setName(addCluster.getName());
        }

        // get the clustered service ids.
        List<ClusterService> clusterServices= clusterServiceRepository.findAllByCluster_IdOrderById(addCluster.getId());

        List<Integer> clusterServiceIds = clusterServices.stream()
                .map(c -> c.getService().getId()).toList();

        // check the custer services ids with incoming services ids.
        if (!clusterServiceIds.equals(addCluster.getServiceIds())) {
            clusterServiceRepository.deleteAll(clusterServices);

            List<ClusterService> newList = new ArrayList<>();

            int i = 0;

            for (Integer id: addCluster.getServiceIds()) {
                com.flex.service_module.impl.entities.Service service = servicesRepository.findByIdAndDeletedIsFalse(id);

                if (service != null) {
                    ClusterService clusterService = ClusterService.builder()
                            .cluster(cluster).service(service).orderNumber(i + 1).build();
                    i++;
                    newList.add(clusterService);
                }
            }

            if (!newList.isEmpty()) {
                clusterServiceRepository.saveAll(newList);
            }
        }

        if (hasUpdate) {
            clusterRepository.save(cluster);
        }

        return SUCCESS("Cluster updated");
    }

    @Override
    public ResponseEntity<?> getAllClusters(HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        List<Cluster> allClusters = clusterRepository.findAllByServiceProvider_IdAndDeletedIsFalse(provider.getId());

        if (!allClusters.isEmpty()) {
            List<ClusterData> clusterDataList = new ArrayList<>();

            for (Cluster cluster : allClusters) {

                List<ClusterService> clusterServiceList = clusterServiceRepository.findAllByCluster_IdOrderById(cluster.getId());

                ClusterData clusterData = ClusterData.builder()
                        .id(cluster.getId())
                        .name(cluster.getName())
                        .services(clusterServiceList.stream().map(s -> ClusterServiceData.builder()
                                .id(s.getService().getId())
                                .name(s.getService().getName())
                                .orderNumber(s.getOrderNumber())
                                .build()).toList())
                        .build();

                clusterDataList.add(clusterData);
            }

            return DATA(clusterDataList);
        }

        return DATA(null);
    }

    @Override
    public ResponseEntity<?> deleteCluster(Integer id, HttpServletRequest request) {
        log.info(request.getRequestURI());

        Cluster cluster = clusterRepository.findByIdAndDeletedIsFalse(id);

        if (cluster == null) {
            return CONFLICT("Cluster not found");
        }

        cluster.setDeleted(true);

        List<ClusterService> clusterServices= clusterServiceRepository.findAllByCluster_IdOrderById(cluster.getId());

        if (!clusterServices.isEmpty()) {
            clusterServiceRepository.deleteAll(clusterServices);
        }

        clusterRepository.save(cluster);

        return DATA("Cluster deleted successfully");
    }

    @Override
    public ResponseEntity<?> nonAssignedClusters(Integer centerId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        UserClaims userClaims = JwtUtil.getClaimsFromToken(request);

        if (userClaims == null || userClaims.getUserId() == null) {
            return CONFLICT("User not found");
        }

        ServiceProvider provider = serviceProviderRepository
                .findByProviderIdAndDeletedIsFalse(userClaims.getProvider());

        if (provider == null) {
            return CONFLICT("Provider not found");
        }

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(centerId);

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        List<Integer> assignedClusterIds = centerClusterRepository.getAssignClusterIds(centerId);

        List<Cluster> allClusters = clusterRepository.findAllByServiceProvider_IdAndDeletedIsFalse(provider.getId());

        if (allClusters == null || allClusters.isEmpty()) {
            return CONFLICT("Clusters not found");
        }

        return DATA(allClusters.stream().filter(e -> !assignedClusterIds.contains(e.getId()))
                .toList());
    }

    @Override
    public ResponseEntity<?> assignClusterToCenter(AssignCluster assignCluster, HttpServletRequest request) {
        log.info(request.getRequestURI());

        Cluster cluster = clusterRepository.findByIdAndDeletedIsFalse(assignCluster.getClusterId());

        if (cluster == null) {
            return CONFLICT("Cluster not found");
        }

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(assignCluster.getCenterId());

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        CenterCluster centerCluster = centerClusterRepository.centerCluster(assignCluster.getCenterId(), assignCluster.getClusterId());

        if (centerCluster != null) {
            return CONFLICT("Cluster already assigned");
        }

        CenterCluster assign = CenterCluster.builder()
                .cluster(cluster)
                .serviceCenter(serviceCenter)
                .build();

        centerClusterRepository.save(assign);

        return SUCCESS("Cluster assigned");
    }

    @Override
    public ResponseEntity<?> getClusters(Integer centerId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(centerId);

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        return null;
    }
}
