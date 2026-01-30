package com.flex.user_module.impl.repositories;

import com.flex.user_module.api.DTO.UsersList;
import com.flex.user_module.impl.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * $DESC
 *
 * @author Yasintha Gunathilake
 * @since 1/13/2026
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    boolean existsByEmailAndDeletedIsFalse(String email);

    boolean existsByIdAndDeletedIsFalse(Integer id);

    User findByEmailAndDeletedIsFalse(String email);

    User findByIdAndDeletedIsFalse(Integer id);

    @Query(
            "SELECT " +
                    " u.id AS id, " +
                    " u.fName AS firstName, " +
                    " u.lName AS lastName, " +
                    " u.email AS email, " +
                    " ud.contact AS mobile, " +
                    " ud.nic AS nic, " +
                    " case when u.userType = 0 then 'user' when u.userType = 1 then 'admin' else 'customer' end AS userType, " +
                    " r.role AS role, " +
                    " sc.name AS serviceCenter, " +
                    " case when us.providerApproved = true then 'approved' else 'pending' end AS providerApproved " +
                    "FROM User u " +
                    " LEFT JOIN UserDetails ud ON ud.user = u " +
                    " LEFT JOIN UserStatus us ON us.user = u " +
                    " LEFT JOIN u.role r " +
                    " LEFT JOIN u.serviceCenter sc " +
                    "WHERE u.deleted = false AND u.id <> :userId " +
                    " AND u.serviceProvider.id = :serviceProviderId " +
                    " AND ( " +
                    "   LOWER(u.fName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "   LOWER(u.lName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "   LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
                    "   ud.contact LIKE CONCAT('%', :search, '%') OR " +
                    "   ud.nic LIKE CONCAT('%', :search, '%') " +
                    " )"
    )
    Page<UsersList> findAllByServiceProvider(
            @Param("serviceProviderId") Integer serviceProviderId,
            @Param("search") String search,
            @Param("userId") Integer userId,
            Pageable pageable
    );



}
