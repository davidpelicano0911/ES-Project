package com.operimus.Marketing.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.operimus.Marketing.entities.CampaignMaterials;

@Repository
public interface CampaignMaterialsRepository extends JpaRepository<CampaignMaterials, Long> {

    // Carrega o material + associações (campanhas)
    @EntityGraph(attributePaths = {"campaigns"})
    Optional<CampaignMaterials> findById(Long id);

    // Buscar todos os materiais associados a um campaign_id
    @EntityGraph(attributePaths = {"campaigns"})
    @Query("SELECT m FROM CampaignMaterials m JOIN m.campaigns c WHERE c.id = :campaignId")
    List<CampaignMaterials> findAllByCampaignId(@Param("campaignId") Long campaignId);
}
