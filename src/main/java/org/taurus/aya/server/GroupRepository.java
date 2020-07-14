package org.taurus.aya.server;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.taurus.aya.server.entity.Group;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long > {
}
