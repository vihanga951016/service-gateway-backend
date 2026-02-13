package com.flex.service_module.impl.services;

import com.flex.common_module.security.http.response.UserClaims;
import com.flex.common_module.security.utils.JwtUtil;
import com.flex.service_module.api.http.DTO.CenterClusterData;
import com.flex.service_module.api.http.requests.AddCluster;
import com.flex.service_module.api.http.requests.AssignCluster;
import com.flex.service_module.api.http.requests.UpdateCenterClusterService;
import com.flex.service_module.api.http.responses.ClusterData;
import com.flex.service_module.api.http.responses.ClusterServiceData;
import com.flex.service_module.api.http.responses.LoadCenterServices;
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
    private final CCSRepository ccsRepository;
    private final AvailableServiceRepository availableServiceRepository;
    private final ServicePointRepository servicePointRepository;

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

        if (assignCluster.getClusterIds().isEmpty()) {
            return CONFLICT("Clusters not found");
        }

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(assignCluster.getCenterId());

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        List<CenterCluster> centerClusters = new ArrayList<>();
        List<CenterClusterServices> centerClusterServices = new ArrayList<>();

        for (Integer clusterId : assignCluster.getClusterIds()) {
            Cluster cluster = clusterRepository.findByIdAndDeletedIsFalse(clusterId);

            if (cluster == null) {
                String message = "Cluster not found for this id: " + clusterId;
                return CONFLICT(message);
            }

            CenterCluster centerCluster = centerClusterRepository.centerCluster(assignCluster.getCenterId(), clusterId);

            if (centerCluster != null) {
                return CONFLICT("Cluster already assigned");
            }

            List<ClusterService> clusterServices = clusterServiceRepository.findAllByCluster_IdOrderById(cluster.getId());

            if (clusterServices.isEmpty()) {
                return CONFLICT("Services not assigned to this cluster");
            }

            CenterCluster assign = CenterCluster.builder()
                    .cluster(cluster)
                    .serviceCenter(serviceCenter)
                    .build();

            for (ClusterService clusterService : clusterServices) {
                CenterClusterServices centerClusterService = CenterClusterServices.builder()
                        .centerCluster(assign)
                        .service(clusterService.getService())
                        .orderNumber(clusterService.getOrderNumber())
                        .prevOrderNumber(clusterService.getOrderNumber()) //this is useful when disable
                        .total(clusterService.getService().getTotalPrice())
                        .downPay(clusterService.getService().getDownPrice())
                        .serviceTime(clusterService.getService().getServiceTime())
                        .disabled(false)
                        .build();

                centerClusterServices.add(centerClusterService);
            }

            centerClusters.add(assign);
        }

        centerClusterRepository.saveAll(centerClusters);

        ccsRepository.saveAll(centerClusterServices);

        return SUCCESS("Cluster assigned");
    }

    @Override
    public ResponseEntity<?> getClusters(Integer centerId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        ServiceCenter serviceCenter = serviceCenterRepository.findByIdAndDeletedIsFalse(centerId);

        if (serviceCenter == null) {
            return CONFLICT("Service center not found");
        }

        return DATA(centerClusterRepository.getClustersByCenterId(serviceCenter.getId()));
    }

    @Override
    public ResponseEntity<?> removeClusterFromCenter(Integer centerClusterId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        CenterCluster centerCluster = centerClusterRepository.getCenterClusterById(centerClusterId);

        if (centerCluster == null) {
            return CONFLICT("Cluster not found");
        }

        List<CenterClusterServices> centerClusterServices = ccsRepository.findAllByCenterCluster_id(centerClusterId);

        if (!centerClusterServices.isEmpty()) {
            ccsRepository.deleteAll(centerClusterServices);
        }

        centerClusterRepository.delete(centerCluster);

        return SUCCESS("Cluster removed successfully");
    }

    @Override
    public ResponseEntity<?> disableCCServices(Integer ccServiceId, HttpServletRequest request) {
        log.info(request.getRequestURI());

         CenterClusterServices centerClusterServices = ccsRepository.findCenterClusterServicesById(ccServiceId);

         if (centerClusterServices == null) {
             return CONFLICT("Service center not found");
         }

         centerClusterServices.setDisabled(!centerClusterServices.isDisabled());

         ccsRepository.save(centerClusterServices);

        List<CenterClusterServices> clusterServicesList = ccsRepository
                .findAllByCenterCluster_id(centerClusterServices.getCenterCluster().getId());

        int i = 1;

        for (CenterClusterServices services : clusterServicesList) {
            if (!services.isDisabled()) {
                log.info("id: {}", i);
                services.setOrderNumber(i);
                i++;
            } else {
                log.info("disable: {}", services.getService());
            }
        }

        ccsRepository.saveAll(clusterServicesList);

        return centerClusterServices.isDisabled() ? SUCCESS("Service disabled") : SUCCESS("Service Enabled");
    }

    @Override
    public ResponseEntity<?> getAllCenterClusterServices(Integer ccId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (!centerClusterRepository.existsById(ccId)) {
            return CONFLICT("Cluster not found in this center");
        }

        return DATA(ccsRepository.centerClusterServicesData(ccId));
    }

    @Override
    public ResponseEntity<?> updateCenterClusterService(UpdateCenterClusterService updateClusterService,
                                                        HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (updateClusterService.getId() == null) {
            return CONFLICT("Cluster id not found");
        }

        CenterClusterServices centerClusterService = ccsRepository.findCenterClusterServicesById(updateClusterService.getId());

        if (centerClusterService == null) {
            return CONFLICT("Cluster not found in this center");
        }

        if (!updateClusterService.getTotal().equals(centerClusterService.getTotal())) {
            centerClusterService.setTotal(updateClusterService.getTotal());
        }

        if (!updateClusterService.getDownPay().equals(centerClusterService.getDownPay())) {
            centerClusterService.setDownPay(updateClusterService.getDownPay());
        }

        if (updateClusterService.getServiceTime() != null
                && !updateClusterService.getServiceTime().equals(centerClusterService.getServiceTime())) {
            centerClusterService.setServiceTime(updateClusterService.getServiceTime());
        }

        ccsRepository.save(centerClusterService);

        return SUCCESS("Cluster updated successfully");
    }

    @Override
    public ResponseEntity<?> reOrderServices(Integer ccId, List<UpdateCenterClusterService> serviceList, HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (ccId == null) {
            return CONFLICT("Cluster id not found");
        }

        if (!centerClusterRepository.existsById(ccId)) {
            return CONFLICT("Cluster not found in this center");
        }

        List<CenterClusterServices> centerClusterServices = ccsRepository.findAllByCenterCluster_id(ccId);

        if (centerClusterServices.isEmpty()) {
            return CONFLICT("No services for this cluster");
        }

        for (UpdateCenterClusterService updateCenterClusterService : serviceList) {

            CenterClusterServices clusterService = ccsRepository
                    .findCenterClusterServicesById(updateCenterClusterService.getId());

            clusterService.setOrderNumber(updateCenterClusterService.getOrderNumber());
            clusterService.setPrevOrderNumber(updateCenterClusterService.getOrderNumber()); //this is useful when disable

            ccsRepository.save(clusterService);
        }

        return SUCCESS(null);
    }

    @Override
    public ResponseEntity<?> clusterServicesMapCheck(Integer ccId, HttpServletRequest request) {
        log.info(request.getRequestURI());

        if (ccId == null) {
            return CONFLICT("Cluster id not found");
        }

        CenterCluster centerCluster = centerClusterRepository.getCenterClusterById(ccId);

        if (centerCluster == null) {
            return CONFLICT("Cluster not found in this center");
        }

        List<CenterClusterServices> centerClusterServices = ccsRepository.findAllByCenterCluster_id(ccId);

        List<com.flex.service_module.impl.entities.Service> services = centerClusterServices.stream()
                .map(CenterClusterServices::getService).toList();

        List<Integer> pointAvailableServiceIds = availableServiceRepository
                .findServicesByServiceCenterId(centerCluster.getServiceCenter().getId());

        List<com.flex.service_module.impl.entities.Service> mustAssignServices = services.stream().filter(
                s -> !pointAvailableServiceIds.contains(s.getId())
        ).toList();

        return DATA(mustAssignServices);
    }
}
