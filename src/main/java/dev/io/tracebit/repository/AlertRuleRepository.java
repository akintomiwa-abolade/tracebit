package dev.io.tracebit.repository;

import dev.io.tracebit.entity.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {
    List<AlertRule> findByStartupIdAndActiveTrue(String startupId);

    List<AlertRule> findAllByActiveTrue();
}